package com.texteditor.apt.CRDT;

import com.texteditor.apt.User;

public class Character_Simulation {

    private static void check(boolean condition, String testName) {
        if (condition) {
            System.out.println(testName + " PASSED");
        } else {
            System.out.println(testName + " FAILED");
        }
    }

    private static void testInsertSingleCharacter() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGen = new CRDT_ID_Generator();
        Char_ID id = idGen.generateIdBetween(null, null, "User1");
        tree.insert(new Char_Node(id, 'A'));
        check(tree.getVisibleText().equals("A"), "testInsertSingleCharacter");
    }

    private static void testInsertMultipleCharacters() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGen = new CRDT_ID_Generator();
        Char_ID idH = idGen.generateIdBetween(null, null, "User1");
        Char_ID idI = idGen.generateIdBetween(idH, null, "User1");
        tree.insert(new Char_Node(idH, 'H'));
        tree.insert(new Char_Node(idI, 'i'));
        check(tree.getVisibleText().equals("Hi"), "testInsertMultipleCharacters");
    }

    private static void testDeleteCharacterTombstone() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGen = new CRDT_ID_Generator();
        Char_ID idH = idGen.generateIdBetween(null, null, "User1");
        Char_ID idI = idGen.generateIdBetween(idH, null, "User1");
        tree.insert(new Char_Node(idH, 'H'));
        tree.insert(new Char_Node(idI, 'i'));
        tree.delete(idH);
        check(tree.getVisibleText().equals("i"), "testDeleteCharacterTombstone");
    }

    private static void testVisibleNodesOrder() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGen = new CRDT_ID_Generator();
        Char_ID idA = idGen.generateIdBetween(null, null, "User1");
        Char_ID idB = idGen.generateIdBetween(idA, null, "User2");
        Char_ID idC = idGen.generateIdBetween(idB, null, "User1");
        tree.insert(new Char_Node(idA, 'A'));
        tree.insert(new Char_Node(idB, 'B'));
        tree.insert(new Char_Node(idC, 'C'));
        Char_Node[] visible = tree.getVisibleNodes();
        boolean orderOk = visible.length == 3
                && visible[0].getValue() == 'A'
                && visible[1].getValue() == 'B'
                && visible[2].getValue() == 'C';
        check(orderOk, "testVisibleNodesOrder");
    }

    private static void testConcurrentInsertSamePosition() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGen = new CRDT_ID_Generator();
        Char_ID idW = idGen.generateIdBetween(null, null, "User1");
        Char_ID idH = idGen.generateIdBetween(null, null, "User2");
        tree.insert(new Char_Node(idW, 'W'));
        tree.insert(new Char_Node(idH, 'H'));
        Char_Node[] visible = tree.getVisibleNodes();
        check(visible.length == 2, "testConcurrentInsertSamePosition");
    }

    public static void main(String[] args) {
        System.out.println("Starting Character CRDT Simulation");
        System.out.println("-------------------------");

        User user1 = new User("User1");
        User user2 = new User("User2");

        String blockID = "block1";
        Character_Operation op1 = user1.createCharInsert(blockID, 0, 'H');
        Character_Operation op2 = user2.createCharInsert(blockID, 0, 'W');
        Character_Operation op3 = user1.createCharInsert(blockID, 1, 'i');
        Character_Operation op4 = user2.createCharDelete(blockID, 0, 'W');

        System.out.println(op1);
        System.out.println(op2);
        System.out.println(op3);
        System.out.println(op4);

        System.out.println("-------------------------");
        System.out.println("Starting Character CRDT Tests");
        System.out.println("-------------------------");

        testInsertSingleCharacter();
        testInsertMultipleCharacters();
        testDeleteCharacterTombstone();
        testVisibleNodesOrder();
        testConcurrentInsertSamePosition();

        System.out.println("-------------------------");
        System.out.println("Character CRDT Simulation complete");
    }
}
