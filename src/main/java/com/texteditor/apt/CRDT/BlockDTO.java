package com.texteditor.apt.CRDT;

import java.util.List;

public class BlockDTO {
    public String blockID;
    public String parentBlockID;
    public boolean deleted;
    public List<CharDTO> characters;

    public BlockDTO() {}

    public BlockDTO(String blockID, String parentBlockID, boolean deleted, List<CharDTO> characters) {
        this.blockID = blockID;
        this.parentBlockID = parentBlockID;
        this.deleted = deleted;
        this.characters = characters;
    }
}