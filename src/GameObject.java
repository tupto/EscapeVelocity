import java.awt.*;

public abstract class GameObject {
    protected Vector2 position;
    protected SpaceGame game;
    protected boolean alive;

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position.setX(position.getX());
        this.position.setY(position.getY());
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public GameObject(SpaceGame game) {
        this.position = new Vector2();
        this.alive = true;
        this.game = game;
    }

    public abstract void update();
    public abstract void paint(Graphics g);
}
