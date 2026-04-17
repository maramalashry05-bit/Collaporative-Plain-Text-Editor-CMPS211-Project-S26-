package com.texteditor.apt.Service;

import com.texteditor.apt.model.Document;
import com.texteditor.apt.repository.DocumentRepository;
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
}