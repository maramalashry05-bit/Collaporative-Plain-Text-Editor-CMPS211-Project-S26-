package com.texteditor.apt;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    // Catches ungraceful disconnects (browser crash, network drop, etc.)
    // The client should send /leave before closing when possible,
    // but this is the safety net for when they can't.
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        System.out.println("[WebSocketEventListener] Session disconnected: " + event.getSessionId());
    }
}
