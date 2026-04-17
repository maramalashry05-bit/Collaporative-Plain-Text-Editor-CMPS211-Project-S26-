package com.texteditor.apt.ui;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
/**
 * Right-side panel showing who is currently editing the document.
 * Supports up to 4 users, each with a distinct color.
 *
 * Phase 3 hook: call addUser() / removeUser() from the WebSocket
 * session-join/leave events when Person 4's access API fires.
 */
public class UserPresencePanel extends VBox {

    // The 4 fixed user colors (same colors used for cursors in the editor)
    public static final Color[] USER_COLORS = {
        Color.web("#4A90E2"),   // Blue   – slot 0
        Color.web("#E24A4A"),   // Red    – slot 1
        Color.web("#4AE27A"),   // Green  – slot 2
        Color.web("#E2C14A")    // Yellow – slot 3
    };

    // username → row node, preserves insertion order
    private final Map<String, HBox> userRows = new LinkedHashMap<>();

    public UserPresencePanel() {
        setSpacing(10);
        setPadding(new Insets(16));
        setPrefWidth(180);
        setStyle("-fx-background-color: #1E1E2E; -fx-border-color: #2E2E3E; -fx-border-width: 0 0 0 1;");

        Label header = new Label("Active Users");
        header.setStyle("-fx-text-fill: #888899; -fx-font-size: 11px; -fx-font-weight: bold;");
        getChildren().add(header);
    }

    /**
     * Add a user to the presence panel.
     *
     * @param username display name
     * @param colorSlot 0–3 (maps to USER_COLORS)
     */
    public void addUser(String username, int colorSlot) {
        if (userRows.containsKey(username)) return;            // already shown
        if (colorSlot < 0 || colorSlot >= USER_COLORS.length) return; // invalid slot

        Color color = USER_COLORS[colorSlot];

        // Colored dot
        Circle dot = new Circle(6, color);

        // Username label
        Label nameLabel = new Label(username);
        nameLabel.setStyle("-fx-text-fill: #CCCCDD; -fx-font-size: 13px;");

        HBox row = new HBox(8, dot, nameLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        userRows.put(username, row);
        getChildren().add(row);
    }

    /**
     * Remove a user from the presence panel (they disconnected).
     */
    public void removeUser(String username) {
        HBox row = userRows.remove(username);
        if (row != null) {
            getChildren().remove(row);
        }
    }

    /**
     * Returns the Color assigned to a slot — used by the cursor overlay
     * to paint the remote cursor in the matching color.
     */
    public static Color getColorForSlot(int slot) {
        if (slot < 0 || slot >= USER_COLORS.length) return Color.WHITE;
        return USER_COLORS[slot];
    }
}
