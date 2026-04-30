package com.texteditor.apt.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class LauncherScreen {

    private final Stage stage;
    private final LocalDatabase localDatabase;

    public LauncherScreen(Stage stage, LocalDatabase localDatabase) {
        this.stage = stage;
        this.localDatabase = localDatabase;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1A1A2E;");

        // ── Title ─────────────────────────────────────────────────────────
        Label title = new Label("CollabEdit");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#E0E0F0"));

        Label subtitle = new Label("Collaborative Text Editor");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.web("#888899"));

        VBox titleBox = new VBox(6, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // ── Error label ───────────────────────────────────────────────────
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#E24A4A"));
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setVisible(false);

        // ── Card 1: New Doc ───────────────────────────────────────────────
        VBox newDocCard = makeCard();
        Label newDocIcon = new Label("📄");
        newDocIcon.setFont(Font.font(40));
        Button newDocBtn = makeCardButton("New Doc.");
        newDocBtn.setOnAction(e -> {
            // Generate a unique ID for this new document
            String newDocId = UUID.randomUUID().toString();

            EditorWindow editor = new EditorWindow(stage, localDatabase);
            editor.show();
            editor.initDocument(newDocId, "User1");
        });
        newDocCard.getChildren().addAll(newDocIcon, newDocBtn);

        // ── Card 2: Browse ────────────────────────────────────────────────
        VBox browseCard = makeCard();
        Label browseIcon = new Label("📋");
        browseIcon.setFont(Font.font(40));
        Button browseBtn = makeCardButton("Browse..");
        browseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open Text File");
            chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    String content = Files.readString(file.toPath());

                    // Use file name as document ID so same file loads same doc
                    String fileDocId = "file-" + file.getName();

                    EditorWindow editor = new EditorWindow(stage, localDatabase);
                    editor.show();
                    editor.initDocument(fileDocId, "User1");

                    // Set the imported text and title
                    editor.getEditorPane().getTextArea().setText(content);
                    editor.getToolbar().setTitle(file.getName());

                } catch (Exception ex) {
                    errorLabel.setText("Could not read file: " + ex.getMessage());
                    errorLabel.setVisible(true);
                }
            }
        });
        browseCard.getChildren().addAll(browseIcon, browseBtn);

        // ── Card 3: Join Session ──────────────────────────────────────────
        VBox joinCard = makeCard();
        Label joinIcon = new Label("👥");
        joinIcon.setFont(Font.font(40));
        TextField codeField = new TextField();
        codeField.setPromptText("Session Code");
        codeField.setStyle(
            "-fx-background-color: #F5F5F5; " +
            "-fx-text-fill: #111; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 12 8 12; " +
            "-fx-pref-width: 150;"
        );
        Button joinBtn = makeCardButton("Join");
        joinBtn.setOnAction(e -> {
            String code = codeField.getText().trim();
            if (code.isEmpty()) {
                errorLabel.setText("⚠ Please enter a session code.");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
                EditorWindow editor = new EditorWindow(stage, localDatabase);
                editor.show();
                // Use the session code as the document ID
                editor.initDocument(code, "Alice");
                editor.connectToServer("ws://localhost:8080/ws", code, "Alice", 1);
            }
        });
        codeField.setOnAction(e -> joinBtn.fire());
        joinCard.getChildren().addAll(joinIcon, codeField, joinBtn);

        // ── Cards row ─────────────────────────────────────────────────────
        HBox cardsRow = new HBox(20);
        cardsRow.setAlignment(Pos.CENTER);
        cardsRow.getChildren().addAll(newDocCard, browseCard, joinCard);

        // ── Center layout ─────────────────────────────────────────────────
        VBox center = new VBox(40);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(60));
        center.getChildren().addAll(titleBox, cardsRow, errorLabel);

        root.setCenter(center);

        // ── Scene ─────────────────────────────────────────────────────────
        Scene scene = new Scene(root, 700, 420);
        stage.setTitle("CollabEdit — Welcome");
        stage.setScene(scene);
        stage.setMinWidth(520);
        stage.setMinHeight(340);
        stage.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private VBox makeCard() {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setPrefWidth(170);
        card.setStyle(
            "-fx-background-color: #F8F8FA; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);"
        );
        return card;
    }

    private Button makeCardButton(String text) {
        Button btn = new Button(text);
        String style =
            "-fx-background-color: #1A1A2E; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 20 8 20; " +
            "-fx-cursor: hand; " +
            "-fx-min-width: 110;";
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(style.replace("#1A1A2E", "#2E2E5E")));
        btn.setOnMouseExited(e  -> btn.setStyle(style));
        return btn;
    }
}
