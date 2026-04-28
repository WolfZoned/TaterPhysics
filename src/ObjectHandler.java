import java.awt.*;
//import java.lang.Object;
import java.util.ArrayList;

public class ObjectHandler {
    private static final double EPAPointEpsilon = 1e-6;
    private static final double EPAConvergenceEpsilon = 1e-5;
    private static final double EPAMinEdgeLenSquared = 1e-12;
    private static final double EPAMinNormalLenSquared = 1e-12;
    private static final double EPAMaxMTVOverlap = 1.5;

    public static final double inactiveRange = 1000;

    public volatile static int activeShapeCount;

    public static class CollisionStore {
        public Object objA;
        public Object objB;
        public Vec normal;
        public Vec contact;
        public double j;
        public Vec relVel;
        public Vec raPerp;
        public Vec rbPerp;
        public Vec objAPos;
        public Vec objBPos;

        public CollisionStore(Object objA, Object objB, Vec normal, Vec contact, double j, Vec relVel, Vec raPerp, Vec rbPerp, Vec objAPos, Vec objBPos) {
            this.objA = objA;
            this.objB = objB;
            this.normal = normal;
            this.contact = contact;
            this.j = j;
            this.relVel = relVel;
            this.raPerp = raPerp;
            this.rbPerp = rbPerp;
            this.objAPos = objAPos;
            this.objBPos = objBPos;
        }

    }

    public static ArrayList<Object> createInitialObjects() {
        ArrayList<Object> objects = new ArrayList<>();
        objects.add(Object.createRectangle(new Color(50, 0, 250), 575, 1200, 0.0, 1100, 1100, new Vec(0, 0), true, false));
        //objects.get(0).angularVel = 0.01;
        objects.add(Object.createRectangle(Options.colors.ORANGE, 450, 550, 0.0, 30, 30, new Vec(0, 0), false, true));
        objects.add(Object.createPolygon(Options.colors.CYAN, 400, 550, 0.0, 30, 30, 5, new Vec(0, 0.3), false, false));
        objects.add(Object.createPolygon(Options.colors.LIGHT_GREEN, 400, 550, 0.0, 60, 60, 3, new Vec(0, 0.3), false, false));
        objects.add(Object.createPolygon(Options.colors.SKY, 450, 500, 0.0, 60, 60, 8, new Vec(0, 0.3), false, false));
        objects.add(Object.createCircle(Options.colors.YELLOW, 500, 500, 0.0, 30, new Vec(0, 0.3), false, false));
        activeShapeCount = objects.size();
        return objects;
    }

    private static void updateActive(Vec stageSize) {
        int activeCount = 0;
        for (Object obj : Stage.objects) {
            if (obj != null) {
                if (obj.updateActive(stageSize, inactiveRange)) {
                    activeCount++;
                }
            }
        }
        activeShapeCount = activeCount;
    }

    public static int mouseHoveredObject(Vec mousePos, ArrayList<Object> objects) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            Object obj = objects.get(i);
            if (obj != null) {
                if (obj.type.equals("polygon")) {
                    if (obj.isPointInside(mousePos)) {
                        return i;
                    }
                } else if (obj.type.equals("circle")) {
                    if (Vec.len(mousePos, obj.pos) <= obj.radius) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static void collisionCalcs(ArrayList<Object> objects) {

        updateActive(new Vec(Stage.drawn.stageWidth, Stage.drawn.stageHeight));

        ArrayList<CollisionStore> collisionStorage = new ArrayList<>();
        for (Object obj : objects) {
            if (obj != null && obj.active) {
                if  (obj.type.equals("polygon")) {
                    obj.boundingBox.polygonCalc(obj.rel, obj.pos);
                } else if (obj.type.equals("circle")) {
                    obj.boundingBox.circleCalc(obj.radius, obj.pos);
                }
            }
        }
        for (int i = 0; i < objects.size(); i++) {
            Object objA = objects.get(i);
            if (objA != null && objA.active) {
                for (int j = i + 1; j < objects.size(); j++) {
                    Object objB = objects.get(j);
                    if (objB != null && objB.active) {
                        if (!objA.linearStatic || !objB.linearStatic || !objA.angularStatic || !objB.angularStatic) {
                            CollisionStore temp = null;
                            if (objA.type.equals("circle") && objB.type.equals("circle")) {
                                temp = circleCollision(objA, objB);
                            } else {
                                temp = polygonCollision(objA, objB);
                            }
                            //} else if (objA.type.equals("circle") && objB.type.equals("polygon") || objA.type.equals("polygon") && objB.type.equals("circle")) {
                            //} else if (objA.type.equals("circle") && objB.type.equals("circle")) {
                            //}
                            if (temp != null) {
                                collisionStorage.add(temp);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < collisionStorage.size(); i++) {
            applyFrictionImpulse(collisionStorage.get(i).objA, collisionStorage.get(i).objB, collisionStorage.get(i).normal, collisionStorage.get(i).contact, collisionStorage.get(i).j, collisionStorage.get(i).relVel, collisionStorage.get(i).raPerp, collisionStorage.get(i).rbPerp, collisionStorage.get(i).objAPos, collisionStorage.get(i).objBPos);
        }
    }

    private static CollisionStore polygonCollision(Object objA, Object objB) {
        if (boundingBoxCollision(objA.boundingBox, objB.boundingBox)) {
            synchronized (Stage.DRAWN_LOCK) {
                ArrayList<Vec> simplexList = new ArrayList<>();
                if (GJKCollision(objA, objB, simplexList)) {
                    Vec MTV = EPACollision(objA, objB, simplexList);
                    if (MTV != null) {
                        MTV = clampMTVToOverlap(objA.boundingBox, objB.boundingBox, MTV);
                        if (MTV == null) {
                            objA.boundingBox.boxColor = new Color(100, 0, 255, 255);
                            objB.boundingBox.boxColor = new Color(100, 0, 255, 255);
                            return null;
                        }
                        // make sure MTV points from A to B for consistent correction/impulse direction
                        Vec centerDelta = objB.pos.sub(objA.pos);
                        if (Vec.dot(MTV, centerDelta) < 0) {
                            MTV = MTV.mul(-1);
                        }
                        objA.boundingBox.boxColor = new Color(72, 255, 0, 255);
                        objB.boundingBox.boxColor = new Color(72, 255, 0, 255);
                        //Stage.drawn.addCenteredLine(new Vec.coloredLine(new Vec(0, 0), MTV.normalize().mul(60), new Color(255, 0, 255, 255), 3));
                        //objA.pos = objA.pos.add(MTV.mul(-0.5));
                        //objB.pos = objB.pos.add(MTV.mul(0.5));
                        //IO.println(MTV);
                        //return null;
                        //IO.println("Collision detected with MTV: " + MTV.x + ", " + MTV.y);
                        Vec objAPosChange;// = MTV.mul(-0.5);
                        Vec objBPosChange;// = MTV.mul(0.5);
                        if (objA.linearStatic && !objB.linearStatic) {
                            objAPosChange = new Vec();
                            objBPosChange = MTV;
                        } else if (!objA.linearStatic && objB.linearStatic) {
                            objAPosChange = MTV.mul(-1);
                            objBPosChange = new Vec();
                        } else {
                            // Apply position correction proportionally to inverse mass (mass-weighted)
                            double totalInverseMass = objA.inverseMass + objB.inverseMass;
                            if (totalInverseMass > 0) {
                                double ratioA = objA.inverseMass / totalInverseMass;
                                double ratioB = objB.inverseMass / totalInverseMass;
                                objAPosChange = MTV.mul(-ratioA);
                                objBPosChange = MTV.mul(ratioB);
                            } else {
                                // just in case both are static
                                objAPosChange = MTV.mul(-0.5);
                                objBPosChange = MTV.mul(0.5);
                            }
                        }
                        objA.posChange.increase(objAPosChange);
                        objB.posChange.increase(objBPosChange);
                        Vec contact = findContactPoints(objA, objB, objA.pos.add(objAPosChange), objB.pos.add(objBPosChange));

                        //return null;
                        if (contact != null) {
                            CollisionStore impulseResult = applyImpulse(objA, objB, MTV, contact, objA.pos, objB.pos);
                            if (impulseResult != null) {
                            }
                            return impulseResult;
                        }
                    } else {
                        objA.boundingBox.boxColor = new Color(100, 0, 255, 255);
                        objB.boundingBox.boxColor = new Color(100, 0, 255, 255);
                    }
                }
            }
        }
        return null;
    }

    private static CollisionStore circleCollision(Object objA, Object objB) {
        synchronized (Stage.DRAWN_LOCK) {
            Vec circleOffset = new Vec(objB.pos.x - objA.pos.x, objB.pos.y - objA.pos.y);
            double circleOverlap = objA.radius + objB.radius - circleOffset.len();
            if (circleOverlap > 0) {
                Vec MTV = circleOffset.normalize().mul(circleOverlap);
                Vec objAPosChange;// = MTV.mul(-0.5);
                Vec objBPosChange;// = MTV.mul(0.5);
                if (objA.linearStatic && !objB.linearStatic) {
                    objAPosChange = new Vec();
                    objBPosChange = MTV;
                } else if (!objA.linearStatic && objB.linearStatic) {
                    objAPosChange = MTV.mul(-1);
                    objBPosChange = new Vec();
                } else {
                    // Apply position correction proportionally to inverse mass (mass-weighted)
                    double totalInverseMass = objA.inverseMass + objB.inverseMass;
                    if (totalInverseMass > 0) {
                        double ratioA = objA.inverseMass / totalInverseMass;
                        double ratioB = objB.inverseMass / totalInverseMass;
                        objAPosChange = MTV.mul(-ratioA);
                        objBPosChange = MTV.mul(ratioB);
                    } else {
                        // just in case both are static
                        objAPosChange = MTV.mul(-0.5);
                        objBPosChange = MTV.mul(0.5);
                    }
                }
                objA.posChange.increase(objAPosChange);
                objB.posChange.increase(objBPosChange);

                Vec contact = objA.pos.add(MTV.mul(objA.radius)).sub(MTV.div(2));

                //return null;
                if (contact != null) {
                    return applyImpulse(objA, objB, MTV, contact, objA.pos, objB.pos);
                }
            }
        }
        return null;
    }

    private static Vec clampMTVToOverlap(Object.BoundingBox bbA, Object.BoundingBox bbB, Vec mtv) {
        if (!isFiniteVec(mtv)) {
            return null;
        }
        double overlapX = Math.min(bbA.xMax, bbB.xMax) - Math.max(bbA.xMin, bbB.xMin);
        double overlapY = Math.min(bbA.yMax, bbB.yMax) - Math.max(bbA.yMin, bbB.yMin);
        if (overlapX <= 0 || overlapY <= 0) {
            return null;
        }
        double maxReasonableMtv = Math.hypot(overlapX, overlapY) * EPAMaxMTVOverlap;
        double mtvLen = mtv.len();
        if (!Double.isFinite(mtvLen)) {
            return null;
        }
        if (mtvLen <= maxReasonableMtv + EPAPointEpsilon) {
            return mtv;
        }
        Vec dir = mtv.normalize();
        if (!isFiniteVec(dir)) {
            return null;
        }
        return dir.mul(maxReasonableMtv);
    }

    private static boolean boundingBoxCollision(Object.BoundingBox bbA, Object.BoundingBox bbB) {
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
        if (Options.GJK.debug) {System.out.println("");}
        if (Options.GJK.debug) {System.out.println("New GJK Collision Check");}
        simplexList.clear();
        simplexList.add(null);
        simplexList.add(null);
        simplexList.add(null);
        Vec direction = Vec.sub(objB.pos, objA.pos);
        if (Vec.dot(direction, direction) <= EPAPointEpsilon) {
            direction = new Vec(1, 0);
        }
        checkGJKPoint(objA, objB, direction, simplexList, 0, "no return", 0.0);
        if (!isFiniteVec(simplexList.get(0)) || Vec.dot(simplexList.get(0), direction) <= 0) {
            if (Options.GJK.debug) {System.out.println("failed on first point");}
            return false;
        }
        if (!checkGJKPoint(objA, objB, simplexList.get(0).mul(-1), simplexList, 1, "insert second check style here", 0.0)) {
            if (Options.GJK.debug) {System.out.println("failed on second point");}
            return false;
        }
        // Calculate perpendicular direction towards origin
        Vec perpDir = Vec.dirOfPerpendicularBisector(simplexList.get(0), simplexList.get(1));
        if (perpDir == null) {
            return false;
        }
        // Check if the perpendicular points towards the origin, if not, flip it
        Vec midpoint = new Vec((simplexList.get(0).x + simplexList.get(1).x) / 2, (simplexList.get(0).y + simplexList.get(1).y) / 2);
        Vec toOrigin = new Vec(-midpoint.x, -midpoint.y);
        if (Vec.dot(perpDir, toOrigin) < 0) {
            // flip direction
            perpDir = Vec.dirOfPerpendicularBisector(simplexList.get(0), simplexList.get(1), true);
        }
        if (!checkGJKPoint(objA, objB, perpDir, simplexList, 2, "insert third check style here", 0.0)) {
            if (Options.GJK.debug) { System.out.println("failed on third point"); }
            return false; //the original had the simplexes reversed and then negated it, so i just changed the order of subtraction here - ok this may still be backwards
        }

        if (!TaterMath.isClockwise(simplexList)) {
            Vec temp = simplexList.get(1);
            simplexList.set(1, simplexList.get(2));
            simplexList.set(2, temp);
        }

        boolean check1Passed;
        boolean check2Passed;
        boolean check3Passed;
        for (int i = 0; i < 20; i++) {
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(2), simplexList.get(0), false) == Vec.isPointRightOfLine(simplexList.get(1), simplexList.get(2), simplexList.get(0), false)) {
                check1Passed = true;
                if (Options.GJK.debug) {System.out.println("check 1 passed");}
            } else {
                check1Passed = false;
                if (Options.GJK.debug) {System.out.println("check 1 failed");}
                if (!checkGJKPoint(objA, objB, Vec.dirOfPerpendicularBisector(simplexList.get(2), simplexList.get(0)), simplexList, 1, "insert third check style here", 0.0)) {
                    if (Options.GJK.debug) {System.out.println("failed on loop check 1");}
                    return false;
                }
            }
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(0), simplexList.get(1), false) == Vec.isPointRightOfLine(simplexList.get(2), simplexList.get(0), simplexList.get(1), false)) {
                check2Passed = true;
                if (Options.GJK.debug) {System.out.println("check 2 passed");}
            } else {
                check2Passed = false;
                if (Options.GJK.debug) {System.out.println("check 2 failed");}
                if (!checkGJKPoint(objA, objB, Vec.dirOfPerpendicularBisector(simplexList.get(0), simplexList.get(1)), simplexList, 2, "insert third check style here", 0.0)) {
                    if (Options.GJK.debug) {System.out.println("failed on loop check 2");}
                    return false;
                }
            }
            if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(1), simplexList.get(2), false) == Vec.isPointRightOfLine(simplexList.get(0), simplexList.get(1), simplexList.get(2), false)) {
                check3Passed = true;
                if (Options.GJK.debug) {System.out.println("check 3 passed");}
            } else {
                check3Passed = false;
                if (Options.GJK.debug) {System.out.println("check 3 failed");}
                if (!checkGJKPoint(objA, objB, Vec.dirOfPerpendicularBisector(simplexList.get(1), simplexList.get(2)), simplexList, 0, "insert third check style here", 0.0)) {
                    if (Options.GJK.debug) {System.out.println("failed on loop check 3");}
                    return false;
                }
            }
            if (check1Passed && check2Passed && check3Passed) {
                if (!originInsideTriangle(simplexList.get(0), simplexList.get(1), simplexList.get(2))) {
                    continue;
                }
                if (Options.GJK.debug) {System.out.println("Successful collision detected");}
                if (Options.GJK.render.drawSuccessfulSimplex) {
                    DrawSimplex(simplexList, Options.GJK.render.successfulSimplexColor, 2.5f);
                }
                return true;
            }
        }
        if (Options.GJK.debug) {
            System.out.println("gjk iteration expired, listing final check pass/fails");
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(2), simplexList.get(0), false) == Vec.isPointRightOfLine(simplexList.get(1), simplexList.get(2), simplexList.get(0), false)) {
            System.out.println("check 1 passed"); } else { System.out.println("check 1 failed");}
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(0), simplexList.get(1), false) == Vec.isPointRightOfLine(simplexList.get(2), simplexList.get(0), simplexList.get(1), false)) {
            System.out.println("check 2 passed"); } else {  System.out.println("check 2 failed");}
        if (Vec.isPointRightOfLine(new Vec(0, 0), simplexList.get(1), simplexList.get(2), false) == Vec.isPointRightOfLine(simplexList.get(0), simplexList.get(1), simplexList.get(2), false)) {
            System.out.println("check 3 passed"); } else { System.out.println("check 3 failed");}
        }
        if (Options.GJK.render.drawFailedSimplex) {
            DrawSimplex(simplexList, Options.GJK.render.failedSimplexColor, 2.5f);
        }
        return false;
    }

    private static Vec EPACollision(Object objA, Object objB, ArrayList<Vec> simplexList) {
        //do a better job of calculating clockwise/counterclockwise inside gjk - determine when and if it switches
        //counterpoint: check if they are clockwise now? don't touch whats not broken (at least i don't think it is???)
        //System.out.println("");
        if (Options.EPA.debug) {System.out.println("Starting EPA");}
        int minDistLineIndex;
        double minDist;
        Vec minDistVec;
        Vec minDistLineStartingVec;
        Vec minDistLineEndingVec;
        boolean clockwise = TaterMath.isClockwise(simplexList);
        if (Options.GJK.debug) {System.out.println("Original clockwise: " + clockwise + "but is now clockwise");}
        if (!clockwise) {
            Vec[] clockwiseVecs = new Vec[simplexList.size()];
            for (int i = 0; i < clockwiseVecs.length; i++) {
                clockwiseVecs[i] = simplexList.get(simplexList.size() - 1 - i);
            }
            for (int i = 0; i < clockwiseVecs.length; i++) {
                simplexList.set(i, clockwiseVecs[i]);
            }
        }
        int EPAAttempts = 30;
        Vec tempVec;
        double tempVecDist;
        Vec newVec;

        for  (int i = 0; i < EPAAttempts; i++) {
            if (Options.GJK.debug) {System.out.println("New Attempt");}
            minDist = Double.MAX_VALUE;
            minDistLineIndex = -1;
            minDistVec = null;
            for  (int EPACounter = 0; EPACounter < simplexList.size(); EPACounter++) {
                Vec a = simplexList.get(EPACounter);
                Vec b = simplexList.get((EPACounter + 1) % simplexList.size());
                Vec edge = b.sub(a);
                if (Vec.dot(edge, edge) <= EPAMinEdgeLenSquared) {
                    continue;
                }
                tempVec = Vec.normDistPoint(new Vec(0, 0), a, b, true);
                tempVecDist = tempVec.len();
                if (tempVecDist < minDist) {
                    minDistVec = tempVec;
                    minDist = tempVecDist;
                    minDistLineIndex = EPACounter;
                }
            }
            if (minDistLineIndex == -1 || minDistVec == null || !isFiniteVec(minDistVec)) {
                return null;
            }
            minDistLineStartingVec = simplexList.get(minDistLineIndex);
            minDistLineEndingVec = simplexList.get((minDistLineIndex + 1) % simplexList.size());
            if (Options.EPA.render.drawMinDistVec) { Stage.drawn.addCenteredPoint(new Vec.coloredVec(minDistVec, new Color(255, 255, 0, 255))); }
            if (Options.EPA.debug) {System.out.println("Closest Line: " + minDistLineStartingVec.x + " " + minDistLineStartingVec.y + "      " + minDistLineEndingVec.x + " " + minDistLineEndingVec.y);}
            double minDistLenSq = Vec.dot(minDistVec, minDistVec);
            if (minDistLenSq <= EPAMinNormalLenSquared) {
                if (Options.EPA.render.drawFailedSimplex) {
                    DrawSimplex(simplexList, Options.EPA.render.failedSimplexColor, 1f);
                }
                return null;
            }

            Vec searchDir = minDistVec.normalize();
            newVec = findGJKPoint(objA, objB, searchDir);
            if (!isFiniteVec(newVec)) {
                return null;
            }

            double edgeDistance = Vec.dot(minDistVec, searchDir);
            double supportDistance = Vec.dot(newVec, searchDir);
            // Converged: advancing support point no longer moves the closest edge meaningfully.
            if (supportDistance - edgeDistance <= EPAConvergenceEpsilon) {
                if (Options.EPA.render.drawSucessfulSimplex) {
                    Stage.drawn.addCenteredPoint(new Vec.coloredVec(newVec, new Color(0, 255, 255, 255)));
                    DrawSimplex(simplexList, Options.EPA.render.successfulSimplexColor, 1f);
                }
                return minDistVec;
            }

            // Repeated support point means no usable expansion left; use current closest edge.
            if (approximatelyEqual(newVec, minDistLineStartingVec) || approximatelyEqual(newVec, minDistLineEndingVec)) {
                return minDistVec;
            }

            if (Options.EPA.render.drawNewVec) { Stage.drawn.addCenteredPoint(new Vec.coloredVec(newVec, new Color(255, 0, 0, 255))); }
            simplexList.add(minDistLineIndex + 1, newVec);

        }
        if (Options.EPA.render.drawFailedSimplex) {
            DrawSimplex(simplexList, Options.EPA.render.failedSimplexColor, 1f);
        }
        return null;
    }

    private static boolean approximatelyEqual(Vec a, Vec b) {
        return Math.abs(a.x - b.x) <= EPAPointEpsilon && Math.abs(a.y - b.y) <= EPAPointEpsilon;
    }

    private static Vec safeNormalize(Vec v) {
        double len = v.len();
        if (!Double.isFinite(len) || len <= EPAPointEpsilon) {
            return null;
        }
        return v.div(len);
    }

    /// checks to make sure it's a real number or whatever
    private static boolean isFiniteVec(Vec v) {
        return v != null && Double.isFinite(v.x) && Double.isFinite(v.y);
    }

    private static boolean originInsideTriangle(Vec a, Vec b, Vec c) {
        double ab = Vec.cross(b.sub(a), a.mul(-1));
        double bc = Vec.cross(c.sub(b), b.mul(-1));
        double ca = Vec.cross(a.sub(c), c.mul(-1));
        boolean hasNeg = (ab < -EPAPointEpsilon) || (bc < -EPAPointEpsilon) || (ca < -EPAPointEpsilon);
        boolean hasPos = (ab > EPAPointEpsilon) || (bc > EPAPointEpsilon) || (ca > EPAPointEpsilon);
        return !(hasNeg && hasPos);
    }

    private static boolean checkGJKPoint(Object objA, Object objB, Vec direction, ArrayList<Vec> simplexList, int pointIndex, String pointCheckStyle, double dirVecLen) {
        if (direction == null) {return false;}
        Vec support = findGJKPoint(objA, objB, direction);
        if (!isFiniteVec(support)) {
            return false;
        }
        simplexList.set(pointIndex, support); //since the list is already initialized with nulls, we can just replace existing points
        if (Options.GJK.render.drawNewPoint) { Stage.drawn.addCenteredPoint(new Vec.coloredVec(simplexList.get(pointIndex), new Color(2, 237, 2, 255))); }
        double dotProduct = Vec.dot(simplexList.get(pointIndex), direction);
        if (!Double.isFinite(dotProduct) || dotProduct <= 0) {
            return false;
        }
        if (pointCheckStyle.equals("insert second check style here")) {
            double normDistBetweenPointsAndOrigin = Vec.normDist(new Vec(0,0), simplexList.get(0), simplexList.get(1), true);
            return normDistBetweenPointsAndOrigin != 0 && normDistBetweenPointsAndOrigin != 1;
        } else if (pointCheckStyle.equals("insert third check style here")) {
            return true;
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
        Vec pointA = null;
        if (objA.type.equals("polygon")) {
            for (Vec vec : objA.rel) {
                index++;
                //currentMag = Vec.dot(vec, direction);
                currentMag = Vec.dot(vec, direction.mul(-1));
                if (currentMag > highestMagAlongDir) {
                    highestMagAlongDir = currentMag;
                    highestIndex = index - 1;
                }
            }
            pointA = objA.rel[highestIndex].add(objA.pos);
        } else if (objA.type.equals("circle")) {
            Vec dirNormalized = safeNormalize(direction.mul(-1));
            if (!isFiniteVec(dirNormalized)) {
                return null;
            }
            pointA = objA.pos.add(dirNormalized.mul(objA.radius));
        }
        //}
        highestMagAlongDir = -Double.MAX_VALUE;
        highestIndex = -1;
        index = 0;
        Vec pointB = null;
        if (objB.type.equals("polygon")) {
            for (Vec vec : objB.rel) { //check
                index++;
                currentMag = Vec.dot(vec, direction);
                if (currentMag > highestMagAlongDir) {
                    highestMagAlongDir = currentMag;
                    highestIndex = index - 1;
                }
            }
            pointB = objB.rel[highestIndex].add(objB.pos);
        } else if (objB.type.equals("circle")) {
            Vec dirNormalized = safeNormalize(direction);
            if (!isFiniteVec(dirNormalized)) {
                return null;
            }
             pointB = objB.pos.add(dirNormalized.mul(objB.radius));
         }
         if (Options.GJK.debug) {System.out.println("DIRECTION: " + direction + "     New Vec: " + Vec.sub(pointB, pointA));}
         return(Vec.sub(pointB, pointA)); //since the list is already initialized with nulls, we can just replace existing points
    }

    private static Vec findContactPoints(Object objA, Object objB, Vec objAPos, Vec objBPos) {
        double tolerance = 0.6;
        int minDistPointIndex1 = -1;
        int minDistPointIndex2 = -1;
        int minDistLineIndex1 = -1;
        int minDistLineIndex2 = -1;
        int shapeIndex1 = -1;
        int shapeIndex2 = -1;

        double contactDist;
        double tempMinDist = Double.MAX_VALUE;
        double contactNormDist = 0;

        Vec contact = null;
        Vec contact1 = null;
        Vec contact2 = null;
        if (Options.ContactPoints.debug) {
            System.out.println("");
            System.out.println("");
            System.out.println("Contact Points round 1:");
        }
        if (objA.type.equals("polygon") && objB.type.equals("polygon")) {
            for  (int i = 0; i < objB.rel.length; i++) {
                for (int j = 0; j < objA.rel.length; j++) {
                    contactNormDist = Vec.normDist(objA.rel[j].add(objAPos), objB.rel[i].add(objBPos), objB.rel[(i+1)%objB.rel.length].add(objBPos),true);
                    contactDist = Vec.distToNormDistPoint(objA.rel[j].add(objAPos), objB.rel[i].add(objBPos), objB.rel[(i+1)%objB.rel.length].add(objBPos), true, contactNormDist);

                    if ((contactDist + tolerance) < tempMinDist) {
                        minDistPointIndex2 = -1;
                        shapeIndex2 = -1;
                        minDistPointIndex1 = j;
                        minDistLineIndex1 = i;
                         shapeIndex1 = 2;
                         tempMinDist = contactDist;
                         if (Options.ContactPoints.debug) { System.out.println("New min 1 found:      MinPointIndex " + minDistPointIndex1 + "   MinLineIndex " + minDistLineIndex1 + "   MinDist " + tempMinDist); }
                     } else if ((contactDist - tolerance) < tempMinDist && minDistPointIndex1 != j && minDistPointIndex2 != j) {
                         minDistPointIndex2 = j;
                         minDistLineIndex2 = i;
                         shapeIndex2 = 2;
                         if (Options.ContactPoints.debug) { System.out.println("New min 2 found:      MinPointIndex " + minDistPointIndex2 + "   MinLineIndex " + minDistLineIndex2 + "   MinDist " + tempMinDist); }
                     }
                 }
             }
             if (Options.ContactPoints.debug) { System.out.println(""); }
             if (Options.ContactPoints.debug) { System.out.println("Contact Points round 2:"); }
             for  (int i = 0; i < objA.rel.length; i++) {
                 for (int j = 0; j < objB.rel.length; j++) {
                     contactNormDist = Vec.normDist(objB.rel[j].add(objBPos), objA.rel[i].add(objAPos), objA.rel[(i+1)%objA.rel.length].add(objAPos),true);
                     contactDist = Vec.distToNormDistPoint(objB.rel[j].add(objBPos), objA.rel[i].add(objAPos), objA.rel[(i+1)%objA.rel.length].add(objAPos), true, contactNormDist);

                     if ((contactDist + tolerance) < tempMinDist) {
                         minDistPointIndex2 = -1;
                         shapeIndex2 = -1;
                         minDistPointIndex1 = j;
                         minDistLineIndex1 = i;
                         shapeIndex1 = 1;
                         tempMinDist = contactDist;
                         if (Options.ContactPoints.debug) { System.out.println("New min 1 found:      MinPointIndex " + minDistPointIndex1 + "   MinLineIndex " + minDistLineIndex1 + "   MinDist " + tempMinDist); }
                     } else if ((contactDist - tolerance) < tempMinDist && minDistPointIndex1 != j && minDistPointIndex2 != j) {
                         minDistPointIndex2 = j;
                         minDistLineIndex2 = i;
                         shapeIndex2 = 1;
                         if (Options.ContactPoints.debug) { System.out.println("New min 2 found:      MinPointIndex " + minDistPointIndex2 + "   MinLineIndex " + minDistLineIndex2 + "   MinDist " + tempMinDist); }
                     }
                 }
             }
         } else if (objA.type.equals("circle") && objB.type.equals("polygon")) {
             double minContactNormDist = 0.0;
             for  (int i = 0; i < objB.rel.length; i++) {
                 contactNormDist = Vec.normDist(objAPos, objB.rel[i].add(objBPos), objB.rel[(i+1)%objB.rel.length].add(objBPos),true);
                 contactDist = Vec.distToNormDistPoint(objAPos, objB.rel[i].add(objBPos), objB.rel[(i+1)%objB.rel.length].add(objBPos), true, contactNormDist);

                 if ((contactDist + tolerance) < tempMinDist) {
                     minDistLineIndex1 = i;
                     shapeIndex1 = 2;
                     minContactNormDist = contactNormDist;
                     //finds furthest point on circle in directrion of
                     tempMinDist = contactDist;
                     if (Options.ContactPoints.debug) { System.out.println("New min found:      MinPointIndex " + minDistPointIndex1 + "   MinLineIndex " + minDistLineIndex1 + "   MinDist " + tempMinDist); }
                 }
             }
             contact = Vec.normDistPoint(objAPos, objB.rel[minDistLineIndex1].add(objBPos), objB.rel[(minDistLineIndex1+1)%objB.rel.length].add(objBPos), true, minContactNormDist);
             if (Options.ContactPoints.debug) {
                 System.out.println(contact.toString());
                 System.out.println("Contact Points Circle 1");
             }
         } else if (objA.type.equals("polygon") && objB.type.equals("circle")) {
             double minContactNormDist = 0.0;
             for  (int i = 0; i < objA.rel.length; i++) {
                 contactNormDist = Vec.normDist(objBPos, objA.rel[i].add(objAPos), objA.rel[(i+1)%objA.rel.length].add(objAPos),true);
                 contactDist = Vec.distToNormDistPoint(objBPos, objA.rel[i].add(objAPos), objA.rel[(i+1)%objA.rel.length].add(objAPos), true, contactNormDist);

                 if ((contactDist + tolerance) < tempMinDist) {
                     minDistLineIndex1 = i;
                     shapeIndex1 = 1;
                     minContactNormDist = contactNormDist;
                     tempMinDist = contactDist;
                     if (Options.ContactPoints.debug) { System.out.println("New min found:      MinPointIndex " + minDistPointIndex1 + "   MinLineIndex " + minDistLineIndex1 + "   MinDist " + tempMinDist); }
                 }
             }
             contact = Vec.normDistPoint(objBPos, objA.rel[minDistLineIndex1].add(objAPos), objA.rel[(minDistLineIndex1+1)%objA.rel.length].add(objAPos), true, minContactNormDist);

             if (Options.ContactPoints.debug) {
                 System.out.println(contact.toString());
                 System.out.println("Contact Points Circle 2");
             }
         }
         if (Options.ContactPoints.debug) {
             System.out.println("");
             System.out.println("Shape A index: " + objA.ID + "   Shape B index: " + objB.ID);
             System.out.println("MinDistPointIndex1: " + minDistPointIndex1 + "   ShapeIndex1: " + shapeIndex1 + "   MinDistPointIndex2: " + minDistPointIndex2 + "   ShapeIndex2: " + shapeIndex2);
         }

         if (objA.type.equals("polygon") && objB.type.equals("polygon")) {
             if (shapeIndex1 == 1) {
                 contact1 = objB.rel[minDistPointIndex1].add(objBPos);
             } else if (shapeIndex1 == 2) {
                 contact1 = objA.rel[minDistPointIndex1].add(objAPos);
             } else {
                 if (Options.ContactPoints.debug) { System.out.println("ERROR: No contact point found"); }
                 return null;
             }
             if (shapeIndex2 == 1) {
                 contact2 = objB.rel[minDistPointIndex2].add(objBPos);
             } else if (shapeIndex2 == 2) {
                 contact2 = objA.rel[minDistPointIndex2].add(objAPos);
             }
             if (shapeIndex2 == -1) {
                 contact = contact1;
             } else {
                 contact = Vec.div(contact1.add(contact2),2);
             }
         }
         if (Options.ContactPoints.debug) { System.out.println("Shape A Type: " + objA.type + "   Shape B Type: " + objB.type); }
        if  (shapeIndex2 != -1) {
            if (Options.ContactPoints.render.drawDoubleContactPoints && contact1 != null) { Stage.drawn.points.add(new Vec.coloredVec(contact1, new Color(20, 66, 6, 255))); }
            if (Options.ContactPoints.render.drawDoubleContactPoints && contact2 != null) { Stage.drawn.points.add(new Vec.coloredVec(contact2, new Color(20, 66, 6, 255))); }
        }
        if (Options.ContactPoints.render.drawMainContactPoint) { Stage.drawn.points.add(new Vec.coloredVec(contact, new Color(61, 255, 0, 255))); }
        return contact;
    }

    private static CollisionStore applyImpulse(Object objA, Object objB, Vec MTV, Vec r, Vec objAPos, Vec objBPos) {
        //contact and "r" are the same thing           MTV and simplexDist are the same thing
        Vec normal = MTV.normalize();
        Vec ra = r.sub(objAPos);
        Vec rb = r.sub(objBPos);
        Vec raPerp = new Vec(0-ra.y, ra.x);
        Vec rbPerp = new Vec(0-rb.y, rb.x);
        Vec relVel = TaterMath.relVel(objB, rbPerp).sub(TaterMath.relVel(objA, raPerp));
        double raPerpDotN = Vec.dot(raPerp, normal);
        double rbPerpDotN = Vec.dot(rbPerp, normal);
        double denominator = (objA.inverseMass + objB.inverseMass) + ((raPerpDotN * raPerpDotN) * objA.inverseInertia) + ((rbPerpDotN * rbPerpDotN) * objB.inverseInertia);
        if (denominator == 0) {
            return null;
        }
        double relVelDotN = Vec.dot(relVel, normal); //contact vel mag
        if (relVelDotN > 0) {
            return null;
        }
        double j = (-(1 + (objA.restitution * objB.restitution)) * relVelDotN) / denominator;
        Vec impulse = normal.mul(j);
        //if (objA.staticObj) {
        //    objB.velChange = objB.velChange.add(impulse.mul(objB.inverseMass).mul(2));
        //    objB.angularVelChange += objB.inverseInertia * Vec.cross(rb, impulse) * 2;
        //} else if (objB.staticObj) {
        //    objA.velChange = objA.velChange.sub(impulse.mul(objA.inverseMass).mul(2));
        //    objA.angularVelChange -= objA.inverseInertia * Vec.cross(ra, impulse) * 2;
        //} else {
        //Stage.drawn.addCenteredLine(new Vec.coloredLine(new Vec(0,0), MTV.normalize().mul(60), new Color(255, 0, 255, 255), 3));
            objA.velChange = objA.velChange.sub(impulse.mul(objA.inverseMass));
            objA.angularVelChange -= objA.inverseInertia * Vec.cross(ra, impulse);
            objB.velChange = objB.velChange.add(impulse.mul(objB.inverseMass));
            objB.angularVelChange += objB.inverseInertia * Vec.cross(rb, impulse);
        //IO.println("Shape A inverse mass: " + objA.inverseMass + "   Shape B inverse mass: " + objB.inverseMass);
        //}
        return new CollisionStore(objA, objB, normal, r, j, relVel, raPerp, rbPerp, objAPos, objBPos); //Missing the static seperators or whatever
    }

    private static void applyFrictionImpulse(Object objA, Object objB, Vec normal, Vec contact, double j, Vec relVel, Vec raPerp, Vec rbPerp, Vec objAPos, Vec objBPos) {
        Vec tangent = TaterMath.tangent(relVel, normal); //tangent is used in place of normal
        if (Math.abs(tangent.x) < 0.0001 && Math.abs(tangent.y) < 0.0001) {
            return;
        }
        tangent = tangent.normalize();
        double kineticFriction = objA.kineticFriction * objB.kineticFriction;
        double staticFriction = objA.staticFriction * objB.staticFriction;

        double raPerpDotN = Vec.dot(raPerp, tangent);
        double rbPerpDotN = Vec.dot(rbPerp, tangent);
        double denominator = (objA.inverseMass + objB.inverseMass) + ((raPerpDotN * raPerpDotN) * objA.inverseInertia) + ((rbPerpDotN * rbPerpDotN) * objB.inverseInertia);
        if (Math.abs(denominator) < 1e-12) {
            return;
        }
        double jFriction = -relVel.dot(tangent) / denominator;
        Vec impulse;
        if (Math.abs(jFriction) <= (j * staticFriction)) {
            impulse = tangent.mul(jFriction);
        } else {
            impulse = tangent.mul(-Math.signum(relVel.dot(tangent)) * j * kineticFriction);
        }
        objA.velChange = objA.velChange.sub(impulse.mul(objA.inverseMass));
        objA.angularVelChange -= objA.inverseInertia * Vec.cross(contact.sub(objAPos), impulse);
        objB.velChange = objB.velChange.add(impulse.mul(objB.inverseMass));
        objB.angularVelChange += objB.inverseInertia * Vec.cross(contact.sub(objBPos), impulse);
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
