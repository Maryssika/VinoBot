package org.example.Bot.Commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Интерфейс, представляющий команду бота.
 * Определяет контракт для выполнения команд Telegram-бота.
 */
public interface Command {
   SendMessage execute(String chatId, String input);
}