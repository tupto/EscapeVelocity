import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Sprite extends GameObject {
    protected BufferedImage[] composite;
    protected Vector2 velocity;
    protected Vector2 scale;
    protected double rotation;
    protected double rotationalVelocity;

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public Vector2 getScale() {
        return scale;
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public void rotate(double rotation) {
        this.rotation += rotation;
    }

    public double getRotationalVelocity() {
        return rotationalVelocity;
    }

    public int getWidth() {
        if (composite == null)
            return 0;
        if (composite.length == 0)
            return 0;
        if (composite[0] == null)
            return 0;
        return composite[0].getWidth() * ((composite.length + 1) / 2);
    }

    public int getHeight() {
        if (composite == null)
            return 0;
        if (composite.length == 0)
            return 0;
        if (composite[0] == null)
            return 0;
        return composite[0].getHeight() * ((composite.length + 1) / 2);
    }

    public int getSegmentWidth() {
        if (composite.length == 0)
            return 0;
        return composite[0].getWidth();
    }

    public int getSegmentHeight() {
        if (composite.length == 0)
            return 0;
        return composite[0].getHeight();
    }

    public void setRotationalVelocity(double rotationalVelocity) {
        this.rotationalVelocity = rotationalVelocity;
    }
    public Sprite(SpaceGame game, BufferedImage sprite) {
        this(game, new BufferedImage[] { sprite });
    }

    public Sprite(SpaceGame game, BufferedImage[] composite) {
        super(game);
        this.velocity = new Vector2();
        this.scale = new Vector2(1);
        this.composite = composite;
    }

    @Override
    public void update() {
        getPosition().add(velocity);
        rotate(rotationalVelocity);
    }

    @Override
    public void paint(Graphics g) {
        if (composite.length == 0 || scale.getX() == 0 || scale.getY() == 0 || composite[0] == null)
            return;

        AffineTransform at = new AffineTransform();
        at.translate(getWidth() / 2.0, getHeight() / 2.0);
        at.scale(scale.getX(), scale.getY());
        at.rotate(rotation);
        at.translate(-getWidth() / 2.0, -getHeight() / 2.0);

        for (int y = 0; y < (composite.length+1) / 2; y++) {
            for (int x = 0; x < (composite.length+1) / 2; x++) {
                BufferedImage segment = composite[(y * composite.length / 2) + x];

                BufferedImage transformed = new BufferedImage(segment.getWidth(), segment.getHeight(), segment.getType());
                final AffineTransformOp transform = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

                int xPos = (int) (getPosition().getX() + (x * segment.getWidth()));
                int yPos = (int) (getPosition().getY() + (y * segment.getHeight()));
                g.drawImage(transform.filter(segment, transformed), xPos, yPos, null);
            }
        }
    }
}
