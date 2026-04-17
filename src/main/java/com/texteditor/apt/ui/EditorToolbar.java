package com.texteditor.apt.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Top toolbar of the editor.
 *
 * Contains:
 *  - Document title (editable)
 *  - Bold / Italic toggle buttons (wired up in Step 2)
 *  - Share button placeholder (wired to Person 4's code in Step 3)
 *  - Connection status indicator
 */
public class EditorToolbar extends HBox {

    private final TextField titleField;
    private final Button boldBtn;
    private final Button italicBtn;
    private final Button shareBtn;
    private final Label statusLabel;

    public EditorToolbar() {
        setSpacing(8);
        setPadding(new Insets(10, 16, 10, 16));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #13131F; -fx-border-color: #2E2E3E; -fx-border-width: 0 0 1 0;");

        // ── Document title ───────────────────────────────────────────────
        titleField = new TextField("Untitled Document");
        titleField.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #EEEEEE; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: transparent; " +
            "-fx-pref-width: 200;"
        );

        // ── Spacer ────────────────────────────────────────────────────────
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // ── Formatting buttons ────────────────────────────────────────────
        boldBtn   = makeToolButton("B", "bold");
        italicBtn = makeToolButton("I", "italic");

        // ── Spacer ────────────────────────────────────────────────────────
        Region spacer2 = new Region();
        spacer2.setPrefWidth(12);

        // ── Share button ──────────────────────────────────────────────────
        shareBtn = new Button("Share");
        shareBtn.setStyle(
            "-fx-background-color: #4A90E2; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 6 14 6 14; " +
            "-fx-cursor: hand;"
        );
        // Phase 3 hook: shareBtn.setOnAction(e -> Person4AccessAPI.requestShareCodes(...));

        // ── Status label ──────────────────────────────────────────────────
        statusLabel = new Label("⬤ Offline");
        statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        getChildren().addAll(titleField, spacer1, boldBtn, italicBtn, spacer2, shareBtn, statusLabel);
    }

    /** Updates the connection status badge shown in the toolbar. */
    public void setStatus(boolean connected) {
        if (connected) {
            statusLabel.setText("⬤ Connected");
            statusLabel.setStyle("-fx-text-fill: #4AE27A; -fx-font-size: 11px;");
        } else {
            statusLabel.setText("⬤ Offline");
            statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        }
    }

    public String getDocumentTitle() {
        return titleField.getText();
    }

    public Button getBoldBtn()   { return boldBtn;   }
    public Button getItalicBtn() { return italicBtn; }
    public Button getShareBtn()  { return shareBtn;  }

    // ── Private helpers ───────────────────────────────────────────────────

    private Button makeToolButton(String text, String type) {
    Button btn = new Button(text);
    final String baseStyle =
        "-fx-background-color: #2A2A3E; " +
        "-fx-text-fill: #CCCCDD; " +
        "-fx-font-size: 13px; " +
        "-fx-min-width: 32; " +
        "-fx-min-height: 28; " +
        "-fx-background-radius: 5; " +
        "-fx-cursor: hand;";

    final String hoverStyle = baseStyle.replace("#2A2A3E", "#3A3A5E");

    btn.setStyle(baseStyle);
    btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
    btn.setOnMouseExited(e  -> btn.setStyle(baseStyle));

    return btn;
} 
}
