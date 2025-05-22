package org.example.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DishDAO {
    private final Connection connection;

    public DishDAO(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Connection cannot be null");
    }

    /**
     * Находит блюда по категории
     */
    public List<String> findDishesByCategory(String category) throws SQLException {
        List<String> dishes = new ArrayList<>();
        String sql = "SELECT name FROM dishes WHERE category = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category.toUpperCase());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dishes.add(rs.getString("name"));
            }
        }

        return dishes;
    }

    /**
     * Находит все блюда, которые хорошо сочетаются с указанным вином
     */
    public List<String> findPairingsForWine(String wineName) throws SQLException {
        List<String> pairings = new ArrayList<>();
        String sql = "SELECT d.name FROM dishes d " +
                "JOIN pairings p ON d.id = p.dish_id " +
                "JOIN wines w ON p.wine_id = w.id " +
                "WHERE w.name LIKE ? ORDER BY p.score DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + wineName + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                pairings.add(rs.getString("name"));
            }
        }

        return pairings;
    }

    /**
     * Добавляет новое блюдо в базу данных
     */
    public boolean addDish(String name, String category, int fatContent, int proteinContent) throws SQLException {
        String sql = "INSERT INTO dishes (name, category, fat_content, protein_content) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, category.toUpperCase());
            stmt.setInt(3, fatContent);
            stmt.setInt(4, proteinContent);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Получает блюдо по ID
     */
    public Dish getDishById(int id) throws SQLException {
        String sql = "SELECT * FROM dishes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setCategory(Dish.DishCategory.valueOf(rs.getString("category")));
                dish.setFatContent(rs.getInt("fat_content"));
                dish.setProteinContent(rs.getInt("protein_content"));
                dish.setCookingTime(rs.getInt("cooking_time"));
                dish.setIngredients(rs.getString("ingredients"));
                dish.setRecipe(rs.getString("recipe"));
                return dish;
            }
        }

        return null;
    }

    /**
     * Обновляет информацию о блюде
     */
    public boolean updateDish(Dish dish) throws SQLException {
        String sql = "UPDATE dishes SET name = ?, category = ?, fat_content = ?, " +
                "protein_content = ?, cooking_time = ?, ingredients = ?, recipe = ? " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dish.getName());
            stmt.setString(2, dish.getCategory().toString());
            stmt.setInt(3, dish.getFatContent());
            stmt.setInt(4, dish.getProteinContent());
            stmt.setInt(5, dish.getCookingTime());
            stmt.setString(6, dish.getIngredients());
            stmt.setString(7, dish.getRecipe());
            stmt.setInt(8, dish.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Удаляет блюдо из базы данных
     */
    public boolean deleteDish(int id) throws SQLException {
        String sql = "DELETE FROM dishes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Находит все блюда в базе данных
     */
    public List<Dish> getAllDishes() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT * FROM dishes";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setCategory(Dish.DishCategory.valueOf(rs.getString("category")));
                dish.setFatContent(rs.getInt("fat_content"));
                dish.setProteinContent(rs.getInt("protein_content"));
                dish.setCookingTime(rs.getInt("cooking_time"));
                dish.setIngredients(rs.getString("ingredients"));
                dish.setRecipe(rs.getString("recipe"));
                dishes.add(dish);
            }
        }

        return dishes;
    }
}