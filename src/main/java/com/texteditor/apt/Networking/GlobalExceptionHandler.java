package com.texteditor.apt.Networking;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public String handleWebSocketError(Exception ex) {
        System.err.println("[GlobalExceptionHandler] WebSocket error: " + ex.getMessage());
        return "ERROR: " + ex.getMessage();
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/queue/errors")
    public String handleBadMessage(IllegalArgumentException ex) {
        System.err.println("[GlobalExceptionHandler] Bad message format: " + ex.getMessage());
        return "MALFORMED_MESSAGE: " + ex.getMessage();
    }
}