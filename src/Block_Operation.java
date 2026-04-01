public class Block_Operation {
    public enum Type {INSERT,DELETE}
    public Type type;
    public String userID;
    public int timestamp;
    public String blockID;
    public String parentBlockID;

     public Block_Operation(Type type, String userID, int timestamp, String blockID, String parentBlockID){
        this.type=type;
        this.userID=userID;
        this.timestamp=timestamp;
        this.blockID=blockID;
        this.parentBlockID=parentBlockID;
    }
}
