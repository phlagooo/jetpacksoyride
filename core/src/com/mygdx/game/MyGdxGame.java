package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;
import static com.mygdx.game.Fireball.*;
import static com.mygdx.game.Rock.*;
import static com.mygdx.game.Coin.*;
import static com.mygdx.game.Potion.*;

public class MyGdxGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture bgImage;
    private Rectangle steve;
    //private Circle stone;
    private int steveAcc = -80;
    private int steve_y_speed;
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private final int WARNING_WIDTH = 100;
    private final int WARNING_HEIGHT = 100;
    private int backgroundSpeed = 6;
    private int fireballSpeed = 14; //

    final int FLOOR_Y = 100;
    private Array<Rock> rocks;
    private Array<Coin> coins;
    private Fireball fireball;
    private Array<Potion> potions;
    float sourceX = 0; // Keep track of background
    private Random rand = new Random();
    private int prevRockIndex;
    private int prevCoinIndex;
    private int prevPotionIndex;
    private Music mainMusic;
    private Sound deathSound;
    private Sound jumpSound;
    private Sound powerUpSound;
    float elapsedTime;
    private Sound coinSound;
    private int xStartPos = 100;
    private int yStartPos = FLOOR_Y;
    private int deathCount;
    private int survivedFrames;
    private BitmapFont font;
    boolean fireballLive = false;
    float timeSinceFireballStart;
    // Movement textures / animations
    private Texture jump;
    Animation<TextureRegion> warningAnimation;
    Animation<TextureRegion> currentSteveAnimationState;
    Animation<TextureRegion> runAnimation;
    Animation<TextureRegion> fallAnimation;

    @Override
    public void create() {
        rocks = new Array<>();
        coins = new Array<>();
        potions = new Array<>();
        // Create all rocks and coins to be used
        for (int i = 0; i < ROCK_COUNT; i++) {
            rocks.add(new Rock((i + 1) * (rand.nextInt(ROCKFLUCTUATION) + ROCKMINIMUM_GAP) + WIDTH));
        }
        for (int i = 0; i < POTION_COUNT; i++) {
            potions.add(new Potion((i+1) * (rand.nextInt(POTIONFLUCTUATION) + POTIONMINIMUM_GAP) + WIDTH));
        }
        for (int i = 0; i < COIN_COUNT; i++) {
            coins.add(new Coin((i + 1) * (rand.nextInt(COINFLUCTUATION) + COINMINIMUM_GAP) + WIDTH, 200 + rand.nextInt(COINFLUCTUATION)));
        }
        fireball = new Fireball(WIDTH, 0);
        batch = new SpriteBatch();
        bgImage = new Texture("background.jpg");
        bgImage.setWrap(Repeat, Repeat);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WIDTH, HEIGHT);
        font = new BitmapFont();
        font.setColor(Color.BLACK);

        mainMusic = Gdx.audio.newMusic(Gdx.files.internal("joyrideTheme.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("death.mp3"));
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump (on chicken).mp3"));
        powerUpSound = Gdx.audio.newSound(Gdx.files.internal("powerup.mp3"));

        // Reference: https://gamedev.stackexchange.com/questions/136659/is-it-possible-to-use-animated-gif-images-in-lbgdx
        runAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("run.gif").read());
        fallAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("fall.gif").read());
        warningAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("warning.gif").read());

        currentSteveAnimationState = runAnimation;
        jump = new Texture("jump.png");
        coinSound = Gdx.audio.newSound(Gdx.files.internal("coin.mp3"));
        mainMusic.setLooping(true);
        mainMusic.setVolume(0.2f);
        mainMusic.play();

        steve = new Rectangle();
        steve.x = xStartPos;
        steve.y = yStartPos;
        steve.width = 140;
        steve.height = 140;
    }

    @Override
    public void render() {
        if (!fireballLive && survivedFrames % 60 == 1) {
            int fireballOdds = rand.nextInt(4);
            if (fireballOdds == 1) {
                fireballLive = true;
                timeSinceFireballStart = 0;
                fireball.reposition(WIDTH, rand.nextInt(HEIGHT - FIREBALL_HEIGHT - MIN_Y_VALUE) + MIN_Y_VALUE);
            }
        }

        ScreenUtils.clear(0, 0, 0, 1);
        elapsedTime += Gdx.graphics.getDeltaTime();
        sourceX += backgroundSpeed;
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Prevent player from clipping the top of the window
        if (steve.y + steve.height >= HEIGHT) {
            steve.y = HEIGHT - steve.height;
            steve_y_speed += steveAcc;
        }

        drawFrame();

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            jumpSound.play(0.1f);
            steve_y_speed = 1200;
        }

        handleGravity();
    }

    private void drawFrame() {
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
        handleRock();
        handleCoins();

        if (fireballLive) {
            timeSinceFireballStart += Gdx.graphics.getDeltaTime();
            //timeSinceFireballStart = 4;
            handleFireball();
        }

        if (steve.y > FLOOR_Y && steve_y_speed >= 0) {
            // Steve moving upwards
            batch.draw(jump, steve.x, steve.y, steve.width, steve.height);
        } else {
            // Steve is either running or falling
            batch.draw(currentSteveAnimationState.getKeyFrame(elapsedTime), steve.x, steve.y, steve.width, steve.height);
        }
        font.draw(batch, Integer.toString(survivedFrames / 60), WIDTH - 70, HEIGHT - 20);
        batch.end(); // Frame finished
    }

    private void handleGravity() {
        // Handle gravity
        steve.y += steve_y_speed * Gdx.graphics.getDeltaTime();
        if (steve.y > FLOOR_Y) { // Steve is in the air
            steve_y_speed += steveAcc;
            currentSteveAnimationState = fallAnimation;
        } else if (steve.y == FLOOR_Y) {
            steve_y_speed = 0;
            currentSteveAnimationState = runAnimation;
        } else if (steve.y < FLOOR_Y) { // Steve is below the floor
            steve_y_speed = 0;
            steve.y = FLOOR_Y;
        }
    }

    private void handleFireball() {
        if (timeSinceFireballStart < 3) { // Show warning sign for 3 seconds
            batch.draw(warningAnimation.getKeyFrame(timeSinceFireballStart),
                    WIDTH - WARNING_WIDTH - 50,
                    fireball.getY(),
                    WARNING_WIDTH,
                    WARNING_HEIGHT);
        } else {
            if (steve.overlaps(fireball.bounds)) {
                resetGame();
            } else if (fireball.getX() < -WIDTH) {
                fireballLive = false;
            } else {
                fireball.reposition(fireball.getX() - fireballSpeed, fireball.getY());
                batch.draw(fireball.getFireball().getKeyFrame(elapsedTime),
                        fireball.getX(),
                        fireball.getY(),
                        FIREBALL_WIDTH,
                        FIREBALL_HEIGHT);
            }
        }
    }

    private void handleRock() {
        survivedFrames++;
        for (int i = 0; i < rocks.size; i++) {
            // Check for collision
            if (steve.overlaps(rocks.get(i).bounds)) {
                resetGame();
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
    }

    private void handleCoins() {
        for (int i = 0; i < coins.size; i++) {
            // Check for collision
            if (steve.overlaps(coins.get(i).bounds)) {
                survivedFrames *= 2;
                coinSound.play();
                coins.get(i).reposition(coins.get(prevCoinIndex).getPosCoin().x + rand.nextInt(COINFLUCTUATION) + COINMINIMUM_GAP, 200 + rand.nextInt(COINFLUCTUATION));
            }
            // If a coin is to the left of the visible window, move it to the right of the window
            if (coins.get(i).getPosCoin().x < -WIDTH) {
                coins.get(i).reposition(coins.get(prevCoinIndex).getPosCoin().x + rand.nextInt(COINFLUCTUATION) + COINMINIMUM_GAP, coins.get(i).getPosCoin().y);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            coins.get(i).reposition(coins.get(i).getPosCoin().x - backgroundSpeed, coins.get(i).getPosCoin().y);
            // Index the rightmost coin
            prevCoinIndex = i;
            batch.draw(coins.get(i).getCoin(), coins.get(i).getPosCoin().x, coins.get(i).getPosCoin().y, COIN_WIDTH, COIN_HEIGHT);
        }
        for (int i = 0; i < potions.size; i++) {
            // Check for collision
            if (steve.overlaps(potions.get(i).bounds)) {
                powerUpSound.play();
                potions.get(i).reposition(potions.get(prevPotionIndex).getPosPotion().x + rand.nextInt(POTIONFLUCTUATION) + POTIONMINIMUM_GAP);
                backgroundSpeed += 3;
            }
            // If a rock is to the left of the visible window, move it to the right of the window
            if (potions.get(i).getPosPotion().x < -WIDTH || rocks.get(i).bounds.overlaps(potions.get(i).bounds)) {
                potions.get(i).reposition(potions.get(prevPotionIndex).getPosPotion().x + rand.nextInt(POTIONFLUCTUATION) + POTIONMINIMUM_GAP);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            potions.get(i).reposition(potions.get(i).getPosPotion().x - backgroundSpeed);
            // Index the rightmost rock
            prevPotionIndex = i;
            batch.draw(potions.get(i).getPotion(), potions.get(i).getPosPotion().x, POTION_Y, POTION_WIDTH, POTION_HEIGHT);
        }
    }

    private void resetGame() {
        deathSound.play();
        deathCount++;
        sourceX = 0;
        survivedFrames = 0;
        fireballLive = false;
        fireball.reposition(WIDTH * 2,
                rand.nextInt(HEIGHT - FIREBALL_HEIGHT - MIN_Y_VALUE) + MIN_Y_VALUE);
        // Randomly arrange all crates to the right of the screen
        for (int j = 0; j < rocks.size; j++) {
            rocks.get(j).reposition((j + 1) * (rand.nextInt(ROCKFLUCTUATION) + ROCKMINIMUM_GAP) + WIDTH);
        }
        for (int i = 0; i < COIN_COUNT; i++) {
            coins.get(i).reposition((i + 1) * (rand.nextInt(COINFLUCTUATION) + COINMINIMUM_GAP) + WIDTH, 200 + rand.nextInt(COINFLUCTUATION));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}