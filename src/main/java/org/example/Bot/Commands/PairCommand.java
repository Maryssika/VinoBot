package org.example.Bot.Commands;

import org.example.DAO.DishDAO;
import org.example.DAO.WineDAO;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;

/**
 * Команда для подбора сочетаний вина и блюд
 */
public class PairCommand implements Command {

    private final WineDAO wineDAO;
    private final DishDAO dishDAO;
    private final String wineName;

    /**
     * Основной конструктор команды
     * @param wineDAO DAO для работы с винами
     * @param dishDAO DAO для работы с блюдами
     * @param wineName название вина для поиска сочетаний
     */
    public PairCommand(WineDAO wineDAO, DishDAO dishDAO, String wineName) {
        this.wineDAO = wineDAO;
        this.dishDAO = dishDAO;
        this.wineName = wineName;
    }

    /**
     * Конструктор для обратной совместимости (без указания вина)
     * @param wineDAO DAO для работы с винами
     * @param dishDAO DAO для работы с блюдами
     */
    public PairCommand(WineDAO wineDAO, DishDAO dishDAO) {
        this(wineDAO, dishDAO, "");
    }

    /**
     * Выполняет команду подбора сочетаний
     * @param chatId ID чата для отправки ответа
     * @param input ввод пользователя (название вина, если не задано в конструкторе)
     * @return сообщение с результатами подбора
     */
    @Override
    public SendMessage execute(String chatId, String input) {
        // Определяем название вина для поиска (из конструктора или ввода пользователя)
        String searchName = wineName.isEmpty() ? input.trim() : wineName;

        // Проверяем, что название вина указано
        if (searchName.isEmpty()) {
            return new SendMessage(chatId, "Пожалуйста, укажите вино для подбора");
        }

        try {
            // Ищем сочетания блюд для указанного вина
            List<String> pairings = wineDAO.findPairings(searchName);

            // Обрабатываем случай, когда сочетания не найдены
            if (pairings == null || pairings.isEmpty()) {
                return new SendMessage(chatId, "Не найдено подходящих блюд для вина: " + searchName +
                        "\n\nДобавить это вино в избранное: /addfavwine " + searchName);
            }

            // Формируем успешный ответ со списком сочетаний
            return new SendMessage(chatId, "Лучшие сочетания для " + searchName + ":\n" +
                    String.join("\n", pairings) +
                    "\n\nДобавить в избранное: /addfavwine " + searchName);
        } catch (Exception e) {
            // Обрабатываем ошибки при поиске
            return new SendMessage(chatId, "Ошибка при поиске сочетаний: " + e.getMessage());
        }
    }
}