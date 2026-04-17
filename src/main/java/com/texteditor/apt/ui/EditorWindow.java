package com.texteditor.apt.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Assembles the full editor window.
 *
 * Layout:
 * ┌────────────────────────────────────────────┐
 * │              EditorToolbar (TOP)           │
 * ├────────────────────────────────┬───────────┤
 * │                                │  User     │
 * │         EditorPane (CENTER)    │  Presence │
 * │                                │  Panel    │
 * └────────────────────────────────┴───────────┘
 *
 * Phase 2 integration notes
 * ─────────────────────────
 * Step 2: Keystroke handlers live inside EditorPane
 * Step 3: WebSocketClient created here and connected to UI components
 * Step 4: applyRemote* calls routed through EditorPane
 * Step 5: Cursor updates routed through EditorPane.updateRemoteCursor()
 */
public class EditorWindow {

    private final Stage stage;
    private final EditorToolbar toolbar;
    private final EditorPane editorPane;
    private final UserPresencePanel presencePanel;

    // Step 3: hold the WebSocket client so we can disconnect on close
    private WebSocketClient wsClient;

    public EditorWindow(Stage stage) {
        this.stage         = stage;
        this.toolbar       = new EditorToolbar();
        this.editorPane    = new EditorPane();
        this.presencePanel = new UserPresencePanel();

        buildUI();
        // addDemoUsers() is now REMOVED — real users come from the server
        // If you want to keep seeing the demo while server is offline,
        // you can temporarily put it back, but remove it before final testing

      /*presencePanel.addUser("Alice", 1);
   presencePanel.addUser("Bob", 2);
   editorPane.updateRemoteCursor("Alice", 5, 1);
   editorPane.updateRemoteCursor("Bob", 10, 2); 
      
      */ 
    }



    public void show() {
        stage.show();
    }

    // ── Step 3: NEW method — call this from EditorApp after show() ────────
    /**
     * Creates the WebSocketClient and connects to the server.
     *
     * Call this from EditorApp like:
     *   window.connectToServer("ws://localhost:8080/ws", "doc-001", "Alice", 1);
     *
     * @param serverUrl  e.g. "ws://localhost:8080/ws"
     * @param docId      the document room ID
     * @param userId     this user's display name
     * @param colorSlot  0–3 for cursor color
     */
    public void connectToServer(String serverUrl,
                                String docId,
                                String userId,
                                int colorSlot) {

        wsClient = new WebSocketClient(editorPane, presencePanel, toolbar);
        wsClient.connect(serverUrl, docId, userId, colorSlot);
    }

    /** Call this to disconnect cleanly (e.g. on window close). */
    public void disconnect() {
        if (wsClient != null) wsClient.disconnect();
    }

    // ── Private: layout assembly ──────────────────────────────────────────

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

    // ── Getters (used by WebSocket client and other components) ──────────

    public EditorPane        getEditorPane()    { return editorPane;    }
    public EditorToolbar     getToolbar()       { return toolbar;       }
    public UserPresencePanel getPresencePanel() { return presencePanel; }
    public WebSocketClient   getWsClient()      { return wsClient;      }
}