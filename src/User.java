import java.util.ArrayList;
import java.util.List;

public class User{
    public String userID;
    public List<Operation> operationList=new ArrayList<>();
    private int clock=0;

    public User(String userID){
        this.userID=userID;
    }

    public Operation createInsert(String blockID, int position, char character){
        clock++;
        Operation operation=new Operation(Operation.Type.INSERT, userID, clock, blockID, position, character);
        operationList.add(operation);
        return operation;
    }

    public Operation createDelete(String blockID, int position, char character){
        clock++;
        Operation operation=new Operation(Operation.Type.DELETE, userID, clock, blockID, position, character);
        operationList.add(operation);
        return operation;
    }
}