package com.texteditor.apt.Controller;

import com.texteditor.apt.model.Document;
import com.texteditor.apt.Service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // 1. DTO (Data Transfer Objects) to format the JSON cleanly
    public record CreateDocResponse(String editorCode, String viewerCode) {}
    public record JoinRequest(String code) {}
    public record JoinResponse(Long documentId, String role, String content) {}

    // 2. POST /api/documents (Creates a new document)
    @PostMapping
    public ResponseEntity<CreateDocResponse> createDocument() {
        Document newDoc = documentService.createNewDocument();
        return ResponseEntity.ok(new CreateDocResponse(
                newDoc.getEditorCode(), 
                newDoc.getViewerCode()
        ));
    }

    // 3. POST /api/documents/join (Let's a user into the room)
    @PostMapping("/join")
    public ResponseEntity<?> joinDocument(@RequestBody JoinRequest request) {
        Optional<Document> docOptional = documentService.findDocumentByCode(request.code());

        if (docOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Invalid access code.");
        }

        Document doc = docOptional.get();
        
        // Figure out if they are an Editor or a Viewer based on the code they used
        String role = doc.getEditorCode().equals(request.code()) ? "EDITOR" : "VIEWER";

        // Return the initial state to the UI!
        return ResponseEntity.ok(new JoinResponse(
                doc.getId(),
                role,
                doc.getContent()
        ));
    }
}