public class NetworkMessage {
    public String opType;
    public String userID;
    public int timestamp;
    public String blockID;
    public String parentBlockID;
    public int position;
    public char character;

    public NetworkMessage() {}

    public NetworkMessage(String opType, String userID, int timestamp, String blockID,String parentBlockID, int position, char character) {
    this.opType = opType;
    this.userID = userID;
    this.timestamp = timestamp;
    this.blockID = blockID;
    this.parentBlockID=parentBlockID;
    this.position = position;
    this.character = character;
}
}