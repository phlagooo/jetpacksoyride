package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Rock {
    private final Texture rock;
    private final Vector2 pos;
    public static final int ROCK_HEIGHT = 150;
    public static final int ROCK_WIDTH = 150;
    public static final int ROCK_COUNT = 4;
    public static final int ROCK_Y = 90;
    public Rectangle bounds;

    // Obstacle placement
    public static final int FLUCTUATION = 300;
    public static final int MINIMUM_GAP = 600; // Min gap between two obstacles

    // Constructor
    public Rock(float x) {
        rock = new Texture("crate.png");
        pos = new Vector2(x, ROCK_Y);
        bounds = new Rectangle(pos.x, pos.y, ROCK_WIDTH, ROCK_HEIGHT);
    }

    /**
     * Move the rock to set X-coordinate, this will move both
     * the Texture and the bounds
     *
     * @param x, the X-coordinate to reposition to
     */
    public void reposition(float x) {
        pos.set(x, 100);
        bounds.setPosition(pos.x, pos.y);
    }

    public Texture getRock() {
        return rock;
    }

    public Vector2 getPosRock() {
        return pos;
    }
}

