import java.util.ArrayList;

public class TaterMath {
    /*public int upMod(int a, int b) {
        return (a-1) % b+1;
    }*/

    /// literally returns atan2
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
    public static boolean isClockwise(ArrayList<Vec> simplexList) {
        //use shoelace - maybe extract full algorithm and then just use a part like in tater?
        return TaterMath.shoelace(simplexList) > 0;
    }

    /// wow, these Javadoc comments are actually really handy (well not this one)
    public static double area(ArrayList<Vec> simplexList) {
        return Math.abs(TaterMath.shoelace(simplexList)) / 2;
    }

    /// returns the angular velocity converted and added to linear velocity
    public static Vec relVel(Object obj, Vec rPerp) { //double aAngVel, double bAngVel, Vec aLinVel, Vec bLinVel
        return rPerp.mul(obj.angularVel).add(obj.vel);
        //return angVel * r perp + linVel
    }

    public static double contactVelMag(Vec relVel, Vec normal) {
        if (relVel.dot(normal) < 0) {
            return 0;
        } else {
            return relVel.dot(normal);
        }
    }

    public static Vec tangent(Vec relVel, Vec normal) {
        // Remove the normal component to get tangential slip velocity at contact.
        return relVel.sub(normal.mul(relVel.dot(normal)));
    }

    public static void wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPointInsideBox(Vec point, Vec boxCornerA,  Vec boxCornerB) {
        return !(point.x < Math.min(boxCornerA.x, boxCornerB.x) || point.x > Math.max(boxCornerA.x, boxCornerB.x) ||
            point.y < Math.min(boxCornerA.y, boxCornerB.y) || point.y > Math.max(boxCornerA.y, boxCornerB.y));
    }
}
