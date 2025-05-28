package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.Bot.WinePairingBot;
import org.example.Utils.DatabaseInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Главный класс приложения для запуска винного бота.
 * Выполняет инициализацию базы данных и запуск Telegram бота.
 */
public class Main {

    /**
     * Точка входа в приложение.
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        // Загрузка конфигурации из .env файла
        Dotenv dotenv = Dotenv.load();

        try {
            // 1. Инициализация структуры базы данных
            DatabaseInitializer.initialize(dotenv);

            // 2. Получение учетных данных бота из переменных окружения
            String botToken = dotenv.get("BOT_TOKEN");
            String botUsername = dotenv.get("BOT_USERNAME");

            // Проверка наличия обязательных параметров
            if (botToken == null || botUsername == null) {
                throw new IllegalStateException("Telegram bot credentials not set in .env file");
            }

            // 3. Создание API для работы с Telegram ботами
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // 4. Регистрация и запуск бота
            botsApi.registerBot(new WinePairingBot(botToken, botUsername));

            // Уведомление об успешном запуске
            System.out.println("Бот успешно запущен!");

        } catch (TelegramApiException e) {
            // Обработка ошибок Telegram API
            System.err.println("Ошибка Telegram API: " + e.getMessage());
        } catch (Exception e) {
            // Обработка прочих исключений
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}