package org.example.Bot.Commands;

import org.example.Bot.Commands.Factories.CommandFactory;
import org.example.DAO.Dish;
import org.example.DAO.DishDAO;
import org.example.DAO.WineDAO;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PairCommand implements Command {
    private final WineDAO wineDAO;
    private final DishDAO dishDAO;
    private final String wineName;
    private final long chatId;
    private final Map<Long, CommandFactory.PairingContext> pairingContexts;

    public PairCommand(WineDAO wineDAO, DishDAO dishDAO, String wineName,
                       long chatId, Map<Long, CommandFactory.PairingContext> pairingContexts) {
        this.wineDAO = wineDAO;
        this.dishDAO = dishDAO;
        this.wineName = wineName;
        this.chatId = chatId;
        this.pairingContexts = pairingContexts;
    }

    @Override
    public SendMessage execute(String chatId, String input) {
        String searchName = wineName.isEmpty() ? input.trim() : wineName;

        if (searchName.isEmpty()) {
            return new SendMessage(chatId, "Пожалуйста, укажите вино для подбора");
        }

        try {
            List<String> pairingNames = wineDAO.findPairings(searchName);
            List<Dish> pairings = pairingNames.stream()
                    .map(dishName -> {
                        try {
                            return dishDAO.getAllDishes().stream()
                                    .filter(d -> d.getName().equalsIgnoreCase(dishName))
                                    .findFirst()
                                    .orElse(null);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            if (pairings.isEmpty()) {
                return new SendMessage(chatId, "Не найдено подходящих блюд для вина: " + searchName);
            }

            StringBuilder response = new StringBuilder("🍷 *Подобранные сочетания для " + searchName + ":*\n\n");
            for (Dish dish : pairings) {
                response.append("🍽 *").append(dish.getName()).append("*\n")
                        .append(dish.toString()).append("\n\n");
            }

            pairingContexts.put(this.chatId,
                    new CommandFactory.PairingContext(searchName, pairings.get(0)));

            response.append("Для оценки этого сочетания используйте команду /rate");

            SendMessage message = new SendMessage(chatId, response.toString());
            message.setParseMode("Markdown");
            message.setReplyMarkup(CommandFactory.createMainKeyboard());
            return message;
        } catch (Exception e) {
            return new SendMessage(chatId, "Ошибка при поиске сочетаний: " + e.getMessage());
        }
    }
}