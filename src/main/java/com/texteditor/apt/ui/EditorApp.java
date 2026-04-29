package com.texteditor.apt.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Collaborative Text Editor.
 * Launches the initial screen and manages the local database lifecycle.
 */
public class EditorApp extends Application {

    private final LocalDatabase localDatabase = new LocalDatabase();

    @Override
    public void start(Stage primaryStage) {
        // Open the local database when the app starts
        localDatabase.connect();

        // Show the launcher screen first; it should navigate to JoinScreen
        LauncherScreen launcher = new LauncherScreen(primaryStage, localDatabase);
        launcher.show();
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