public class CRDT_Tree_Node {
    private Char_Node charNode; // The actual character and its ID
    private CRDT_Tree_Node left;
    private CRDT_Tree_Node right;

    public CRDT_Tree_Node(Char_Node charNode) {
        this.charNode = charNode;
        this.left = null;
        this.right = null;
    }

    // Getters and Setters
    public Char_Node getCharNode() { return charNode; }
    public CRDT_Tree_Node getLeft() { return left; }
    public void setLeft(CRDT_Tree_Node left) { this.left = left; }
    public CRDT_Tree_Node getRight() { return right; }
    public void setRight(CRDT_Tree_Node right) { this.right = right; }
}
