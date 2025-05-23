package org.example.Bot.Commands.Factories;

import org.example.Bot.Commands.Command;
import org.example.DAO.Dish;
import org.example.DAO.DishDAO;
import org.example.DAO.Wine;
import org.example.DAO.WineDAO;
import org.example.Utils.ExcelFavoritesManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика команд с поддержкой состояний для обработки последовательных действий пользователя.
 * Управляет состояниями при добавлении в избранное и других многошаговых операциях.
 */
public class StatefulCommandFactory {
    /**
     * Хранит состояния пользователей в формате chatId -> состояние
     */
    private static final Map<Long, String> userStates = new HashMap<>();
    private static WineDAO wineDAO;
    private static DishDAO dishDAO;

    /**
     * Инициализирует DAO объекты для работы с винами и блюдами
     * @param wineDAO DAO для работы с винами
     * @param dishDAO DAO для работы с блюдами
     */
    public static void initialize(WineDAO wineDAO, DishDAO dishDAO) {
        StatefulCommandFactory.wineDAO = wineDAO;
        StatefulCommandFactory.dishDAO = dishDAO;
    }

    /**
     * Возвращает команду на основе текста сообщения и текущего состояния пользователя
     * @param messageText текст сообщения от пользователя
     * @param chatId идентификатор чата пользователя
     * @return соответствующая команда для выполнения
     */
    public static Command getCommand(String messageText, long chatId) {
        String lowerCaseText = messageText.toLowerCase().trim();

        // Проверяем наличие активного состояния у пользователя
        String state = userStates.get(chatId);
        if (state != null) {
            // Обрабатываем ввод в зависимости от текущего состояния
            switch (state) {
                case "ADD_FAV_WINE":
                    userStates.remove(chatId);
                    return createAddFavoriteCommand(
                            wineName -> {
                                try {
                                    List<Wine> wines = wineDAO.findWinesByName(wineName);
                                    if (!wines.isEmpty()) {
                                        ExcelFavoritesManager.addFavorite(wines.get(0).getName(), "Wine");
                                        return "Вино добавлено в избранное: " + wines.get(0).getName();
                                    }
                                    return "Вино не найдено";
                                } catch (Exception e) {
                                    return "Ошибка: " + e.getMessage();
                                }
                            }
                    );
                case "ADD_FAV_DISH":
                    userStates.remove(chatId);
                    return createAddFavoriteCommand(
                            dishName -> {
                                try {
                                    List<Dish> dishes = dishDAO.getAllDishes().stream()
                                            .filter(d -> d.getName().equalsIgnoreCase(dishName))
                                            .toList();
                                    if (!dishes.isEmpty()) {
                                        ExcelFavoritesManager.addFavorite(dishes.get(0).getName(), "Dish");
                                        return "Блюдо добавлено в избранное: " + dishes.get(0).getName();
                                    }
                                    return "Блюдо не найдено";
                                } catch (Exception e) {
                                    return "Ошибка: " + e.getMessage();
                                }
                            }
                    );
            }
        }

        // Обрабатываем стандартные команды, если у пользователя нет активного состояния
        if (lowerCaseText.startsWith("/addfavwine")) {
            userStates.put(chatId, "ADD_FAV_WINE");
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Введите название вина для добавления в избранное:");
        }
        else if (lowerCaseText.startsWith("/addfavdish")) {
            userStates.put(chatId, "ADD_FAV_DISH");
            return (cId, input) -> new SendMessage(String.valueOf(cId),
                    "Введите название блюда для добавления в избранное:");
        }

        return CommandFactory.getCommand(messageText, chatId);
    }

    /**
     * Создает команду для добавления в избранное с использованием переданного обработчика
     * @param processor функция обработки ввода пользователя
     * @return команда для добавления в избранное
     */
    private static Command createAddFavoriteCommand(java.util.function.Function<String, String> processor) {
        return (cId, input) -> new SendMessage(String.valueOf(cId), processor.apply(input.trim()));
    }
}