package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Includes all getters and setters for the game's obstacles
public abstract class Obstacle {
    public Texture image;
    public Vector2 pos;
    public int HEIGHT;
    public int WIDTH;
    public Rectangle bounds;

    public Obstacle(float x, float y, int w, int h) {
        WIDTH = w;
        HEIGHT = h;
        pos = new Vector2(x, y);
        bounds = new Rectangle(pos.x, pos.y, WIDTH, HEIGHT);
    }

    public void reposition(float x) {
        pos.set(x, 100);
        bounds.setPosition(pos.x, pos.y);
    }

    public void reposition(float x, float y) {
        pos.set(x, y);
        bounds.setPosition(pos.x, pos.y);
    }

    public Texture get() {
        return image;
    }

    public Vector2 getPos() {
        return pos;
    }

}
