package org.example.Bot.Commands.Factories;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.Bot.Commands.*;
import org.example.DAO.Dish;
import org.example.DAO.DishDAO;
import org.example.DAO.Wine;
import org.example.DAO.WineDAO;
import org.example.Utils.ExcelFavoritesManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

/**
 * Фабрика команд для Telegram бота, отвечающая за создание экземпляров команд
 * на основе входящих сообщений и текущего состояния пользователя.
 * Обрабатывает все основные команды бота: подбор сочетаний, фильтрацию вин,
 * оценку сочетаний и управление избранным.
 */
public class CommandFactory {

    private static final WineDAO wineDAO;
    private static final DishDAO dishDAO;
    private static final Map<Long, String> userStates = new HashMap<>();
    private static final Map<Long, PairingContext> pairingContexts = new HashMap<>();

    static {
        try {

            Dotenv dotenv = loadConfiguration();
            Connection connection = createDatabaseConnection(dotenv);

            wineDAO = new WineDAO(connection);
            dishDAO = new DishDAO(connection);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации CommandFactory", e);
        }
    }

    /**
     * Загружает конфигурацию из .env файла
     * @return объект Dotenv с загруженными переменными окружения
     * @throws IllegalStateException если отсутствуют обязательные переменные
     */

    private static Dotenv loadConfiguration() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        validateEnvVariable(dotenv, "POSTGRES_URL");
        validateEnvVariable(dotenv, "POSTGRES_USER");
        validateEnvVariable(dotenv, "POSTGRES_PASSWORD");
        validateEnvVariable(dotenv, "BOT_TOKEN");
        validateEnvVariable(dotenv, "BOT_USERNAME");
        return dotenv;
    }

    /**
     * Проверяет наличие и валидность переменной окружения
     * @param dotenv объект с переменными окружения
     * @param key имя проверяемой переменной
     * @throws IllegalStateException если переменная отсутствует или пуста
     */

    private static void validateEnvVariable(Dotenv dotenv, String key) {
        if (dotenv.get(key) == null || dotenv.get(key).isEmpty()) {
            throw new IllegalStateException("Необходимая переменная " + key + " отсутствует в .env файле");
        }
    }

    /**
     * Создает соединение с базой данных PostgreSQL
     * @param dotenv объект с параметрами подключения
     * @return объект Connection для работы с БД
     * @throws SQLException при ошибках подключения
     */

    private static Connection createDatabaseConnection(Dotenv dotenv) throws SQLException {
        return DriverManager.getConnection(
                dotenv.get("POSTGRES_URL"),
                dotenv.get("POSTGRES_USER"),
                dotenv.get("POSTGRES_PASSWORD")
        );
    }

    /**
     * Возвращает соответствующую команду на основе текста сообщения и состояния пользователя
     * @param messageText текст сообщения от пользователя
     * @param chatId идентификатор чата
     * @return экземпляр Command для обработки сообщения
     */

    public static Command getCommand(String messageText, long chatId) {
        if ("/start".equalsIgnoreCase(messageText.trim())) {
            return new StartCommand();
        }

        String state = userStates.get(chatId);
        if (state != null) {
            return handleUserState(state, chatId, messageText.trim());
        }

        String lowerCaseText = messageText.toLowerCase().trim();


        if (lowerCaseText.startsWith("/red") || lowerCaseText.startsWith("/white") ||
                lowerCaseText.startsWith("/rose") || lowerCaseText.startsWith("/dessert")) {
            return createWineTypeFilterCommand(lowerCaseText.substring(1));
        }

        else if (lowerCaseText.startsWith("/pair") || !messageText.startsWith("/")) {
            String wineName = lowerCaseText.startsWith("/pair") ?
                    messageText.substring(5).trim() : messageText.trim();
            return new PairCommand(wineDAO, dishDAO, wineName, chatId, pairingContexts);
        }

        else if (lowerCaseText.startsWith("/wines")) {
            return createListCommand("Список вин:\n", wineDAO::getAllWines, Wine::toString);
        }

        else if (lowerCaseText.startsWith("/dishes")) {
            return createListCommand("Список блюд:\n", dishDAO::getAllDishes, Dish::toString);
        }

        else if (lowerCaseText.startsWith("/rate")) {
            return handleRatingCommand(chatId, messageText);
        }

        else if (lowerCaseText.startsWith("/favorites")) {
            return (cId, ignored) -> {
                try {
                    List<String> favorites = ExcelFavoritesManager.getFavorites();
                    return new SendMessage(String.valueOf(cId), favorites.isEmpty() ?
                            "Список избранных сочетаний пуст" :
                            "Избранные сочетания:\n" + String.join("\n\n", favorites));
                } catch (Exception e) {
                    return new SendMessage(String.valueOf(cId),
                            "Ошибка при получении избранных сочетаний: " + e.getMessage());
                }
            };
        }

        else if (lowerCaseText.startsWith("/help")) {
            return (cId, ignored) -> new SendMessage(String.valueOf(cId),
                    "Доступные команды:\n" +
                            "/red - красные вина\n" +
                            "/white - белые вина\n" +
                            "/rose - розовые вина\n" +
                            "/dessert - десертные вина\n" +
                            "[Название вина] - подбор блюд\n" +
                            "/wines - список всех вин\n" +
                            "/dishes - список всех блюд\n" +
                            "/rate [good/bad] - оценить сочетание\n" +
                            "/favorites - избранные сочетания\n" +
                            "/help - справка");
        }
        return new UnknownCommand();
    }

    /**
     * Создает команду для фильтрации вин по типу
     * @param wineType тип вина (red, white, rose, dessert)
     * @return команда для выполнения фильтрации
     */

    private static Command createWineTypeFilterCommand(String wineType) {
        return (cId, input) -> {
            try {

                List<Wine> wines = wineDAO.getAllWines().stream()
                        .filter(w -> w.getType().toString().equalsIgnoreCase(wineType))
                        .toList();

                if (wines.isEmpty()) {
                    return new SendMessage(String.valueOf(cId), "Не найдено вин типа: " + wineType);
                }


                StringBuilder response = new StringBuilder("Вина типа " + wineType + ":\n\n");
                for (Wine wine : wines) {
                    response.append(wine.toString()).append("\n\n");
                }
                return new SendMessage(String.valueOf(cId), response.toString());
            } catch (Exception e) {
                return new SendMessage(String.valueOf(cId),
                        "Ошибка при получении списка вин: " + e.getMessage());
            }
        };
    }

    /**
     * Обрабатывает команду оценки сочетания
     * @param chatId ID чата пользователя
     * @param messageText текст сообщения с оценкой
     * @return соответствующая команда для обработки оценки
     */

    private static Command handleRatingCommand(long chatId, String messageText) {
        String[] parts = messageText.split(" ");

        if (parts.length < 2) {
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Используйте: /rate good или /rate bad");
        }

        String rating = parts[1].toLowerCase();
        if (!"good".equals(rating) && !"bad".equals(rating)) {
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Некорректная оценка. Используйте good или bad");
        }


        PairingContext context = pairingContexts.get(chatId);
        if (context == null) {
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Нет активного сочетания для оценки");
        }


        if ("good".equals(rating)) {
            userStates.put(chatId, "CONFIRM_FAVORITE");
            return (cId, input) -> {
                // Предлагаем пользователю добавить сочетание в избранное
                SendMessage message = new SendMessage(String.valueOf(cId),
                        "Сочетание оценено положительно. Добавить в избранное? (да/нет)");
                message.setReplyMarkup(createYesNoKeyboard());
                return message;
            };
        } else {

            pairingContexts.remove(chatId);
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Спасибо за оценку! Попробуйте другие сочетания.");
        }
    }

    /**
     * Обрабатывает состояние пользователя (подтверждение добавления в избранное)
     * @param state текущее состояние пользователя
     * @param chatId ID чата
     * @param input ввод пользователя
     * @return команда для обработки состояния
     */

    private static Command handleUserState(String state, long chatId, String input) {
        if ("CONFIRM_FAVORITE".equals(state)) {
            userStates.remove(chatId);
            PairingContext context = pairingContexts.get(chatId);
            if (context == null) {
                return (cId, ignored) -> new SendMessage(String.valueOf(cId),
                        "Ошибка: контекст сочетания утерян");
            }


            if ("да".equalsIgnoreCase(input)) {
                try {
                    // Сохраняем сочетание в Excel
                    ExcelFavoritesManager.addFavorite(context.getWineName(), context.getDishName());
                    pairingContexts.remove(chatId);
                    return (cId, ignored2) -> new SendMessage(String.valueOf(cId),
                            "Сочетание добавлено в избранное!");
                } catch (Exception e) {
                    return (cId, ignored2) -> new SendMessage(String.valueOf(cId),
                            "Ошибка при добавлении в избранное: " + e.getMessage());
                }
            } else {

                pairingContexts.remove(chatId);
                return (cId, ignored2) -> new SendMessage(String.valueOf(cId),
                        "Хорошо, сочетание не было сохранено.");
            }
        }
        return new UnknownCommand();
    }

    /**
     * Создает клавиатуру с кнопками "Да"/"Нет"
     * @return объект ReplyKeyboardMarkup
     */

    private static ReplyKeyboardMarkup createYesNoKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Да");
        row.add("Нет");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    /**
     * Создает команду для вывода списка элементов
     * @param header заголовок списка
     * @param supplier поставщик данных (может выбрасывать исключения)
     * @param formatter функция форматирования элементов
     * @return команда для вывода списка
     */

    private static <T> Command createListCommand(
            String header,
            ThrowingSupplier<List<T>> supplier,
            Function<T, String> formatter) {
        return (cId, input) -> {
            try {
                List<T> items = supplier.get();
                if (items.isEmpty()) {
                    return new SendMessage(String.valueOf(cId), "Список пуст");
                }
                StringBuilder response = new StringBuilder(header);
                for (T item : items) {
                    response.append(formatter.apply(item)).append("\n\n");
                }
                return new SendMessage(String.valueOf(cId), response.toString());
            } catch (Exception e) {
                return new SendMessage(String.valueOf(cId),
                        "Ошибка при получении данных: " + e.getMessage());
            }
        };
    }

    /**
     * Функциональный интерфейс для поставщиков, которые могут выбрасывать исключения
     * @param <T> тип возвращаемого значения
     */

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /**
     * Класс для хранения контекста текущего сочетания вина и блюда
     */

    public static class PairingContext {
        private final String wineName;
        private final String dishName;

        public PairingContext(String wineName, String dishName) {
            this.wineName = wineName;
            this.dishName = dishName;
        }

        public String getWineName() {
            return wineName;
        }

        public String getDishName() {
            return dishName;
        }
    }
}