package org.example.Bot.Commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static org.example.Bot.Commands.Factories.CommandFactory.createMainKeyboard;

public class StartCommand implements Command {
    @Override
    public SendMessage execute(String chatId, String input) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableMarkdown(true);
        message.setText(getWelcomeText());
        message.setReplyMarkup(createMainKeyboard());
        return message;
    }

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