package com.texteditor.apt.CRDT;

public class Character_Tree {
    private CRDT_Tree_Node root;

    public Character_Tree() {
        this.root = null;
    }

    // --- 1. INSERT OPERATION ---
    // Receives a new character and places it in the Binary Search Tree
    public void insert(Char_Node newCharNode) {
        root = insertRec(root, newCharNode);
    }

    private CRDT_Tree_Node insertRec(CRDT_Tree_Node current, Char_Node newCharNode) {
        // If we reach an empty spot, insert the new node here
        if (current == null) {
            return new CRDT_Tree_Node(newCharNode);
        }

        // Compare the CharId arrays to decide if it goes left or right
        int comparison = newCharNode.getId().compareTo(current.getCharNode().getId());

        if (comparison < 0) {
            current.setLeft(insertRec(current.getLeft(), newCharNode));
        } else if (comparison > 0) {
            current.setRight(insertRec(current.getRight(), newCharNode));
        } else {
            // If it's exactly 0, the IDs are identical (which shouldn't happen 
            // with unique Site IDs), but we simply return current to ignore duplicates.
        }

        return current;
    }

    // --- 2. RETRIEVE THE DOCUMENT TEXT (IN-ORDER TRAVERSAL) ---
    public String getVisibleText() {
        // StringBuilder is in java.lang, so it requires absolutely NO imports!
        StringBuilder textBuilder = new StringBuilder();
        buildTextRec(root, textBuilder);
        return textBuilder.toString();
    }
    /** OMNIA 
 * Returns all visible (non-deleted) nodes in order.
 * Used by the UI to map cursor positions to CRDT IDs.
 */
public Char_Node[] getVisibleNodes() {
    // Count visible nodes first
    int count = countVisible(root);
    Char_Node[] result = new Char_Node[count];
    int[] index = {0};
    collectVisible(root, result, index);
    return result;
}

private int countVisible(CRDT_Tree_Node node) {
    if (node == null) return 0;
    int count = node.getCharNode().isDeleted() ? 0 : 1;
    return count + countVisible(node.getLeft()) + countVisible(node.getRight());
}

private void collectVisible(CRDT_Tree_Node node, Char_Node[] result, int[] index) {
    if (node == null) return;
    collectVisible(node.getLeft(), result, index);
    if (!node.getCharNode().isDeleted()) {
        result[index[0]++] = node.getCharNode();
    }
    collectVisible(node.getRight(), result, index);
}
// OMNIA 
    private void buildTextRec(CRDT_Tree_Node node, StringBuilder textBuilder) {
        if (node != null) {
            // 1. Visit Left
            buildTextRec(node.getLeft(), textBuilder);
            
            // 2. Visit Node (Only append the character if it is NOT a tombstone!)
            if (!node.getCharNode().isDeleted()) {
                textBuilder.append(node.getCharNode().getValue());
            }
            
            // 3. Visit Right
            buildTextRec(node.getRight(), textBuilder);
        }
    }

    //delete operation
    public void delete(Char_ID targetid) {
        deleteRec(root, targetid);
    }

    private void deleteRec(CRDT_Tree_Node current, Char_ID targetid) {
        // If we reach an empty spot, doc is empty
        if (current == null) {
            return;
        }

        int comparison = targetid.compareTo(current.getCharNode().getId());

        if (comparison < 0) {
            deleteRec(current.getLeft(),targetid);
        } else if (comparison > 0) {
           deleteRec(current.getRight(), targetid);
        } else {
            //found, mark as deleted
            current.getCharNode().markAsDeleted();
        }
    }
}