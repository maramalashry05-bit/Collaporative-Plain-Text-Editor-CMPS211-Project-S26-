package com.texteditor.apt.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Collaborative Text Editor (Phase 2 - UI Shell).
 * Run this class to launch the editor window.
 */
public class EditorApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        EditorWindow window = new EditorWindow(primaryStage);
        window.show();
        window.connectToServer("ws://localhost:8080/ws", "doc-001", "Alice", 1); ///when person two connect 
    }

    public static void main(String[] args) {
        launch(args);
    }
}
