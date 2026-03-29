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
}