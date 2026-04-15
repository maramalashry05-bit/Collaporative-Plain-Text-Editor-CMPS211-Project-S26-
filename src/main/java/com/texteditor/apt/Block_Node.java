package com.texteditor.apt;

public class Block_Node {
    private String blockID;
    private String parentBlockID;
    private boolean isDeleted;
    private Character_Tree content;


    public Block_Node(String blockID,String parentBlockID){
        this.blockID=blockID;
        this.parentBlockID=parentBlockID;
        this.isDeleted=false;
        this.content=new Character_Tree();
    }

    public String getBlockID(){
        return blockID;
    }
    public String getParentBlockID(){
        return parentBlockID;
    }
    public void markAsDeleted() { 
        this.isDeleted = true; 
    }
    public boolean isDeleted() { 
        return isDeleted; 
    }
    public Character_Tree getContent() {
    return content;
}
}
