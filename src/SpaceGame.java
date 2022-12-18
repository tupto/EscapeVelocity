import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class SpaceGame extends Canvas {
    public static void main(String[] args) {
        new SpaceGame();
    }

    private static final int UPDATE_TIME = 1000 / 60;

    private final JFrame frame;
    private final List<GameObject> gameObjects;
    private final HashMap<String, BufferedImage> images;

    private boolean running = false;
    private boolean turbo = false;

    private Vector2 cameraPosition;
    private double zoom;
    private GameObject trackCameraTo;

    private final BufferStrategy bufferStrategy;

    private final GraphicsConfiguration graphicsConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    public SpaceGame() {
        gameObjects = (Collections.synchronizedList(new ArrayList<GameObject>()));
        images = new HashMap<>();

        PlayerRocket player;
        player = new PlayerRocket(this, loadImage("Warship.png"));

        trackCameraTo = player;
        zoom = 0.1;

        player.setPosition(new Vector2(80000, 1500));
        player.setRotation(Math.PI/2);
        player.setVelocity(new Vector2(0, 0.2));

        //GravitationalSprite planet = new GravitationalSprite(this,
        //        new BufferedImage[] { loadImage("Earth0.png"), loadImage("Earth1.png"), loadImage("Earth2.png"), loadImage("Earth3.png") },
        //        1024, 500000, 50000);
        GravitationalSprite planet = new GravitationalSprite(this, loadImage("Earth.png"),
                1024, 50000, 2500);
        planet.setPosition(new Vector2(80000, 0));
        //planet.setRotationalVelocity(0.001);

        GravitationalSprite moon = new GravitationalSprite(this, loadImage("Moon.png"), 256, 5000, 400);
        moon.setPosition(new Vector2(84000, 0));

        Sun sun = new Sun(this, 1600, 1e7, 6e4);
        sun.setPosition(new Vector2(0, 0));

        planet.orbit(sun);
        moon.orbit(planet);
        player.orbit(planet);

        Sprite other = new Sprite(this, loadImage("Rocket.png"));
        other.setPosition(new Vector2(80, 150));

        frame = new JFrame("Escape Velocity");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                running = false;
            }
        });

        final Dimension gameSize = new Dimension(900, 600);
        final SpaceGame game = this;

        setPreferredSize(gameSize);
        setMinimumSize(gameSize);
        setMaximumSize(gameSize);

        setFocusable(true);
        requestFocusInWindow();

        setIgnoreRepaint(true);

        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                zoom *= 1.05;
            }
            if (e.getWheelRotation() > 0) {
                zoom *= 0.95;
            }
        });

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                synchronized (gameObjects) {
                    Iterator<GameObject> i = gameObjects.iterator(); // Must be in synchronized block
                    while (i.hasNext()) {
                        GameObject go = i.next();
                        if (go instanceof KeyListener) {
                            ((KeyListener) go).keyTyped(e);
                        }
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (gameObjects) {
                    Iterator<GameObject> i = gameObjects.iterator(); // Must be in synchronized block
                    while (i.hasNext()) {
                        GameObject go = i.next();
                        if (go instanceof KeyListener) {
                            ((KeyListener) go).keyPressed(e);
                        }
                    }
                }

                if (e.getKeyChar() == '+') {
                    zoom *= 1.05;
                }
                if (e.getKeyChar() == '-') {
                    zoom *= 0.95;
                }

                if (e.getKeyChar() == 't') {
                    turbo = !turbo;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                synchronized (gameObjects) {
                    Iterator<GameObject> i = gameObjects.iterator(); // Must be in synchronized block
                    while (i.hasNext()) {
                        GameObject go = i.next();
                        if (go instanceof KeyListener) {
                            ((KeyListener) go).keyReleased(e);
                        }
                    }
                }
            }
        });

        frame.add(this);
        frame.pack();

//        Timer updateTimer = new Timer(UPDATE_TIME, e -> {
//            update();
//        });
//        updateTimer.start();

        spawnObject(player);
        spawnObject(planet);
        spawnObject(moon);
        spawnObject(sun);

        frame.setVisible(true);

        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();

        run();
    }

    public void run() {
        running = true;
        while (running) {
            long updateStart = System.nanoTime();
            update();

            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            paint(g);

            g.dispose();
            bufferStrategy.show();

            long renderTime = (System.nanoTime() - updateStart) / 1000000;
            try {
                Thread.sleep((long) Math.max(0, (1.0 / 60 * 1000) - renderTime));
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
        }
        frame.dispose();
    }

    public BufferedImage loadImage(String path) {
        if (images.containsKey(path))
            return images.get(path);

        BufferedImage image;
        final BufferedImage compatible;
        try {
            image = ImageIO.read(new File(path));

            compatible = graphicsConfig.createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());
            Graphics g = compatible.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        images.put(path, compatible);

        return compatible;
    }

    public void spawnObject(GameObject object) {
        gameObjects.add(object);
    }

    public void update() {
        for (int it = 0; it < (turbo ? 32 : 1); it++) {
            synchronized (gameObjects) {
                for (int i = gameObjects.size() - 1; i >= 0; i--) {
                    GameObject objA = gameObjects.get(i);
                    if (objA instanceof Collidable) {
                        for (int j = i - 1; j >= 0; j--) {
                            GameObject objB = gameObjects.get(j);
                            if (objB instanceof Collidable) {
                                Collidable a = ((Collidable) objA);
                                Collidable b = ((Collidable) objB);

                                for (Collider aCol : a.getColliders()) {
                                    for (Collider bCol : b.getColliders()) {
                                        if (aCol.checkCollision(bCol)) {
                                            a.onCollide(aCol, bCol, b);
                                            b.onCollide(bCol, aCol, a);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ArrayList<GameObject> removeObjects = new ArrayList<>();
                for (int i = gameObjects.size() - 1; i >= 0; i--) {
                    GameObject go = gameObjects.get(i);
                    if (go == null) {
                        System.out.println("null game object");
                        gameObjects.remove(i);
                        continue;
                    }
                    if (!go.alive) {
                        removeObjects.add(go);
                        continue;
                    }

                    go.update();
                }
                for (GameObject go : removeObjects) {
                    gameObjects.remove(go);
                }
            }
        }

        //panel.repaint();
    }

    public void paint(Graphics g) {
        final Dimension gameSize = getSize();

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, (int) gameSize.getWidth(), (int) gameSize.getHeight());

        Vector2 screen  = new Vector2(900, 600);
        screen.divide(2 * zoom, 2 * zoom);
        Vector2 center = new Vector2(trackCameraTo.position);
        center.add(32);

        AffineTransform at = g2d.getTransform();

        at.scale(zoom, zoom);
        at.translate(screen.getX(), screen.getY());
        at.translate(-center.getX(), -center.getY());

        g2d.setTransform(at);


        for (int i = gameObjects.size() - 1; i >= 0; i--) {
            GameObject go = gameObjects.get(i);
            go.paint(g2d);
        }
    }
}
