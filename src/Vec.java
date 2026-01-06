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

    // formula stuff
    public static double dot(Vec a, Vec b) {
        return a.x * b.x + a.y * b.y;
    }

    public static double cross(Vec a, Vec b) {
        return a.x * b.y - a.y * b.x;
    }

    public static Vec dirOfPerpendicularBisector(Vec a, Vec b) { return dirOfPerpendicularBisector(a, b, false); }
    public static Vec dirOfPerpendicularBisector(Vec a, Vec b, boolean flip) {
        if (flip) {
            return new Vec(-1 * a.y * ((b.x * a.y) - (a.x * b.y)), -1 * a.x * ((b.y * a.x) - (a.y * b.x)));
        } else {
            return new Vec(a.y * ((b.x * a.y) - (a.x * b.y)), a.x * ((b.y * a.x) - (a.y * b.x)));
        }
    }

    public double length() {return Math.sqrt(x * x + y * y); }

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
}
