package com.texteditor.apt.Networking;

public class MessageHandler {
   ServerState Server;


    public MessageHandler(ServerState s){
        this.Server=s;
    }

    public NetworkMessage Handle(String docId, String json){
        NetworkMessage msg= Message_Converter.JSONtoNetworkMessage(json);
        if(msg== null)
            return null;
        if (msg.opType == null || msg.userID == null || msg.blockID == null) {
            System.err.println("missing fields");
            return null;
}
        Server.ApplyOperation(docId, msg);
        return msg;
    }
}
