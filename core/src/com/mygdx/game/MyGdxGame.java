package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;
import static com.mygdx.game.Rock.*;
import static com.mygdx.game.Coin.*;

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
    private Array<Coin> coins;
    float sourceX = 0; // Keep track of background
    private Random rand = new Random();
    private int prevRockIndex;
    private int prevCoinIndex;
    private Music mainMusic;
    private Sound deathSound;
    private Sound jumpSound;
    private Sound coinSound;
    private int xStartPos = 100;
    private int yStartPos = FLOOR_Y;
    private int deathCount;
    private int survive;
    private BitmapFont font;


    @Override
    public void create() {
        rocks = new Array<>();
        coins = new Array<>();
        // Create all rocks and coins to be used
        for (int i = 0; i < ROCK_COUNT; i++) {
            rocks.add(new Rock((i+1) * (rand.nextInt(ROCKFLUCTUATION) + ROCKMINIMUM_GAP) + WIDTH));
        }
        for (int i = 0; i < COIN_COUNT; i++) {
            coins.add(new Coin((i+1) * (rand.nextInt(COINFLUCTUATION) + COINMINIMUM_GAP) + WIDTH, 200 + rand.nextInt(COINFLUCTUATION)));
        }
        batch = new SpriteBatch();
        steveImage = new Texture("belle.png");
        bgImage = new Texture("background.jpg");
        bgImage.setWrap(Repeat, Repeat);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WIDTH, HEIGHT);
        font = new BitmapFont();
        font.setColor(Color.BLACK);
        mainMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("death.mp3"));
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump (on chicken).mp3"));
        coinSound = Gdx.audio.newSound(Gdx.files.internal("coin.mp3"));
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
        font.draw(batch, Integer.toString(survive/60), WIDTH - 30, HEIGHT - 20);
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
      survive++;
      for (int i = 0; i < rocks.size; i++) {
            // Check for collision
            if (steve.overlaps(rocks.get(i).bounds)) {
                deathSound.play();
                deathCount++;
                sourceX = 0;
                survive = 0;
                // Randomly arrange all crates to the right of the screen
                for (int j = 0; j < rocks.size; j++) {
                    rocks.get(j).reposition((j+1) * (rand.nextInt(ROCKFLUCTUATION) + ROCKMINIMUM_GAP) + WIDTH);
                }
                break;
            }
            // If a rock is to the left of the visible window, move it to the right of the window
            if (rocks.get(i).getPosRock().x < -WIDTH) {
                rocks.get(i).reposition(rocks.get(prevRockIndex).getPosRock().x + rand.nextInt(ROCKFLUCTUATION) + ROCKMINIMUM_GAP + 400);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            rocks.get(i).reposition(rocks.get(i).getPosRock().x - backgroundSpeed);
            // Index the rightmost rock
            prevRockIndex = i;
            batch.draw(rocks.get(i).getRock(), rocks.get(i).getPosRock().x, ROCK_Y, ROCK_WIDTH, ROCK_HEIGHT);
        }
        for (int i = 0; i < coins.size; i++) {
            // Check for collision
            if (steve.overlaps(coins.get(i).bounds)) {
                survive *= 2;
                coinSound.play();
                coins.get(i).reposition(coins.get(prevCoinIndex).getPosCoin().x + rand.nextInt(COINFLUCTUATION) + COINMINIMUM_GAP + 400, 200 + rand.nextInt(COINFLUCTUATION));
            }
            // If a coin is to the left of the visible window, move it to the right of the window
            if (coins.get(i).getPosCoin().x < -WIDTH) {
                coins.get(i).reposition(coins.get(prevCoinIndex).getPosCoin().x + rand.nextInt(COINFLUCTUATION) + COINMINIMUM_GAP + 400, coins.get(i).getPosCoin().y);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            coins.get(i).reposition(coins.get(i).getPosCoin().x - backgroundSpeed, coins.get(i).getPosCoin().y);
            // Index the rightmost coin
            prevCoinIndex = i;
            batch.draw(coins.get(i).getCoin(), coins.get(i).getPosCoin().x, coins.get(i).getPosCoin().y, COIN_WIDTH, COIN_HEIGHT);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}