package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

public class Coin extends Obstacle {
    // Constructor
    public Coin(float x, float y) {
        super(x, y, 100, 100);
        image = new Texture("coin.png");
    }
}