public class CombinedSimulation {
    public static void main(String[] args) {
        User user1 = new User("User1");
        User user2 = new User("User2");

        Block_CRDT document = new Block_CRDT();

        Block_Operation op1 = user1.createBlockInsert("block1", "root");
        Block_Operation op2 = user2.createBlockInsert("block2", "root");

        document.insertBlock(op1.blockID, op1.parentBlockID);
        document.insertBlock(op2.blockID, op2.parentBlockID);

        System.out.println("=== Block Operations ===");
        System.out.println(op1);
        System.out.println(op2);

        // User1 types "Hi" inside block1
        CRDT_ID_Generator idGen = new CRDT_ID_Generator();
        Block_Node block1 = document.getSpecificBlock("block1");
        Block_Node block2 = document.getSpecificBlock("block2");

        Char_ID idH = idGen.generateIdBetween(null, null, "User1");
        document.insertCharacter("block1", new Char_Node(idH, 'H'));

        Char_ID idI = idGen.generateIdBetween(idH, null, "User1");
        document.insertCharacter("block1", new Char_Node(idI, 'i'));

        // User2 types "Hey" inside block2
        Char_ID idH2 = idGen.generateIdBetween(null, null, "User2");
        document.insertCharacter("block2", new Char_Node(idH2, 'H'));
        Char_ID idE = idGen.generateIdBetween(idH2, null, "User2");
        document.insertCharacter("block2", new Char_Node(idE, 'e'));
        Char_ID idY = idGen.generateIdBetween(idE, null, "User2");
        document.insertCharacter("block2", new Char_Node(idY, 'y'));

        // Print the full document
        System.out.println("=== Full Document ===");
        System.out.println("block1: " + document.getBlockText("block1"));
        System.out.println("block2: " + document.getBlockText("block2"));
    }
}

