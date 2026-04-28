import java.util.Random;

public class Main {

    public static Stage stage1;
    public static void main(String[] args) {
         stage1 = new Stage(1200, 800, 1, 0.05, 1, 60);
         stage1.runSteps(999999999);
    }
    /*public static Stage stage1;
    public static void main(String[] args) {
         stage1 = new Stage(640, 480, 1, 0.05, 1, 60);
         stage1.runSteps(999999999);
    }*/
}