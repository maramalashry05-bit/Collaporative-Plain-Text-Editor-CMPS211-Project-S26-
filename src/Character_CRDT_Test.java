public class Character_CRDT_Test {

    // Our custom mini-testing framework (No JUnit needed!)
    private static void assertEquals(String expected, String actual, String testName) {
        if (!expected.equals(actual)) {
            // If the text doesn't match, crash the program and show the error
            throw new RuntimeException("❌ " + testName + " FAILED! Expected: '" + expected + "', but got: '" + actual + "'");
        }
        System.out.println("✅ " + testName + " PASSED.");
    }

    private static void assertTrue(boolean condition, String testName) {
        if (!condition) {
            throw new RuntimeException("❌ " + testName + " FAILED! Condition was false.");
        }
    }

    // --- THE TESTS ---

    private static void testBasicSequentialInsertion() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGenerator = new CRDT_ID_Generator();

        Char_ID id1 = idGenerator.generateIdBetween(null, null, "UserA");
        tree.insert(new Char_Node(id1, 'H'));

        Char_ID id2 = idGenerator.generateIdBetween(id1, null, "UserA");
        tree.insert(new Char_Node(id2, 'i'));

        assertEquals("Hi", tree.getVisibleText(), "testBasicSequentialInsertion");
    }

    private static void testTypingInTheMiddle() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGenerator = new CRDT_ID_Generator();

        // User A types "Cat"
        Char_ID idC = idGenerator.generateIdBetween(null, null, "UserA");
        tree.insert(new Char_Node(idC, 'C'));

        Char_ID idA = idGenerator.generateIdBetween(idC, null, "UserA");
        tree.insert(new Char_Node(idA, 'a'));

        Char_ID idT = idGenerator.generateIdBetween(idA, null, "UserA");
        tree.insert(new Char_Node(idT, 't'));
        
        // User B realizes it should be "Chat", so they insert 'h' between 'C' and 'a'
        Char_ID idH = idGenerator.generateIdBetween(idC, idA, "UserB");
        tree.insert(new Char_Node(idH, 'h'));

        assertEquals("Chat", tree.getVisibleText(), "testTypingInTheMiddle");
    }

    private static void testTombstoneDeletion() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGenerator = new CRDT_ID_Generator();

        Char_ID idD = idGenerator.generateIdBetween(null, null, "UserA");
        tree.insert(new Char_Node(idD, 'D'));

        Char_ID idO = idGenerator.generateIdBetween(idD, null, "UserA");
        Char_Node nodeO = new Char_Node(idO, 'o');
        tree.insert(nodeO);

        Char_ID idG = idGenerator.generateIdBetween(idO, null, "UserA");
        tree.insert(new Char_Node(idG, 'g'));

        // User presses backspace on 'o'
        nodeO.markAsDeleted();

        assertTrue(nodeO.isDeleted(), "testTombstoneDeletion (Flag Check)");
        assertEquals("Dg", tree.getVisibleText(), "testTombstoneDeletion (Text Check)");
    }

    private static void testConcurrentConflictResolution() {
        Character_Tree tree = new Character_Tree();
        CRDT_ID_Generator idGenerator = new CRDT_ID_Generator();

        // Base document has "A"
        Char_ID idA = idGenerator.generateIdBetween(null, null, "System");
        tree.insert(new Char_Node(idA, 'A'));

        // Alice and Bob type concurrently right after "A"
        Char_ID user1Id = idGenerator.generateIdBetween(idA, null, "Alice");
        Char_Node nodeFromAlice = new Char_Node(user1Id, 'B');

        Char_ID user2Id = idGenerator.generateIdBetween(idA, null, "Bob");
        Char_Node nodeFromBob = new Char_Node(user2Id, 'C');

        // Server receives them
        tree.insert(nodeFromAlice);
        tree.insert(nodeFromBob);

        // Tie is broken by SiteID ("Alice" comes before "Bob")
        assertEquals("ABC", tree.getVisibleText(), "testConcurrentConflictResolution");
    }

    // --- MAIN METHOD TO RUN EVERYTHING ---
    public static void main(String[] args) {
        System.out.println("Starting Custom CRDT Tests...\n");

        testBasicSequentialInsertion();
        testTypingInTheMiddle();
        testTombstoneDeletion();
        testConcurrentConflictResolution();

        System.out.println("\n🎉 ALL TESTS PASSED! Phase 1 Part 1 Character CRDT is done.");
    }
}
