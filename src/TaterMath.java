import java.util.ArrayList;

public class TaterMath {
    /*public int upMod(int a, int b) {
        return (a-1) % b+1;
    }*/

    public static double vecToAng(double x, double y) {
        return Math.atan2(y, x);
    }

    //public boolean clockwise()

    public static double shoelace(ArrayList<Vec> list) {
        double shoelaceBuild = 0;
        for (int i = 0; i < list.size(); i++) {
            shoelaceBuild += (list.get(i).x * list.get((i+1) % list.size()).y) - (list.get((i+1) % list.size()).x * list.get(i).y);
        }
        return shoelaceBuild;
    }

}
