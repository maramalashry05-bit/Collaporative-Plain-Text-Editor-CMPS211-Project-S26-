package com.texteditor.apt;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Message_Converter {
     private static final ObjectMapper mapper = new ObjectMapper();



     //char operation to json
    public static String ChartoJSON(Character_Operation charop){
        try{
            NetworkMessage msg= new NetworkMessage();
            msg.opType =  "CHAR_" + charop.type.name();
            msg.userID= charop.userID;
            msg.timestamp= charop.timestamp;
            msg.blockID= charop.blockID;
            msg.position= charop.position;
            msg.character= charop.character;

            String chartojson= mapper.writeValueAsString(msg);
             return chartojson;
        }
        catch (Exception e) {
            System.err.println("Failed to convert: " + e.getMessage());
            return null;
        }
    }


    //block operation to json
     public static String BlocktoJSON(Block_Operation blockop){
        try{
            NetworkMessage msg= new NetworkMessage();
            msg.opType =  "BLOCK_" + blockop.type.name();
            msg.userID= blockop.userID;
            msg.timestamp= blockop.timestamp;
            msg.blockID= blockop.blockID;
            msg.parentBlockID= blockop.parentBlockID;

            String blocktojson= mapper.writeValueAsString(msg);
             return blocktojson;
        }
        catch (Exception e) {
            System.err.println("Failed to convert: " + e.getMessage());
            return null;
        }
    }


    //json to networkmessage
    public static NetworkMessage JSONtoNetworkMessage(String json){
        try{
            NetworkMessage msg= new NetworkMessage();
            msg= mapper.readValue(json,NetworkMessage.class);
            return msg;
        }
        catch (Exception e) {
            System.err.println("Failed to convert: " + e.getMessage());
            return null;
        }
    }
}
