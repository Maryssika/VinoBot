package org.example.Bot.Commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.example.Bot.Commands.Factories.CommandFactory.createMainKeyboard;

public class UnknownCommand implements Command {
    @Override
    public SendMessage execute(String chatId, String input) {
        SendMessage message = new SendMessage(chatId,
                "Неизвестная команда. Используйте /help для списка команд.");
        message.setReplyMarkup(createMainKeyboard());
        return message;
    }
}