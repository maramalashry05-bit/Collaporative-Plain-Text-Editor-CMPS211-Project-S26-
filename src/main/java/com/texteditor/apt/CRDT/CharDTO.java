package com.texteditor.apt.CRDT;

public class CharDTO {
    public int position;
    public String userID;
    public char character;
    public boolean deleted;

    public CharDTO() {}

    public CharDTO(int position, String userID, char character, boolean deleted) {
        this.position = position;
        this.userID = userID;
        this.character = character;
        this.deleted = deleted;
    }
}