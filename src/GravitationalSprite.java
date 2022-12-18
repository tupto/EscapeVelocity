import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GravitationalSprite extends PhysicsSprite {
    protected CircleCollider circleOfInfluence;

    private List<GravitationalSprite> children;

    public GravitationalSprite(SpaceGame game, BufferedImage sprite, double collisionRadius, double influenceRadius, double mass) {
        this(game, new BufferedImage[] { sprite }, collisionRadius, influenceRadius, mass);
    }

    public GravitationalSprite(SpaceGame game, BufferedImage[] composite, double collisionRadius, double influenceRadius, double mass) {
        super(game, composite, new CircleCollider((int) collisionRadius, null, true), PhysicsType.STATIC, mass);
        this.circleOfInfluence = new CircleCollider((int) influenceRadius, new Vector2(position).add(getWidth(), getHeight()), false);
        children = new ArrayList<>();
    }

    @Override
    public void setPosition(Vector2 position) {
        super.setPosition(position);
        position.add(getWidth()/2.0, getHeight()/2.0);
        this.circleOfInfluence.setPosition(position);
    }

    @Override
    public void onCollide(Collider a, Collider b, Collidable other) {
        if (a.equals(circleOfInfluence)) {
            if (other instanceof PhysicsSprite && !(other instanceof  GravitationalSprite)) {
                PhysicsSprite body = (PhysicsSprite) other;
                if (body.currentBody == null) {
                    if (mass > body.mass)
                        body.currentBody = this;
                }
                else if (body.currentBody != this) {
                    if (body.collider.getCenter().distanceTo(body.currentBody.collider.getCenter()) > body.currentBody.getInfluenceRadius()) {
                        body.currentBody = this;
                    }
                    else if (mass < body.currentBody.mass) {
                        body.currentBody = this;
                    }
                }

                double bodyRad = collider.getHeight() / 2;
                Vector2 dist = new Vector2(a.getCenter()).subtract(b.getCenter());
                double r = dist.getMagnitude();
                double force = GRAVITATIONAL_CONSTANT * (mass * body.mass) / (r*r);

                Vector2 accel = dist.getNormalised().multiply(force/(body.mass));

                body.velocity.add(accel);
                //body.position.add(new Vector2(velocity).divide((r - bodyRad) / r));
            }
        }

        super.onCollide(a, b, other);
    }

    @Override
    public List<Collider> getColliders() {
        return Arrays.asList(circleOfInfluence, collider);
    }

    @Override
    public void update() {
        velocity = getOrbitVelocity();
        for (GravitationalSprite gs: children) {
            Vector2 updatedPos = new Vector2(gs.position).add(velocity);
            gs.setPosition(updatedPos);
        }

        super.update();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

//        g.setColor(new Color(0.6f, 0.6f, 0.f, 0.2f));
//        double r = circleOfInfluence.getRadius();
//        g.fillOval((int) (circleOfInfluence.getCenter().getX() - r), (int) (circleOfInfluence.getCenter().getY() - r), (int) (2*r), (int) (2*r));
    }

    @Override
    public double getInfluenceRadius() {
        return circleOfInfluence.getRadius();
    }

    @Override
    public void orbit(GravitationalSprite body) {
        super.orbit(body);
        body.children.add(this);

        Vector2 dist = new Vector2(collider.getCenter()).subtract(body.collider.getCenter());
        double semiMajor = dist.getMagnitude();

        double soi = semiMajor * Math.pow(mass / body.mass, 0.4);
        circleOfInfluence.setRadius(soi);
    }

    private Vector2 getOrbitVelocity() {
        if (currentBody == null)
            return new Vector2(0);

        double mu = GRAVITATIONAL_CONSTANT * (mass + currentBody.mass);
        Vector2 dist = new Vector2(collider.getCenter()).subtract(currentBody.collider.getCenter());
        double semiMajor = dist.getMagnitude();

        Vector2 dir = dist.getNormalised();
        Vector2 moveDir = new Vector2(-dir.getY(), dir.getX());

        //v^2 = mu * (2 / r - 1/a)
        //For circular orbits: (2 / r - 1/a) = 1 / a
        double v = Math.sqrt(mu * (1/ semiMajor));

        return moveDir.multiply(v);
    }
}
