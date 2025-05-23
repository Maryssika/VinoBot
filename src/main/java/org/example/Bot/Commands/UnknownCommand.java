package org.example.Bot.Commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class UnknownCommand implements Command {
    @Override
    public SendMessage execute(String chatId, String input) {
        return new SendMessage(chatId, "Неизвестная команда. Доступные команды:\n" +
                "Доступные команды:\n" +
                "/red - красные вина\n" +
                "/white - белые вина\n" +
                "/rose - розовые вина\n" +
                "/dessert - десертные вина\n" +
                "[Название вина] - подбор блюд\n" +
                "/wines - список всех вин\n" +
                "/dishes - список всех блюд\n" +
                "/rate - оценить текущее сочетание\n" +
                "/favorites - избранные сочетания\n" +
                "/help - справка");
    }
}