import java.awt.*;
import java.util.ArrayList;

public class ObjectHandler {
    public static ArrayList<Object> createInitialObjects() {
        // Create a rectangle object
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(new Object(new Color(200, 0, 0), 210, 200, 0.0, 100, 50, objects.size()));
        // Create a custom shape object (triangle)
        Vec[] setRel = new Vec[] {
            new Vec(2, 32),
            new Vec(27, -13),
            new Vec(-23, -13)
        };
        objects.add(new Object(new Color(0, 200, 0), 350, 200, 0.0, setRel, objects.size()));
        return objects;
    }

    public static int mouseHoveredObject(Vec mousePos, ArrayList<Object> objects) {
        for (Object obj : objects) {
            if (obj != null && obj.type.equals("polygon")) {
                if (obj.isPointInside(mousePos)) {
                    return obj.index;
                }
            }
        }
        return -1;
    }

    public static void collisionCalcs(ArrayList<Object> objects) {
        for (Object obj : objects) {
            if (obj != null && obj.type.equals("polygon")) {
                obj.boundingBox.calc(obj.rel, obj.pos);
            }
        }
        for (int i = 0; i < objects.size(); i++) {
            Object objA = objects.get(i);
            if (objA != null && objA.type.equals("polygon")) {
                for (int j = i + 1; j < objects.size(); j++) {
                    Object objB = objects.get(j);
                    if (objB != null && objB.type.equals("polygon")) {
                        polygonPolygonCollision(objA, objB);
                    }
                }
            }
        }
    }

    private static void polygonPolygonCollision(Object objA, Object objB) {
        if (polygonBoundingBoxCollision(objA.boundingBox, objB.boundingBox)) {
            if (GJKCollision(objA, objB)) {

            }
        }
    }

    private static boolean polygonBoundingBoxCollision(Object.BoundingBox bbA, Object.BoundingBox bbB) {
        if (bbA.xMax < bbB.xMin || bbA.xMin > bbB.xMax) {
            bbA.boxColor = new Color(255, 140, 90, 255);
            bbB.boxColor = new Color(255, 140, 90, 255);
            return false;
        }
        if (bbA.yMax < bbB.yMin || bbA.yMin > bbB.yMax) {
            bbA.boxColor = new Color(255, 140, 90, 255);
            bbB.boxColor = new Color(255, 140, 90, 255);
            return false;
        }
        bbA.boxColor = new Color(255, 255, 0, 255);
        bbB.boxColor = new Color(255, 255, 0, 255);
        return true;
    }

    private static boolean GJKCollision(Object objA, Object objB) { //make sure all shapes are clockwise for now
        Vec[] simplex = new Vec[3];
        Vec direction = Vec.sub(objB.pos, objA.pos);
        findGJKPointWithDirection(objA, objB, direction, simplex, 0, "no return", 0.0);
        if (findGJKPointWithDirection(objA, objB, simplex[1].mul(-1), simplex, 1, "insert second check style here", 0.0)) {
            return false;
        }
        if (findGJKPointWithDirection(objA, objB, Vec.dirOfPerpendicularBisector(Vec.sub(simplex[1], simplex[0]), simplex[0]), simplex, 2, "insert third check style here", 0.0)) {
            return false; //the original had the simplexes reversed and then negated it, so i just changed the order of subtraction here -- also the perpendicular bisector also makes sure that the origin is on the correct side so we don't need to check it later
        }
        for (int i = 0; i < 20; i++) {
            
        }
        return false;
    }

    private static boolean findGJKPointWithDirection(Object objA, Object objB, Vec direction, Vec[] simplex, int pointIndex, String pointCheckStyle, double dirVecLen) {
        double highestMagAlongDir;
        int highestIndex;
        int index;
        double currentMag;


        //if (objA.type.equals("circle")) {

        //} else {
            highestMagAlongDir = -Double.MAX_VALUE;
            highestIndex = -1;
            index = 0;
            for (Vec vec : objA.rel) {
                index++;
                currentMag = Vec.dot(vec, direction.mul(-1));
                if (currentMag > highestMagAlongDir) {
                    highestMagAlongDir = currentMag;
                    highestIndex = index - 1;
                }
            }
            Vec pointA = objA.rel[highestIndex].add(objA.pos);
        //}
        highestMagAlongDir = -Double.MAX_VALUE;
        highestIndex = -1;
        index = 0;
        for (Vec vec : objB.rel) {
            index++;
            currentMag = Vec.dot(vec, direction);
            if (currentMag > highestMagAlongDir) {
                highestMagAlongDir = currentMag;
                highestIndex = index - 1;
            }
        }
        Vec pointB = objB.rel[highestIndex].add(objB.pos);
        simplex[pointIndex] = Vec.sub(pointB, pointA); //since the list is already initialized with nulls, we can just replace existing points
        if (pointCheckStyle.equals("insert second check style here")) {
            return 0 > Vec.dot(simplex[0], simplex[0].mul(-1));
        } else if (pointCheckStyle.equals("insert third check style here")) {
            
        } else if (pointCheckStyle.equals("insert fourth check style here")) {

        }

        return false;
    }
}
