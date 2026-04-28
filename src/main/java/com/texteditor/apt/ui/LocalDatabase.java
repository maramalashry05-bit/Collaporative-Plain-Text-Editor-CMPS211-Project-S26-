package com.texteditor.apt.ui;

import java.sql.*;

public class LocalDatabase {

    private Connection connection;

    public void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:local_editor.db");
            createTableIfNeeded();
            System.out.println("[LocalDatabase] Connected.");
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to connect: " + e.getMessage());
        }
    }

    private void createTableIfNeeded() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS last_session (" +
                     "id INTEGER PRIMARY KEY," +
                     "username TEXT," +
                     "access_code TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveSession(String username, String accessCode) {
        try {
            connection.createStatement().execute("DELETE FROM last_session");
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO last_session (username, access_code) VALUES (?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, accessCode);
            stmt.executeUpdate();
            System.out.println("[LocalDatabase] Session saved.");
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to save session: " + e.getMessage());
        }
    }

    public String[] loadLastSession() {
        try {
            ResultSet rs = connection.createStatement()
                .executeQuery("SELECT username, access_code FROM last_session LIMIT 1");
            if (rs.next()) {
                return new String[]{rs.getString("username"), rs.getString("access_code")};
            }
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to load session: " + e.getMessage());
        }
        return null;
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to close: " + e.getMessage());
        }
    }
}