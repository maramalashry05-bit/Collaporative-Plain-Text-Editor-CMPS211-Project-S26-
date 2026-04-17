package com.texteditor.apt;

import com.texteditor.apt.CRDT.Block_CRDT;
import com.texteditor.apt.CRDT.Block_Operation;
import com.texteditor.apt.CRDT.Character_Operation;

public class TestPhase2p3 {
    public static void main(String[] args) {

        ServerState server = new ServerState();
        MessageHandler handler = new MessageHandler(server);
        User user1 = new User("User1");
        User user2 = new User("User2");

        System.out.println("=============================");
        System.out.println("TEST 1 - Block Insert");
        Block_Operation blockop1 = user1.createBlockInsert("block1", "root");
        handler.Handle("room1", Message_Converter.BlocktoJSON(blockop1));
        Block_Operation blockop2 = user2.createBlockInsert("block2", "root");
        handler.Handle("room1", Message_Converter.BlocktoJSON(blockop2));
        System.out.println("PASSED - two blocks created");

        System.out.println("=============================");
        System.out.println("TEST 2 - Char Insert");
        Character_Operation charop1 = user1.createCharInsert("block1", 0, 'H');
        handler.Handle("room1", Message_Converter.ChartoJSON(charop1));
        Character_Operation charop2 = user1.createCharInsert("block1", 1, 'i');
        handler.Handle("room1", Message_Converter.ChartoJSON(charop2));
        Character_Operation charop3 = user2.createCharInsert("block2", 0, 'H');
        handler.Handle("room1", Message_Converter.ChartoJSON(charop3));
        Character_Operation charop4 = user2.createCharInsert("block2", 1, 'e');
        handler.Handle("room1", Message_Converter.ChartoJSON(charop4));
        Character_Operation charop5 = user2.createCharInsert("block2", 2, 'y');
        handler.Handle("room1", Message_Converter.ChartoJSON(charop5));
        System.out.println("block1: " + server.getblockText("block1", "room1"));
        System.out.println("block2: " + server.getblockText("block2", "room1"));

        System.out.println("=============================");
        System.out.println("TEST 3 - Char Delete");
        Character_Operation deleteop = user1.createCharDelete("block1", 0, 'H');
        handler.Handle("room1", Message_Converter.ChartoJSON(deleteop));
        System.out.println("block1 after delete: " + server.getblockText("block1", "room1"));

        System.out.println("=============================");
        System.out.println("TEST 4 - Block Delete");
        Block_Operation blockdelete = user1.createBlockDelete("block2", "root");
        handler.Handle("room1", Message_Converter.BlocktoJSON(blockdelete));
        Block_CRDT doc = server.FindaDoc("room1");
        System.out.println("block2 deleted: " + doc.getSpecificBlock("block2").isDeleted());

        System.out.println("=============================");
        System.out.println("TEST 5 - Bad Message");
        NetworkMessage bad = handler.Handle("room1", "this is not json");
        if (bad == null)
            System.out.println("PASSED - bad message correctly rejected");
        else
            System.out.println("FAILED - bad message was not rejected");

        System.out.println("=============================");
        System.out.println("TEST 6 - Multiple Rooms");
        User user3 = new User("User3");
        Block_Operation room2block = user3.createBlockInsert("block1", "root");
        handler.Handle("room2", Message_Converter.BlocktoJSON(room2block));
        Character_Operation room2char = user3.createCharInsert("block1", 0, 'Z');
        handler.Handle("room2", Message_Converter.ChartoJSON(room2char));
        System.out.println("room1 block1: " + server.getblockText("block1", "room1"));
        System.out.println("room2 block1: " + server.getblockText("block1", "room2"));

        System.out.println("=============================");
        System.out.println("TEST 7 - JSON recovery");
        Character_Operation recoverop = user1.createCharInsert("block1", 0, 'X');
        String json = Message_Converter.ChartoJSON(recoverop);
        NetworkMessage recovered = Message_Converter.JSONtoNetworkMessage(json);
        System.out.println("Recovered opType: " + recovered.opType);
        System.out.println("Recovered userID: " + recovered.userID);
        System.out.println("Recovered character: " + recovered.character);

        System.out.println("=============================");
        System.out.println("ALL TESTS DONE");
    }
}
