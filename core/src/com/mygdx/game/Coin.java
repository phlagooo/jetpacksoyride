package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Coin {
    private final Texture coin;
    private final Vector2 pos;
    public static final int COIN_HEIGHT = 100;
    public static final int COIN_WIDTH = 100;
    public static final int COIN_COUNT = 2;
    public Rectangle bounds;

    // Obstacle placement
    public static final int COIN_FLUCTUATION = 300;
    public static final int COIN_MINIMUM_GAP = 800; // Min gap between two obstacles

    // Constructor
    public Coin(float x, float y) {
        coin = new Texture("coin.png");
        pos = new Vector2(x, y);
        bounds = new Rectangle(pos.x, pos.y, COIN_WIDTH, COIN_HEIGHT);
    }

    /**
     * Move the coin to set X and Y coordinates, this will move both
     * the Texture and the bounds
     *
     * @param x, the X-coordinate to reposition to
     * @param y, the Y-coordinate to reposition to
     */
    public void reposition(float x, float y) {
        pos.set(x, y);
        bounds.setPosition(pos.x, pos.y);
    }

    public Texture getCoin() {
        return coin;
    }

    public Vector2 getPosCoin() {
        return pos;
    }
}