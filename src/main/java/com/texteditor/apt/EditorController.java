package com.texteditor.apt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class EditorController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomManager roomManager;

    @MessageMapping("/doc/{docId}/join")
    public void joinRoom(@DestinationVariable String docId, @Payload NetworkMessage msg) {
        boolean joined = roomManager.joinRoom(docId, msg.userID);
        if (!joined) {
            NetworkMessage error = new NetworkMessage(
                "ERROR_ROOM_FULL", "SERVER", 0, docId, null, 0, '\0');
            messagingTemplate.convertAndSend("/topic/doc/" + docId + "/errors/" + msg.userID, error);
            System.out.println("[EditorController] Room full, rejected: " + msg.userID);
            return;
        }
        NetworkMessage notice = new NetworkMessage(
            "USER_JOINED", msg.userID, msg.timestamp, docId, null, 0, '\0');
        messagingTemplate.convertAndSend("/topic/doc/" + docId, notice);
        System.out.println("[EditorController] " + msg.userID + " joined room: " + docId);
    }

    //main method: receives a CRDT operation and broadcasts to everyone
    @MessageMapping("/doc/{docId}/operation")
    public void handleOperation(@DestinationVariable String docId, @Payload NetworkMessage msg) {
        if (msg == null || msg.opType == null || msg.userID == null || msg.blockID == null) {
            System.err.println("[EditorController] Rejected malformed message in doc: " + docId);
            return;
        }
        if (!roomManager.isInRoom(docId, msg.userID)) {
            System.err.println("[EditorController] " + msg.userID + " not in room " + docId + " — dropped");
            return;
        }
        System.out.println("[EditorController] Broadcasting: op=" + msg.opType
            + " user=" + msg.userID + " doc=" + docId + " block=" + msg.blockID
            + (msg.character != '\0' ? " char=" + msg.character : ""));

        //broadcast to clients subscribed to this document room
        messagingTemplate.convertAndSend("/topic/doc/" + docId, msg);
    }

    @MessageMapping("/doc/{docId}/leave")
    public void leaveRoom(@DestinationVariable String docId,
                          @Payload NetworkMessage msg) {
        roomManager.leaveRoom(docId, msg.userID);
        NetworkMessage notice = new NetworkMessage(
            "USER_LEFT", msg.userID, msg.timestamp, docId, null, 0, '\0');
        messagingTemplate.convertAndSend("/topic/doc/" + docId, notice);
        System.out.println("[EditorController] " + msg.userID + " left room: " + docId);
    }
}
