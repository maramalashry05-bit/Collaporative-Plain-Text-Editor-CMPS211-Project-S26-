package com.texteditor.apt.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.LinkedHashMap;
import java.util.Map;

public class EditorPane extends StackPane {

    private final TextArea textArea;
    private final Canvas cursorCanvas;
    private final LocalEditorState crdtState;
    private boolean updatingFromCRDT = false;
    private final Map<String, int[]> remoteCursors = new LinkedHashMap<>();
    private WebSocketClient wsClient = null;
    private String currentBlockId = "default-block";

    private static final double CHAR_W = 8.4;
    private static final double CHAR_H = 19.0;
    private static final double TEXT_PADDING_LEFT = 8;
    private static final double TEXT_PADDING_TOP = 6;

    public EditorPane() {
        crdtState = new LocalEditorState("LocalUser");
        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setStyle(
            "-fx-control-inner-background: #1A1A2E; " +
            "-fx-text-fill: #E0E0F0; " +
            "-fx-font-family: 'Consolas', monospace; " +
            "-fx-font-size: 14px; " +
            "-fx-highlight-fill: #4A90E2; " +
            "-fx-highlight-text-fill: white; " +
            "-fx-border-color: transparent; " +
            "-fx-background-color: #1A1A2E;"
        );
        //setupKeyHandlers();

textArea.textProperty().addListener((obs, oldText, newText) -> {
        if (!updatingFromCRDT) {
            // Find what changed
            if (newText.length() > oldText.length()) {
                int caret = textArea.getCaretPosition();
                char lastChar = newText.charAt(Math.max(0, caret - 1));
                
                // Update CRDT
                crdtState.localInsert(Math.max(0, caret - 1), lastChar);
                
                // Sync with server
                if (wsClient != null) {
                    wsClient.sendInsert(caret - 1, lastChar, currentBlockId);
                }
            }
        }
    });
        
        // Step 5: send cursor position to server whenever caret moves
        // textArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
        //     if (updatingFromCRDT) return;
        //     if (wsClient != null) {
        //         wsClient.sendCursorUpdate(newVal.intValue(), currentBlockId);
        //     }
        // });

        cursorCanvas = new Canvas();
        cursorCanvas.setMouseTransparent(true);
        cursorCanvas.widthProperty().bind(widthProperty());
        cursorCanvas.heightProperty().bind(heightProperty());
        cursorCanvas.widthProperty().addListener(o -> paintCursors());
        cursorCanvas.heightProperty().addListener(o -> paintCursors());
        getChildren().addAll(textArea, cursorCanvas);
        setStyle("-fx-background-color: #1A1A2E;");
    }

    public TextArea getTextArea() { return textArea; }
    public void setTextSilently(String text) {
    updatingFromCRDT = true;
    textArea.setText(text);
    updatingFromCRDT = false;
}
    public LocalEditorState getCrdtState() { return crdtState; }

    public void setWebSocketClient(WebSocketClient client, String blockId) {
        this.wsClient = client;
        this.currentBlockId = blockId;
    }

    public void updateRemoteCursor(String username, int caretPos, int colorSlot) {
        remoteCursors.put(username, new int[]{caretPos, colorSlot});
        paintCursors();
    }

    public void removeRemoteCursor(String username) {
        remoteCursors.remove(username);
        paintCursors();
    }

    public void applyRemoteInsert(int position, char character) {
        String newText = crdtState.localInsert(position, character);
        int savedCaret = textArea.getCaretPosition();
        int newCaret = (position <= savedCaret) ? savedCaret + 1 : savedCaret;
        refreshTextArea(newText, newCaret);
    }

    public void applyRemoteDelete(int position) {
        com.texteditor.apt.CRDT.Char_Node node = crdtState.getNodeAtIndex(position);
        if (node == null) return;
        String newText = crdtState.applyRemoteDelete(node.getId());
        int savedCaret = textArea.getCaretPosition();
        refreshTextArea(newText, position < savedCaret ? savedCaret - 1 : savedCaret);
    }

    private void setupKeyHandlers() {
        textArea.setOnKeyPressed(event -> {
            if (updatingFromCRDT) return;
            if (event.getCode() == KeyCode.BACK_SPACE) {
                event.consume();
                int caret = textArea.getCaretPosition();
                if (caret == 0) return;
                String newText = crdtState.localDelete(caret);
                refreshTextArea(newText, caret - 1);
                if (wsClient != null) {
                    wsClient.sendDelete(caret - 1, currentBlockId);
                }
            }
            if (event.getCode() == KeyCode.DELETE) {
                event.consume();
                int caret = textArea.getCaretPosition();
                if (caret >= crdtState.getVisibleText().length()) return;
                String newText = crdtState.localDelete(caret + 1);
                refreshTextArea(newText, caret);
                if (wsClient != null) {
                    wsClient.sendDelete(caret, currentBlockId);
                }
            }
        });

       textArea.setOnKeyTyped(event -> {
    if (updatingFromCRDT) return;
    
    String typed = event.getCharacter();
    if (typed == null || typed.isEmpty()) return;
    
    char c = typed.charAt(0);
    // Standard validation
    if (c < 32 && c != '\n' && c != '\r' && c != '\t') return;
    if (c == 127) return;

    // --- KEY CHANGE HERE ---
    event.consume(); // This stops JavaFX from typing the character itself
    // -----------------------

    int caret = textArea.getCaretPosition();
    
    // Manually update your state
    String newText = crdtState.localInsert(caret, c);
    
    // Manually update the UI and move the cursor
    refreshTextArea(newText, caret + 1);
    
    // Send to server
    if (wsClient != null) {
        wsClient.sendInsert(caret, c, currentBlockId);
    }
});
    }

 private void refreshTextArea(String newText, int newCaret) {
    updatingFromCRDT = true;

    // Use replaceText instead of setText for a smoother update
    // This tells JavaFX: "Just change the content, don't destroy the box"
    textArea.replaceText(0, textArea.getLength(), newText);

    // Force the cursor movement into a RunLater to ensure it happens AFTER the render
    javafx.application.Platform.runLater(() -> {
        int clampedCaret = Math.max(0, Math.min(newCaret, newText.length()));
        textArea.positionCaret(clampedCaret);
        
        // This is the magic line - make sure the box is ready for the next letter
        textArea.requestFocus(); 
        
        updatingFromCRDT = false;
        paintCursors();
    });

}

    private void paintCursors() {
        GraphicsContext gc = cursorCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, cursorCanvas.getWidth(), cursorCanvas.getHeight());
        String fullText = textArea.getText();
        if (fullText == null) fullText = "";
        for (Map.Entry<String, int[]> entry : remoteCursors.entrySet()) {
            String username = entry.getKey();
            int caretPos = Math.min(entry.getValue()[0], fullText.length());
            int colorSlot = entry.getValue()[1];
            int row = 0, col = 0;
            for (int i = 0; i < caretPos; i++) {
                if (fullText.charAt(i) == '\n') { row++; col = 0; }
                else col++;
            }
            double x = TEXT_PADDING_LEFT + col * CHAR_W;
            double y = TEXT_PADDING_TOP + row * CHAR_H;
            Color color = UserPresencePanel.getColorForSlot(colorSlot);
            gc.setStroke(color);
            gc.setLineWidth(2);
            gc.strokeLine(x, y, x, y + CHAR_H);
            gc.setFill(color);
            gc.setFont(Font.font("Monospace", 10));
            gc.fillText(username, x + 2, y - 2);
        }
    }
}
