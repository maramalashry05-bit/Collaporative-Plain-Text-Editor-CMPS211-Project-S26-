package com.texteditor.apt.CRDT;

import java.util.LinkedList;

    public class Block_CRDT {
        private static final String ROOT_ID = "root";
        private LinkedList<Block_Node> blocks;


    public Block_CRDT(){
            this.blocks=new LinkedList<>();
            Block_Node theroot = new Block_Node(ROOT_ID, null);
            blocks.add(theroot);
        }


    public void insertBlock(String blockid, String parentid){
        Block_Node insertedBlock = new Block_Node(blockid, parentid);
        blocks.add(insertedBlock);
    }

    public void deleteBlock(String blockid){
        for(int i=0; i<blocks.size(); i++){
            Block_Node todelete = blocks.get(i);
            if (todelete.getBlockID().equals(blockid)){
                todelete.markAsDeleted();
            }
        }
    }

    public Block_Node getSpecificBlock(String blockid){
        for(int i=0; i<blocks.size(); i++){
            Block_Node tobefound = blocks.get(i);
            if (tobefound.getBlockID().equals(blockid)){
                return tobefound;
            }
        }
        return null;
    }

    public LinkedList<Block_Node> getAllBlocksInOrder(String rootid){
        LinkedList<Block_Node> list= new LinkedList<>();
        findAllChildren(rootid, list);
        return list;
    }

    public void findAllChildren(String parentID, LinkedList<Block_Node> result){
        LinkedList<Block_Node> allblocks= getBlocks();
        for(int i=0; i<allblocks.size();i++){
            Block_Node tobefound = blocks.get(i);
            if(tobefound.getParentBlockID() != null && tobefound.getParentBlockID().equals(parentID) && !tobefound.isDeleted()){
                result.add(tobefound);
                findAllChildren(tobefound.getBlockID(), result);
            }
        }
    }

    public LinkedList<Block_Node> getBlocks(){
            return blocks;  
    }

    public void insertCharacter(String blockID, Char_Node charNode) {
        Block_Node block = getSpecificBlock(blockID);
        if (block != null && !block.isDeleted()) {
            block.getContent().insert(charNode);
        }
}

    public String getBlockText(String blockID) {
        Block_Node block = getSpecificBlock(blockID);
        if (block != null && !block.isDeleted()) {
            return block.getContent().getVisibleText();
        }
        return "";
    }
}
