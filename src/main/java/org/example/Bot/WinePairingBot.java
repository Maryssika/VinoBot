package org.example.Bot;

import org.example.Bot.Commands.Command;
import org.example.Bot.Commands.Factories.CommandFactory;
import org.example.Bot.Commands.StartCommand;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import java.util.*;


/**
 * Основной класс Telegram бота для подбора сочетаний вина и блюд.
 */
public class WinePairingBot extends TelegramLongPollingBot {
    private final String botToken;
    private final String botUsername;
    private final Map<Long, Boolean> ageVerifiedUsers = new HashMap<>();

    /**
     * Конструктор бота
     * @param botToken токен бота
     * @param botUsername имя бота
     */
    public WinePairingBot(String botToken, String botUsername) {
        this.botToken = Objects.requireNonNull(botToken, "Токен бота не может быть null");
        this.botUsername = Objects.requireNonNull(botUsername, "Имя бота не может быть null");
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            try {
                // Обработка команды /start (всегда запрашиваем возраст)
                if ("/start".equalsIgnoreCase(messageText)) {
                    SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("🍷 *Проверка возраста*\n\n" +
                            "Для использования бота вам должно быть 18 лет или больше.\n\n" +
                            "Пожалуйста, введите вашу дату рождения в формате ДД.ММ.ГГГГ (например, 01.01.1990):");
                    message.setParseMode("Markdown");
                    execute(message);
                    return;
                }

                // Проверка введенной даты рождения
                if (messageText.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                    if (isUserAdult(messageText, update)) {  // Передаем update как параметр
                        // Возраст подтвержден - показываем стартовое сообщение
                        execute(createStartMessage(chatId));
                    } else {
                        SendMessage message = new SendMessage();
                        message.setChatId(String.valueOf(chatId));
                        message.setText("❌ *Доступ запрещен*\n\n" +
                                "К сожалению, вам меньше 18 лет. Использование бота запрещено.");
                        message.setParseMode("Markdown");
                        execute(message);
                    }
                    return;
                }

                // Если это не /start и не дата рождения - проверяем возраст
                if (!ageVerifiedUsers.getOrDefault(chatId, false)) {
                    SendMessage message = new SendMessage();
                    message.setChatId(String.valueOf(chatId));
                    message.setText("⚠️ Пожалуйста, сначала подтвердите ваш возраст, используя команду /start");
                    execute(message);
                    return;
                }

                // Основная логика обработки команд
                Command command = CommandFactory.getCommand(messageText, chatId);
                SendMessage response = command.execute(String.valueOf(chatId), messageText);
                execute(response);

            } catch (TelegramApiException e) {
                sendErrorMessage(chatId, "Ошибка при отправке сообщения");
                e.printStackTrace();
            } catch (Exception e) {
                sendErrorMessage(chatId, "Произошла ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean isUserAdult(String input, Update update) {  // Добавляем параметр Update
        // Если input - это chatId (число), проверяем в карте ageVerifiedUsers
        try {
            long chatId = Long.parseLong(input);
            return ageVerifiedUsers.getOrDefault(chatId, false);
        } catch (NumberFormatException e) {
            // Если input не число, значит это дата рождения в формате ДД.ММ.ГГГГ
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                Date birthDateObj = sdf.parse(input);
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDateObj);
                Calendar today = Calendar.getInstance();

                int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
                if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
                        (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                                today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
                    age--;
                }

                // Если возраст подтвержден, сохраняем в карту
                if (age >= 18) {
                    ageVerifiedUsers.put(update.getMessage().getChatId(), true);
                }

                return age >= 18;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    /**
     * Создает приветственное сообщение для команды /start
     */
        private SendMessage createStartMessage(long chatId) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.enableMarkdown(true);

            String welcomeText = "🍷 *Добро пожаловать!* 🍽\n\n" +
                    "Я помогу вам подобрать идеальные сочетания вин и блюд.\n\n" +
                    "*Основные команды:*\n" +
                    "/pair - подобрать сочетания для вина\n" +
                    "/red - красные вина\n" +
                    "/white - белые вина\n" +
                    "/rose - розовые вина\n" +
                    "/dessert - десертные вина\n" +
                    "/wines - список всех вин\n" +
                    "/dishes - список всех блюд\n" +
                    "/rate - оценить текущее сочетание\n" +
                    "/favorites - избранные сочетания\n" +
                    "/help - справка\n\n" +
                    "Выберите действие:";

            message.setText(welcomeText);
            message.setReplyMarkup(createMainKeyboard());

            return message;
        }

    /**
     * Создает основную клавиатуру
     */
    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд кнопок
        KeyboardRow row1 = new KeyboardRow();
        row1.add("/red");
        row1.add("/white");
        row1.add("/rose");
        row1.add("/dessert");

        // Второй ряд кнопок
        KeyboardRow row2 = new KeyboardRow();
        row2.add("/wines");
        row2.add("/dishes");
        row2.add("/pair");

        // Третий ряд кнопок
        KeyboardRow row3 = new KeyboardRow();
        row3.add("/rate");
        row3.add("/favorites");
        row3.add("/help");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    /**
     * Отправляет сообщение об ошибке
     */
    private void sendErrorMessage(long chatId, String errorMessage) {
        SendMessage errorResponse = new SendMessage();
        errorResponse.setChatId(String.valueOf(chatId));
        errorResponse.setText(errorMessage);

        try {
            execute(errorResponse);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}