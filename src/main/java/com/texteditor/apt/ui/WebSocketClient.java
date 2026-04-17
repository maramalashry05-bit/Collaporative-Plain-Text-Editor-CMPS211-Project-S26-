package com.texteditor.apt.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.texteditor.apt.NetworkMessage;
import javafx.application.Platform;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

/**
 * Connects the JavaFX UI to the Spring Boot WebSocket server.
 *
 * Usage:
 *   WebSocketClient client = new WebSocketClient(editorPane, presencePanel);
 *   client.connect("ws://localhost:8080/ws", "docId123", "Alice");
 */
public class WebSocketClient {

    private final EditorPane editorPane;
    private final UserPresencePanel presencePanel;
    private final EditorToolbar toolbar;

    private StompSession stompSession;
    private String docId;
    private String userId;
    private int colorSlot;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketClient(EditorPane editorPane,
                           UserPresencePanel presencePanel,
                           EditorToolbar toolbar) {
        this.editorPane    = editorPane;
        this.presencePanel = presencePanel;
        this.toolbar       = toolbar;
    }

    /**
     * Connect to the server and join a document room.
     *
     * @param serverUrl  e.g. "ws://localhost:8080/ws"
     * @param docId      the document/room ID
     * @param userId     this user's display name
     * @param colorSlot  0-3 for cursor color
     */
    public void connect(String serverUrl, String docId, String userId, int colorSlot) {
        this.docId     = docId;
        this.userId    = userId;
        this.colorSlot = colorSlot;

        WebSocketStompClient stompClient = new WebSocketStompClient(
            new StandardWebSocketClient()
        );
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompClient.connectAsync(serverUrl, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders headers) {
                stompSession = session;

                // Subscribe to the document room
                session.subscribe("/topic/doc/" + docId, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return NetworkMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        NetworkMessage msg = (NetworkMessage) payload;
                        handleIncomingMessage(msg);
                    }
                });

                // Join the room
                NetworkMessage joinMsg = new NetworkMessage(
                    "JOIN", userId, 0, docId, null, 0, '\0'
                );
                session.send("/app/doc/" + docId + "/join", joinMsg);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    toolbar.setStatus(true);
                    presencePanel.addUser(userId, colorSlot);
                });

                System.out.println("[WebSocketClient] Connected and joined room: " + docId);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable ex) {
                System.err.println("[WebSocketClient] Transport error: " + ex.getMessage());
                Platform.runLater(() -> toolbar.setStatus(false));
            }
        });
    }

    /**
     * Send a local INSERT operation to the server.
     */
    public void sendInsert(int position, char character, String blockId) {
        if (stompSession == null || !stompSession.isConnected()) return;

        NetworkMessage msg = new NetworkMessage(
            "INSERT", userId,
            (int) System.currentTimeMillis(),
            blockId, null, position, character
        );

        stompSession.send("/app/doc/" + docId + "/operation", msg);
    }

    /**
     * Send a local DELETE operation to the server.
     */
    public void sendDelete(int position, String blockId) {
        if (stompSession == null || !stompSession.isConnected()) return;

        NetworkMessage msg = new NetworkMessage(
            "DELETE", userId,
            (int) System.currentTimeMillis(),
            blockId, null, position, '\0'
        );

        stompSession.send("/app/doc/" + docId + "/operation", msg);
    }
public void sendCursorUpdate(int position, String blockId) {
    if (stompSession == null || !stompSession.isConnected()) return;

    NetworkMessage msg = new NetworkMessage(
        "CURSOR", userId,
        (int) System.currentTimeMillis(),
        blockId, null, position, '\0'
    );

    stompSession.send("/app/doc/" + docId + "/operation", msg);
}
    /**
     * Disconnect from the server gracefully.
     */
    public void disconnect() {
        if (stompSession != null && stompSession.isConnected()) {
            NetworkMessage leaveMsg = new NetworkMessage(
                "LEAVE", userId, 0, docId, null, 0, '\0'
            );
            stompSession.send("/app/doc/" + docId + "/leave", leaveMsg);
            stompSession.disconnect();
        }
        Platform.runLater(() -> toolbar.setStatus(false));
    }

    // ── Private: handle incoming messages ────────────────────────────────

    private void handleIncomingMessage(NetworkMessage msg) {
        if (msg == null || msg.opType == null) return;

        Platform.runLater(() -> {
            switch (msg.opType) {
                case "INSERT":
                    // Don't apply our own messages back
                    if (!msg.userID.equals(userId)) {
                        editorPane.applyRemoteInsert(msg.position, msg.character);
                    }
                    break;

                case "DELETE":
                    if (!msg.userID.equals(userId)) {
                        editorPane.applyRemoteDelete(msg.position);
                    }
                    break;
                    case "CURSOR":
    if (!msg.userID.equals(userId)) {
        int slot = getNextColorSlot();
        editorPane.updateRemoteCursor(msg.userID, msg.position, slot);
    }
    break;

                case "USER_JOINED":
                    if (!msg.userID.equals(userId)) {
                        int slot = getNextColorSlot();
                        presencePanel.addUser(msg.userID, slot);
                    }
                    break;

                case "USER_LEFT":
                    presencePanel.removeUser(msg.userID);
                    editorPane.removeRemoteCursor(msg.userID);
                    break;

                default:
                    System.out.println("[WebSocketClient] Unknown op: " + msg.opType);
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
