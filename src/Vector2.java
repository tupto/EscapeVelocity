public class Vector2 {
    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Vector2() {}
    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Vector2(double n) {
        this.x = n;
        this.y = n;
    }
    public Vector2(Vector2 v) {
        this.x = v.getX();
        this.y = v.getY();
    }

    public double getMagnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 getNormalised() {
        Vector2 norm = new Vector2(this);
        return norm.divide(getMagnitude());
    }

    public double angle(Vector2 b) {
        double magA = getMagnitude();
        double magB = b.getMagnitude();

        Vector2 a = new Vector2(this);

        return Math.acos((x * b.x + y * b.y) / (magA * magB));
    }

    public double distanceTo(Vector2 b) {
        if (b == null)
            return Double.POSITIVE_INFINITY;

        double x2 = x - b.getX();
        double y2 = y - b.getY();
        return Math.sqrt(x2 * x2 + y2 * y2);
    }

    public double cross(Vector2 b) {
        return (x * b.y) - (y * b.x);
    }

    public double dot(Vector2 b) {
        return getMagnitude() * b.getMagnitude() * Math.cos(angle(b));
    }

    public Vector2 add(Vector2 b) {
        x += b.getX();
        y += b.getY();

        return this;
    }

    public Vector2 subtract(Vector2 b) {
        x -= b.getX();
        y -= b.getY();

        return this;
    }

    public Vector2 multiply(Vector2 b) {
        x *= b.getX();
        y *= b.getY();

        return this;
    }

    public Vector2 divide(Vector2 b) {
        x /= b.getX();
        y /= b.getY();

        return this;
    }

    public Vector2 add(double b) {
        x += b;
        y += b;

        return this;
    }

    public Vector2 add(double x, double y) {
        this.x += x;
        this.y += y;

        return this;
    }

    public Vector2 subtract(double b) {
        x -= b;
        y -= b;

        return this;
    }

    public Vector2 subtract(double x, double y) {
        this.x -= x;
        this.y -= y;

        return this;
    }

    public Vector2 multiply(double b) {
        x *= b;
        y *= b;

        return this;
    }

    public Vector2 multiply(double x, double y) {
        this.x *= x;
        this.y *= y;

        return this;
    }

    public Vector2 divide(double b) {
        x /= b;
        y /= b;

        return this;
    }

    public Vector2 divide(double x, double y) {
        this.x /= x;
        this.y /= y;

        return this;
    }

    @Override
    public boolean equals(Object b) {
        if (!(b instanceof Vector2))
            return false;

        return x == ((Vector2)b).getX() && y == ((Vector2) b).getY();
    }

    @Override
    public String toString() {
        return "{" +
                "x=" + String.format("%.4f", x) +
                ", y=" + String.format("%.4f", y) +
                '}';
    }

    public static Vector2 fromRadian(double radian) {
        return new Vector2(Math.cos(radian), Math.sin(radian));
    }
}
