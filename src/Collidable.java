import java.util.List;

public interface Collidable {
    void onCollide(Collider a, Collider b, Collidable other);
    List<Collider> getColliders();
}
