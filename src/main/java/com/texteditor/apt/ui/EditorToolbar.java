package com.texteditor.apt.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;


public class EditorToolbar extends HBox {

    private final TextField  titleField;
    private final Button     boldBtn;
    private final Button     italicBtn;
    private final Button     shareBtn;
    private final Button     undoBtn;
    private final Button     redoBtn;
    private final Button     backBtn;
    private final Label      statusLabel;

    // ── File menu ─────────────────────────────────────────────────────────
    private final Button      fileMenuBtn;
    private final ContextMenu fileMenu;
    private final MenuItem    importItem;
    private final MenuItem    exportItem;
    private final MenuItem    deleteItem;

    public EditorToolbar() {
        setSpacing(6);
        setPadding(new Insets(10, 16, 10, 16));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #13131F; -fx-border-color: #2E2E3E; -fx-border-width: 0 0 1 0;");

        backBtn = makeIconButton("← Back");

        fileMenuBtn = makeTextButton("File ▾");

        importItem = new MenuItem("Import");
        exportItem = new MenuItem("Export");
        deleteItem = new MenuItem("Delete");
        deleteItem.setStyle("-fx-text-fill: #E24A4A; -fx-font-size: 13px;"); // red color

        fileMenu = new ContextMenu(importItem, exportItem, deleteItem);
        fileMenu.setStyle(
            "-fx-background-color: #1E1E2E; " +
            "-fx-border-color: #3E3E5E; " +
            "-fx-border-width: 1;"
        );

        fileMenuBtn.setOnAction(e ->
            fileMenu.show(fileMenuBtn,
                fileMenuBtn.localToScreen(0, fileMenuBtn.getHeight()).getX(),
                fileMenuBtn.localToScreen(0, fileMenuBtn.getHeight()).getY())
        );

        undoBtn = makeIconButton("↩");
        redoBtn = makeIconButton("↪");

        Region gapAfterRedo = new Region();
        gapAfterRedo.setPrefWidth(12);

        titleField = new TextField("Untitled Document");
        titleField.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #EEEEEE; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: transparent; " +
            "-fx-pref-width: 200;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boldBtn = makeIconButton("B");
        boldBtn.setStyle(boldBtn.getStyle() + "-fx-font-weight: bold;");
        italicBtn = makeIconButton("I");
        italicBtn.setStyle(italicBtn.getStyle() + "-fx-font-style: italic;");

        Region gapBeforeShare = new Region();
        gapBeforeShare.setPrefWidth(8);

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

        statusLabel = new Label("⬤ Offline");
        statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        getChildren().addAll(
            backBtn, fileMenuBtn, undoBtn, redoBtn, gapAfterRedo,
            titleField,
            spacer,
            boldBtn, italicBtn, gapBeforeShare,
            shareBtn, gapBeforeStatus,
            statusLabel
        );
    }

    public void setStatus(boolean connected) {
        if (connected) {
            statusLabel.setText("⬤ Connected");
            statusLabel.setStyle("-fx-text-fill: #4AE27A; -fx-font-size: 11px;");
        } else {
            statusLabel.setText("⬤ Offline");
            statusLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        }
    }

    public void setTitle(String title) {
        if (title != null && !title.isBlank()) titleField.setText(title);
    }

    public String    getDocumentTitle() { return titleField.getText(); }
    public TextField getTitleField()    { return titleField; }

    public Button   getBoldBtn()    { return boldBtn;    }
    public Button   getItalicBtn()  { return italicBtn;  }
    public Button   getShareBtn()   { return shareBtn;   }
    public Button   getUndoBtn()    { return undoBtn;    }
    public Button   getRedoBtn()    { return redoBtn;    }
    public Button   getBackBtn()    { return backBtn;    }
    public Button   getFileMenuBtn(){ return fileMenuBtn; }

    public MenuItem getImportItem() { return importItem; }
    public MenuItem getExportItem() { return exportItem; }
    public MenuItem getDeleteItem() { return deleteItem; }

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
        btn.setOnMouseExited (e -> btn.setStyle(style));
        return btn;
    }

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
        btn.setOnMouseExited (e -> btn.setStyle(style));
        return btn;
    }
}
