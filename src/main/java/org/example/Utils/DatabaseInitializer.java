package org.example.Utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;

/**
 * Класс для инициализации структуры базы данных.
 * Создает необходимые таблицы (wines, dishes, pairings) при запуске приложения.
 */
public class DatabaseInitializer {

    /**
     * Инициализирует базу данных с использованием предоставленного объекта Dotenv.
     * @param dotenv объект Dotenv с загруженными переменными окружения
     * @throws IllegalStateException если отсутствуют обязательные параметры подключения
     * @throws RuntimeException если произошла ошибка при инициализации БД
     */
    public static void initialize(Dotenv dotenv) {
        String url = dotenv.get("POSTGRES_URL");
        String user = dotenv.get("POSTGRES_USER");
        String password = dotenv.get("POSTGRES_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Database configuration is missing in .env file");
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            createWinesTable(stmt);
            createDishesTable(stmt);
            createPairingsTable(stmt);

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Создает таблицу wines, если она не существует.
     * @param stmt Statement для выполнения SQL-запросов
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    private static void createWinesTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS wines (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "tannins INT CHECK (tannins BETWEEN 1 AND 5), " +
                "acidity INT CHECK (acidity BETWEEN 1 AND 5), " +
                "region VARCHAR(100), " +
                "vintage INT, " +
                "description TEXT)");
    }

    /**
     * Создает таблицу dishes, если она не существует.
     * @param stmt Statement для выполнения SQL-запросов
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    private static void createDishesTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS dishes (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "category VARCHAR(50) NOT NULL, " +
                "fat_content INT CHECK (fat_content BETWEEN 1 AND 5), " +
                "protein_content INT CHECK (protein_content BETWEEN 1 AND 5), " +
                "cooking_time INT, " +
                "ingredients TEXT, " +
                "recipe TEXT)");
    }

    /**
     * Создает таблицу pairings для хранения сочетаний вин и блюд, если она не существует.
     * @param stmt Statement для выполнения SQL-запросов
     * @throws SQLException если произошла ошибка при выполнении запроса
     */
    private static void createPairingsTable(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS pairings (" +
                "wine_id INT REFERENCES wines(id), " +
                "dish_id INT REFERENCES dishes(id), " +
                "score INT, " +
                "PRIMARY KEY (wine_id, dish_id))");
    }
}