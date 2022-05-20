package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

public class Crate extends Obstacle {
    public static final int CRATE_Y = 90;

    // Constructor
    public Crate(float x) {
        super(x, CRATE_Y, 110, 110);
        image = new Texture("crate.png");
    }
}
