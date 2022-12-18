import java.awt.image.BufferedImage;

public class Projectile extends PhysicsSprite {
    private GameObject owner;

    public Projectile(SpaceGame game, BufferedImage sprite, GameObject owner) {
        super(game, sprite, new CircleCollider(2, null, true), PhysicsType.STATIC, 0.01);
        this.owner = owner;
    }

    @Override
    public void onCollide(Collider a, Collider b, Collidable other) {
        if (b.isSolid()) {
            if (other.equals(owner))
                return;

            alive = false;
        }
    }

    public GameObject getOwner() {
        return owner;
    }
}
