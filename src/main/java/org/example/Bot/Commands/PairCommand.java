package org.example.Bot.Commands;

import org.example.Bot.Commands.Factories.CommandFactory;
import org.example.DAO.DishDAO;
import org.example.DAO.WineDAO;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.List;
import java.util.Map;

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
            List<String> pairings = wineDAO.findPairings(searchName);

            if (pairings == null || pairings.isEmpty()) {
                return new SendMessage(chatId, "Не найдено подходящих блюд для вина: " + searchName);
            }

            // Сохраняем контекст для последующей оценки
            if (!pairings.isEmpty()) {
                pairingContexts.put(this.chatId,
                        new CommandFactory.PairingContext(searchName, pairings.get(0)));
            }

            return new SendMessage(chatId, "Лучшие сочетания для " + searchName + ":\n" +
                    String.join("\n", pairings) +
                    "\n\nОцените сочетание: /rate good или /rate bad");
        } catch (Exception e) {
            return new SendMessage(chatId, "Ошибка при поиске сочетаний: " + e.getMessage());
        }
    }
}