package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Potion {
    private final Texture potion;
    private final Vector2 pos;
    public static final int POTION_HEIGHT = 150;
    public static final int POTION_WIDTH = 150;
    public static final int POTION_COUNT = 1;
    public static final int POTION_Y = 90;
    public Rectangle bounds;

    // Obstacle placement
    public static final int POTION_FLUCTUATION = 300;
    public static final int POTION_MINIMUM_GAP = 10000; // Min gap between two obstacles

    // Constructor
    public Potion(float x) {
        potion = new Texture("potion.png");
        pos = new Vector2(x, POTION_Y);
        bounds = new Rectangle(pos.x, pos.y, POTION_WIDTH, POTION_HEIGHT);
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

    public Texture getPotion() {
        return potion;
    }

    public Vector2 getPosPotion() {
        return pos;
    }
}

