package com.texteditor.apt.Networking;

import com.texteditor.apt.CRDT.Block_CRDT;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServerState {
    
    // Maps numeric docID -> CollaborativeDocument object for O(1) lookup
    private final ConcurrentHashMap<String, CollaborativeDocument> activeDocs = new ConcurrentHashMap<>();

    /**
     * Called by the Controller when a message arrives.
     */
    public void ApplyOperation(String docID, NetworkMessage msg) {
        CollaborativeDocument liveDoc = activeDocs.get(docID);
        
        // Only apply if the document exists. 
        // In a real-time app, the doc should be loaded during the JOIN phase.
        if (liveDoc != null) {
            liveDoc.handleOperation(msg);
        } else {
            System.err.println("[ServerState] Operation rejected: Document " + docID + " not loaded in memory.");
        }
    }

    /**
     * Compatibility helper: Returns the CRDT for serialization or sync.
     */
    public Block_CRDT FindaDoc(String docid) {
        CollaborativeDocument doc = activeDocs.get(docid);
        return (doc != null) ? doc.getCrdt() : null; 
    }

    /**
     * Logic for loading a document.
     * Use putIfAbsent to ensure we don't overwrite a live document with an old DB snapshot.
     */
    public void addDocumentToMemory(String docId, Block_CRDT loadedCrdt) {
        if (!activeDocs.containsKey(docId)) {
            activeDocs.put(docId, new CollaborativeDocument(docId, loadedCrdt));
            System.out.println("[ServerState] Document " + docId + " successfully moved to RAM.");
        }
    }

    public CollaborativeDocument getLiveDocument(String docId) {
        return activeDocs.get(docId);
    }
    
    public void removeDocument(String docId) {
        activeDocs.remove(docId);
    }
}