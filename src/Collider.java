public interface Collider {
    boolean checkCollision(Collider other);
    boolean isSolid();
    Vector2 getCenter();
    double getWidth();
    double getHeight();
}
