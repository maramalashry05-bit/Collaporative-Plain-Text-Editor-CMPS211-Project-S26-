package com.texteditor.apt;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        System.out.println("[WebSocketEventListener] Session disconnected: " + event.getSessionId());
    }
}
