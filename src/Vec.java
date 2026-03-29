import java.awt.*;

public class Vec {
    public double x;
    public double y;

    public Vec() {
        this.x = 0;
        this.y = 0;
    }

    public Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec copy() {
        return new Vec(this.x, this.y);
    }

    public static class coloredVec {
        public Vec vec;
        public Color color;
        public String name;
        public int size;

        public coloredVec(Vec vec, Color color) {
            this.vec = new Vec(vec.x, vec.y);
            this.color = color;
            this.name = "";
            this.size = 1;
        }
        public coloredVec(Vec vec, Color color, String name) {
            this.vec = new Vec(vec.x, vec.y);
            this.color = color;
            this.name = name;
            this.size = 1;
        }
        public coloredVec(Vec vec, Color color, int size) {
            this.vec = new Vec(vec.x, vec.y);
            this.color = color;
            this.name = "";
            this.size = size;
        }
        public coloredVec(Vec vec, Color color, String name, int size) {
            this.vec = new Vec(vec.x, vec.y);
            this.color = color;
            this.name = name;
            this.size = size;
        }
    }

    public static class coloredLine {
        public Vec start;
        public Vec end;
        public Color color;
        public String name;
        public float width;

        public coloredLine(Vec start, Vec end, Color color) {
            this.start = start;
            this.end = end;
            this.color = color;
            this.width = 1;
        }
        public coloredLine(Vec start, Vec end, Color color, float width) {
            this.start = start;
            this.end = end;
            this.color = color;
            this.width = width;
        }
    }

    public boolean equals(Vec vec) {
        if (this == vec) return true;
        if (vec == null) return false;
        return Double.compare(vec.x, this.x) == 0 && Double.compare(vec.y, this.y) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static double distance(Vec a, Vec b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Vector creators
    public static Vec fromAngWithLen(double ang, double len) {
        return new Vec(len * Math.cos(ang), len * Math.sin(ang));
    }

    public static Vec[] copyArray(Vec[] arr) {
        Vec[] newArr = new Vec[arr.length];
        for (int i = 0; i < arr.length; i++) {
            newArr[i] = arr[i].copy();
        }
        return newArr;
    }

    // Formula stuff
    public static double dot(Vec a, Vec b) {
        return (a.x * b.x) + (a.y * b.y);
    }

    public double dot(Vec b) { return this.x *  b.x + this.y * b.y; }

    public static double cross(Vec a, Vec b) {
        return a.x * b.y - a.y * b.x;
    }

    public static Vec dirOfPerpendicularBisector(Vec a, Vec b) { return dirOfPerpendicularBisector(a, b, false); }
    public static Vec dirOfPerpendicularBisector(Vec a, Vec b, boolean flip) {
        // Compute perpendicular to 'a', scaled by cross product of a and b
        // This simplifies to: perp(a) * cross(a, b)
        // This uses the winding product instead of the original triple product from the first engine :)
        double crossProduct = cross(a, b);
        Vec result;
        if (flip) {
            // Perpendicular counterclockwise: (-a.y, a.x)
            result = new Vec(-a.y * crossProduct, a.x * crossProduct);
        } else {
            // Perpendicular clockwise: (a.y, -a.x)
            result = new Vec(a.y * crossProduct, -a.x * crossProduct);
        }
        // Normalize for numerical stability in GJK algorithm
        double length = Math.sqrt(result.x * result.x + result.y * result.y);
        if (length > 0) {
            return new Vec(result.x / length, result.y / length);
        }
        IO.println("Warning: Zero-length perpendicular bisector direction vector.");
        IO.println(length);
        return result; // Return to protect against division by zero
    }

    public static boolean isPointRightOfLine(Vec point, Vec linePointA, Vec linePointB, boolean inclusive) {
        if (inclusive) {
            return ((linePointB.x - linePointA.x) * (point.y - linePointA.y) - (linePointB.y - linePointA.y) * (point.x - linePointA.x)) <= 0;
        } else {
            return ((linePointB.x - linePointA.x) * (point.y - linePointA.y) - (linePointB.y - linePointA.y) * (point.x - linePointA.x)) < 0;
        }
    }

    public static double normDist(Vec p, Vec a, Vec b, boolean clamp) {
        //finding where p is along line AB in a value from 0-1
        double AxtoPx = p.x - a.x;
        double AytoPy = p.y - a.y;
        double AxtoBx = b.x - a.x;
        double AytoBy = b.y - a.y;

        double dot = AxtoPx * AxtoBx + AytoPy * AytoBy;
        double len_sq = AxtoBx * AxtoBx + AytoBy * AytoBy;
        double param = -1;
        if (len_sq != 0) { //in case of 0 length line
            param = dot / len_sq;
        }
        if (clamp) {
            if (param < 0) {
                return 0;
            } else if (param > 1) {
                return 1;
            } else {
                return param;
            }
        } else {
            return param;
        }
    }

    public static Vec normDistPoint(Vec p, Vec a, Vec b, boolean clamp, double setNormDist) {
        //finding the closest point to p on line AB
        return new Vec(a.x + setNormDist * (b.x - a.x), a.y + setNormDist * (b.y - a.y));
    }

    public static Vec normDistPoint(Vec p, Vec a, Vec b, boolean clamp) {
        //finding the closest point to p on line AB
        double param = normDist(p, a, b, clamp);
        return new Vec(a.x + param * (b.x - a.x), a.y + param * (b.y - a.y));
    }

    public static double distToNormDistPoint(Vec p, Vec a, Vec b, boolean clamp, double setNormDist) {
        Vec closest = normDistPoint(p, a, b, clamp, setNormDist);
        return distance(p, closest);
    }

    public static double distToNormDistPoint(Vec p, Vec a, Vec b, boolean clamp) {
        Vec closest = normDistPoint(p, a, b, clamp);
        return distance(p, closest);
    }

    public static double distInDir(Vec a, Vec dir) {
        double dotProduct = dot(a, dir);
        double dirLengthSquared = dot(dir, dir);
        if (dirLengthSquared == 0) {
            throw new IllegalArgumentException("Direction vector cannot be the zero vector.");
        }
        return dotProduct / Math.sqrt(dirLengthSquared);
    }

    public double toAng() { return Math.atan2(y, x); }

    public void increase(Vec vec) {
        this.x += vec.x;
        this.y += vec.y;
    }

    public void decrease(Vec vec) {
        this.x -= vec.x;
        this.y -= vec.y;
    }

    public Vec rotate(double ang) {
        double cosTheta = Math.cos(ang);
        double sinTheta = Math.sin(ang);
        return this.rotate(ang, cosTheta, sinTheta);
    }

    public Vec rotate(double ang, double cosTheta, double sinTheta) {
        return new Vec(this.x * cosTheta - this.y * sinTheta, this.x * sinTheta + this.y * cosTheta);
    }

    // Instance arithmetic helpers
    public Vec add(Vec v) { return new Vec(this.x + v.x, this.y + v.y); }

    public Vec add(double s) {  return new Vec(this.x + s, this.y + s); }

    public Vec add(double sx, double sy) {  return new Vec(this.x + sx, this.y + sy); }

    public Vec sub(Vec v) { return new Vec(this.x - v.x, this.y - v.y); }

    public Vec sub(double s) { return new Vec(this.x - s, this.y - s); }

    public Vec mul(double s) {
        return new Vec(this.x * s, this.y * s);
    }

    public Vec div(double s) {
        return new Vec(this.x / s, this.y / s);
    }

    // Static helpers
    public static Vec add(Vec a, Vec b) {
        return new Vec(a.x + b.x, a.y + b.y);
    }

    public static Vec add(Vec v, double s) {
        return new Vec(v.x + s, v.y + s);
    }

    public static Vec add(Vec v, double sx, double sy) {
        return new Vec(v.x + sx, v.y + sy);
    }

    public static Vec sub(Vec a, Vec b) {
        return new Vec(a.x - b.x, a.y - b.y);
    }

    public static Vec sub(Vec v, double s) {
        return new Vec(v.x - s, v.y - s);
    }

    public static Vec mul(Vec v, double s) {
        return new Vec(v.x * s, v.y * s);
    }

    public static Vec div(Vec v, double s) {
        return new Vec(v.x / s, v.y / s);
    }

    @Override
    public String toString() {
        return "Vec(" + x + ", " + y + ")";
    }

    public static double len(Vec a, Vec b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    public static double len(Vec a) {
        return Math.sqrt(a.x * a.x + a.y * a.y);
    }

    public double len() {
        return Math.sqrt(x * x + y * y);
    }

    public Vec normalize() {
        double length = this.len();
        if (length == 0) {
            throw new IllegalArgumentException("Cannot normalize the zero vector.");
        }
        return new Vec(this.x / length, this.y / length);
    }

    public static Vec normalize(Vec a) {
        return a.normalize();
    }
}
