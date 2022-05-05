package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

public class MyGdxGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture steveImage;
    private Texture stoneImage;
    private Texture bgImage;
    private Rectangle steve;
    private Circle stone;
    private int steveAcc = -80;
    private int steveSpeed;
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    final int BACKGROUND_SPEED = 6; //
    final int FLOOR_Y = 100;
    float sourceX = 0; // Keep track of background

    @Override
    public void create() {
        batch = new SpriteBatch();
        steveImage = new Texture("badlogic.jpg");
        stoneImage = new Texture("rock.png");
        bgImage = new Texture("background.jpg");
        bgImage.setWrap(Repeat, Repeat);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, WIDTH, HEIGHT);

        steve = new Rectangle();
        steve.x = 100;
        steve.y = 0;
        steve.height = 50;
        steve.width = 30;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        sourceX += BACKGROUND_SPEED;
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        sourceX %= bgImage.getWidth();
        // Moving background
        batch.draw(bgImage,
                // position and size of texture
                0, 0, WIDTH, HEIGHT,
                // srcX, srcY, srcWidth, srcHeight
                (int) sourceX, 0, bgImage.getWidth(), bgImage.getHeight(),
                // flipX, flipY
                false, false);
        batch.draw(steveImage, steve.x, steve.y);
        batch.end();

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            steveSpeed = 1200;
        }

        steve.y += steveSpeed * Gdx.graphics.getDeltaTime();

        if (steve.y > FLOOR_Y) {
            steveSpeed += steveAcc;
        } else if (steve.y == FLOOR_Y) {
            steveSpeed = 0;
        } else if (steve.y < FLOOR_Y) {
            steveSpeed = 0;
            steve.y = FLOOR_Y;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}