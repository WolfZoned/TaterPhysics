import java.awt.*;

public class Object {
    Vec pos = new Vec(); //position of center of mass

    public int index;

    public double rotation; //radians
    public String type;
    public Color color;

    public boolean collidable;
    public boolean active;
    public boolean staticObj;
    public boolean outlined;

    public Vec[] rel;
    private Vec[] setRel;
    public Vec[] bindRel;
    private Vec[] setBindRel;

    public BoundingBox boundingBox;

    public double area;
    public double density;
    public double friction;
    public double restitution;


    public Vec vel; //linear velocity
    public double angularVel; //angular velocity
    public double setAngularVel;

    public double inertia; //mass moment of inertia
    public double inverseInertia;
    public double mass;
    public double inverseMass;


    public Object(Color color, double x, double y, double rotation, Vec[] points, int index) { //polygon shape
        //make sure all shapes are clockwise for now
        this.pos = new Vec(x, y);
        this.vel = new Vec(0.0, 0.0);
        this.rotation = rotation;
        this.angularVel = 0.4;
        this.type = "polygon";
        this.color = color;
        this.density = 1.0;
        this.outlined = false;
        this.index = index;
        this.boundingBox = new BoundingBox();
        this.setRel = points;
        this.rel = Vec.copyArray(this.setRel);
        PropertiesCalc(this.setRel, 1.0, this.density);
        if (this.mass != 0) {
            this.inverseMass = 1.0 / this.mass;
        } else {
            this.inverseMass = 0.0;
        }
        if (this.inertia != 0) {
            this.inverseInertia = 1.0 / this.inertia;
        } else {
            this.inverseInertia = 0.0;
        }
    }

    public Object(Color color, double x, double y, double rotation, double width, double height, int index) { //rectangle
        this(color, x, y, rotation, new Vec[] {
            new Vec(width / 2, height / 2),
            new Vec(width / 2, height / -2),
            new Vec(width / -2, height / -2),
            new Vec(width / -2, height / 2)}
        , index);
        //this.pos.x = x;
        //this.pos.y = y;
    }/* else if (type.equals("circle")) {
        this.pos.x = x;
        this.pos.y = y;
        this.rotation = rotation;
        this.color = color;
        this.density = 1.0; */

    public void updateRelativeCoordinates() {
        double cosTheta = Math.cos(this.rotation);
        double sinTheta = Math.sin(this.rotation);
        for (int i = 0; i < setRel.length; i++) {
            rel[i] = setRel[i].rotate(this.rotation, cosTheta, sinTheta);
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

    private void PropertiesCalc(Vec[] rel, double depth, double density) {
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
        this.mass = mass;
        this.area = area;
        this.inertia = mmoi;
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

        public void calc(Vec[] rel, Vec pos) {
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
    }
}