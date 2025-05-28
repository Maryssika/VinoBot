package org.example.Bot.Commands;

import org.example.Bot.Commands.Factories.CommandFactory;
import org.example.DAO.Dish;
import org.example.DAO.DishDAO;
import org.example.DAO.WineDAO;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.example.Bot.Commands.Factories.CommandFactory.createMainKeyboard;

/**
 * Команда для подбора сочетаний блюд к указанному вину.
 * Реализует интерфейс Command для обработки запросов на подбор сочетаний.
 */
public class PairCommand implements Command {
    // DAO для работы с винами
    private final WineDAO wineDAO;
    // DAO для работы с блюдами
    private final DishDAO dishDAO;
    // Название вина, для которого подбираются сочетания
    private final String wineName;
    // ID чата пользователя
    private final long chatId;
    // Коллекция для хранения контекстов текущих сочетаний
    private final Map<Long, CommandFactory.PairingContext> pairingContexts;

    /**
     * Конструктор команды подбора сочетаний
     * @param wineDAO DAO для работы с винами
     * @param dishDAO DAO для работы с блюдами
     * @param wineName название вина для подбора сочетаний
     * @param chatId ID чата пользователя
     * @param pairingContexts коллекция для хранения контекстов сочетаний
     */
    public PairCommand(WineDAO wineDAO, DishDAO dishDAO, String wineName,
                       long chatId, Map<Long, CommandFactory.PairingContext> pairingContexts) {
        this.wineDAO = wineDAO;
        this.dishDAO = dishDAO;
        this.wineName = wineName;
        this.chatId = chatId;
        this.pairingContexts = pairingContexts;
    }

    /**
     * Выполняет подбор сочетаний блюд для указанного вина
     * @param chatId ID чата для отправки ответа
     * @param input ввод пользователя (не используется)
     * @return SendMessage с результатами подбора сочетаний
     */
    @Override
    public SendMessage execute(String chatId, String input) {
        try {
            // Получаем список названий блюд, сочетающихся с вином
            List<String> pairingNames = wineDAO.findPairings(wineName);

            // Преобразуем названия блюд в объекты Dish
            List<Dish> pairings = pairingNames.stream()
                    .map(dishName -> {
                        try {
                            // Ищем блюдо по названию
                            return dishDAO.getAllDishes().stream()
                                    .filter(d -> d.getName().equalsIgnoreCase(dishName))
                                    .findFirst()
                                    .orElse(null);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull) // Фильтруем null-значения
                    .toList();

            // Если сочетаний не найдено
            if (pairings.isEmpty()) {
                return new SendMessage(chatId, "Не найдено подходящих блюд для вина: " + wineName);
            }

            // Формируем ответ с найденными сочетаниями
            StringBuilder response = new StringBuilder("🍷 *Подобранные сочетания для " + wineName + ":*\n\n");
            for (Dish dish : pairings) {
                response.append("🍽 *").append(dish.getName()).append("*\n")
                        .append(dish.toString()).append("\n\n");
            }

            // Сохраняем контекст текущего сочетания
            pairingContexts.put(this.chatId,
                    new CommandFactory.PairingContext(wineName, pairings.get(0)));

            // Добавляем подсказку для оценки сочетания
            response.append("Для оценки этого сочетания используйте команду /rate");

            // Создаем и настраиваем сообщение для отправки
            SendMessage message = new SendMessage(chatId, response.toString());
            message.setParseMode("Markdown"); // Включаем Markdown-разметку
            message.setReplyMarkup(createMainKeyboard()); // Добавляем основную клавиатуру
            return message;

        } catch (Exception e) {
            // В случае ошибки возвращаем сообщение об ошибке
            return new SendMessage(chatId, "Ошибка при поиске сочетаний: " + e.getMessage());
        }
    }
}