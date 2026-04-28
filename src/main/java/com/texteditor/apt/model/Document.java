package com.texteditor.apt.model;

import jakarta.persistence.*;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String editorCode;
    private String viewerCode;

    // @Lob and TEXT ensure the database can hold a massive JSON string
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    public Document() {
        // Default constructor needed by JPA
    }

    public Document(String editorCode, String viewerCode, String content) { 
        this.editorCode = editorCode;
        this.viewerCode = viewerCode;
        this.content = content;
    }

    // Getters
    public Long getId() { return id; }
    public String getEditorCode() { return editorCode; }
    public String getViewerCode() { return viewerCode; }
    public String getContent() { return content; }

    // Setters
    public void setContent(String content) { this.content = content; }
}
