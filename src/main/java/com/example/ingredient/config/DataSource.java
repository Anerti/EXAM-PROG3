package com.example.ingredient.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {

    private final String jdbcUrl;
    private final String user;
    private final String password;

    public DataSource(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
