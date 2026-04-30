package com.texteditor.apt.Document;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // Generates a secure, URL-safe 8-character string
    private String generateSecureCode() {
        byte[] randomBytes = new byte[6];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Creates a new document with empty JSON array "[]" and random codes
    public Document createNewDocument() {
        String editorCode = "edit-" + generateSecureCode();
        String viewerCode = "view-" + generateSecureCode();
        
        Document doc = new Document(editorCode, viewerCode, "[]");
        return documentRepository.save(doc);
    }

    // Finds the document by whatever code the user typed in
    public Optional<Document> findDocumentByCode(String code) {
        return documentRepository.findByEditorCodeOrViewerCode(code, code);
    }

    //find a doc and update it
    public Document updateDocumentContent(String docId, String newContent) {
        try {
        Long id = Long.parseLong(docId);
        Optional<Document> doc = documentRepository.findById(id);
        if (doc.isPresent()) {
            Document d = doc.get();
            d.setContent(newContent);
            return documentRepository.save(d);
        }
        throw new RuntimeException("Document not found");
    } catch (NumberFormatException e) {
        throw new RuntimeException("Invalid document ID:" +docId);
    }
    }


    public Optional<Document> findById(Long id) {
    return documentRepository.findById(id);
    }
}