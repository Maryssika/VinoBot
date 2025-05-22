package org.example.Bot.Commands;

import org.example.Bot.Commands.Command;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class StartCommand implements Command {
    @Override
    public SendMessage execute(String chatId, String input) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableMarkdown(true);

        String welcomeText = "🍷 *Добро пожаловать в WinePairingBot!* 🍽\n\n" +
                "Я помогу вам подобрать идеальные сочетания вин и блюд.\n\n" +
                "*Доступные команды:*\n" +
                "🔹 /pair [вино] - подобрать блюда к вину\n" +
                "🔹 /findwine [блюдо] - подобрать вино к блюду\n" +
                "🔹 /addfavwine - добавить вино в избранное\n" +
                "🔹 /addfavdish - добавить блюдо в избранное\n" +
                "🔹 /mywines - показать избранные вина\n" +
                "🔹 /mydishes - показать избранные блюда\n\n" +
                "Просто введите команду или нажмите на неё!";

        message.setText(welcomeText);
        return message;
    }
}