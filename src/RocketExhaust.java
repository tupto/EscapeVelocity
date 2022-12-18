import java.awt.image.BufferedImage;

public class RocketExhaust extends Sprite {

    private static final double LIFETIME_LENGTH = 20;

    private double lifetime;

    public RocketExhaust(SpaceGame game, BufferedImage sprite) {
        super(game, sprite);
        this.lifetime = 0;
    }

    @Override
    public void update() {
        super.update();

        double s = Math.cos((lifetime / LIFETIME_LENGTH) * Math.PI / 2);
        lifetime += 1;

        scale = new Vector2(s);
        if (lifetime == LIFETIME_LENGTH) {
            setAlive(false);
        }
    }
}
