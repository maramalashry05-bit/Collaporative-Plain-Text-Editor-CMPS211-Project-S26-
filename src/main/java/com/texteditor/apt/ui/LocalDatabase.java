package com.texteditor.apt.ui;

import com.texteditor.apt.CRDT.Block_CRDT;
import com.texteditor.apt.CRDT.CRDTSerializer;

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
        String sessionTable = "CREATE TABLE IF NOT EXISTS last_session (" +
                     "id INTEGER PRIMARY KEY," +
                     "username TEXT," +
                     "access_code TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sessionTable);
        }

        String documentsTable = "CREATE TABLE IF NOT EXISTS documents (" +
                     "doc_id  TEXT PRIMARY KEY," +
                     "title   TEXT," +
                     "content TEXT," +
                     "crdt_json TEXT)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(documentsTable);
        }

        System.out.println("[LocalDatabase] Tables ready.");
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

    public void saveDocumentText(String docId, String title, String text) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO documents (doc_id, title, content) VALUES (?, ?, ?)");
            stmt.setString(1, docId);
            stmt.setString(2, title);
            stmt.setString(3, text);
            stmt.executeUpdate();
            System.out.println("[LocalDatabase] Document text saved: " + docId);
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to save document text: " + e.getMessage());
        }
    }

    public String loadDocumentText(String docId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT content FROM documents WHERE doc_id = ?");
            stmt.setString(1, docId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("[LocalDatabase] Document text loaded: " + docId);
                return rs.getString("content");
            }
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to load document text: " + e.getMessage());
        }
        return "";
    }

    public void saveDocument(String docId, String title, Block_CRDT crdt) {
        try {
            String json = CRDTSerializer.serialize(crdt);
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO documents (doc_id, title, crdt_json) VALUES (?, ?, ?)");
            stmt.setString(1, docId);
            stmt.setString(2, title);
            stmt.setString(3, json);
            stmt.executeUpdate();
            System.out.println("[LocalDatabase] Document CRDT saved: " + docId);
        } catch (Exception e) {
            System.err.println("[LocalDatabase] Failed to save document CRDT: " + e.getMessage());
        }
    }

    public Block_CRDT loadDocument(String docId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT crdt_json FROM documents WHERE doc_id = ?");
            stmt.setString(1, docId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String json = rs.getString("crdt_json");
                System.out.println("[LocalDatabase] Document CRDT loaded: " + docId);
                return CRDTSerializer.deserialize(json);
            }
        } catch (Exception e) {
            System.err.println("[LocalDatabase] Failed to load document CRDT: " + e.getMessage());
        }
        return new Block_CRDT();
    }

    public String loadDocumentTitle(String docId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT title FROM documents WHERE doc_id = ?");
            stmt.setString(1, docId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to load title: " + e.getMessage());
        }
        return "Untitled Document";
    }

    public boolean documentExists(String docId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM documents WHERE doc_id = ?");
            stmt.setString(1, docId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to check document: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    public void deleteDocument(String docId) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM documents WHERE doc_id = ?");
            stmt.setString(1, docId);
            stmt.executeUpdate();
            System.out.println("[LocalDatabase] Document deleted: " + docId);
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to delete document: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("[LocalDatabase] Failed to close: " + e.getMessage());
        }
    }
}

