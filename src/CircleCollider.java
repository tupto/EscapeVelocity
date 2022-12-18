public class CircleCollider implements Collider {
    private double radius;
    private Vector2 position;
    private boolean solid;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public CircleCollider(int radius, Vector2 position, boolean solid) {
        this.radius = radius;
        this.position = position;
        this.solid = solid;
    }

    public boolean intersects(CircleCollider b) {
        if (position == null || b.position == null)
            return false;
        double dist = new Vector2(position).distanceTo(b.position);

        return Math.abs(dist) < radius + b.radius;
    }

    @Override
    public boolean checkCollision(Collider other) {
        if (other instanceof CircleCollider)
            return intersects((CircleCollider) other);
        return false;
    }

    @Override
    public boolean isSolid() {
        return solid;
    }

    @Override
    public Vector2 getCenter() {
        return position;
    }

    @Override
    public double getWidth() {
        return radius*2;
    }

    @Override
    public double getHeight() {
        return radius*2;
    }
}
