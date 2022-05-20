package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import static com.mygdx.game.Crate.CRATE_Y;

public class Fireball extends Obstacle {
    public static final int MIN_Y_VALUE = CRATE_Y + 110;
    Animation<TextureRegion> fireballAnimation;

    // Constructor
    public Fireball(float x, float y) {
        super(x, y, 235, 90);
        fireballAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("fireball.gif").read());
    }

    public Animation<TextureRegion> getFireball() {
        return fireballAnimation;
    }

}
