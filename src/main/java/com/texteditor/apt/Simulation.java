package com.texteditor.apt;

import java.rmi.server.Operation;

public class Simulation {
    public static void main(String[] args) {
        User user1 = new User("User1");
        User user2 = new User("User2");

        String blockID = "Block1";

        Character_Operation operation1 = user1.createCharInsert(blockID, 0, 'H');
        Character_Operation operation2 = user2.createCharInsert(blockID, 0, 'W');
        Character_Operation operation3 = user1.createCharInsert(blockID, 1, 'i');
        Character_Operation operation4 = user2.createCharDelete(blockID, 0, 'W');

        System.out.println(operation1);
        System.out.println(operation2);
        System.out.println(operation3);
        System.out.println(operation4);
    }
}