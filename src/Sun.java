import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Sun extends GravitationalSprite {
    double radius;
    public Sun(SpaceGame game, double collisionRadius, double influenceRadius, double mass) {
        super(game, (BufferedImage) null, collisionRadius, influenceRadius, mass);
        radius = collisionRadius;
    }

    @Override
    public void paint(Graphics g) {
        int xPos = (int) getPosition().getX();
        int yPos = (int) getPosition().getY();
        g.setColor(Color.yellow);
        g.fillOval((int) (xPos-radius), (int) (yPos-radius), (int) (radius*2), (int) (radius*2));
    }
}
