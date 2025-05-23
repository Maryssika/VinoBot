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
                "/red - красные вина\n" +
                "/white - белые вина\n" +
                "/rose - розовые вина\n" +
                "/dessert - десертные вина\n" +
                "[Название вина] - подбор блюд\n" +
                "/wines - список всех вин\n" +
                "/dishes - список всех блюд\n" +
                "/rate - оценить текущее сочетание\n" +
                "/favorites - избранные сочетания\n" +
                "/help - справка\n\n" +
                "Просто введите команду или нажмите на неё!";

        message.setText(welcomeText);
        return message;
    }
}