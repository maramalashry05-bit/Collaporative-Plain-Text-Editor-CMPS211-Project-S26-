public class Simulation {
    public static void main(String[] args) {
        User user1 = new User("User1");
        User user2 = new User("User2");

        String blockID = "Block1";

        Operation operation1 = user1.createInsert(blockID, 0, 'H');
        Operation operation2 = user2.createInsert(blockID, 0, 'W');
        Operation operation3 = user1.createInsert(blockID, 1, 'i');
        Operation operation4 = user2.createDelete(blockID, 0, 'W');

        System.out.println(operation1);
        System.out.println(operation2);
        System.out.println(operation3);
        System.out.println(operation4);
    }
}