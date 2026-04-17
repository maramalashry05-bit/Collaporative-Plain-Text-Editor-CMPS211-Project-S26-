package com.texteditor.apt.repository;

import com.texteditor.apt.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    // Spring Data JPA magically turns this method name into a SQL query!
    // It searches for a document where the code matches EITHER the editor or viewer code.
    Optional<Document> findByEditorCodeOrViewerCode(String editorCode, String viewerCode);
}
