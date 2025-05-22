package org.example.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DAO класс для работы с винами и их сочетаниями с блюдами
 */
public class WineDAO {
    private final Connection connection;

    public WineDAO(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Соединение с БД не может быть null");
    }

    /**
     * Находит сочетания блюд для указанного вина
     */
    public List<String> findPairings(String wineName) {
        try {
            List<String> pairings = new ArrayList<>();
            String sql = "SELECT d.name FROM pairings p " +
                    "JOIN dishes d ON p.dish_id = d.id " +
                    "JOIN wines w ON p.wine_id = w.id " +
                    "WHERE w.name LIKE ? ORDER BY p.score DESC";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, "%" + wineName + "%");
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    pairings.add(rs.getString(1));
                }
            }
            return pairings;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске сочетаний для вина: " + wineName, e);
        }
    }

    /**
     * Добавляет новое вино в базу данных
     */
    public Wine addWine(Wine wine) {
        String sql = "INSERT INTO wines (name, type, tannins, acidity, region, vintage, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, wine.getName());
            stmt.setString(2, wine.getType().toString());
            stmt.setInt(3, wine.getTannins());
            stmt.setInt(4, wine.getAcidity());
            stmt.setString(5, wine.getRegion());
            stmt.setInt(6, wine.getVintage());
            stmt.setString(7, wine.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Создание вина не удалось, ни одна запись не добавлена");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    wine.setId(generatedKeys.getInt(1));
                    return wine;
                } else {
                    throw new DataAccessException("Создание вина не удалось, ID не получен");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при добавлении вина", e);
        }
    }

    /**
     * Получает вино по ID
     */
    public Wine getWineById(int id) {
        String sql = "SELECT * FROM wines WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRowToWine(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при получении вина по ID: " + id, e);
        }
    }

    /**
     * Обновляет информацию о вине
     */
    public Wine updateWine(Wine wine) {
        String sql = "UPDATE wines SET name = ?, type = ?, tannins = ?, acidity = ?, " +
                "region = ?, vintage = ?, description = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, wine.getName());
            stmt.setString(2, wine.getType().toString());
            stmt.setInt(3, wine.getTannins());
            stmt.setInt(4, wine.getAcidity());
            stmt.setString(5, wine.getRegion());
            stmt.setInt(6, wine.getVintage());
            stmt.setString(7, wine.getDescription());
            stmt.setInt(8, wine.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Обновление вина не удалось, ни одна запись не изменена");
            }
            return wine;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при обновлении вина с ID: " + wine.getId(), e);
        }
    }

    /**
     * Удаляет вино из базы данных
     */
    public boolean deleteWine(int id) {
        String sql = "DELETE FROM wines WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при удалении вина с ID: " + id, e);
        }
    }

    /**
     * Получает все вина из базы данных
     */
    public List<Wine> getAllWines() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM wines")) {

            List<Wine> wines = new ArrayList<>();
            while (rs.next()) {
                wines.add(mapRowToWine(rs));
            }
            return wines;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при получении списка вин", e);
        }
    }

    /**
     * Находит вина по названию (поиск с LIKE)
     */
    public List<Wine> findWinesByName(String name) {
        String sql = "SELECT * FROM wines WHERE name LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();

            List<Wine> wines = new ArrayList<>();
            while (rs.next()) {
                wines.add(mapRowToWine(rs));
            }
            return wines;
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка при поиске вин по названию: " + name, e);
        }
    }

    /**
     * Преобразует строку ResultSet в объект Wine
     */
    private Wine mapRowToWine(ResultSet rs) throws SQLException {
        Wine wine = new Wine ();
        wine.setId(rs.getInt("id"));
        wine.setName(rs.getString("name"));
        wine.setType(Wine.WineType.valueOf(rs.getString("type")));
        wine.setTannins(rs.getInt("tannins"));
        wine.setAcidity(rs.getInt("acidity"));
        wine.setRegion(rs.getString("region"));
        wine.setVintage(rs.getInt("vintage"));
        wine.setDescription(rs.getString("description"));
        return wine;
    }

    /**
     * Непроверяемое исключение для ошибок доступа к данным
     */
    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message) {
            super(message);
        }

        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}