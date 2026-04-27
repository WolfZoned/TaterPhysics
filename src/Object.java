import java.awt.*;
import java.util.Objects;

public class Object implements Cloneable {
    Vec pos = new Vec(); //position of center of mass

    public int ID;
    public static int lastID;

    public double rotation; //radians
    public String type;
    public Color color;

    public boolean collidable;
    public boolean active;
    public boolean linearStatic;
    public boolean angularStatic;
    public boolean outlined;

    public Vec[] rel;
    private Vec[] setRel;
    public Vec[] bindRel;
    private Vec[] setBindRel;

    public double radius; //for circles only

    public BoundingBox boundingBox;

    public double area;
    public double density;
    public double kineticFriction;
    public double staticFriction;
    public double restitution;

    public Vec setAcceleration;

    public Vec vel; //linear velocity
    public double angularVel; //angular velocity
    public double setAngularVel;

    public Vec velChange;
    public double angularVelChange;
    public Vec posChange;

    public double inertia; //mass moment of inertia
    public double inverseInertia;
    public double mass;
    public double inverseMass;

    static double oldRotation;

    /// polygon constructor
    public Object(Color color, double x, double y, double rotation, Vec[] points, Vec setAcceleration, boolean linearStatic, boolean angularStatic) { //polygon shape
        //make sure all shapes are counter-clockwise for now
        this.pos = new Vec(x, y);
        this.vel = new Vec(0.0, 0.0);
        this.rotation = rotation;
        this.angularVel = 0;
        this.type = "polygon";
        this.color = color;
        this.density = 1.0;
        this.outlined = false;
        this.ID = lastID++;
        lastID = ID;
        this.boundingBox = new BoundingBox();
        this.setRel = points;
        this.rel = Vec.copyArray(this.setRel);
        this.velChange = new Vec(0.0, 0.0);
        this.angularVelChange = 0.0;
        this.posChange = new Vec(0.0, 0.0);
        this.kineticFriction = 0.7;
        this.staticFriction = 0.85;
        this.setAcceleration = setAcceleration;
        this.linearStatic = linearStatic;
        this.angularStatic = angularStatic;
        this.restitution = 0.7;

        if (!linearStatic || !angularStatic) {
            polygonPropertiesCalc(this.setRel, 1.0, this.density);
        }
        if (this.linearStatic) {
            this.mass = Double.POSITIVE_INFINITY;
            this.inverseMass = 0;
        } else {
            if (this.mass != 0) {
                this.inverseMass = 1.0 / this.mass;
            } else {
                this.inverseMass = 0.0;
            }
        }
        if  (this.angularStatic) {
            this.inertia = Double.POSITIVE_INFINITY;
            this.inverseInertia = 0;
        } else {
            if (this.inertia != 0) {
                this.inverseInertia = 1.0 / this.inertia;
            } else {
                this.inverseInertia = 0.0;
            }
        }
        if (Options.Object.logCreation) {
            IO.println("mass: " + this.mass + ", inertia: " + this.inertia);
            IO.println("inverse mass: " + this.inverseMass + ", inverse inertia: " + this.inverseInertia);
            IO.println("area: " + this.area);
        }
    }

    /// circle constructor
    public Object(Color color, double x, double y, double rotation, double radius, Vec setAcceleration, boolean linearStatic, boolean angularStatic) { //polygon shape
        //make sure all shapes are counter-clockwise for now
        this.pos = new Vec(x, y);
        this.vel = new Vec(0.0, 0.0);
        this.rotation = rotation;
        this.angularVel = 0;
        this.type = "circle";
        this.color = color;
        this.density = 1.0;
        this.outlined = false;
        this.ID = lastID++;
        lastID = ID;
        this.radius = radius;
        this.boundingBox = new BoundingBox();
        this.velChange = new Vec(0.0, 0.0);
        this.angularVelChange = 0.0;
        this.posChange = new Vec(0.0, 0.0);
        this.kineticFriction = 0.7;
        this.staticFriction = 0.85;
        this.setAcceleration = setAcceleration;
        this.linearStatic = linearStatic;
        this.angularStatic = angularStatic;
        this.restitution = 0.7;

        this.area = radius * radius * Math.PI;

        if (this.linearStatic) {
            this.mass = Double.POSITIVE_INFINITY;
            this.inverseMass = 0;
        } else {
            this.mass = this.area * this.density;
            if (this.mass != 0) {
                this.inverseMass = 1.0 / this.mass;
            } else {
                this.inverseMass = 0.0;
            }
        }
        if  (this.angularStatic) {
            this.inertia = Double.POSITIVE_INFINITY;
            this.inverseInertia = 0;
        } else {
            this.inertia = this.mass * radius * radius / 2;
            if (this.inertia != 0) {
                this.inverseInertia = 1.0 / this.inertia;
            } else {
                this.inverseInertia = 0.0;
            }
        }
        if (Options.Object.logCreation) {
            IO.println("mass: " + this.mass + ", inertia: " + this.inertia);
            IO.println("inverse mass: " + this.inverseMass + ", inverse inertia: " + this.inverseInertia);
        }
    }

    public static Object createRectangle(Color color, double x, double y, double rotation, double width, double height, Vec setAcceleration, boolean linearStatic, boolean angularStatic) {//rectangle
        return new Object(color, x, y, rotation, new Vec[] {
                        new Vec(width / 2, height / 2),
                        new Vec(width / 2, height / -2),
                        new Vec(width / -2, height / -2),
                        new Vec(width / -2, height / 2)}
                , setAcceleration, linearStatic, angularStatic);
    }

    public static Object createCircle(Color color, double x, double y, double rotation, double radius, Vec setAcceleration, boolean linearStatic, boolean angularStatic) {
        return new Object(color, x, y, rotation, radius, setAcceleration, linearStatic, angularStatic);
    }

    public static Object createPolygon(Color color, double x, double y, double rotation, double width, double height, int points, Vec setAcceleration, boolean linearStatic,  boolean angularStatic) {
        return new Object(color, x, y, rotation, generatePolygonPoints(width, height, points), setAcceleration, linearStatic, angularStatic);
    }

    public static Object createCustom(Color color, double x, double y, double rotation, Vec[] points, Vec setAcceleration, boolean linearStatic,  boolean angularStatic) {
        return new Object(color, x, y, rotation, points, setAcceleration, linearStatic, angularStatic);
    }

    private static Vec[] generatePolygonPoints(double width, double height, int points) {
        Vec[] polygonPoints = new Vec[points];
        double angleStep = -2 * Math.PI / points;
        for (int i = 0; i < points; i++) {
            double angle = i * angleStep;
            double x = (width / 2) * Math.cos(angle);
            double y = (height / 2) * Math.sin(angle);
            polygonPoints[i] = new Vec(x, y);
        }
        return polygonPoints;
    }

    public void updateRelativeCoordinates() {
        if (oldRotation != rotation) {
            double cosTheta = Math.cos(this.rotation);
            double sinTheta = Math.sin(this.rotation);
            for (int i = 0; i < setRel.length; i++) {
                rel[i] = setRel[i].rotate(cosTheta, sinTheta);
            }
            this.oldRotation = this.rotation;
        }
    }

    public boolean isPointInside(Vec point) {
        var pos = 0;
        var neg = 0;

        for (var i = 0; i < this.rel.length; i++)
        {
            //If point is in the polygon
            if (rel[i].equals(point))
                return true;

            //And the i+1'th, or if i is the last, with the first point
            var i2 = (i+1) % rel.length;

            //Compute the cross product
            var d = ((point.x - this.pos.x) - rel[i].x)*(rel[i2].y - rel[i].y) - ((point.y - this.pos.y) - rel[i].y)*(rel[i2].x - rel[i].x);

            if (d > 0) pos++;
            if (d < 0) neg++;

            //If the sign changes, then point is outside
            if (pos > 0 && neg > 0)
                return false;
        }

        //If no change in direction, then on same side of all segments, and thus inside
        return true;

        /*int count = rel.length;
        int prev = count - 1;
        boolean inside = false;
        for (int index = 0; index < count; index++) {
            Vec a = Vec.add(rel[prev], pos);
            Vec b = Vec.add(rel[index], pos);
            if (((b.y > point.y) != (a.y > point.y)) &&
                (point.x < (a.x - b.x) * (point.y - b.y) / (a.y - b.y) + b.x)) {
                inside = !inside;
            }
            prev = index;
        }
        return inside;*/
    }

    public int closestPoint(Vec point) {
        double minDist = Double.MAX_VALUE;
        int minDistIndex = -1;
        double tempMinDist;
        for (int i = 0; i < rel.length; i++) {
            tempMinDist = Vec.distance(Vec.add(rel[i], pos), point);
            if (tempMinDist < minDist) {
                minDist = tempMinDist;
                minDistIndex = i;
            }
        }
        return minDistIndex;
    }

    /// sets the mass, area, and inertia of shape - not needed for static shapes
    private void polygonPropertiesCalc(Vec[] rel, double depth, double density) {
        // Accumulate the following values
        double area = 0.0;
        double mass = 0.0;
        Vec center = new Vec(0.0, 0.0);
        double mmoi = 0.0;

        // Take each vertex pair starting from the last-first vertex
        // in order to consider all sides.
        int count = rel.length;
        int prev = count - 1;
        for (int index = 0; index < count; index++) {
            Vec a = rel[prev];
            Vec b = rel[index];

            double area_step = TriangleArea(a, b);
            double mass_step = density * area_step * depth;
            Vec center_step = TriangleCenter(a, b);
            double mmoi_step = TriangleMmoi(a, b, mass_step);

            area += area_step;
            center = Vec.add(Vec.mul(center, mass), Vec.mul(center_step, mass_step)).div(mass + mass_step);
            mass += mass_step;
            mmoi += mmoi_step;

            prev = index;
        }

        // Transfer mass moment of inertia from the origin to the center of mass
        mmoi -= mass * Vec.dot(center, center);

        // use area, mass, center and mmoi
        this.mass = -mass;
        this.area = -area;
        this.inertia = -mmoi;
        for (int i = 0; i < rel.length; i++) {
            rel[i] = Vec.sub(rel[i], center);
        }
    }

    double TriangleArea(Vec a, Vec b) {
        return Vec.cross(a, b) / 2;
    }

    Vec TriangleCenter(Vec a, Vec b) {
        // (a + b) / 3
        return Vec.add(a, b).div(3.0);
    }
    double TriangleMmoi (Vec a, Vec b,double triangleMass)
    {
        return triangleMass / 6 * (Vec.dot(a, a) + Vec.dot(b, b) + Vec.dot(a, b));
    }

    public static class BoundingBox {
        public double xMin;
        public double xMax;
        public double yMin;
        public double yMax;
        public Color boxColor;

        public BoundingBox() {
            this.xMin = 0;
            this.xMax = 0;
            this.yMin = 0;
            this.yMax = 0;
            this.boxColor = new Color(255, 140, 90, 255);
        }

        public void polygonCalc(Vec[] rel, Vec pos) {
            // calculate bounding box from rel coordinates
            xMin = rel[0].x;
            xMax = rel[0].x;
            yMin = rel[0].y;
            yMax = rel[0].y;
            for (Vec relPoint : rel) {
                if (relPoint.x < xMin) {
                    xMin = relPoint.x;
                }
                if (relPoint.x > xMax) {
                    xMax = relPoint.x;
                }
                if (relPoint.y < yMin) {
                    yMin = relPoint.y;
                }
                if (relPoint.y > yMax) {
                    yMax = relPoint.y;
                }
            }
            xMin += pos.x;
            xMax += pos.x;
            yMin += pos.y;
            yMax += pos.y;
        }

        public void circleCalc(double radius, Vec pos) {
            xMin = pos.x - radius;
            xMax = pos.x + radius;
            yMin = pos.y - radius;
            yMax = pos.y + radius;
        }
    }

    public boolean updateActive(Vec stageSize, double inactiveRange) {
        if (this.pos.x > inactiveRange + stageSize.x || this.pos.x < -inactiveRange || this.pos.y > inactiveRange + stageSize.y || this.pos.y < -inactiveRange) {
            this.active = false;
        } else {
            this.active = true;
        }
        return this.active;
    }

    @Override
    public Object clone() {
        if (Objects.equals(this.type, "polygon")) {
            Object cloned = new Object(this.color, this.pos.x, this.pos.y, this.rotation, Vec.copyArray(this.setRel), this.setAcceleration.copy(), this.linearStatic, this.angularStatic);
            cloned.vel = this.vel.copy();
            cloned.angularVel = this.angularVel;
            cloned.updateRelativeCoordinates();
            return cloned;
        } else if  (Objects.equals(this.type, "circle")) {
            Object cloned = new Object(this.color, this.pos.x, this.pos.y, this.rotation, this.radius, this.setAcceleration.copy(), this.linearStatic, this.angularStatic);
            cloned.vel = this.vel.copy();
            cloned.angularVel = this.angularVel;
            return cloned;
        } else {
            return null;
        }
    }
}