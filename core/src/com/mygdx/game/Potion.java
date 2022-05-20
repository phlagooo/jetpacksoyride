package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

public class Potion extends Obstacle {
    public static final int POTION_Y = 90;

    // Constructor
    public Potion(float x) {
        super(x, POTION_Y, 75, 75);
        image = new Texture("potion.png");
    }
}
