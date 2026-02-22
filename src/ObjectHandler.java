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
            synchronized (Stage.DRAWN_LOCK) {
                if (GJKCollision(objA, objB)) {
                    objA.boundingBox.boxColor = new Color(72, 255, 0, 255);
                    objB.boundingBox.boxColor = new Color(72, 255, 0, 255);
                }
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
        IO.println("");
        IO.println("New GJK Collision Check");
        Vec[] simplex = new Vec[3];
        Vec direction = Vec.sub(objB.pos, objA.pos);
        findGJKPointWithDirection(objA, objB, direction, simplex, 0, "no return", 0.0);
        if (!findGJKPointWithDirection(objA, objB, simplex[0].mul(-1), simplex, 1, "insert second check style here", 0.0)) {
            IO.println("failed on second point");
            return false;
        }
        // Calculate perpendicular direction towards origin
        Vec perpDir = Vec.dirOfPerpendicularBisector(simplex[0], simplex[1]);
        /*// Check if the perpendicular points towards the origin, if not, flip it
        Vec midpoint = new Vec((simplex[0].x + simplex[1].x) / 2, (simplex[0].y + simplex[1].y) / 2);
        Vec toOrigin = new Vec(-midpoint.x, -midpoint.y);
        if (Vec.dot(perpDir, toOrigin) < 0) {
            // Flip the direction
            perpDir = Vec.dirOfPerpendicularBisector(simplex[0], simplex[1], true);
        }*/
        if (!findGJKPointWithDirection(objA, objB, perpDir, simplex, 2, "insert third check style here", 0.0)) {
            IO.println("failed on third point");
            return false; //the original had the simplexes reversed and then negated it, so i just changed the order of subtraction here - ok this may still be backwards
        }


        boolean check1Passed;
        boolean check2Passed;
        boolean check3Passed;
        for (int i = 0; i < 20; i++) {
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplex[2], simplex[0], false) == Vec.isPointRightOfLine(simplex[1], simplex[2], simplex[0], false)) {
                check1Passed = true;
                IO.println("check 1 passed");
            } else {
                check1Passed = false;
                IO.println("check 1 failed");
                if (!findGJKPointWithDirection(objA, objB, Vec.dirOfPerpendicularBisector(simplex[2], simplex[0]), simplex, 1, "insert third check style here", 0.0)) {
                    IO.println("failed on loop check 1");
                    return false;
                }
            }
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplex[0], simplex[1], false) == Vec.isPointRightOfLine(simplex[2], simplex[0], simplex[1], false)) {
                check2Passed = true;
                IO.println("check 2 passed");
            } else {
                check2Passed = false;
                IO.println("check 2 failed");
                if (!findGJKPointWithDirection(objA, objB, Vec.dirOfPerpendicularBisector(simplex[0], simplex[1]), simplex, 2, "insert third check style here", 0.0)) {
                    IO.println("failed on loop check 2");
                    return false;
                }
            }
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplex[1], simplex[2], false) == Vec.isPointRightOfLine(simplex[0], simplex[1], simplex[2], false)) {
                check3Passed = true;
                IO.println("check 3 passed");
            } else {
                check3Passed = false;
                IO.println("check 3 failed");
                if (!findGJKPointWithDirection(objA, objB, Vec.dirOfPerpendicularBisector(simplex[1], simplex[2]), simplex, 0, "insert third check style here", 0.0)) {
                    IO.println("failed on loop check 3");
                    return false;
                }
            }
            if (check1Passed && check2Passed && check3Passed) {
                IO.println("Successful collision detected");
                DrawSimplex(simplex, new Color(0, 255, 255, 255));
                return true;
            }
        }
        IO.println("gjk iteration expired, listing final check pass/fails");
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplex[2], simplex[0], false) == Vec.isPointRightOfLine(simplex[1], simplex[2], simplex[0], false)) {
            IO.println("check 1 passed"); } else { IO.println("check 1 failed");}
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplex[0], simplex[1], false) == Vec.isPointRightOfLine(simplex[2], simplex[0], simplex[1], false)) {
            IO.println("check 2 passed"); } else {  IO.println("check 2 failed");}
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplex[1], simplex[2], false) == Vec.isPointRightOfLine(simplex[0], simplex[1], simplex[2], false)) {
            IO.println("check 3 passed"); } else { IO.println("check 3 failed");}
        DrawSimplex(simplex, new Color(255, 0, 255, 255));
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
        if (simplex[pointIndex] != null) {
            Stage.drawn.addCenteredPoint(new Vec.coloredVec(simplex[pointIndex], new Color(255, 0, 0, 255)));
        }
        simplex[pointIndex] = Vec.sub(pointB, pointA); //since the list is already initialized with nulls, we can just replace existing points
        Stage.drawn.addCenteredPoint(new Vec.coloredVec(simplex[pointIndex], new Color(0, 255, 0, 255)));
        if (pointCheckStyle.equals("insert second check style here")) {
            double normDistBetweenPointsAndOrigin = Vec.normDist(new Vec(0,0), simplex[0], simplex[1], true);
            return normDistBetweenPointsAndOrigin != 0 && normDistBetweenPointsAndOrigin != 1;
        } else if (pointCheckStyle.equals("insert third check style here")) {
            // verify that the support point in search direction actually passes the origin
            // if dot(newPoint, searchDirection) <= 0, the shapes don't intersect
            double dotProduct = Vec.dot(simplex[pointIndex], direction);
            //IO.println("New Third Check:" + (dotProduct > 0) + "       Old Third Check:" + (Vec.isPointRightOfLine(new Vec(0,0), simplex[(1+pointIndex)%3], simplex[(2+pointIndex)%3], false)));
            return (dotProduct > 0);
            //return Vec.isPointRightOfLine(new Vec(0,0), simplex[(1+pointIndex)%3], simplex[(2+pointIndex)%3], false); //uses the pointIndex to determine which two points to use for the line
        } else if (pointCheckStyle.equals("insert fourth check style here")) {

        }
        return false;
    }

    private static void DrawSimplex(Vec[] simplex, Color color) {
        for (int i = 0; i < simplex.length; i++) {
            if (simplex[i] != null) {
                Stage.drawn.addCenteredLine(new Vec.coloredLine(simplex[i], simplex[(i + 1) % simplex.length], color));
            }
        }
    }
}
