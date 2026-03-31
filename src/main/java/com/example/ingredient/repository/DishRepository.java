package com.example.ingredient.repository;

import com.example.ingredient.entity.*;

import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;

    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Dish> findAll() {
        List<Dish> dishes = new ArrayList<>();
        String sql = "SELECT id, name, dish_type, selling_price FROM dish";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));
                dish.setDishIngredients(findIngredientsByDishId(dish.getId()));
                dishes.add(dish);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dishes;
    }

    public Dish findById(Integer id) {
        String sql = "SELECT id, name, dish_type, selling_price FROM dish WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Dish dish = new Dish();
                    dish.setId(rs.getInt("id"));
                    dish.setName(rs.getString("name"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                    dish.setPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));
                    dish.setDishIngredients(findIngredientsByDishId(id));
                    return dish;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<DishIngredient> findIngredientsByDishId(Integer idDish) {
        List<DishIngredient> dishIngredients = new ArrayList<>();
        String sql = """
            SELECT ingredient.id, ingredient.name, ingredient.price, ingredient.category,
                   di.required_quantity, di.unit
            FROM ingredient
            JOIN dish_ingredient di ON di.id_ingredient = ingredient.id
            WHERE id_dish = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDish);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setPrice(rs.getDouble("price"));
                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                    DishIngredient dishIngredient = new DishIngredient();
                    dishIngredient.setIngredient(ingredient);
                    dishIngredient.setQuantity(rs.getObject("required_quantity") == null ? null : rs.getDouble("required_quantity"));
                    dishIngredient.setUnit(Unit.valueOf(rs.getString("unit")));

                    dishIngredients.add(dishIngredient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dishIngredients;
    }

    public void updateDishIngredients(Integer dishId, List<Ingredient> ingredients) {
        String deleteSql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        String insertSql = """
            INSERT INTO dish_ingredient (id, id_ingredient, id_dish, required_quantity, unit)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }

            if (ingredients != null && !ingredients.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    int nextId = getNextSerialValue(conn, "dish_ingredient", "id");
                    for (Ingredient ingredient : ingredients) {
                        ps.setInt(1, nextId++);
                        ps.setInt(2, ingredient.getId());
                        ps.setInt(3, dishId);
                        ps.setDouble(4, 1.0);
                        ps.setString(5, "PCS");
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName) throws SQLException {
        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException("No sequence found for " + tableName + "." + columnName);
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";
        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT pg_get_serial_sequence(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName);
        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}
