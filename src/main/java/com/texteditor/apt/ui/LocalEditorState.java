package com.texteditor.apt.ui;

import com.texteditor.apt.CRDT.CRDT_ID_Generator;
import com.texteditor.apt.CRDT.Char_ID;
import com.texteditor.apt.CRDT.Char_Node;
import com.texteditor.apt.CRDT.Character_Tree;

public class LocalEditorState {

    private final Character_Tree characterTree;
    private final CRDT_ID_Generator idGenerator;
    private final String siteId;

    public LocalEditorState(String siteId) {
        this.siteId = siteId;
        this.characterTree = new Character_Tree();
        this.idGenerator = new CRDT_ID_Generator();
    }

    public String localInsert(int caretPosition, char character) {
        Char_Node[] visible = characterTree.getVisibleNodes();

        Char_ID prevId = (caretPosition > 0 && caretPosition - 1 < visible.length)
                ? visible[caretPosition - 1].getId() : null;

        Char_ID nextId = (caretPosition < visible.length)
                ? visible[caretPosition].getId() : null;

        Char_ID newId = idGenerator.generateIdBetween(prevId, nextId, siteId);

        characterTree.insert(new Char_Node(newId, character));

        return characterTree.getVisibleText();
    }

    public String localDelete(int caretPosition) {
        Char_Node[] visible = characterTree.getVisibleNodes();
        int targetIndex = caretPosition - 1;

        if (targetIndex < 0 || targetIndex >= visible.length) {
            return characterTree.getVisibleText();
        }

        characterTree.delete(visible[targetIndex].getId());
        return characterTree.getVisibleText();
    }

    public String applyRemoteInsert(Char_Node remoteNode) {
        characterTree.insert(remoteNode);
        return characterTree.getVisibleText();
    }

    public String applyRemoteDelete(Char_ID targetId) {
        characterTree.delete(targetId);
        return characterTree.getVisibleText();
    }

    public Char_Node getNodeAtIndex(int index) {
        Char_Node[] visible = characterTree.getVisibleNodes();
        if (index < 0 || index >= visible.length) return null;
        return visible[index];
    }

    public String getVisibleText() {
        return characterTree.getVisibleText();
    }

    public String getSiteId() {
        return siteId;
    }

    
}