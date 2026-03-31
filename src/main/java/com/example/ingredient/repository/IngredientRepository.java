package com.example.ingredient.repository;

import com.example.ingredient.entity.*;

import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ingredient> findAll() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT id, name, category, price FROM ingredient";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                ingredient.setPrice(rs.getDouble("price"));
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredients;
    }

    public Ingredient findById(Integer id) {
        String sql = "SELECT id, name, category, price FROM ingredient WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
                    ingredient.setPrice(rs.getDouble("price"));
                    return ingredient;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public StockValue getStockValueAt(Instant t, Integer ingredientId, Unit unit) {
        String sql = """
            SELECT unit,
                   SUM(CASE
                       WHEN stock_movement.type = 'IN' THEN quantity
                       WHEN stock_movement.type = 'OUT' THEN -1 * quantity
                       ELSE 0 END) as actual_quantity
            FROM stock_movement
            WHERE id_ingredient = ? AND unit = ? AND creation_datetime <= ?
            GROUP BY unit
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ps.setString(2, unit.name());
            ps.setTimestamp(3, Timestamp.from(t));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StockValue stockValue = new StockValue();
                    stockValue.setQuantity(rs.getDouble("actual_quantity"));
                    stockValue.setUnit(Unit.valueOf(rs.getString("unit")));
                    return stockValue;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<StockMovement> findStockMovementsByIngredientId(Integer id) {
        List<StockMovement> stockMovements = new ArrayList<>();
        String sql = """
            SELECT id, quantity, unit, type, creation_datetime
            FROM stock_movement
            WHERE id_ingredient = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockMovement stockMovement = new StockMovement();
                    stockMovement.setId(rs.getInt("id"));
                    stockMovement.setType(MovementTypeEnum.valueOf(rs.getString("type")));
                    stockMovement.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                    StockValue stockValue = new StockValue();
                    stockValue.setQuantity(rs.getDouble("quantity"));
                    stockValue.setUnit(Unit.valueOf(rs.getString("unit")));
                    stockMovement.setValue(stockValue);

                    stockMovements.add(stockMovement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stockMovements;
    }

    public List<StockMovement> findStockMovementsByIngredientIdAndDateRange(Integer id, Instant from, Instant to) {
        List<StockMovement> stockMovements = new ArrayList<>();
        String sql = """
            SELECT id, quantity, unit, type, creation_datetime
            FROM stock_movement
            WHERE id_ingredient = ? AND creation_datetime >= ? AND creation_datetime <= ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setTimestamp(2, Timestamp.from(from));
            ps.setTimestamp(3, Timestamp.from(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockMovement stockMovement = new StockMovement();
                    stockMovement.setId(rs.getInt("id"));
                    stockMovement.setType(MovementTypeEnum.valueOf(rs.getString("type")));
                    stockMovement.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                    StockValue stockValue = new StockValue();
                    stockValue.setQuantity(rs.getDouble("quantity"));
                    stockValue.setUnit(Unit.valueOf(rs.getString("unit")));
                    stockMovement.setValue(stockValue);

                    stockMovements.add(stockMovement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stockMovements;
    }

    public void saveStockMovement(StockMovement stockMovement, Integer ingredientId) {
        String sql = """
            INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
            VALUES (?, ?, ?, ?::movement_type, ?::unit, ?)
            """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            int nextId = getNextSerialValue(conn, "stock_movement", "id");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nextId);
                ps.setInt(2, ingredientId);
                ps.setDouble(3, stockMovement.getValue().getQuantity());
                ps.setString(4, stockMovement.getType().name());
                ps.setString(5, stockMovement.getValue().getUnit().name());
                ps.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                ps.executeUpdate();
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
