package com.texteditor.apt.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class EditorWindow {

    private final Stage              stage;
    private final EditorToolbar      toolbar;
    private final EditorPane         editorPane;
    private final UserPresencePanel  presencePanel;
    private final LocalDatabase      localDatabase;

    private WebSocketClient wsClient;

    // Tracks the last known text so we can detect what changed (CRDT hooks)
    private String lastText = "";

    public EditorWindow(Stage stage, LocalDatabase localDatabase) {
        this.stage         = stage;
        this.localDatabase = localDatabase;
        this.toolbar       = new EditorToolbar();
        this.editorPane    = new EditorPane();
        this.presencePanel = new UserPresencePanel();

        buildUI();
        wireToolbarButtons();
        setupKeystrokeInterception();
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

        // Disconnect cleanly when the window X button is clicked
        stage.setOnCloseRequest(e -> disconnect());
    }

    // ── Toolbar buttons ───────────────────────────────────────────────────

    private void wireToolbarButtons() {
        // Back to launcher
        toolbar.getBackBtn().setOnAction(e -> {
            disconnect();
            LauncherScreen launcher = new LauncherScreen(stage, localDatabase);
            launcher.show();
        });

        // Undo / Redo
        toolbar.getUndoBtn().setOnAction(e -> {
            editorPane.getTextArea().requestFocus();
            editorPane.getTextArea().undo();
        });
        toolbar.getRedoBtn().setOnAction(e -> {
            editorPane.getTextArea().requestFocus();
            editorPane.getTextArea().redo();
        });

        // Bold — wraps selected text with **text**
        toolbar.getBoldBtn().setOnAction(e -> {
            String selected = editorPane.getTextArea().getSelectedText();
            if (!selected.isEmpty()) {
                int start = editorPane.getTextArea().getSelection().getStart();
                int end   = editorPane.getTextArea().getSelection().getEnd();
                editorPane.getTextArea().replaceText(start, end, "**" + selected + "**");
            }
        });

        // Italic — wraps selected text with *text*
        toolbar.getItalicBtn().setOnAction(e -> {
            String selected = editorPane.getTextArea().getSelectedText();
            if (!selected.isEmpty()) {
                int start = editorPane.getTextArea().getSelection().getStart();
                int end   = editorPane.getTextArea().getSelection().getEnd();
                editorPane.getTextArea().replaceText(start, end, "*" + selected + "*");
            }
        });
    }

    // ── Keystroke interception (CRDT hooks) ───────────────────────────────

    private void setupKeystrokeInterception() {
        lastText = editorPane.getTextArea().getText();

        editorPane.getTextArea().textProperty().addListener((obs, oldText, newText) -> {
            int caretPos = editorPane.getTextArea().getCaretPosition();

            if (newText.length() > oldText.length()) {
                // INSERT detected
                int position = caretPos - 1;
                if (position >= 0 && position < newText.length()) {
                    onLocalInsert(position, newText.charAt(position));
                }
            } else if (newText.length() < oldText.length()) {
                // DELETE detected
                onLocalDelete(caretPos);
            }

            lastText = newText;
        });
    }

    /**
     * Called every time the user inserts a character.
     * Person 3 plugs their CRDT code here.
     *
     * @param position  the index where the character was inserted
     * @param character the character that was inserted
     */
    private void onLocalInsert(int position, char character) {
        System.out.println("INSERT '" + character + "' at position " + position);
        // Person 3 hooks here:
        // CRDTOperation op = crdt.localInsert(position, character);
        // webSocketClient.send(op.toJson());
    }

    /**
     * Called every time the user deletes a character.
     * Person 3 plugs their CRDT code here.
     *
     * @param position the index where the character was deleted
     */
    private void onLocalDelete(int position) {
        System.out.println("DELETE at position " + position);
        // Person 3 hooks here:
        // CRDTOperation op = crdt.localDelete(position);
        // webSocketClient.send(op.toJson());
    }

    // ── WebSocket ─────────────────────────────────────────────────────────

    /**
     * Creates the WebSocketClient and connects to the server.
     * Call this from EditorApp after show():
     *   window.connectToServer("ws://localhost:8080/ws", "doc-001", "Alice", 1);
     *
     * @param serverUrl  e.g. "ws://localhost:8080/ws"
     * @param docId      the document room ID
     * @param userId     this user's display name
     * @param colorSlot  0–3 for cursor color
     */
    public void connectToServer(String serverUrl, String docId,
                                String userId, int colorSlot) {
        wsClient = new WebSocketClient(editorPane, presencePanel, toolbar);
        wsClient.connect(serverUrl, docId, userId, colorSlot);
    }

    /** Disconnects the WebSocket cleanly (called on Back or window close). */
    public void disconnect() {
        if (wsClient != null) wsClient.disconnect();
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public EditorPane        getEditorPane()    { return editorPane;    }
    public EditorToolbar     getToolbar()       { return toolbar;       }
    public UserPresencePanel getPresencePanel() { return presencePanel; }
    public WebSocketClient   getWsClient()      { return wsClient;      }
}