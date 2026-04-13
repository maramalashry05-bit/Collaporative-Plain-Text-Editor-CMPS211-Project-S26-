public class TestConverter {
    public static void main(String[] args) {

        // Test 1 - Character Operation to JSON
        User user1 = new User("User1");
        Character_Operation charop = user1.createCharInsert("block1", 0, 'H');
        String json = Message_Converter.ChartoJSON(charop);
        System.out.println("Char to JSON: " + json);

        // Test 2 - Block Operation to JSON
        Block_Operation blockop = user1.createBlockInsert("block1", "root");
        String blockjson = Message_Converter.BlocktoJSON(blockop);
        System.out.println("Block to JSON: " + blockjson);

        // Test 3 - JSON back to NetworkMessage
        NetworkMessage msg = Message_Converter.JSONtoNetworkMessage(json);
        System.out.println("Recovered userID: " + msg.userID);
        System.out.println("Recovered character: " + msg.character);
    }
}