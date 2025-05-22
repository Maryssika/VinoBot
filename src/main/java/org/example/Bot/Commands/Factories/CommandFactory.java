package org.example.Bot.Commands.Factories;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.Bot.Commands.Command;
import org.example.Bot.Commands.PairCommand;
import org.example.Bot.Commands.StartCommand;
import org.example.Bot.Commands.UnknownCommand;
import org.example.DAO.Dish;
import org.example.DAO.DishDAO;
import org.example.DAO.Wine;
import org.example.DAO.WineDAO;
import org.example.Utils.ExcelFavoritesManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика команд для Telegram бота, отвечающая за создание экземпляров команд
 * на основе входящих сообщений и текущего состояния пользователя.
 */
public class CommandFactory {
    // DAO объекты для работы с винами и блюдами
    private static final WineDAO wineDAO;
    private static final DishDAO dishDAO;

    // Карта для хранения состояний пользователей (chatId -> состояние)
    private static final Map<Long, String> userStates = new HashMap<>();

    // Статический блок инициализации (выполняется при загрузке класса)
    static {
        try {
            // Загрузка конфигурации из .env файла
            Dotenv dotenv = loadConfiguration();

            // Создание подключения к базе данных
            Connection connection = createDatabaseConnection(dotenv);

            // Инициализация DAO объектов
            wineDAO = new WineDAO(connection);
            dishDAO = new DishDAO(connection);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации CommandFactory", e);
        }
    }

    /**
     * Загружает конфигурацию из .env файла
     */
    private static Dotenv loadConfiguration() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Проверка наличия обязательных переменных окружения
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
     * Создает соединение с базой данных
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

        // Проверка текущего состояния пользователя
        String state = userStates.get(chatId);
        if (state != null) {
            return handleUserState(state, chatId, messageText.trim());
        }

        String lowerCaseText = messageText.toLowerCase().trim();

        // Обработка команды /pair или прямого ввода названия вина
        if (lowerCaseText.startsWith("/pair") || !messageText.startsWith("/")) {
            String wineName = lowerCaseText.startsWith("/pair") ?
                    messageText.substring(5).trim() : messageText.trim();
            return new PairCommand(wineDAO, dishDAO, wineName);
        }
        // Обработка команды /wines (список вин)
        else if (lowerCaseText.startsWith("/wines")) {
            return createListCommand("Список вин:\n", wineDAO::getAllWines, Wine::toString);
        }
        // Обработка команды /dishes (список блюд)
        else if (lowerCaseText.startsWith("/dishes")) {
            return createListCommand("Список блюд:\n", dishDAO::getAllDishes, Dish::toString);
        }
        // Обработка команды /help (справка)
        else if (lowerCaseText.startsWith("/help")) {
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Доступные команды:\n" +
                            "[Название вина] - подбор блюд к вину\n" +
                            "/wines - список всех вин\n" +
                            "/dishes - список всех блюд\n" +
                            "/addfavwine - добавить вино в избранное\n" +
                            "/addfavdish - добавить блюдо в избранное\n" +
                            "/favwines - список избранных вин\n" +
                            "/favdishes - список избранных блюд\n" +
                            "/help - справка по командам");
        }
        // Обработка команды /addfavwine (добавление вина в избранное)
        else if (lowerCaseText.startsWith("/addfavwine")) {
            userStates.put(chatId, "ADD_FAV_WINE");
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Введите название вина для добавления в избранное:");
        }
        // Обработка команды /addfavdish (добавление блюда в избранное)
        else if (lowerCaseText.startsWith("/addfavdish")) {
            userStates.put(chatId, "ADD_FAV_DISH");
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Введите название блюда для добавления в избранное:");
        }
        // Обработка команды /favwines (список избранных вин)
        else if (lowerCaseText.startsWith("/favwines")) {
            return (cId, input) -> {
                try {
                    List<String> favorites = ExcelFavoritesManager.getFavoriteWines();
                    return new SendMessage(String.valueOf(cId), favorites.isEmpty() ?
                            "Список избранных вин пуст" :
                            "Избранные вина:\n" + String.join("\n", favorites));
                } catch (IOException e) {
                    return new SendMessage(String.valueOf(cId),
                            "Ошибка при получении избранных вин: " + e.getMessage());
                }
            };
        }
        // Обработка команды /favdishes (список избранных блюд)
        else if (lowerCaseText.startsWith("/favdishes")) {
            return (cId, input) -> {
                try {
                    List<String> favorites = ExcelFavoritesManager.getFavoriteDishes();
                    return new SendMessage(String.valueOf(cId), favorites.isEmpty() ?
                            "Список избранных блюд пуст" :
                            "Избранные блюда:\n" + String.join("\n", favorites));
                } catch (IOException e) {
                    return new SendMessage(String.valueOf(cId),
                            "Ошибка при получении избранных блюд: " + e.getMessage());
                }
            };
        }
        // Обработка команды /searchwine (поиск вин)
        else if (lowerCaseText.startsWith("/searchwine")) {
            return createSearchCommand(
                    "Результаты поиска вин:\n",
                    "Введите название вина для поиска, например: /searchwine Каберне",
                    wineDAO::findWinesByName,
                    Wine::toString);
        }
        // Обработка команды /searchdish (поиск блюд)
        else if (lowerCaseText.startsWith("/searchdish")) {
            return createSearchCommand(
                    "Результаты поиска блюд:\n",
                    "Введите название блюда для поиска, например: /searchdish Стейк",
                    (name) -> {
                        try {
                            return dishDAO.getAllDishes().stream()
                                    .filter(d -> d.getName().toLowerCase().contains(name.toLowerCase()))
                                    .toList();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    Dish::toString);
        }

        // Если команда не распознана
        return new UnknownCommand();
    }

    /**
     * Обрабатывает состояние пользователя и возвращает соответствующую команду
     */
    private static Command handleUserState(String state, long chatId, String input) {
        userStates.remove(chatId);

        switch (state) {
            case "ADD_FAV_WINE":
                return (cId, ignored) -> {
                    try {
                        List<Wine> wines = wineDAO.findWinesByName(input);
                        if (!wines.isEmpty()) {
                            ExcelFavoritesManager.addWineToFavorites(wines.get(0));
                            return new SendMessage(String.valueOf(cId),
                                    "Вино добавлено в избранное: " + wines.get(0).getName());
                        }
                        return new SendMessage(String.valueOf(cId), "Вино не найдено");
                    } catch (Exception e) {
                        return new SendMessage(String.valueOf(cId),
                                "Ошибка при добавлении вина: " + e.getMessage());
                    }
                };
            case "ADD_FAV_DISH":
                return (cId, ignored) -> {
                    try {
                        List<Dish> dishes = dishDAO.getAllDishes().stream()
                                .filter(d -> d.getName().equalsIgnoreCase(input))
                                .toList();
                        if (!dishes.isEmpty()) {
                            ExcelFavoritesManager.addDishToFavorites(dishes.get(0));
                            return new SendMessage(String.valueOf(cId),
                                    "Блюдо добавлено в избранное: " + dishes.get(0).getName());
                        }
                        return new SendMessage(String.valueOf(cId), "Блюдо не найдено");
                    } catch (Exception e) {
                        return new SendMessage(String.valueOf(cId),
                                "Ошибка при добавлении блюда: " + e.getMessage());
                    }
                };
            default:
                return new UnknownCommand();
        }
    }

    /**
     * Создает команду для вывода списка элементов
     */
    private static <T> Command createListCommand(
            String header,
            ThrowingSupplier<List<T>> supplier,
            java.util.function.Function<T, String> formatter) {
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
     * Создает команду для поиска элементов
     */
    private static <T> Command createSearchCommand(
            String header,
            String usageMessage,
            java.util.function.Function<String, List<T>> searcher,
            java.util.function.Function<T, String> formatter) {
        return (cId, input) -> {
            String[] parts = input.split(" ", 2);
            if (parts.length < 2) {
                return new SendMessage(String.valueOf(cId), usageMessage);
            }
            String searchTerm = parts[1].trim();
            if (searchTerm.isEmpty()) {
                return new SendMessage(String.valueOf(cId), "Поисковый запрос не может быть пустым");
            }
            try {
                List<T> results = searcher.apply(searchTerm);
                if (results.isEmpty()) {
                    return new SendMessage(String.valueOf(cId),
                            "Ничего не найдено по запросу: " + searchTerm);
                }
                StringBuilder response = new StringBuilder(header);
                for (T result : results) {
                    response.append(formatter.apply(result)).append("\n\n");
                }
                return new SendMessage(String.valueOf(cId), response.toString());
            } catch (Exception e) {
                return new SendMessage(String.valueOf(cId),
                        "Ошибка при поиске: " + e.getMessage());
            }
        };
    }

    /**
     * Функциональный интерфейс для поставщиков, которые могут выбрасывать исключения
     */
    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}