package com.texteditor.apt.CRDT;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CRDTSerializer {
    private static final ObjectMapper objectmapper=new ObjectMapper();
    

    public static String serialize(Block_CRDT blockcrdt) throws Exception {
        List<BlockDTO> dtoList = new ArrayList<>();
       for (int i = 0; i < blockcrdt.getBlocks().size(); i++) {
                Block_Node block = blockcrdt.getBlocks().get(i);
                BlockDTO blockDTO = new BlockDTO(block.getBlockID(), block.getParentBlockID(), block.isDeleted(), new ArrayList<>());
                
        for (int j = 0; j < block.getContent().getAllNodes().size(); j++) {
                 Char_Node charNode = block.getContent().getAllNodes().get(j);
                CharDTO charDTO = new CharDTO(charNode.getPosition(), charNode.getUserID(), charNode.getValue(), charNode.isDeleted());
                blockDTO.characters.add(charDTO);
        }
        dtoList.add(blockDTO);
    }
     return objectmapper.writeValueAsString(dtoList);}

     public static Block_CRDT deserialize(String json) throws Exception {
        if (json == null || json.equals("[]")) return new Block_CRDT();
        List<BlockDTO> dtoList = objectmapper.readValue(json, 
        objectmapper.getTypeFactory().constructCollectionType(List.class, BlockDTO.class));
        Block_CRDT crdt = new Block_CRDT();
        crdt.getBlocks().clear();
        for (int i = 0; i < dtoList.size(); i++) {
            BlockDTO blockDTO = dtoList.get(i);
            Block_Node blockNode = new Block_Node(blockDTO.blockID, blockDTO.parentBlockID);
            if (blockDTO.deleted) blockNode.markAsDeleted();
            for (int j = 0; j < blockDTO.characters.size(); j++) {
                CharDTO charDTO = blockDTO.characters.get(j);
                Char_ID charId = new Char_ID(new Identifier[]{new Identifier(charDTO.position, charDTO.userID)});
                Char_Node charNode = new Char_Node(charId, charDTO.character);
                if (charDTO.deleted) charNode.markAsDeleted();
                blockNode.getContent().insert(charNode);
            }
            crdt.getBlocks().add(blockNode);
        }
        return crdt;
     }
}