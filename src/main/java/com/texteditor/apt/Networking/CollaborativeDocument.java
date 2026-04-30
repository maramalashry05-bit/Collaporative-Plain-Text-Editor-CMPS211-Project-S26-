package com.texteditor.apt.Networking;

import com.texteditor.apt.CRDT.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Represents a single live document on the server.
 * Wraps the CRDT logic to keep the ServerState clean.
 */
public class CollaborativeDocument {
    private final String docID;
    private final Block_CRDT crdtStructure;
    
    // Tracks users currently looking at THIS specific document
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public CollaborativeDocument(String docID) {
        this.docID = docID;
        this.crdtStructure = new Block_CRDT();
    }
// Overloaded constructor for loading from DB
public CollaborativeDocument(String docID, Block_CRDT existingCrdt) {
    this.docID = docID;
    this.crdtStructure = (existingCrdt != null) ? existingCrdt : new Block_CRDT();
}

// Getter for the CRDT
public Block_CRDT getCrdt() {
    return crdtStructure;
}
    // --- CRDT Operations ---

    public synchronized void handleOperation(NetworkMessage msg) {
        switch (msg.opType) {
            case "INSERT":
            case "CHAR_INSERT":
                applyInsert(msg);
                break;
            case "DELETE":
            case "CHAR_DELETE":
                applyDelete(msg);
                break;
            case "BLOCK_INSERT":
                crdtStructure.insertBlock(msg.blockID, msg.parentBlockID);
                break;
        }
    }

    private void applyInsert(NetworkMessage msg) {
        // Create the unique CRDT Identifier for this character
        Identifier[] ids = { new Identifier(msg.position, msg.userID) };
        Char_ID charId = new Char_ID(ids);
        Char_Node node = new Char_Node(charId, msg.character);
        
        crdtStructure.insertCharacter(msg.blockID, node);
    }

    private void applyDelete(NetworkMessage msg) {
        Identifier[] ids = { new Identifier(msg.position, msg.userID) };
        Char_ID charId = new Char_ID(ids);
        Block_Node block = crdtStructure.getSpecificBlock(msg.blockID);
        
        if (block != null) {
            block.getContent().delete(charId);
        }
    }

    // --- State Management ---

    public String getFullText(String blockID) {
        return crdtStructure.getBlockText(blockID);
    }

    public void addUser(String userId) { activeUsers.add(userId); }
    public void removeUser(String userId) { activeUsers.remove(userId); }
    public String getDocID() { return docID; }
}