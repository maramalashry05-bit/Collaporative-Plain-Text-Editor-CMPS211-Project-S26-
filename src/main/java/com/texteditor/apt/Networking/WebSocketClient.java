package com.texteditor.apt.Networking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texteditor.apt.ui.EditorPane;
import com.texteditor.apt.ui.EditorToolbar;
import com.texteditor.apt.ui.UserPresencePanel;

import javafx.application.Platform;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

/**
 * Connects the JavaFX UI to the Spring Boot WebSocket server.
 */
public class WebSocketClient {

    private final EditorPane editorPane;
    private final UserPresencePanel presencePanel;
    private final EditorToolbar toolbar;

    private StompSession stompSession;
    private String docId;
    private String userId;
    private int colorSlot;

    public WebSocketClient(EditorPane editorPane,
                           UserPresencePanel presencePanel,
                           EditorToolbar toolbar) {
        this.editorPane    = editorPane;
        this.presencePanel = presencePanel;
        this.toolbar       = toolbar;
    }

    public void connect(String serverUrl, String docId, String userId, int colorSlot) {
        this.docId     = docId;
        this.userId    = userId;
        this.colorSlot = colorSlot;

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompClient.connectAsync(serverUrl, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders headers) {
                stompSession = session;

                // 1. Subscribe to Broadcasts (Other people's typing)
                session.subscribe("/topic/doc/" + docId, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) { return NetworkMessage.class; }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        handleIncomingMessage((NetworkMessage) payload);
                    }
                });

                // 2. 🚨 THE FIX: Subscribe to Private Initial Sync
                // This topic receives the full current text ONLY for this specific user
                session.subscribe("/topic/doc/" + docId + "/sync/" + userId, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) { return String.class; }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        String fullContent = (String) payload;
                        System.out.println("[WebSocketClient] Received initial sync.");
                        Platform.runLater(() -> {
                            // This must update the UI AND the local CRDT structure
                            editorPane.initializeContent(fullContent);
                        });
                    }
                });

                // 3. Send JOIN message to trigger the server-side loading
                NetworkMessage joinMsg = new NetworkMessage("JOIN", userId, 0, docId, null, 0, '\0');
                session.send("/app/doc/" + docId + "/join", joinMsg);

                Platform.runLater(() -> {
                    toolbar.setStatus(true);
                    presencePanel.addUser(userId, colorSlot);
                });
            }

            @Override
            public void handleTransportError(StompSession session, Throwable ex) {
                System.err.println("[WebSocketClient] Transport error: " + ex.getMessage());
                Platform.runLater(() -> toolbar.setStatus(false));
            }
        });
    }

    // ── Sending Operations ───────────────────────────────────────────────

    public void sendInsert(int position, char character, String blockId) {
        sendMessage("INSERT", position, character, blockId);
    }

    public void sendDelete(int position, String blockId) {
        sendMessage("DELETE", position, '\0', blockId);
    }

    public void sendCursorUpdate(int position, String blockId) {
        sendMessage("CURSOR", position, '\0', blockId);
    }

    private void sendMessage(String opType, int pos, char c, String bId) {
        if (stompSession != null && stompSession.isConnected()) {
            NetworkMessage msg = new NetworkMessage(opType, userId, (int) System.currentTimeMillis(), bId, null, pos, c);
            stompSession.send("/app/doc/" + docId + "/operation", msg);
        }
    }

    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.send("/app/doc/" + docId + "/leave", new NetworkMessage("LEAVE", userId, 0, docId, null, 0, '\0'));
            stompSession.disconnect();
        }
        Platform.runLater(() -> toolbar.setStatus(false));
    }

    // ── Handling Incoming ────────────────────────────────────────────────

    private void handleIncomingMessage(NetworkMessage msg) {
        if (msg == null || msg.opType == null) return;

        Platform.runLater(() -> {
            boolean isRemote = !msg.userID.equals(userId);

            switch (msg.opType) {
                case "INSERT":
                    if (isRemote) editorPane.applyRemoteInsert(msg.position, msg.character);
                    break;
                case "DELETE":
                    if (isRemote) editorPane.applyRemoteDelete(msg.position);
                    break;
                case "CURSOR":
                    if (isRemote) editorPane.updateRemoteCursor(msg.userID, msg.position, getNextColorSlot());
                    break;
                case "USER_JOINED":
                    if (isRemote) presencePanel.addUser(msg.userID, getNextColorSlot());
                    break;
                case "USER_LEFT":
                    presencePanel.removeUser(msg.userID);
                    editorPane.removeRemoteCursor(msg.userID);
                    break;
            }
        });
    }

    private int nextSlot = 1;
    private int getNextColorSlot() {
        int slot = nextSlot;
        nextSlot = (nextSlot + 1) % 4;
        return slot;
    }
}