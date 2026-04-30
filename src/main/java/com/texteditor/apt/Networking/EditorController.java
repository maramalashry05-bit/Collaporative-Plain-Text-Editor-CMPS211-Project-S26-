package com.texteditor.apt.Networking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
        boolean joined = roomManager.joinRoom(docId, msg.userID);
        if (!joined) {
            NetworkMessage error = new NetworkMessage("ERROR_ROOM_FULL", "SERVER", 0, docId, null, 0, '\0');
            messagingTemplate.convertAndSend("/topic/doc/" + docId + "/errors/" + msg.userID, error);
            return;
        }

        // 1. Ensure document is loaded in ServerState
        if (serverState.getLiveDocument(docId) == null) {
            documentService.findById(Long.parseLong(docId)).ifPresent(doc -> {
                try {
                    Block_CRDT loaded = CRDTSerializer.deserialize(doc.getContent());
                    serverState.addDocumentToMemory(docId, loaded);
                } catch (Exception e) {
                    System.err.println("[EditorController] Deserialization failed: " + e.getMessage());
                }
            });
        }

        // 2. 🚨 THE FIX: Send current content ONLY to the new user
        CollaborativeDocument liveDoc = serverState.getLiveDocument(docId);
        if (liveDoc != null) {
            // We use a specific sub-topic for the user's ID to avoid spamming everyone
            String currentContent = liveDoc.getFullText("main-block"); 
            messagingTemplate.convertAndSend("/topic/doc/" + docId + "/sync/" + msg.userID, currentContent);
        }

        // 3. Notify others
        NetworkMessage notice = new NetworkMessage("USER_JOINED", msg.userID, msg.timestamp, docId, null, 0, '\0');
        messagingTemplate.convertAndSend("/topic/doc/" + docId, notice);
    }

    @MessageMapping("/doc/{docId}/operation")
    public void handleOperation(@DestinationVariable String docId, @Payload NetworkMessage msg) {
        if (msg == null || msg.opType == null || !roomManager.isInRoom(docId, msg.userID)) return;

        // Apply to the server-side CRDT "Brain"
        serverState.ApplyOperation(docId, msg);

        // Broadcast the keypress to all other users
        messagingTemplate.convertAndSend("/topic/doc/" + docId, msg);

        // Optional: Persist to DB
        saveToDatabase(docId);
    }

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
        roomManager.leaveRoom(docId, msg.userID);
        NetworkMessage notice = new NetworkMessage("USER_LEFT", msg.userID, msg.timestamp, docId, null, 0, '\0');
        messagingTemplate.convertAndSend("/topic/doc/" + docId, notice);
    }
}