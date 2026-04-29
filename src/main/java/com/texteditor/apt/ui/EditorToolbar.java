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
 *  - Back button (returns to launcher/join screen)
 *  - Undo / Redo / Export actions
 *  - Document title (editable)
 *  - Bold / Italic toggle buttons
 *  - Share button placeholder (wired to Person 4's code in Step 3)
 *  - Connection status indicator
 */
public class EditorToolbar extends HBox {

    private final TextField titleField;
    private final Button    boldBtn;
    private final Button    italicBtn;
    private final Button    shareBtn;
    private final Button    undoBtn;
    private final Button    redoBtn;
    private final Button    exportBtn;
    private final Button    backBtn;
    private final Label     statusLabel;

    public EditorToolbar() {
        setSpacing(6);
        setPadding(new Insets(10, 16, 10, 16));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #13131F; -fx-border-color: #2E2E3E; -fx-border-width: 0 0 1 0;");

        // ── Back button ───────────────────────────────────────────────────
        backBtn = makeIconButton("← Back");

        // ── Undo / Redo / Export ──────────────────────────────────────────
        undoBtn   = makeIconButton("↩");
        redoBtn   = makeIconButton("↪");
        exportBtn = makeTextButton("Export");

        Region gapAfterExport = new Region();
        gapAfterExport.setPrefWidth(12);

        // ── Document title ────────────────────────────────────────────────
        titleField = new TextField("Untitled Document");
        titleField.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #EEEEEE; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: transparent; " +
            "-fx-pref-width: 200;"
        );

        // ── Push everything after title to the right ──────────────────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Bold / Italic ─────────────────────────────────────────────────
        boldBtn = makeIconButton("B");
        boldBtn.setStyle(boldBtn.getStyle() + "-fx-font-weight: bold;");
        italicBtn = makeIconButton("I");
        italicBtn.setStyle(italicBtn.getStyle() + "-fx-font-style: italic;");

        Region gapBeforeShare = new Region();
        gapBeforeShare.setPrefWidth(8);

        // ── Share button ──────────────────────────────────────────────────
        // Phase 3 hook: shareBtn.setOnAction(e -> Person4AccessAPI.requestShareCodes(...));
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

        Region gapBeforeStatus = new Region();
        gapBeforeStatus.setPrefWidth(10);

        // ── Connection status ─────────────────────────────────────────────
        statusLabel = new Label("⬤ Offline");
        statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        getChildren().addAll(
            backBtn, undoBtn, redoBtn, exportBtn, gapAfterExport,
            titleField,
            spacer,
            boldBtn, italicBtn, gapBeforeShare,
            shareBtn, gapBeforeStatus,
            statusLabel
        );
    }

    // ── Public API ────────────────────────────────────────────────────────

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

    /** Programmatically sets the document title (e.g. when loading from DB). */
    public void setTitle(String title) {
        if (title != null && !title.isBlank()) titleField.setText(title);
    }

    public String getDocumentTitle() { return titleField.getText(); }

    public Button getBoldBtn()   { return boldBtn;   }
    public Button getItalicBtn() { return italicBtn; }
    public Button getShareBtn()  { return shareBtn;  }
    public Button getUndoBtn()   { return undoBtn;   }
    public Button getRedoBtn()   { return redoBtn;   }
    public Button getExportBtn() { return exportBtn; }
    public Button getBackBtn()   { return backBtn;   }

    // ── Private helpers ───────────────────────────────────────────────────

    /** For icon-sized buttons (fixed 32×28): Back, Undo, Redo, Bold, Italic. */
    private Button makeIconButton(String text) {
        Button btn = new Button(text);
        String style =
            "-fx-background-color: #2A2A3E; " +
            "-fx-text-fill: #CCCCDD; " +
            "-fx-font-size: 13px; " +
            "-fx-min-width: 32; " +
            "-fx-min-height: 28; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;";
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(style.replace("#2A2A3E", "#3A3A5E")));
        btn.setOnMouseExited(e  -> btn.setStyle(style));
        return btn;
    }

    /** For text-label buttons with padding: Export. */
    private Button makeTextButton(String text) {
        Button btn = new Button(text);
        String style =
            "-fx-background-color: #2A2A3E; " +
            "-fx-text-fill: #CCCCDD; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-min-height: 28; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 5 12 5 12; " +
            "-fx-cursor: hand;";
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(style.replace("#2A2A3E", "#3A3A5E")));
        btn.setOnMouseExited(e  -> btn.setStyle(style));
        return btn;
    }
}