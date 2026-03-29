import java.awt.*;
//import java.lang.Object;
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
        objects.add(new Object(new Color(0, 177, 0), 350, 200, 0.0, setRel, objects.size()));
        return objects;
    }

    public static int mouseHoveredObject(Vec mousePos, ArrayList<Object> objects) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            Object obj = objects.get(i);
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
                ArrayList<Vec> simplexList = new ArrayList<Vec>();
                if (GJKCollision(objA, objB, simplexList)) {
                    Vec MTV = EPACollision(objA, objB, simplexList);
                    if (MTV != null) {
                        // make sure MTV points from A to B for consistent correction/impulse direction
                        Vec centerDelta = objB.pos.sub(objA.pos);
                        if (Vec.dot(MTV, centerDelta) < 0) {
                            MTV = MTV.mul(-1);
                        }
                        objA.boundingBox.boxColor = new Color(72, 255, 0, 255);
                        objB.boundingBox.boxColor = new Color(72, 255, 0, 255);
                        objA.pos = objA.pos.add(MTV.mul(-0.5));
                        objB.pos = objB.pos.add(MTV.mul(0.5));
                        Vec contact = findContactPoints(objA, objB);
                        if (contact != null) { applyImpulse(objA, objB, MTV, contact); }
                    } else {
                        objA.boundingBox.boxColor = new Color(100, 0, 255, 255);
                        objB.boundingBox.boxColor = new Color(100, 0, 255, 255);
                    }
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

    private static boolean GJKCollision(Object objA, Object objB, ArrayList<Vec> simplexList) { //make sure all shapes are clockwise for now
        if (Options.GJK.genAndDisplayAllOutsidePoints) {generateAndDisplayAllOutsidePoints(objA, objB);}
        if (Options.GJK.debug) {IO.println("");}
        if (Options.GJK.debug) {IO.println("New GJK Collision Check");}
        simplexList.add(null);
        simplexList.add(null);
        simplexList.add(null);
        Vec direction = Vec.sub(objB.pos, objA.pos);
        checkGJKPoint(objA, objB, direction, simplexList, 0, "no return", 0.0);
        if (!checkGJKPoint(objA, objB, simplexList.get(0).mul(-1), simplexList, 1, "insert second check style here", 0.0)) {
            if (Options.GJK.debug) {IO.println("failed on second point");}
            return false;
        }
        // Calculate perpendicular direction towards origin
        Vec perpDir = Vec.dirOfPerpendicularBisector(simplexList.get(0), simplexList.get(1));
        /*// Check if the perpendicular points towards the origin, if not, flip it
        Vec midpoint = new Vec((simplexList.get(0).x + simplexList.get(1).x) / 2, (simplexList.get(0).y + simplexList.get(1).y) / 2);
        Vec toOrigin = new Vec(-midpoint.x, -midpoint.y);
        if (Vec.dot(perpDir, toOrigin) < 0) {
            // Flip the direction
            perpDir = Vec.dirOfPerpendicularBisector(simplexList.get(0), simplexList.get(1), true);
        }*/
        if (!checkGJKPoint(objA, objB, perpDir, simplexList, 2, "insert third check style here", 0.0)) {
            IO.println("failed on third point");
            return false; //the original had the simplexes reversed and then negated it, so i just changed the order of subtraction here - ok this may still be backwards
        }

        boolean check1Passed;
        boolean check2Passed;
        boolean check3Passed;
        for (int i = 0; i < 20; i++) {
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(2), simplexList.get(0), false) == Vec.isPointRightOfLine(simplexList.get(1), simplexList.get(2), simplexList.get(0), false)) {
                check1Passed = true;
                if (Options.GJK.debug) {IO.println("check 1 passed");}
            } else {
                check1Passed = false;
                if (Options.GJK.debug) {IO.println("check 1 failed");}
                if (!checkGJKPoint(objA, objB, Vec.dirOfPerpendicularBisector(simplexList.get(2), simplexList.get(0)), simplexList, 1, "insert third check style here", 0.0)) {
                    if (Options.GJK.debug) {IO.println("failed on loop check 1");}https://flavortown.hackclub.com/projects/5129
                    return false;
                }
            }
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(0), simplexList.get(1), false) == Vec.isPointRightOfLine(simplexList.get(2), simplexList.get(0), simplexList.get(1), false)) {
                check2Passed = true;
                if (Options.GJK.debug) {IO.println("check 2 passed");}
            } else {
                check2Passed = false;
                if (Options.GJK.debug) {IO.println("check 2 failed");}
                if (!checkGJKPoint(objA, objB, Vec.dirOfPerpendicularBisector(simplexList.get(0), simplexList.get(1)), simplexList, 2, "insert third check style here", 0.0)) {
                    if (Options.GJK.debug) {IO.println("failed on loop check 2");}
                    return false;
                }
            }
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(1), simplexList.get(2), false) == Vec.isPointRightOfLine(simplexList.get(0), simplexList.get(1), simplexList.get(2), false)) {
                check3Passed = true;
                if (Options.GJK.debug) {IO.println("check 3 passed");}
            } else {
                check3Passed = false;
                if (Options.GJK.debug) {IO.println("check 3 failed");}
                if (!checkGJKPoint(objA, objB, Vec.dirOfPerpendicularBisector(simplexList.get(1), simplexList.get(2)), simplexList, 0, "insert third check style here", 0.0)) {
                    if (Options.GJK.debug) {IO.println("failed on loop check 3");}
                    return false;
                }
            }
            if (check1Passed && check2Passed && check3Passed) {
                if (Options.GJK.debug) {IO.println("Successful collision detected");}
                if (Options.GJK.render.drawSuccessfulSimplex) {
                    DrawSimplex(simplexList, Options.GJK.render.successfulSimplexColor, 2.5f);
                }
                return true;
            }
        }
        if (Options.GJK.debug) {
            IO.println("gjk iteration expired, listing final check pass/fails");
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(2), simplexList.get(0), false) == Vec.isPointRightOfLine(simplexList.get(1), simplexList.get(2), simplexList.get(0), false)) {
            IO.println("check 1 passed"); } else { IO.println("check 1 failed");}
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(0), simplexList.get(1), false) == Vec.isPointRightOfLine(simplexList.get(2), simplexList.get(0), simplexList.get(1), false)) {
            IO.println("check 2 passed"); } else {  IO.println("check 2 failed");}
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(1), simplexList.get(2), false) == Vec.isPointRightOfLine(simplexList.get(0), simplexList.get(1), simplexList.get(2), false)) {
            IO.println("check 3 passed"); } else { IO.println("check 3 failed");}
        }
        if (Options.GJK.render.drawFailedSimplex) {
            DrawSimplex(simplexList, Options.GJK.render.failedSimplexColor, 2.5f);
        }
        return false;
    }

    private static Vec EPACollision(Object objA, Object objB, ArrayList<Vec> simplexList) {
        //do a better job of calculating clockwise/counterclockwise inside gjk - determine when and if it switches
        //counterpoint: check if they are clockwise now? don't touch whats not broken (at least i don't think it is???)
        //IO.println("");
        if (Options.EPA.debug) {IO.println("Starting EPA");}
        int minDistLineIndex;
        double minDist;
        Vec minDistVec;
        Vec minDistLineStartingVec;
        Vec minDistLineEndingVec;
        double minDistAng;
        boolean clockwise = TaterMath.isClockwise(simplexList);
        if (Options.GJK.debug) {IO.println("Original clockwise: " + clockwise + "but is now clockwise");}
        if (!clockwise) {
            Vec[] clockwiseVecs = new Vec[simplexList.size()];
            for (int i = 0; i < clockwiseVecs.length; i++) {
                clockwiseVecs[i] = simplexList.get(simplexList.size() - 1 - i);
            }
            for (int i = 0; i < clockwiseVecs.length; i++) {
                simplexList.set(i, clockwiseVecs[i]);
            }
        }
        clockwise = TaterMath.isClockwise(simplexList);
        int EPAAttempts = 30;
        Vec tempVec;
        double tempVecDist;
        Vec newVec;
        double[] EPAMemory = new double[4]; //2 sets of x,y, and index to catch repeating points and terminate early

        for  (int i = 0; i < EPAAttempts; i++) {
            if (Options.GJK.debug) {IO.println("New Attempt");}
            minDist = Double.MAX_VALUE;
            minDistLineIndex = -1;
            minDistVec = null;
            for  (int EPACounter = 0; EPACounter < simplexList.size(); EPACounter++) {
                tempVec = Vec.normDistPoint(new Vec(0, 0), simplexList.get(EPACounter), simplexList.get((EPACounter + 1) % simplexList.size()), true);
                tempVecDist = tempVec.len();
                if (tempVecDist < minDist) {
                    minDistVec = tempVec;
                    minDist = tempVecDist;
                    minDistLineIndex = EPACounter;
                }
            }
            minDistLineStartingVec = simplexList.get(minDistLineIndex);
            minDistLineEndingVec = simplexList.get((minDistLineIndex + 1) % simplexList.size());
            Stage.drawn.addCenteredPoint(new Vec.coloredVec(minDistVec, new Color(255, 255, 0, 255)));
            if (Options.GJK.debug) {IO.println("Closest Line: " + minDistLineStartingVec.x + " " + minDistLineStartingVec.y + "      " + minDistLineEndingVec.x + " " + minDistLineEndingVec.y);}
            minDistAng = TaterMath.vecToAng(minDistVec.x, minDistVec.y);
            if (minDistAng == 0) {
                if (Options.GJK.debug) {IO.println("These shapes are perfectly colliding");}
                if (Options.GJK.debug) {IO.println("You can port over the code to handle this, but maybe just get gud?");}
                if (Options.EPA.render.drawFailedSimplex) {
                    DrawSimplex(simplexList, Options.EPA.render.failedSimplexColor, 1f);
                }
                return null;
            }
            newVec = findGJKPoint(objA, objB, minDistVec);
            //checks if simplex already contrains newVec - if it does, then we have found the closest point on the Minkowski Difference to the origin, and can terminate with collision result
            if (newVec.x == minDistLineStartingVec.x && newVec.y == minDistLineStartingVec.y || newVec.x == minDistLineEndingVec.x && newVec.y == minDistLineEndingVec.y) {
                Stage.drawn.addCenteredPoint(new Vec.coloredVec(newVec, new Color(0, 255, 255, 255)));
                if (Options.EPA.render.drawSucessfulSimplex) {
                    DrawSimplex(simplexList, Options.EPA.render.successfulSimplexColor, 1f);
                }
                return minDistVec;
            }
            Stage.drawn.addCenteredPoint(new Vec.coloredVec(newVec, new Color(255, 0, 0, 255)));
            if (EPAMemory[0] == minDistVec.x && EPAMemory[1] == minDistVec.y || EPAMemory[2] == minDistVec.x && EPAMemory[3] == minDistVec.y) {
                if (Options.GJK.debug) {IO.println("Before terminating, EPA ran for " + EPAAttempts + " attempts");}
                if (Options.GJK.debug) {IO.println("EPA failed due to repeat of failed point");}
                if (Options.EPA.render.drawFailedSimplex) {
                    DrawSimplex(simplexList, Options.EPA.render.failedSimplexColor, 1f);
                }
                return null;
            }
            if (EPAMemory[0] != 0) {
                EPAMemory[2] = EPAMemory[0];
                EPAMemory[3] = EPAMemory[1];
            }
            EPAMemory[0] = minDistVec.x;
            EPAMemory[1] = minDistVec.y;
            simplexList.add(minDistLineIndex + 1, newVec);

        }
        if (Options.EPA.render.drawFailedSimplex) {
            DrawSimplex(simplexList, Options.EPA.render.failedSimplexColor, 1f);
        }
        return null;
    }

    private static boolean checkGJKPoint(Object objA, Object objB, Vec direction, ArrayList<Vec> simplexList, int pointIndex, String pointCheckStyle, double dirVecLen) {
        simplexList.set(pointIndex, findGJKPoint(objA, objB, direction)); //since the list is already initialized with nulls, we can just replace existing points
        Stage.drawn.addCenteredPoint(new Vec.coloredVec(simplexList.get(pointIndex), new Color(2, 237, 2, 255)));
        if (pointCheckStyle.equals("insert second check style here")) {
            double normDistBetweenPointsAndOrigin = Vec.normDist(new Vec(0,0), simplexList.get(0), simplexList.get(1), true);
            return normDistBetweenPointsAndOrigin != 0 && normDistBetweenPointsAndOrigin != 1;
        } else if (pointCheckStyle.equals("insert third check style here")) {
            // verify that the support point in search direction actually passes the origin
            // if dot(newPoint, searchDirection) <= 0, the shapes don't intersect
            double dotProduct = Vec.dot(simplexList.get(pointIndex), direction);
            //IO.println("New Third Check:" + (dotProduct > 0) + "       Old Third Check:" + (Vec.isPointRightOfLine(new Vec(0,0), simplexList.get((1+pointIndex)%3), simplexList.get((2+pointIndex)%3), false)));
            return (dotProduct > 0);
            //return Vec.isPointRightOfLine(new Vec(0,0), simplexList.get((1+pointIndex)%3), simplexList.get((2+pointIndex)%3), false); //uses the pointIndex to determine which two points to use for the line
        } else if (pointCheckStyle.equals("insert fourth check style here")) {
            return 0.1 < Math.abs(dirVecLen - Vec.distInDir(simplexList.get(pointIndex), direction));
        }
        return false;
    }

    private static Vec findGJKPoint(Object objA, Object objB, Vec direction) {
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
            //currentMag = Vec.dot(vec, direction);
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
        for (Vec vec : objB.rel) { //check
            index++;
            currentMag = Vec.dot(vec, direction);
            if (currentMag > highestMagAlongDir) {
                highestMagAlongDir = currentMag;
                highestIndex = index - 1;
            }
        }
        Vec pointB = objB.rel[highestIndex].add(objB.pos);
        if (Options.GJK.debug) {IO.println("DIRECTION: " + direction + "     New Vec: " + Vec.sub(pointB, pointA));}
        return(Vec.sub(pointB, pointA)); //since the list is already initialized with nulls, we can just replace existing points
    }

    private static Vec findContactPoints(Object objA, Object objB) {
        double tolerance = 0.6;
        int minDistPointIndex1 = 0;
        int minDistPointIndex2 = 0;
        int minDistLineIndex1 = 0;
        int minDistLineIndex2;
        int shapeIndex1 = 0;
        int shapeIndex2 = 0;

        double contactBuild;
        double tempMinDist = Double.MAX_VALUE;
        double tempNormDist;
        IO.println("");
        IO.println("");
        IO.println("Contact Points round 1:");
        for  (int i = 0; i < objB.rel.length; i++) {
            for (int j = 0; j < objA.rel.length; j++) {
                tempNormDist = Vec.normDist(objA.rel[j].add(objA.pos), objB.rel[i].add(objB.pos), objB.rel[(i+1)%objB.rel.length].add(objB.pos),true);
                contactBuild = Vec.distToNormDistPoint(objA.rel[j].add(objA.pos), objB.rel[i].add(objB.pos), objB.rel[(i+1)%objB.rel.length].add(objB.pos), true, tempNormDist);

                if ((contactBuild + tolerance) < tempMinDist) {
                    minDistPointIndex2 = -1;
                    shapeIndex2 = -1;
                    minDistPointIndex1 = j;
                    minDistLineIndex1 = i;
                    shapeIndex1 = 2;
                    tempMinDist = contactBuild;
                    IO.println("New min 1 found:      MinPointIndex " + minDistPointIndex1 + "   MinLineIndex " + minDistLineIndex1 + "   MinDist " + tempMinDist);
                } else if ((contactBuild - tolerance) < tempMinDist && minDistPointIndex1 != j && minDistPointIndex2 != j) {
                    minDistPointIndex2 = j;
                    minDistLineIndex2 = i;
                    shapeIndex2 = 2;
                    IO.println("New min 2 found:      MinPointIndex " + minDistPointIndex2 + "   MinLineIndex " + minDistLineIndex2 + "   MinDist " + tempMinDist);
                }
            }

        }
        IO.println("");
        IO.println("Contact Points round 2:");
        for  (int i = 0; i < objA.rel.length; i++) {
            for (int j = 0; j < objB.rel.length; j++) {
                tempNormDist = Vec.normDist(objB.rel[j].add(objB.pos), objA.rel[i].add(objA.pos), objA.rel[(i+1)%objA.rel.length].add(objA.pos),true);
                contactBuild = Vec.distToNormDistPoint(objB.rel[j].add(objB.pos), objA.rel[i].add(objA.pos), objA.rel[(i+1)%objA.rel.length].add(objA.pos), true, tempNormDist);

                if ((contactBuild + tolerance) < tempMinDist) {
                    minDistPointIndex2 = -1;
                    shapeIndex2 = -1;
                    minDistPointIndex1 = j;
                    minDistLineIndex1 = i;
                    shapeIndex1 = 1;
                    tempMinDist = contactBuild;
                    IO.println("New min 1 found:      MinPointIndex " + minDistPointIndex1 + "   MinLineIndex " + minDistLineIndex1 + "   MinDist " + tempMinDist);
                } else if ((contactBuild - tolerance) < tempMinDist && minDistPointIndex1 != j && minDistPointIndex2 != j) {
                    minDistPointIndex2 = j;
                    minDistLineIndex2 = i;
                    shapeIndex2 = 1;
                    IO.println("New min 2 found:      MinPointIndex " + minDistPointIndex2 + "   MinLineIndex " + minDistLineIndex2 + "   MinDist " + tempMinDist);
                }

            }
        }
        Vec contact;
        Vec contact1;
        Vec contact2 = null;
        IO.println("");
        IO.println("Shape A index: " + objA.index + "   Shape A length: " + objA.rel.length + "   Shape B index: " + objB.index + "   Shape B length: " + objB.rel.length);
        IO.println("MinDistPointIndex1: " + minDistPointIndex1 + "   ShapeIndex1: " + shapeIndex1 + "   MinDistPointIndex2: " + minDistPointIndex2 + "   ShapeIndex2: " + shapeIndex2);
        if (shapeIndex1 == 1) {
            contact1 = objB.rel[minDistPointIndex1].add(objB.pos);
        } else if (shapeIndex1 == 2) {
            contact1 = objA.rel[minDistPointIndex1].add(objA.pos);
        } else {
            IO.println("ERROR: No contact point found");
            return null;
        }
        if (shapeIndex2 == 1) {
            contact2 = objB.rel[minDistPointIndex2].add(objB.pos);
        } else if (shapeIndex2 == 2) {
            contact2 = objA.rel[minDistPointIndex2].add(objA.pos);
        }
        if (shapeIndex2 == -1) {
            contact = contact1;
        } else {
            contact = Vec.div(contact1.add(contact2),2);
        }
        Stage.drawn.points.add(new Vec.coloredVec(contact1, new Color(20, 66, 6, 255)));
        if  (shapeIndex2 != -1) {
            Stage.drawn.points.add(new Vec.coloredVec(contact2, new Color(20, 66, 6, 255)));
        }
        Stage.drawn.points.add(new Vec.coloredVec(contact, new Color(61, 255, 0, 255)));
        return contact;
    }

    private static void applyImpulse(Object objA, Object objB, Vec MTV, Vec r) {
        //contact and "r" are the same thing            MTV and simplexDist are the same thing

        Vec normal = MTV.normalize();
        Vec ra = r.sub(objA.pos);
        Vec rb = r.sub(objB.pos);
        Vec raPerp = new Vec(0-ra.y, ra.x);
        Vec rbPerp = new Vec(0-rb.y, rb.x);
        Vec relVel = TaterMath.relVel(objB, rbPerp).sub(TaterMath.relVel(objA, raPerp));
        double raPerpDotN = Vec.dot(raPerp, normal);
        double rbPerpDotN = Vec.dot(rbPerp, normal);
        double denominator = (objA.inverseMass + objB.inverseMass) + ((raPerpDotN * raPerpDotN) * objA.inverseInertia) + ((rbPerpDotN * rbPerpDotN) * objB.inverseInertia);
        if (denominator == 0) {
            return;
        }
        double relVelDotN = Vec.dot(relVel, normal);
        if (relVelDotN > 0) {
            return;
        }
        double j = (-(1 + (objA.restitution * objB.restitution)) * relVelDotN) / denominator;
        Vec impulse = normal.mul(j);
        objA.velChange = objA.velChange.sub(impulse.mul(objA.inverseMass));
        objA.angularVelChange -= objA.inverseInertia * Vec.cross(ra, impulse);
        objB.velChange = objB.velChange.add(impulse.mul(objB.inverseMass));
        objB.angularVelChange += objB.inverseInertia * Vec.cross(rb, impulse);

        //Missing the static seperators or whatever
    }

    private static void DrawSimplex(ArrayList<Vec> simplexList, Color color, float width) {
        for (int i = 0; i < simplexList.size(); i++) {
            if (simplexList.get(i) != null) {
                Stage.drawn.addCenteredLine(new Vec.coloredLine(simplexList.get(i), simplexList.get((i + 1) % simplexList.size()), color, width));
            }
        }
    }

    /// debug tool to help visualize the possible points, but generated in the worst possible way lol. hope this is disabled later or the lag will be crazy
    private static void generateAndDisplayAllOutsidePoints(Object objA, Object objB) {
        Vec firstPoint = null;
        Vec oldPoint = null;
        for (int i = 0; i < 360; i++) {
            Vec dir = Vec.fromAngWithLen(Math.toRadians(i), 100);
            Vec point = findGJKPoint(objA, objB, dir);
            if (firstPoint == null) {
                firstPoint = point;
                oldPoint = point;
                Stage.drawn.addCenteredPoint(new Vec.coloredVec(point, Options.GJK.render.generateAndDisplayAllOutsidePointsColor));
            } else if (!point.equals(firstPoint) && !point.equals(oldPoint)) {
                oldPoint = point;
                Stage.drawn.addCenteredPoint(new Vec.coloredVec(point, Options.GJK.render.generateAndDisplayAllOutsidePointsColor));
            }
        }
    }
}