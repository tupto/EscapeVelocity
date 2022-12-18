import javax.xml.crypto.dsig.Transform;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PlayerRocket extends PhysicsSprite implements KeyListener {

    private enum ShipSides {
        PORT,
        STARBOARD
    };

    private static final double ROTATIONAL_ACCELERATION = 0.002;
    private static final double ACCELERATION = 0.08;
    private static final int GUN_COOLDOWN = 8;
    private static final Vector2 EXHAUST_OFFSET = new Vector2(-20, 0);
    private static final Vector2 PORT_GUN_OFFSET = new Vector2(0, -10);
    private static final Vector2 STARBOARD_GUN_OFFSET = new Vector2(0, 10);

    private int gunTimer = GUN_COOLDOWN;

    private boolean accelerating;
    private boolean turning;
    private boolean shooting;

    private ShipSides turnDir;
    private ShipSides currGun;

    private double throttle = 1;

    public PlayerRocket(SpaceGame game, BufferedImage sprite) {
        super(game, sprite, new CircleCollider(16, null, true), PhysicsType.KINEMATIC, 0.1);
    }

    @Override
    public void onCollide(Collider a, Collider b, Collidable other) {
        if (b.isSolid()) {
            if (other instanceof Projectile)
                if (((Projectile) other).getOwner().equals(this))
                    return;
        }
        super.onCollide(a, b, other);
    }

    @Override
    public void update() {
        super.update();

        if (accelerating) {
            getVelocity().add(Vector2.fromRadian(getRotation()).multiply(ACCELERATION).multiply(throttle));

            RocketExhaust exhaust = new RocketExhaust(game, game.loadImage("RocketExhaust.png"));

            Vector2 centre = new Vector2(getPosition());
            centre.add(new Vector2(exhaust.getWidth() / 2, exhaust.getHeight() / 2));

            Vector2 exhaustPos = new Vector2(centre);
            Vector2 angles = Vector2.fromRadian(rotation);
            exhaustPos.add(new Vector2(angles.getX() * EXHAUST_OFFSET.getX() - angles.getY() * EXHAUST_OFFSET.getY(),
                    angles.getY() * EXHAUST_OFFSET.getX() + angles.getX() * EXHAUST_OFFSET.getY()));
            exhaust.setPosition(exhaustPos);

            Vector2 behind =  Vector2.fromRadian(rotation + Math.PI);
            behind.multiply(2);
            Vector2 exhaustVelocity = new Vector2(getVelocity());
            exhaustVelocity.add(behind);

            exhaust.setVelocity(exhaustVelocity);

            game.spawnObject(exhaust);
        }

        if (turning)
            setRotationalVelocity(getRotationalVelocity() + (turnDir == ShipSides.PORT ? -ROTATIONAL_ACCELERATION : ROTATIONAL_ACCELERATION));

        if (shooting) {
            if (gunTimer >= GUN_COOLDOWN) {
                Projectile bullet = new Projectile(game, game.loadImage("Bullet.png"), this);

                Vector2 centre = new Vector2(getPosition());
                centre.add(new Vector2(getWidth() / 2, getHeight() / 2));

                Vector2 bulletPos = new Vector2(centre);
                Vector2 angles = Vector2.fromRadian(rotation);
                Vector2 offset = currGun == ShipSides.PORT ? PORT_GUN_OFFSET : STARBOARD_GUN_OFFSET;

                bulletPos.add(new Vector2(angles.getX() * offset.getX() - angles.getY() * offset.getY(),
                        angles.getY() * offset.getX() + angles.getX() * offset.getY()));
                bullet.setPosition(bulletPos);
                bullet.setRotation(getRotation());

                Vector2 forward =  Vector2.fromRadian(rotation);

                forward.multiply(10);
                Vector2 bulletVelocity = new Vector2(getVelocity());
                bulletVelocity.add(forward);

                bullet.setVelocity(forward);

                game.spawnObject(bullet);

                gunTimer = 0;
                currGun = currGun == ShipSides.PORT ? ShipSides.STARBOARD : ShipSides.PORT;

                Vector2 behind =  Vector2.fromRadian(rotation + Math.PI);
                behind.multiply(0.01);
                velocity.add(behind);
            }
        }

        if (gunTimer < GUN_COOLDOWN)
            gunTimer++;
    }

    @Override
    public void paint(Graphics g) {
        Vector2 centre = new Vector2(getPosition());
        centre.add(new Vector2(getWidth() / 2, getHeight() / 2));

        Vector2 velocityLine = new Vector2(getVelocity());
        velocityLine.multiply(30);
        velocityLine.add(centre);

        Graphics2D g2d = (Graphics2D)g;

        if (Math.abs(centre.distanceTo(velocityLine)) >= 1) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.blue);
            //g2d.drawLine((int) centre.getX(), (int) centre.getY(), (int) velocityLine.getX(), (int) velocityLine.getY());
        }


        if (orbitalData != null) {
            Vector2 apo = orbitalData.getApoapsis();
            Vector2 per = orbitalData.getPeriapsis();

            g.setColor(Color.red);
            g.drawString("AP", (int) (apo.getX() - 32), (int) apo.getY() - 32);
            g.fillOval((int) apo.getX() - 30, (int) apo.getY() - 30, 60, 60);
            g.setColor(Color.green);
            g.drawString("PE", (int) (per.getX() - 32), (int) per.getY() - 32);
            g.fillOval((int) per.getX() - 30, (int) per.getY() - 30, 60, 60);

            AffineTransform t = g2d.getTransform();
            try {
                g2d.setTransform(new AffineTransform());

                double speed = orbitalData.getRelativeVelocity().getMagnitude();

                g.setColor(speed > orbitalData.getEscapeVelocity() ? Color.red : Color.green);
                g.drawString(String.format("Speed: %.2f", speed), (int) 5, 12);

                g.setColor(Color.green);
                g.drawString(String.format("Dist: %.2f", orbitalData.getDistance()), 5, 24);

                double radius = (currentBody.getWidth() / 2.0);
                Vector2 bodyCenter = new Vector2(currentBody.position).add(radius);
                double apoaDist = orbitalData.getApoapsis().distanceTo(bodyCenter) - (currentBody.getWidth() / 2.0);
                double periDist = orbitalData.getPeriapsis().distanceTo(bodyCenter) - (currentBody.getWidth() / 2.0);

                g.setColor(apoaDist < 0 ? Color.red : Color.green);
                g.drawString(String.format("Apoa: %.2f", apoaDist), 5, 36);
                g.setColor(periDist < 0 ? Color.red : Color.green);
                g.drawString(String.format("Peri: %.2f", periDist), 5, 48);

                g.setColor(Color.green);
                g.drawString(String.format("Mass: %.2f", currentBody.mass), 5, 72);

                g.setColor(Color.green);
                g.drawString(String.format("Throttle: %.0f %%", throttle*100), 450, 595);

                g2d.setTransform(t);
            } catch (Exception e) {
            }
        }

        super.paint(g);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("pressed " + e.getKeyChar());
        if (e.getKeyChar() == 'w')
            accelerating = true;

        if (e.getKeyChar() == 'a') {
            turning = true;
            turnDir = ShipSides.PORT;
        }

        if (e.getKeyChar() == 'd') {
            turning = true;
            turnDir = ShipSides.STARBOARD;
        }

        if (e.getKeyChar() == ' ') {
            shooting = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            //if (throttle < 1) {
                throttle += 0.05;
            //}
            if (throttle > 1) {
                //throttle = 1;
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            if (throttle > 0) {
                throttle -= 0.05;
            }
            if (throttle < 0) {
                throttle = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'w')
            accelerating = false;

        if (e.getKeyChar() == 'a') {
            turning = false;
        }

        if (e.getKeyChar() == 'd') {
            turning = false;
        }

        if (e.getKeyChar() == ' ') {
            shooting = false;
        }
    }
}
