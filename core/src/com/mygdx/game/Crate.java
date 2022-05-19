package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Crate {
    private final Texture crate;
    private final Vector2 pos;
    public static final int CRATE_HEIGHT = 110;
    public static final int CRATE_WIDTH = 110;
    public static final int CRATE_COUNT = 3;
    public static final int CRATE_Y = 90;
    public Rectangle bounds;

    // Obstacle placement
    public static final int CRATE_FLUCTUATION = 300;
    public static final int CRATE_MINIMUM_GAP = 600; // Min gap between two obstacles

    // Constructor
    public Crate(float x) {
        crate = new Texture("crate.png");
        pos = new Vector2(x, CRATE_Y);
        bounds = new Rectangle(pos.x, pos.y, CRATE_WIDTH, CRATE_HEIGHT);
    }

    /**
     * Move the crate to set X-coordinate, this will move both
     * the Texture and the bounds
     *
     * @param x, the X-coordinate to reposition to
     */
    public void reposition(float x) {
        pos.set(x, 100);
        bounds.setPosition(pos.x, pos.y);
    }

    public Texture getCrate() {
        return crate;
    }

    public Vector2 getPosCrate() {
        return pos;
    }
}

