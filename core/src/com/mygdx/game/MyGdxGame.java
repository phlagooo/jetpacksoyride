package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;
import static com.mygdx.game.Rock.*;

public class MyGdxGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture steveImage;
    private Texture bgImage;
    private Rectangle steve;
    private Circle stone;
    private int steveAcc = -80;
    private int steveSpeed;
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private int backgroundSpeed = 6; //
    final int FLOOR_Y = 100;
    private Array<Rock> rocks;
    float sourceX = 0; // Keep track of background
    private Random rand = new Random();
    private int prevRockIndex;
    private Music mainMusic;
    private Sound deathSound;
    private Sound jumpSound;
    private int xStartPos = 100;
    private int yStartPos = FLOOR_Y;
    private int deathCount;


    @Override
    public void create() {
        rocks = new Array<>();
        // Create all rocks to be used
        for (int i = 0; i < ROCK_COUNT; i++) {
            rocks.add(new Rock((i+1) * (rand.nextInt(FLUCTUATION) + MINIMUM_GAP) + WIDTH));
        }
        batch = new SpriteBatch();
        steveImage = new Texture("belle.png");
        bgImage = new Texture("background.jpg");
        bgImage.setWrap(Repeat, Repeat);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WIDTH, HEIGHT);
        mainMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("death.mp3"));
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump (on chicken).mp3"));
        mainMusic.setLooping(true);
        mainMusic.play();

        steve = new Rectangle();
        steve.x = xStartPos;
        steve.y = yStartPos;
        steve.width = 150;
        steve.height = 180;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        sourceX += backgroundSpeed;
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Prevent player from clipping the top of the window
        if (steve.y + steve.height >= HEIGHT) {
            steve.y = HEIGHT - steve.height;
            steveSpeed += steveAcc;
        }

        // Start render
        batch.begin();
        sourceX %= bgImage.getWidth();

        // Draw background
        batch.draw(bgImage,
                // position and size of texture
                0, 0, WIDTH, HEIGHT,
                // srcX, srcY, srcWidth, srcHeight
                (int) sourceX, 0, bgImage.getWidth(), bgImage.getHeight(),
                // flipX, flipY
                false, false);

        // Handle and draw obstacles
        collisionLogic();

        batch.draw(steveImage, steve.x, steve.y, steve.width, steve.height);
        batch.end(); // Frame finished

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            jumpSound.play();
            steveSpeed = 1200;
        }

        // Handle gravity
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

    private void collisionLogic() {
      for (int i = 0; i < rocks.size; i++) {
            // Check for collision
            if (steve.overlaps(rocks.get(i).bounds)) {
                deathSound.play();
                deathCount++;
                sourceX = 0;
                // Randomly arrange all crates to the right of the screen
                for (int j = 0; j < rocks.size; j++) {
                    rocks.get(j).reposition((j+1) * (rand.nextInt(FLUCTUATION) + MINIMUM_GAP) + WIDTH);
                }
                break;
            }
            // If a rock is to the left of the visible window, move it to the right of the window
            if (rocks.get(i).getPosRock().x < -WIDTH) {
                rocks.get(i).reposition(rocks.get(prevRockIndex).getPosRock().x + rand.nextInt(FLUCTUATION) + MINIMUM_GAP + 400);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            rocks.get(i).reposition(rocks.get(i).getPosRock().x - backgroundSpeed);
            // Index the rightmost rock
            prevRockIndex = i;
            batch.draw(rocks.get(i).getRock(), rocks.get(i).getPosRock().x, ROCK_Y, ROCK_WIDTH, ROCK_HEIGHT);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}