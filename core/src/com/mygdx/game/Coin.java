package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Coin extends Obstacle {
    // Constructor
    public Coin(float x, float y) {
        super(x, y, 100, 100);
        image = new Texture("coin.png");
    }

}