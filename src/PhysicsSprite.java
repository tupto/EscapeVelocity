import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class PhysicsSprite extends Sprite implements Collidable {
    public static final double GRAVITATIONAL_CONSTANT = 1.0;

    public enum PhysicsType {
        STATIC,
        KINEMATIC
    }

    protected Collider collider;
    protected PhysicsType type;
    protected double mass;
    protected PhysicsSprite currentBody;
    protected OrbitalData orbitalData;

    public PhysicsSprite(SpaceGame game, BufferedImage sprite, Collider collider, PhysicsType type, double mass) {
        this(game, new BufferedImage[] { sprite }, collider, type, mass);
    }

    public PhysicsSprite(SpaceGame game, BufferedImage[] composite, Collider collider, PhysicsType type, double mass) {
        super(game, composite);
        this.collider = collider;
        this.type = type;
        this.mass = mass;

        if (collider instanceof CircleCollider) {
            Vector2 center = new Vector2(position);
            center.add(((double) getWidth())/2, ((double) getHeight())/2);
            ((CircleCollider)collider).setPosition(center);
        }
    }

    @Override
    public void setPosition(Vector2 position) {
        super.setPosition(position);

        if (collider instanceof CircleCollider) {
            Vector2 center = new Vector2(position);
            center.add(((double) getWidth())/2, ((double) getHeight())/2);
            ((CircleCollider)collider).setPosition(center);
        }
    }

    @Override
    public void update() {
        super.update();

        if (collider instanceof CircleCollider) {
            Vector2 center = new Vector2(position);
            center.add(((double) getWidth())/2, ((double) getHeight())/2);
            ((CircleCollider)collider).setPosition(center);
        }

        if (currentBody != null) {
            Vector2 a = currentBody.collider.getCenter();
            Vector2 b = collider.getCenter();
            double mu = GRAVITATIONAL_CONSTANT * (currentBody.mass + mass);
            Vector2 relVelocity = new Vector2(velocity).subtract(currentBody.velocity);
            if (currentBody.currentBody != null) {
                relVelocity.subtract(currentBody.currentBody.velocity);
            }
            orbitalData = OrbitalData.calculateOrbit(a, new Vector2(b).subtract(a), relVelocity, mu);
        }
    }

    @Override
    public void onCollide(Collider a, Collider b, Collidable other) {
        if (!a.equals(collider) || !b.isSolid()) {
            return;
        }

        if (other instanceof PhysicsSprite) {
            if (a instanceof CircleCollider && b instanceof CircleCollider)
            {
                PhysicsSprite bSprite = (PhysicsSprite) other;

                Vector2 aMid = a.getCenter();
                Vector2 bMid = b.getCenter();
                Vector2 dist = new Vector2(aMid).subtract(bMid);
                Vector2 norm = dist.getNormalised();
                Vector2 tan = new Vector2(-(bMid.getX() - aMid.getX()), bMid.getY() - aMid.getY()).getNormalised();

                Vector2 relVelocity = new Vector2(((PhysicsSprite) other).velocity).subtract(velocity);
                Vector2 paraVelocity = new Vector2(tan).multiply(relVelocity.dot(tan));
                Vector2 perpVelocity = new Vector2(relVelocity).subtract(paraVelocity);

                Vector2 centerOfMass = null;
                if (type == PhysicsType.STATIC) {
                    if (bSprite.type == PhysicsType.STATIC) {
                        return;
                    }
                    centerOfMass = aMid;
                }
                else if (type == PhysicsType.KINEMATIC) {
                    if (bSprite.type == PhysicsType.STATIC) {
                        centerOfMass = bMid;
                    }
                    else {
                        double centreOfMassX = (mass * aMid.getX() + bSprite.mass * bMid.getX()) / (mass * bSprite.mass);
                        double centreOfMassY = (mass * aMid.getY() + bSprite.mass * bMid.getY()) / (mass * bSprite.mass);
                        centerOfMass = new Vector2(centreOfMassX, centreOfMassY);
                    }
                } else {
                    return; //This should not happen
                }

                double mag = dist.getMagnitude();
                double expectedMag = ((CircleCollider) a).getRadius() + ((CircleCollider) b).getRadius();
                double massRatio = centerOfMass.distanceTo(aMid) / expectedMag;
                double distToMove = (expectedMag - mag) * massRatio;

                Vector2 posOffset = new Vector2(norm).multiply(distToMove);

                position.add(posOffset);

                if (bSprite.type == PhysicsType.STATIC) {
                    //velocity = new Vector2(((PhysicsSprite) other).velocity);
                    Vector2 velOffset = new Vector2(perpVelocity).multiply(massRatio);
                    velocity.add(velOffset);
                }

//                Vector2 velOffset = new Vector2(perpVelocity).multiply(massRatio);
//                velocity.add(velOffset);
            }
        }
    }

    public void orbit(GravitationalSprite body) {
        currentBody = body;

        double mu = GRAVITATIONAL_CONSTANT * (mass + currentBody.mass);
        Vector2 dist = new Vector2(collider.getCenter()).subtract(currentBody.collider.getCenter());
        double semiMajor = dist.getMagnitude();

        Vector2 dir = dist.getNormalised();
        Vector2 moveDir = new Vector2(-dir.getY(), dir.getX());

        //v^2 = mu * (2 / r - 1/a)
        //For circular orbits: (2 / r - 1/a) = 1 / a
        double v = Math.sqrt(mu * (1/ semiMajor));

        moveDir.multiply(v);

        velocity = moveDir;
    }

    @Override
    public List<Collider> getColliders() {
        return Arrays.asList(collider);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paint(g);
    }

    public double getInfluenceRadius() {
        return 0;
    }
}
