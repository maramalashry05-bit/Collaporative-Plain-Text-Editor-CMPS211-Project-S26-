package com.texteditor.apt.Networking;

import java.lang.reflect.Type;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.texteditor.apt.ui.EditorPane;
import com.texteditor.apt.ui.EditorToolbar;
import com.texteditor.apt.ui.UserPresencePanel;

import javafx.application.Platform;

public class WebSocketClient {

    private final EditorPane editorPane;
    private final UserPresencePanel presencePanel;
    private final EditorToolbar toolbar;

    private StompSession stompSession;
    private String docId;
    private String userId;
    private int colorSlot;

    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private int reconnectAttempts = 0;
    private String lastServerUrl;

    public WebSocketClient(EditorPane editorPane, UserPresencePanel presencePanel, EditorToolbar toolbar) {
        this.editorPane    = editorPane;
        this.presencePanel = presencePanel;
        this.toolbar       = toolbar;
    }

    public void connect(String serverUrl, String docId, String userId, int colorSlot) {
        this.docId      = docId;
        this.userId     = userId;
        this.colorSlot  = colorSlot;
        this.lastServerUrl = serverUrl;

        try {
            WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            stompClient.connectAsync(serverUrl, new StompSessionHandlerAdapter() {

                @Override
                public void afterConnected(StompSession session, StompHeaders headers) {
                    stompSession = session;
                    reconnectAttempts = 0;

                    try {
                        session.subscribe("/topic/doc/" + docId, new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) { return NetworkMessage.class; }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                try {
                                    handleIncomingMessage((NetworkMessage) payload);
                                } catch (Exception e) {
                                    System.err.println("[WebSocketClient] Error handling incoming frame: " + e.getMessage());
                                }
                            }
                        });

                        session.subscribe("/topic/doc/" + docId + "/sync/" + userId, new StompFrameHandler() {
                            @Override
                            public Type getPayloadType(StompHeaders headers) { return String.class; }

                            @Override
                            public void handleFrame(StompHeaders headers, Object payload) {
                                try {
                                    String fullContent = (String) payload;
                                    Platform.runLater(() -> editorPane.initializeContent(fullContent));
                                } catch (Exception e) {
                                    System.err.println("[WebSocketClient] Error applying sync: " + e.getMessage());
                                }
                            }
                        });

                        NetworkMessage joinMsg = new NetworkMessage("JOIN", userId, 0, docId, null, 0, '\0');
                        session.send("/app/doc/" + docId + "/join", joinMsg);

                        Platform.runLater(() -> {
                            toolbar.setStatus(true);
                            presencePanel.addUser(userId, colorSlot);
                        });

                    } catch (Exception e) {
                        System.err.println("[WebSocketClient] Error during session setup: " + e.getMessage());
                        Platform.runLater(() -> toolbar.setStatus(false));
                    }
                }

                @Override
                public void handleTransportError(StompSession session, Throwable ex) {
                    System.err.println("[WebSocketClient] Transport error: " + ex.getMessage());
                    Platform.runLater(() -> toolbar.setStatus(false));

                    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        reconnectAttempts++;
                        System.out.println("[WebSocketClient] Reconnect attempt " + reconnectAttempts + "...");
                        try {
                            Thread.sleep(2000L * reconnectAttempts);
                            connect(lastServerUrl, docId, userId, colorSlot);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        System.err.println("[WebSocketClient] Max reconnect attempts reached. Giving up.");
                    }
                }

                @Override
                public void handleException(StompSession session, StompCommand command,
                                            StompHeaders headers, byte[] payload, Throwable ex) {
                    System.err.println("[WebSocketClient] STOMP exception on command " + command + ": " + ex.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("[WebSocketClient] Failed to initiate connection: " + e.getMessage());
            Platform.runLater(() -> toolbar.setStatus(false));
        }
    }

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
        try {
            if (stompSession != null && stompSession.isConnected()) {
                NetworkMessage msg = new NetworkMessage(opType, userId, (int) System.currentTimeMillis(), bId, null, pos, c);
                stompSession.send("/app/doc/" + docId + "/operation", msg);
            } else {
                System.err.println("[WebSocketClient] Cannot send: session not connected.");
            }
        } catch (Exception e) {
            System.err.println("[WebSocketClient] Failed to send message: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (stompSession != null && stompSession.isConnected()) {
                stompSession.send("/app/doc/" + docId + "/leave",
                    new NetworkMessage("LEAVE", userId, 0, docId, null, 0, '\0'));
                stompSession.disconnect();
            }
        } catch (Exception e) {
            System.err.println("[WebSocketClient] Error during disconnect: " + e.getMessage());
        } finally {
            Platform.runLater(() -> toolbar.setStatus(false));
        }
    }

    private void handleIncomingMessage(NetworkMessage msg) {
        if (msg == null || msg.opType == null) return;

        Platform.runLater(() -> {
            try {
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
                    default:
                        System.err.println("[WebSocketClient] Unknown opType: " + msg.opType);
                }
            } catch (Exception e) {
                System.err.println("[WebSocketClient] Error applying remote message: " + e.getMessage());
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