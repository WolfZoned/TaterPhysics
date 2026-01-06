public class Main {
    public static Stage stage1;
    public static void main(String[] args) {
         stage1 = new Stage(640, 480, 1, 0.05, 20);
         stage1.runSteps(999999999);
    }

    //make sure to have large periods of time without modifying the physics object to allow rendering
}