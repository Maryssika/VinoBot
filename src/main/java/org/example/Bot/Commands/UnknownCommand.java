package org.example.Bot.Commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class UnknownCommand implements Command {
    @Override
    public SendMessage execute(String chatId, String input) {
        return new SendMessage(chatId, "Неизвестная команда. Доступные команды:\n" +
                "[Название вина] - подбор блюд к вину\n" +
                "/wines - список всех вин\n" +
                "/dishes - список всех блюд\n" +
                "/addfavwine - добавить вино в избранное\n" +
                "/addfavdish - добавить блюдо в избранное\n" +
                "/favwines - список избранных вин\n" +
                "/favdishes - список избранных блюд\n" +
                "/help - справка по командам");
    }
}