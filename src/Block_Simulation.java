import java.util.LinkedList;

public class Block_Simulation {

    // this method is our judge - takes a true/false condition and a test name
    // if condition is true it prints PASSED, if false it prints FAILED
    private static void check(boolean condition, String testName) {
        if (condition) {
            System.out.println(testName + " PASSED");
        } else {
            System.out.println(testName + " FAILED");
        }
    }

    // Test 1
    // what we test: when a new document is created, does root block exist automatically?
    // how we test: create a document, search for "root", check it is not null
    // what we look for: root must never be null because every document needs a starting point
    private static void testRootExists() {
        Block_CRDT document = new Block_CRDT();
        Block_Node root = document.getSpecificBlock("root");
        check(root != null, "testRootExists");
    }

    // Test 2
    // what we test: can we insert a block into the document?
    // how we test: insert block1 after root, then search for block1
    // what we look for: block1 must be found meaning insertion worked correctly
    private static void testInsertBlock() {
        Block_CRDT document = new Block_CRDT();
        document.insertBlock("block1", "root");
        Block_Node found = document.getSpecificBlock("block1");
        check(found != null, "testInsertBlock");
    }

    // Test 3
    // what we test: does deletion use tombstone instead of actually removing the block?
    // how we test: insert block1, delete it, find it, check isDeleted flag is true
    // what we look for: block1 must still exist in memory but marked as deleted
    // this is critical for CRDT because another user might be editing that block
    private static void testDeleteBlock() {
        Block_CRDT document = new Block_CRDT();
        document.insertBlock("block1", "root");
        document.deleteBlock("block1");
        Block_Node found = document.getSpecificBlock("block1");
        check(found.isDeleted() == true, "testDeleteBlock");
    }

    // Test 4
    // what we test: does a deleted block disappear from the visible ordered list?
    // how we test: insert block1 and block2, delete block1, get ordered list
    //              loop through the list looking for block1
    // what we look for: block1 must NOT appear in the list even though it exists in memory
    private static void testDeletedBlockNotVisible() {
        Block_CRDT document = new Block_CRDT();
        document.insertBlock("block1", "root");
        document.insertBlock("block2", "root");
        document.deleteBlock("block1");
        LinkedList<Block_Node> result = document.getAllBlocksInOrder("root");
        boolean found = false;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).getBlockID().equals("block1")) {
                found = true;
            }
        }
        check(found == false, "testDeletedBlockNotVisible");
    }

    // Test 5
    // what we test: are blocks returned in the correct order based on parent relationships?
    // how we test: insert block1 after root, block2 after block1, block3 after root
    //              get ordered list and check positions
    // what we look for: order must be block1 -> block2 -> block3
    //                   because block2 is a child of block1 so it comes right after it
    //                   this proves our recursive findAllChildren works correctly
    private static void testBlockOrdering() {
        Block_CRDT document = new Block_CRDT();
        document.insertBlock("block1", "root");
        document.insertBlock("block2", "block1");
        document.insertBlock("block3", "root");
        LinkedList<Block_Node> result = document.getAllBlocksInOrder("root");
        check(result.get(0).getBlockID().equals("block1"), "testBlockOrdering block1 first");
        check(result.get(1).getBlockID().equals("block2"), "testBlockOrdering block2 second");
        check(result.get(2).getBlockID().equals("block3"), "testBlockOrdering block3 third");
    }

    // Test 6
    // what we test: when two users insert blocks at the same time do both appear?
    // how we test: User1 and User2 both insert a block after root at the same time (simulated)
    //              get ordered list and check size is 2
    // what we look for: both blocks must appear, no one's work is lost
    // this is the core of CRDT - concurrent operations must all be applied correctly
    private static void testConcurrentInserts() {
        Block_CRDT document = new Block_CRDT();
        document.insertBlock("User1block", "root");
        document.insertBlock("User2block", "root");
        LinkedList<Block_Node> result = document.getAllBlocksInOrder("root");
        check(result.size() == 2, "testConcurrentInserts");
    }

    // Test 7
    // what we test: can we insert characters inside a block using the Character_Tree?
    // how we test: insert block1, get it, use CRDT_ID_Generator to create char IDs
    //              insert H then i into block1's content, check visible text is "Hi"
    // what we look for: visible text must equal "Hi"
    // this proves our Block CRDT integrates correctly with teammate's Character CRDT
    private static void testCharactersInsideBlock() {
        Block_CRDT document = new Block_CRDT();
        document.insertBlock("block1", "root");
        Block_Node block1 = document.getSpecificBlock("block1");
        CRDT_ID_Generator idGen = new CRDT_ID_Generator();
        Char_ID idH = idGen.generateIdBetween(null, null, "User1");
        block1.getContent().insert(new Char_Node(idH, 'H'));
        Char_ID idI = idGen.generateIdBetween(idH, null, "User1");
        block1.getContent().insert(new Char_Node(idI, 'i'));
        check(block1.getContent().getVisibleText().equals("Hi"), "testCharactersInsideBlock");
    }

    public static void main(String[] args) {

        // simulate two users creating block operations like teammate did
        // this shows operations being created before we test if they work
        System.out.println("Starting Block CRDT Operations");
        System.out.println("-------------------------");

       User user1 = new User("User1");
User user2 = new User("User2");

Block_Operation op1 = user1.createBlockInsert("block1", "root");
Block_Operation op2 = user2.createBlockInsert("block2", "root");
Block_Operation op3 = user1.createBlockDelete("block1", "root");

        // now run all tests to verify everything works correctly
        System.out.println("-------------------------");
        System.out.println("Starting Block CRDT Tests");
        System.out.println("-------------------------");

        testRootExists();
        testInsertBlock();
        testDeleteBlock();
        testDeletedBlockNotVisible();
        testBlockOrdering();
        testConcurrentInserts();
        testCharactersInsideBlock();

        System.out.println("-------------------------");
        System.out.println("Tests complete");
    }
}
