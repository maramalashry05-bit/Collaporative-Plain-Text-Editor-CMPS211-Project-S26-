package com.texteditor.apt.Networking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import com.texteditor.apt.CRDT.Block_CRDT;
import com.texteditor.apt.CRDT.CRDTSerializer;
import com.texteditor.apt.Document.DocumentService;

@Controller
public class EditorController {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private ServerState serverState;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private RoomManager roomManager;

    @MessageMapping("/doc/{docId}/join")
    public void joinRoom(@DestinationVariable String docId, @Payload NetworkMessage msg) {
        if (msg == null || msg.userID == null || docId == null) {
            System.err.println("[EditorController] JOIN rejected: missing fields.");
            return;
        }

        try {
            boolean joined = roomManager.joinRoom(docId, msg.userID);
            if (!joined) {
                NetworkMessage error = new NetworkMessage("ERROR_ROOM_FULL", "SERVER", 0, docId, null, 0, '\0');
                messagingTemplate.convertAndSend("/topic/doc/" + docId + "/errors/" + msg.userID, error);
                return;
            }

            if (serverState.getLiveDocument(docId) == null) {
                try {
                    documentService.findById(Long.parseLong(docId)).ifPresent(doc -> {
                        try {
                            Block_CRDT loaded = CRDTSerializer.deserialize(doc.getContent());
                            serverState.addDocumentToMemory(docId, loaded);
                        } catch (Exception e) {
                            System.err.println("[EditorController] Deserialization failed: " + e.getMessage());
                            serverState.addDocumentToMemory(docId, new Block_CRDT());
                        }
                    });
                } catch (NumberFormatException e) {
                    System.err.println("[EditorController] Invalid docId format: " + docId);
                    return;
                }
            }

            CollaborativeDocument liveDoc = serverState.getLiveDocument(docId);
            if (liveDoc != null) {
                String currentContent = liveDoc.getFullText("main-block");
                messagingTemplate.convertAndSend("/topic/doc/" + docId + "/sync/" + msg.userID, currentContent);
            }

            NetworkMessage notice = new NetworkMessage("USER_JOINED", msg.userID, msg.timestamp, docId, null, 0, '\0');
            messagingTemplate.convertAndSend("/topic/doc/" + docId, notice);

            for (String existingUser : roomManager.getUsers(docId)) {
                if (!existingUser.equals(msg.userID)) {
                    NetworkMessage existing = new NetworkMessage("USER_JOINED", existingUser, 0, docId, null, 0, '\0');
                    messagingTemplate.convertAndSend("/topic/doc/" + docId + "/presence/" + msg.userID, existing);
                }
            }
            
        } catch (Exception e) {
            System.err.println("[EditorController] Unexpected error in joinRoom: " + e.getMessage());
        }
    }

    @MessageMapping("/doc/{docId}/operation")
    @Async
    public void handleOperation(@DestinationVariable String docId, @Payload NetworkMessage msg) {
        if (msg == null || msg.opType == null || msg.userID == null) {
            System.err.println("[EditorController] Operation rejected: missing fields.");
            return;
        }

        try {
            if (!roomManager.isInRoom(docId, msg.userID)) {
                System.err.println("[EditorController] Operation rejected: user not in room.");
                return;
            }

            serverState.ApplyOperation(docId, msg);
            messagingTemplate.convertAndSend("/topic/doc/" + docId, msg);
            //saveToDatabase(docId);

        } catch (Exception e) {
            System.err.println("[EditorController] Error handling operation: " + e.getMessage());
        }
    }

    @Async
    private void saveToDatabase(String docId) {
        try {
            CollaborativeDocument liveDoc = serverState.getLiveDocument(docId);
            if (liveDoc != null) {
                String json = CRDTSerializer.serialize(liveDoc.getCrdt());
                documentService.updateDocumentContent(docId, json);
            }
        } catch (Exception e) {
            System.err.println("[EditorController] Save failed: " + e.getMessage());
        }
    }

    @MessageMapping("/doc/{docId}/leave")
    public void leaveRoom(@DestinationVariable String docId, @Payload NetworkMessage msg) {
        if (msg == null || msg.userID == null) return;

        try {
            roomManager.leaveRoom(docId, msg.userID);
            NetworkMessage notice = new NetworkMessage("USER_LEFT", msg.userID, msg.timestamp, docId, null, 0, '\0');
            messagingTemplate.convertAndSend("/topic/doc/" + docId, notice);
        } catch (Exception e) {
            System.err.println("[EditorController] Error in leaveRoom: " + e.getMessage());
        }
    }
}