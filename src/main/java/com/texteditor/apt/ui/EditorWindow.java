package com.texteditor.apt.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import com.texteditor.apt.Networking.WebSocketClient;

public class EditorWindow {

    private final Stage             stage;
    private final EditorToolbar     toolbar;
    private final EditorPane        editorPane;
    private final UserPresencePanel presencePanel;
    private final LocalDatabase     localDatabase;

    private WebSocketClient wsClient;

    private String docId;
    private String userId;

    public EditorWindow(Stage stage, LocalDatabase localDatabase) {
        this.stage         = stage;
        this.localDatabase = localDatabase;
        this.toolbar       = new EditorToolbar();
        this.editorPane    = new EditorPane();
        this.presencePanel = new UserPresencePanel();

        buildUI();
        wireToolbarButtons();
    }

    public void initDocument(String docId, String userId) {
        this.docId  = docId;
        this.userId = userId;
// Tell the EditorPane what your name is
        editorPane.setMyUsername(userId);

        // Generate the color slot based on your name (modulo 4 because you have 4 colors)
        int myColorSlot = Math.abs(userId.hashCode()) % 4;
        
        // Use YOUR exact method!
        presencePanel.addUser(userId, myColorSlot);
        if (localDatabase.documentExists(docId)) {
            String savedText = localDatabase.loadDocumentText(docId);
            String title     = localDatabase.loadDocumentTitle(docId);
            toolbar.setTitle(title);
            
            // 1. Populate the CRDT in the background first!
            for (int i = 0; i < savedText.length(); i++) {
                editorPane.getCrdtState().localInsert(i, savedText.charAt(i));
            }
            
            // 2. Then show it on the screen
            editorPane.setTextSilently(savedText);
            System.out.println("[EditorWindow] Document loaded from DB: " + docId);
        } else {
            System.out.println("[EditorWindow] New document created: " + docId);
        }

        // Save to DB on every keystroke
        editorPane.getTextArea().textProperty().addListener((obs, oldText, newText) -> {
            saveCurrentDocument();
        });

        // ── RENAME: save to DB when user finishes editing the title ───────
        toolbar.getTitleField().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                saveCurrentDocument();
                System.out.println("[EditorWindow] Document renamed to: " + toolbar.getDocumentTitle());
            }
        });
    }

// ──Permission Handling ─────────────────────────────
    /**
     * Locks or unlocks the UI based on the user's role from the database.
     * Call this right after the user successfully joins the document.
     */
    public void applyPermissions(String role) {
        if ("VIEWER".equalsIgnoreCase(role)) {
            System.out.println("[EditorWindow] Applying VIEWER permissions. Locking UI.");
            
            // 1. Lock the text area so they cannot type
            editorPane.getTextArea().setEditable(false);
            
            // 2. Disable editing tools in the toolbar
            toolbar.getUndoBtn().setDisable(true);
            toolbar.getRedoBtn().setDisable(true);
            toolbar.getBoldBtn().setDisable(true);
            toolbar.getItalicBtn().setDisable(true);
            toolbar.getImportItem().setDisable(true); // Viewers shouldn't import
            toolbar.getDeleteItem().setDisable(true); // Viewers shouldn't delete
            
            // 3. Update the title so they know they are in read-only mode
            stage.setTitle(toolbar.getDocumentTitle() + " (Read-Only Mode)");
            
        } else if ("EDITOR".equalsIgnoreCase(role)) {
            System.out.println("[EditorWindow] Applying EDITOR permissions. Unlocking UI.");
            
            // 1. Unlock everything
            editorPane.getTextArea().setEditable(true);
            
            toolbar.getUndoBtn().setDisable(false);
            toolbar.getRedoBtn().setDisable(false);
            toolbar.getBoldBtn().setDisable(false);
            toolbar.getItalicBtn().setDisable(false);
            toolbar.getImportItem().setDisable(false);
            toolbar.getDeleteItem().setDisable(false);
            
            stage.setTitle(toolbar.getDocumentTitle() + " (Editor Mode)");
        }
    }


    public void show() {
        stage.show();
    }

    // ── Layout ────────────────────────────────────────────────────────────

    private void buildUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1A1A2E;");

        root.setTop(toolbar);
        root.setCenter(editorPane);
        root.setRight(presencePanel);

        BorderPane.setMargin(editorPane, new Insets(0));

        Scene scene = new Scene(root, 960, 620);
        scene.getStylesheets().add(getClass().getResource("/editor.css") != null
                ? getClass().getResource("/editor.css").toExternalForm()
                : "");

        stage.setTitle("CollabEdit — Phase 2");
        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(400);

        stage.setOnCloseRequest(e -> {
            saveCurrentDocument();
            disconnect();
        });
    }

    // ── Toolbar buttons ───────────────────────────────────────────────────

    private void wireToolbarButtons() {
        toolbar.getBackBtn().setOnAction(e -> {
            saveCurrentDocument();
            disconnect();
            LauncherScreen launcher = new LauncherScreen(stage, localDatabase);
            launcher.show();
        });

        toolbar.getUndoBtn().setOnAction(e -> {
            editorPane.getTextArea().requestFocus();
            editorPane.getTextArea().undo();
        });
        toolbar.getRedoBtn().setOnAction(e -> {
            editorPane.getTextArea().requestFocus();
            editorPane.getTextArea().redo();
        });

        toolbar.getBoldBtn().setOnAction(e -> {
            String selected = editorPane.getTextArea().getSelectedText();
            if (!selected.isEmpty()) {
                int start = editorPane.getTextArea().getSelection().getStart();
                int end   = editorPane.getTextArea().getSelection().getEnd();
                editorPane.getTextArea().replaceText(start, end, "**" + selected + "**");
            }
        });

        toolbar.getItalicBtn().setOnAction(e -> {
            String selected = editorPane.getTextArea().getSelectedText();
            if (!selected.isEmpty()) {
                int start = editorPane.getTextArea().getSelection().getStart();
                int end   = editorPane.getTextArea().getSelection().getEnd();
                editorPane.getTextArea().replaceText(start, end, "*" + selected + "*");
            }
        });

        toolbar.getExportItem().setOnAction(e -> handleExport());
        toolbar.getImportItem().setOnAction(e -> handleImport());

        // ── DELETE ────────────────────────────────────────────────────────
        toolbar.getDeleteItem().setOnAction(e -> handleDelete());
    }

    // ── Persistence ───────────────────────────────────────────────────────

    private void saveCurrentDocument() {
        if (docId == null) return;
        String text  = editorPane.getTextArea().getText();
        String title = toolbar.getDocumentTitle();
        localDatabase.saveDocumentText(docId, title, text);
    }

    // ── Import logic ──────────────────────────────────────────────────────

    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Text File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) content.append("\n");
                content.append(line);
                firstLine = false;
            }
        } catch (IOException ex) {
            showAlert(AlertType.ERROR, "Import Failed",
                "Could not read the file:\n" + ex.getMessage());
            return;
        }

        // Load into editor through CRDT character by character
        String text = content.toString();
        editorPane.setTextSilently("");
        for (int i = 0; i < text.length(); i++) {
            editorPane.getCrdtState().localInsert(i, text.charAt(i));
        }
        editorPane.setTextSilently(text);

        String fileName = file.getName();
        if (fileName.endsWith(".txt")) fileName = fileName.substring(0, fileName.length() - 4);
        toolbar.setTitle(fileName);

        editorPane.getTextArea().positionCaret(0);
        saveCurrentDocument();

        showAlert(AlertType.INFORMATION, "Import Successful",
            "File imported:\n" + file.getAbsolutePath());
    }

    // ── Export logic ──────────────────────────────────────────────────────

    private void handleExport() {
        String content = editorPane.getTextArea().getText();

        if (content == null || content.isBlank()) {
            showAlert(AlertType.WARNING, "Nothing to Export",
                "The document is empty. Please write something before exporting.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Document");

        String docTitle = toolbar.getDocumentTitle().trim();
        if (docTitle.isEmpty()) docTitle = "Untitled Document";
        fileChooser.setInitialFileName(docTitle + ".txt");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt")
        );

        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            showAlert(AlertType.INFORMATION, "Export Successful",
                "Document saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            showAlert(AlertType.ERROR, "Export Failed",
                "Could not save the file:\n" + ex.getMessage());
        }
    }

    // ── Delete logic ──────────────────────────────────────────────────────

    private void handleDelete() {
        // Show confirmation dialog before deleting
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Delete Document");
        confirm.setHeaderText("Are you sure you want to delete this document?");
        confirm.setContentText("\"" + toolbar.getDocumentTitle() + "\" will be permanently deleted.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete from DB
            localDatabase.deleteDocument(docId);
            System.out.println("[EditorWindow] Document deleted: " + docId);

            // Disconnect and go back to launcher
            disconnect();
            LauncherScreen launcher = new LauncherScreen(stage, localDatabase);
            launcher.show();
        }
    }

    // ── WebSocket ─────────────────────────────────────────────────────────

    public void connectToServer(String serverUrl, String docId,
                                String userId, int colorSlot) {
        wsClient = new WebSocketClient(editorPane, presencePanel, toolbar);
        wsClient.connect(serverUrl, docId, userId, colorSlot);
    }

    public void disconnect() {
        if (wsClient != null) wsClient.disconnect();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public EditorPane        getEditorPane()    { return editorPane;    }
    public EditorToolbar     getToolbar()       { return toolbar;       }
    public UserPresencePanel getPresencePanel() { return presencePanel; }
    public WebSocketClient   getWsClient()      { return wsClient;      }
    public String            getDocId()         { return docId;         }



    
}
