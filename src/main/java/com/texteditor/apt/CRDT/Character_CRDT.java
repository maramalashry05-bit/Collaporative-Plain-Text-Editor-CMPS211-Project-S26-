package com.texteditor.apt.CRDT;

public class Character_CRDT {
    private Character_Tree tree;

    public Character_CRDT() {
        this.tree = new Character_Tree();
    }

    public void insert(Char_Node charNode) {
        tree.insert(charNode);
    }

    public void delete(Char_ID targetId) {
        tree.delete(targetId);
    }

    public String getVisibleText() {
        return tree.getVisibleText();
    }
}
