package org.example.Bot.Commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.example.Bot.Commands.Factories.CommandFactory.createMainKeyboard;

/**
 * Обработчик неизвестных команд бота.
 * Возвращает пользователю сообщение о неизвестной команде
 * и подсказку для получения списка доступных команд.
 */
public class UnknownCommand implements Command {

    /**
     * Обрабатывает неизвестную команду и возвращает сообщение с подсказкой
     * @param chatId идентификатор чата пользователя
     * @param input текст введенной пользователем команды (не используется)
     * @return SendMessage с информацией о неизвестной команде
     *         и основной клавиатурой для навигации
     */
    @Override
    public SendMessage execute(String chatId, String input) {
        // Создаем сообщение с текстом о неизвестной команде
        SendMessage message = new SendMessage(chatId,
                "Неизвестная команда. Используйте /help для списка команд.");

        // Добавляем основную клавиатуру для удобства навигации
        message.setReplyMarkup(createMainKeyboard());

        return message;
    }
}