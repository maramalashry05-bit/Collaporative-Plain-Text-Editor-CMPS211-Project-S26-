/* 
package com.texteditor.apt.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Collaborative Text Editor (Phase 2 - UI Shell).
 * Run this class to launch the editor window.
 */
/* 
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
*/
package com.texteditor.apt.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class EditorApp extends Application {

    private final LocalDatabase localDatabase = new LocalDatabase();

    @Override
    public void start(Stage primaryStage) {
        // Open the local database when the app starts
        localDatabase.connect();

        // Show the join screen instead of hardcoded values
        JoinScreen joinScreen = new JoinScreen(primaryStage, localDatabase);
        joinScreen.show();
    }

    @Override
    public void stop() {
        // Close the local database when the app exits
        localDatabase.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
