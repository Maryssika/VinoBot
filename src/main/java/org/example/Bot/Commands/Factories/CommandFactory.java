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
 * Фабрика команд для обработки сообщений Telegram бота
 */

public class CommandFactory {
    private static final WineDAO wineDAO;
    private static final DishDAO dishDAO;
    private static final Map<Long, String> userStates = new HashMap<>();
    private static final Map<Long, PairingContext> pairingContexts = new HashMap<>();
    private static final Map<Long, Boolean> waitingForWineInput = new HashMap<>();

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


    private static Dotenv loadConfiguration() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        validateEnvVariable(dotenv, "POSTGRES_URL");
        validateEnvVariable(dotenv, "POSTGRES_USER");
        validateEnvVariable(dotenv, "POSTGRES_PASSWORD");
        validateEnvVariable(dotenv, "BOT_TOKEN");
        validateEnvVariable(dotenv, "BOT_USERNAME");
        return dotenv;
    }

    private static void validateEnvVariable(Dotenv dotenv, String key) {
        if (dotenv.get(key) == null || dotenv.get(key).isEmpty()) {
            throw new IllegalStateException("Необходимая переменная " + key + " отсутствует в .env файле");
        }
    }

    private static Connection createDatabaseConnection(Dotenv dotenv) throws SQLException {
        return DriverManager.getConnection(
                dotenv.get("POSTGRES_URL"),
                dotenv.get("POSTGRES_USER"),
                dotenv.get("POSTGRES_PASSWORD")
        );
    }

    public static Command getCommand(String messageText, long chatId) {
        if ("/start".equalsIgnoreCase(messageText.trim())) {
            return new StartCommand();
        }

        // Обработка команды отмены
        if ("отмена".equalsIgnoreCase(messageText.trim())) {
            waitingForWineInput.remove(chatId);
            return (cId, input) -> {
                SendMessage message = new SendMessage(String.valueOf(cId),
                        "Поиск сочетаний отменен.");
                message.setReplyMarkup(createMainKeyboard());
                return message;
            };
        }

        // Если пользователь в состоянии ожидания ввода вина
        if (waitingForWineInput.getOrDefault(chatId, false)) {
            waitingForWineInput.remove(chatId);
            return new PairCommand(wineDAO, dishDAO, messageText.trim(), chatId, pairingContexts);
        }

        String state = userStates.get(chatId);
        if (state != null) {
            return handleUserState(state, chatId, messageText.trim());
        }

        String lowerCaseText = messageText.toLowerCase().trim();

        if (lowerCaseText.startsWith("/red")) {
            return createWineTypeFilterCommand("Красное");
        }
        else if (lowerCaseText.startsWith("/white")) {
            return createWineTypeFilterCommand("Белое");
        }
        else if (lowerCaseText.startsWith("/rose")) {
            return createWineTypeFilterCommand("Розовое");
        }
        else if (lowerCaseText.startsWith("/dessert")) {
            return createWineTypeFilterCommand("Десертное");
        }
        else if (lowerCaseText.startsWith("/pair")) {
            waitingForWineInput.put(chatId, true);
            return (cId, input) -> {
                SendMessage message = new SendMessage(String.valueOf(cId),
                        "Введите название вина для поиска сочетаний:");
                message.setReplyMarkup(createCancelKeyboard());
                return message;
            };
        }
        else if (lowerCaseText.startsWith("/wines")) {
            return createListCommand("Список вин:\n", wineDAO::getAllWines, Wine::toString);
        }
        else if (lowerCaseText.startsWith("/dishes")) {
            return createListCommand("Список блюд:\n", dishDAO::getAllDishes, Dish::toString);
        }
        else if (lowerCaseText.startsWith("/rate")) {
            return handleRateCommand(chatId);
        }
        else if (lowerCaseText.equals("хорошо") || lowerCaseText.equals("плохо")) {
            return handleRatingResponse(chatId, lowerCaseText);
        }
        else if (lowerCaseText.startsWith("/favorites")) {
            return (cId, input) -> {
                try {
                    List<String> favorites = ExcelFavoritesManager.getFavorites();
                    SendMessage message = new SendMessage(String.valueOf(cId), favorites.isEmpty() ?
                            "Список избранных сочетаний пуст" :
                            "Избранные сочетания:\n" + String.join("\n\n", favorites));
                    message.setReplyMarkup(createMainKeyboard());
                    return message;
                } catch (Exception e) {
                    return new SendMessage(String.valueOf(cId),
                            "Ошибка при получении избранных сочетаний: " + e.getMessage());
                }
            };
        }
        else if (lowerCaseText.startsWith("/help")) {
            SendMessage helpMessage = new SendMessage(String.valueOf(chatId),
                    "Доступные команды:\n" +
                            "/pair - подобрать сочетания для вина\n" +
                            "/red - красные вина\n" +
                            "/white - белые вина\n" +
                            "/rose - розовые вина\n" +
                            "/dessert - десертные вина\n" +
                            "/wines - список всех вин\n" +
                            "/dishes - список всех блюд\n" +
                            "/rate - оценить текущее сочетание\n" +
                            "/favorites - избранные сочетания\n" +
                            "/help - справка");
            helpMessage.setReplyMarkup(createMainKeyboard());
            return (cId, input) -> helpMessage;
        }

        return new UnknownCommand();
    }

    private static ReplyKeyboardMarkup createCancelKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Отмена");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private static Command handleRateCommand(long chatId) {
        return (cId, input) -> {
            PairingContext context = pairingContexts.get(chatId);
            if (context == null) {
                return new SendMessage(String.valueOf(cId),
                        "Сначала подберите сочетание с помощью команды /pair [вино]");
            }

            SendMessage message = new SendMessage(String.valueOf(cId),
                    "Текущее сочетание для оценки:\n" +
                            "🍷 Вино: " + context.getWineName() + "\n" +
                            "🍽 Блюдо: " + context.getDish().getName() + "\n\n" +
                            "Напишите 'хорошо' или 'плохо' для оценки этого сочетания");
            message.setReplyMarkup(createRatingKeyboard());
            return message;
        };
    }

    private static Command handleRatingResponse(long chatId, String rating) {
        return (cId, input) -> {
            PairingContext context = pairingContexts.get(chatId);
            if (context == null) {
                return new SendMessage(String.valueOf(cId),
                        "Нет активного сочетания для оценки. Сначала подберите сочетание.");
            }

            if ("хорошо".equals(rating)) {
                userStates.put(chatId, "CONFIRM_FAVORITE");
                SendMessage message = new SendMessage(String.valueOf(cId),
                        "Вы оценили сочетание как хорошее:\n" +
                                "🍷 Вино: " + context.getWineName() + "\n" +
                                "🍽 Блюдо: " + context.getDish().getName() + "\n\n" +
                                "Добавить это сочетание в избранное?");
                message.setReplyMarkup(createYesNoKeyboard());
                return message;
            } else {
                pairingContexts.remove(chatId);
                SendMessage response = new SendMessage(String.valueOf(cId),
                        "Спасибо за вашу оценку! Сочетание помечено как неподходящее.");
                response.setReplyMarkup(createMainKeyboard());
                return response;
            }
        };
    }

    private static Command createWineTypeFilterCommand(String russianType) {
        return (cId, input) -> {
            try {
                List<Wine> wines = wineDAO.getAllWines().stream()
                        .filter(w -> w.getType().toString().equalsIgnoreCase(russianType))
                        .toList();

                if (wines.isEmpty()) {
                    return new SendMessage(String.valueOf(cId), "Не найдено вин типа: " + russianType);
                }

                StringBuilder response = new StringBuilder("Вина типа " + russianType + ":\n\n");
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
                    ExcelFavoritesManager.addFavorite(context.getWineName(),
                            context.getDish().getName() + " - " + context.getDish().toString());
                    pairingContexts.remove(chatId);
                    SendMessage message = new SendMessage(String.valueOf(chatId),
                            "Сочетание добавлено в избранное!");
                    message.setReplyMarkup(createMainKeyboard());
                    return (cId, ignored2) -> message;
                } catch (Exception e) {
                    return (cId, ignored2) -> new SendMessage(String.valueOf(cId),
                            "Ошибка при добавлении в избранное: " + e.getMessage());
                }
            } else {
                pairingContexts.remove(chatId);
                SendMessage message = new SendMessage(String.valueOf(chatId),
                        "Хорошо, сочетание не было сохранено.");
                message.setReplyMarkup(createMainKeyboard());
                return (cId, ignored2) -> message;
            }
        }
        return new UnknownCommand();
    }

    public static ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("/red");
        row1.add("/white");
        row1.add("/rose");
        row1.add("/dessert");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("/wines");
        row2.add("/dishes");
        row2.add("/pair");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("/rate");
        row3.add("/favorites");
        row3.add("/help");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private static ReplyKeyboardMarkup createRatingKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("хорошо");
        row.add("плохо");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

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

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public static class PairingContext {
        private final String wineName;
        private final Dish dish;

        public PairingContext(String wineName, Dish dish) {
            this.wineName = wineName;
            this.dish = dish;
        }

        public String getWineName() {
            return wineName;
        }

        public Dish getDish() {
            return dish;
        }
    }
}