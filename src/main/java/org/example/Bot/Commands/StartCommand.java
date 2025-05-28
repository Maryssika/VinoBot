package org.example.Bot.Commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static org.example.Bot.Commands.Factories.CommandFactory.createMainKeyboard;

/**
 * Команда для обработки стартового сообщения бота (/start).
 * Отправляет приветственное сообщение с описанием возможностей бота.
 */
public class StartCommand implements Command {

    /**
     * Обрабатывает команду /start и возвращает приветственное сообщение
     * @param chatId идентификатор чата пользователя
     * @param input ввод пользователя (не используется)
     * @return SendMessage с приветственным текстом и основной клавиатурой
     */
    @Override
    public SendMessage execute(String chatId, String input) {
        // Создаем новое сообщение
        SendMessage message = new SendMessage();

        // Устанавливаем ID чата
        message.setChatId(chatId);

        // Включаем поддержку Markdown разметки
        message.enableMarkdown(true);

        // Устанавливаем текст сообщения из метода getWelcomeText()
        message.setText(getWelcomeText());

        // Добавляем основную клавиатуру с командами
        message.setReplyMarkup(createMainKeyboard());

        return message;
    }

    /**
     * Формирует текст приветственного сообщения
     * @return строка с форматированным приветствием и списком команд
     */
    private String getWelcomeText() {
        return """
            🍷 *Винный гид* 🍽
            
            Я помогу подобрать идеальные сочетания вин и блюд!
            
            *Основные команды:*
            🔹 /pair - подбор сочетаний
            🔹 /favorites - ваши избранные пары
            🔹 /red - красные вина
            🔹 /white - белые вина
            🔹 /rose - розовые вина
            🔹 /help - справка
            
            Просто введите команду или нажмите кнопку меню!""";
    }
}