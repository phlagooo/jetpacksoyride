package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import static com.mygdx.game.Rock.ROCK_HEIGHT;
import static com.mygdx.game.Rock.ROCK_Y;

public class Fireball {
    private final Vector2 pos;
    public static final int FIREBALL_HEIGHT = 90;
    public static final int FIREBALL_WIDTH = 235;
    public static final int MIN_Y_VALUE = ROCK_Y + ROCK_HEIGHT;
    public Rectangle bounds;
    Animation<TextureRegion> fireballAnimation;

    // Constructor
    public Fireball(float x, float y) {
        pos = new Vector2(x, y);
        bounds = new Rectangle(pos.x, pos.y, FIREBALL_WIDTH, FIREBALL_HEIGHT);
        fireballAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("fireball.gif").read());
    }

    public void reposition(float x, float y) {
        pos.set(x, y);
        bounds.setPosition(pos.x, pos.y);
    }

    public Animation<TextureRegion> getFireball() {
        return fireballAnimation;
    }

    public float getX() {
        return pos.x;
    }

    public float getY() {
        return pos.y;
    }
}
