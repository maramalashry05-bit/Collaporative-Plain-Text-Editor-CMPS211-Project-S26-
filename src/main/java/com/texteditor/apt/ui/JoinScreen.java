package com.texteditor.apt.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JoinScreen {

    private final Stage stage;
    private final LocalDatabase localDatabase;

    public JoinScreen(Stage stage, LocalDatabase localDatabase) {
        this.stage = stage;
        this.localDatabase = localDatabase;
    }

    public void show() {
        Label title = new Label("Collaborative Text Editor");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label nameLabel = new Label("Your Name:");
        nameLabel.setStyle("-fx-text-fill: white;");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Alice");

        Label codeLabel = new Label("Access Code:");
        codeLabel.setStyle("-fx-text-fill: white;");
        TextField codeField = new TextField();
        codeField.setPromptText("e.g. edit-abc123");

        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: red;");

        Button joinButton = new Button("Join");
        joinButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Load last session if it exists
        String[] lastSession = localDatabase.loadLastSession();
        if (lastSession != null) {
            nameField.setText(lastSession[0]);
            codeField.setText(lastSession[1]);
        }

        joinButton.setOnAction(e -> {
            String username = nameField.getText().trim();
            String accessCode = codeField.getText().trim();

            if (username.isEmpty() || accessCode.isEmpty()) {
                errorLabel.setText("Please fill in both fields.");
                return;
            }

            // Save to local database
            localDatabase.saveSession(username, accessCode);

            // Call the server to get the numeric document ID
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    new java.net.URL("http://localhost:8080/api/documents/join")
                    .openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String body = "{\"code\":\"" + accessCode + "\"}";
                conn.getOutputStream().write(body.getBytes());

                if (conn.getResponseCode() == 404) {
                    errorLabel.setText("Invalid access code.");
                    return;
                }

                // Read response
                String response = new String(conn.getInputStream().readAllBytes());
                com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(response);

                String documentId = json.get("documentId").asText();
                String role = json.get("role").asText();
                //debugging
                System.out.println("[JoinScreen] Got documentId: " + documentId);
                System.out.println("[JoinScreen] Got role: " + role);
                /////////
                
                System.out.println("[JoinScreen] Joined as " + role + " on doc " + documentId);

                // Open the editor with the numeric document ID
                EditorWindow editorWindow = new EditorWindow(stage, localDatabase);
                editorWindow.show();
                editorWindow.connectToServer("ws://localhost:8080/ws", documentId, username, 1);

            } catch (Exception ex) {
                errorLabel.setText("Could not connect to server.");
                System.err.println("[JoinScreen] Error: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(12, title, nameLabel, nameField, codeLabel, codeField, errorLabel, joinButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #1A1A2E;");

        Scene scene = new Scene(layout, 350, 300);
        stage.setScene(scene);
        stage.setTitle("Join Document");
        stage.show();
    }
}