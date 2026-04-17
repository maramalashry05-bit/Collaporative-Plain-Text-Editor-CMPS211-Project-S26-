package com.texteditor.apt;

import java.util.ArrayList;
import java.util.List;

import com.texteditor.apt.CRDT.Block_Operation;
import com.texteditor.apt.CRDT.Character_Operation;

public class User{
    public String userID;
    public List<Character_Operation> charoperationList=new ArrayList<>();
     public List<Block_Operation> blockOperationList = new ArrayList<>();
    private int clock=0;

    public User(String userID){
        this.userID=userID;
    }

    public Character_Operation createCharInsert(String blockID, int position, char character){
        clock++;
        Character_Operation operation=new Character_Operation(Character_Operation.Type.INSERT, userID, clock, blockID, position, character);
        charoperationList.add(operation);
        return operation;
    }

    public Character_Operation createCharDelete(String blockID, int position, char character){
        clock++;
        Character_Operation operation=new Character_Operation(Character_Operation.Type.DELETE, userID, clock, blockID, position, character);
        charoperationList.add(operation);
        return operation;
    }

    public Block_Operation createBlockInsert(String blockID, String parentBlockID ){
        clock++;
        Block_Operation operation = new Block_Operation(Block_Operation.Type.INSERT, userID, clock, blockID, parentBlockID);
        blockOperationList.add(operation); 
        return operation;
    } 

    public Block_Operation createBlockDelete(String blockID, String parentBlockID ){
        clock++;
        Block_Operation operation = new Block_Operation(Block_Operation.Type.DELETE, userID, clock, blockID, parentBlockID);
        blockOperationList.add(operation); 
        return operation;
    } 
}