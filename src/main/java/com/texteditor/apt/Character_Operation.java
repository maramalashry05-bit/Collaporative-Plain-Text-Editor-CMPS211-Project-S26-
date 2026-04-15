package com.texteditor.apt;

public class Character_Operation{
    public enum Type {INSERT,DELETE}
    public Type type;
    public String userID;
    public int timestamp;
    public String blockID;
    public int position;
    public char character;

    public Character_Operation(Type type, String userID, int timestamp, String blockID, int position, char character){
        this.type=type;
        this.userID=userID;
        this.timestamp=timestamp;
        this.blockID=blockID;
        this.position=position;
        this.character=character;
    }

    @Override
    public String toString(){
        if (type==Type.INSERT){
            return "insert (" + character + ") at position " + position + " by user " + userID + " at time " + timestamp;
        } 
        else {
            return "delete (" + character + ") at position " + position + " by user " + userID + " at time " + timestamp;
        }
    }
}
