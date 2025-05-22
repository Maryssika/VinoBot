package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.Bot.WinePairingBot;
import org.example.Utils.DatabaseInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        try {
            // Инициализация БД с передачей Dotenv
            DatabaseInitializer.initialize(dotenv);

            // Получение конфигурации бота
            String botToken = dotenv.get("BOT_TOKEN");
            String botUsername = dotenv.get("BOT_USERNAME");

            if (botToken == null || botUsername == null) {
                throw new IllegalStateException("Telegram bot credentials not set in .env file");
            }

            // Запуск бота
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new WinePairingBot(botToken, botUsername));
            System.out.println("Бот успешно запущен!");

        } catch (TelegramApiException e) {
            System.err.println("Ошибка Telegram API: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}