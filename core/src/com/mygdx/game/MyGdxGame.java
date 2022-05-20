package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;
import static com.mygdx.game.Crate.CRATE_Y;
import static com.mygdx.game.Fireball.MIN_Y_VALUE;
import static com.mygdx.game.Potion.POTION_Y;

/**
 * A 2d endless runner with the objective of surviving as long as possible.
 * The movement input is space to jump
 * Collectables:
 * - Coins: increases the player's score
 * - Potions: increases the player's speed
 * Obstacles:
 * - Crates: game ends on collision with the player
 * - Fireballs: Sent after a short warning, game ends on collision with the player
 */
public class MyGdxGame extends ApplicationAdapter {

    enum Screen {
        TITLE, MAIN_GAME, GAME_OVER
    }

    Screen currentScreen = Screen.TITLE;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture bgImage;
    private Rectangle steve;
    //private Circle stone;
    private final int steveAcc = -80;
    private int steve_y_speed;
    private final int WIDTH = 1280;
    private final int HEIGHT = 720;
    private final int WARNING_WIDTH = 100;
    private final int WARNING_HEIGHT = 100;
    private int backgroundSpeed = 6;
    private int fireballSpeed = 14; //
    private final int CRATE_COUNT = 3;
    private final int CRATE_FLUCTUATION = 300;
    private final int CRATE_MINIMUM_GAP = 600;
    private final int POTION_COUNT = 1;
    private final int POTION_FLUCTUATION = 300;
    private final int POTION_MINIMUM_GAP = 10000;
    public final int POTION_SPEEDUP = 2;
    private final int COIN_COUNT = 1;
    private final int COIN_FLUCTUATION = 300;
    private final int COIN_MINIMUM_GAP = 800;
    final int FLOOR_Y = 100;
    private Array<Crate> crates;
    private Array<Coin> coins;
    private Fireball fireball;
    private Array<Potion> potions;
    float sourceX = 0; // Keep track of background
    private final Random rand = new Random();
    private int prevCrateIndex;
    private int prevCoinIndex;
    private int prevPotionIndex;
    private Music mainMusic;
    private Sound deathSound;
    private Sound jumpSound;
    private Sound powerUpSound;
    float elapsedTime;
    private Sound coinSound;
    private final int xStartPos = 100;
    private final int yStartPos = FLOOR_Y;
    private int deathCount;
    private int survivedFrames;
    private BitmapFont font;
    private BitmapFont fontclick;
    boolean fireballLive = false;
    float timeSinceFireballStart;
    // Movement textures / animations
    private Texture jump;
    Animation<TextureRegion> warningAnimation;
    Animation<TextureRegion> currentSteveAnimationState;
    Animation<TextureRegion> runAnimation;
    Animation<TextureRegion> fallAnimation;

    private ArrayList<Integer> highscores;

    /**
     * Initialize the game by spawning all objects
     * and assigning assets
     */
    @Override
    public void create() {
        crates = new Array<>();
        coins = new Array<>();
        potions = new Array<>();
        // Create all crates and coins to be used
        for (int i = 0; i < CRATE_COUNT; i++) {
            crates.add(new Crate((i + 1) * (rand.nextInt(CRATE_FLUCTUATION) + CRATE_MINIMUM_GAP) + WIDTH));
        }
        for (int i = 0; i < POTION_COUNT; i++) {
            potions.add(new Potion((i + 1) * (rand.nextInt(POTION_FLUCTUATION) + POTION_MINIMUM_GAP) + WIDTH));
        }
        for (int i = 0; i < COIN_COUNT; i++) {
            coins.add(new Coin((i + 1) * (rand.nextInt(COIN_FLUCTUATION) + COIN_MINIMUM_GAP) + WIDTH, 200 + rand.nextInt(COIN_FLUCTUATION)));
        }
        fireball = new Fireball(WIDTH, 0);
        batch = new SpriteBatch();
        bgImage = new Texture("background.jpg");
        bgImage.setWrap(Repeat, Repeat);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, WIDTH, HEIGHT);
        font = new BitmapFont();
        font.setColor(Color.BLACK);
        fontclick = new BitmapFont();
        fontclick.setColor(Color.BLACK);
        highscores = new ArrayList<>();

        mainMusic = Gdx.audio.newMusic(Gdx.files.internal("joyrideTheme.mp3"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("death.mp3"));
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump (on chicken).mp3"));
        powerUpSound = Gdx.audio.newSound(Gdx.files.internal("powerup.mp3"));
        coinSound = Gdx.audio.newSound(Gdx.files.internal("coin.mp3"));

        // Reference: https://gamedev.stackexchange.com/questions/136659/is-it-possible-to-use-animated-gif-images-in-lbgdx
        runAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("run.gif").read());
        fallAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("fall.gif").read());
        warningAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("warning.gif").read());

        currentSteveAnimationState = runAnimation;
        jump = new Texture("jump.png");

        steve = new Rectangle();
        steve.x = xStartPos;
        steve.y = yStartPos;
        steve.width = 140;
        steve.height = 140;
    }

    /**
     * Handle game states
     */
    @Override
    public void render() {
        if (currentScreen == Screen.TITLE) {
            ScreenUtils.clear(0, 0, 0, 1);

            batch.begin();
            batch.draw(bgImage,
                    // position and size of texture
                    0, 0, WIDTH, HEIGHT);
            GlyphLayout glyphlayout = new GlyphLayout();
            glyphlayout.setText(fontclick, "Press to play");
            if (Gdx.input.getX() > WIDTH / 2 - glyphlayout.width / 2 && Gdx.input.getX() < WIDTH / 2 + glyphlayout.width / 2 && Gdx.input.getY() < HEIGHT - HEIGHT / 4 && Gdx.input.getY() > HEIGHT - HEIGHT / 4 - glyphlayout.height) {
                fontclick.setColor(Color.WHITE);
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    currentScreen = Screen.MAIN_GAME;
                    fontclick.setColor(Color.BLACK);
                }
            } else {
                fontclick.setColor(Color.BLACK);
            }
            fontclick.draw(batch, glyphlayout, WIDTH / 2 - glyphlayout.width / 2, HEIGHT / 4 + glyphlayout.height);
            batch.end();

        } else if (currentScreen == Screen.MAIN_GAME) {
            mainMusic.setLooping(true);
            mainMusic.setVolume(0.2f);
            mainMusic.play();
            if (!fireballLive && survivedFrames % 60 == 1) {
                int fireballOdds = rand.nextInt(4);
                if (fireballOdds == 1) {
                    fireballLive = true;
                    timeSinceFireballStart = 0;
                    fireball.reposition(WIDTH, rand.nextInt(HEIGHT - fireball.HEIGHT - MIN_Y_VALUE) + MIN_Y_VALUE);
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
        } else if (currentScreen == Screen.GAME_OVER) {
            mainMusic.stop();
            ScreenUtils.clear(.25f, 0, 0, 1);

            batch.begin();
            batch.draw(bgImage,
                    // position and size of texture
                    0, 0, WIDTH, HEIGHT);
            GlyphLayout restart = new GlyphLayout();
            restart.setText(fontclick, "Press to restart");
            GlyphLayout score = new GlyphLayout();
            score.setText(font, Integer.toString(survivedFrames / 60));
            GlyphLayout scoretext = new GlyphLayout();
            scoretext.setText(font, "Score:");
            if (Gdx.input.getX() > WIDTH / 2 - restart.width / 2 && Gdx.input.getX() < WIDTH / 2 + restart.width / 2 && Gdx.input.getY() < HEIGHT - HEIGHT / 4 && Gdx.input.getY() > HEIGHT - HEIGHT / 4 - restart.height) {
                fontclick.setColor(Color.WHITE);
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    currentScreen = Screen.MAIN_GAME;
                    survivedFrames = 0;
                    fontclick.setColor(Color.BLACK);
                }
            } else {
                fontclick.setColor(Color.BLACK);
            }

            GlyphLayout highscorestext = new GlyphLayout();
            highscorestext.setText(font, "Highscores:");
            font.draw(batch, highscorestext, WIDTH / 2 - highscorestext.width / 2, HEIGHT / 4 + 420);
            for (int i = 0; i < highscores.size(); i++) {
                GlyphLayout highscore = new GlyphLayout();
                highscore.setText(font, Integer.toString(highscores.get(i)));
                font.draw(batch, highscore, WIDTH / 2 - highscore.width / 2, HEIGHT / 4 + 400 - score.height * i * 2);
            }
            fontclick.draw(batch, restart, WIDTH / 2 - restart.width / 2, HEIGHT / 4 + restart.height);
            font.draw(batch, score, WIDTH / 2 - score.width / 2, HEIGHT / 4 + score.height + 80);
            font.draw(batch, scoretext, WIDTH / 2 - scoretext.width / 2, HEIGHT / 4 + scoretext.height * 3 + 80);
            batch.end();
        }
    }

    private void drawFrame() {
        survivedFrames++;

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
        handleCrate();
        handleCoins();
        handlePotion();

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
                    fireball.getPos().y,
                    WARNING_WIDTH,
                    WARNING_HEIGHT);
        } else {
            if (steve.overlaps(fireball.bounds)) {
                resetGame();
            } else if (fireball.getPos().x < -WIDTH) {
                fireballLive = false;
            } else {
                fireball.reposition(fireball.getPos().x - fireballSpeed, fireball.getPos().y);
                batch.draw(fireball.getFireball().getKeyFrame(elapsedTime),
                        fireball.getPos().x,
                        fireball.getPos().y,
                        fireball.WIDTH,
                        fireball.HEIGHT);
            }
        }
    }

    private void handleCrate() {
        for (int i = 0; i < crates.size; i++) {
            // Check for collision
            if (steve.overlaps(crates.get(i).bounds)) {
                resetGame();
                break;
            }
            // If a crate is to the left of the visible window, move it to the right of the window
            if (crates.get(i).getPos().x < -WIDTH) {
                crates.get(i).reposition(crates.get(prevCrateIndex).getPos().x + rand.nextInt(CRATE_FLUCTUATION) + CRATE_MINIMUM_GAP + 400);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            crates.get(i).reposition(crates.get(i).getPos().x - backgroundSpeed);

            // Index the rightmost crate
            prevCrateIndex = i;
            batch.draw(crates.get(i).get(), crates.get(i).getPos().x, CRATE_Y, crates.get(i).WIDTH, crates.get(i).HEIGHT);
        }
    }

    private void handleCoins() {
        for (int i = 0; i < coins.size; i++) {
            // Check for collision
            if (steve.overlaps(coins.get(i).bounds)) {
                coinSound.play();
                coins.get(i).reposition(coins.get(prevCoinIndex).getPos().x + rand.nextInt(COIN_FLUCTUATION) + COIN_MINIMUM_GAP, 200 + rand.nextInt(COIN_FLUCTUATION));
                survivedFrames *= 2;
            }
            // If a coin is to the left of the visible window, move it to the right of the window
            if (coins.get(i).getPos().x < -WIDTH) {
                coins.get(i).reposition(coins.get(prevCoinIndex).getPos().x + rand.nextInt(COIN_FLUCTUATION) + COIN_MINIMUM_GAP, coins.get(i).getPos().y);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            coins.get(i).reposition(coins.get(i).getPos().x - backgroundSpeed, coins.get(i).getPos().y);
            // Index the rightmost coin
            prevCoinIndex = i;
            batch.draw(coins.get(i).get(), coins.get(i).getPos().x, coins.get(i).getPos().y, coins.get(i).WIDTH, coins.get(i).HEIGHT);
        }
    }

    private void handlePotion() {
        for (int i = 0; i < potions.size; i++) {
            // Check for collision
            if (steve.overlaps(potions.get(i).bounds)) {
                powerUpSound.play();
                potions.get(i).reposition(potions.get(prevPotionIndex).getPos().x + rand.nextInt(POTION_FLUCTUATION) + POTION_MINIMUM_GAP);
                backgroundSpeed += POTION_SPEEDUP;
                fireballSpeed += POTION_SPEEDUP;
            }
            // If a potion is to the left of the visible window, move it to the right of the window
            if (potions.get(i).getPos().x < -WIDTH || crates.get(i).bounds.overlaps(potions.get(i).bounds)) {
                potions.get(i).reposition(potions.get(prevPotionIndex).getPos().x + rand.nextInt(POTION_FLUCTUATION) + POTION_MINIMUM_GAP);
            }
            // Use reposition() in order to move the bounds as well, and not just the Texture
            potions.get(i).reposition(potions.get(i).getPos().x - backgroundSpeed);
            // Index the rightmost crate
            prevPotionIndex = i;
            batch.draw(potions.get(i).get(), potions.get(i).getPos().x, POTION_Y, potions.get(i).WIDTH, potions.get(i).HEIGHT);
        }
    }

    private void resetGame() {
        deathSound.play();
        deathCount++;
        sourceX = 0;
        fireballLive = false;
        backgroundSpeed = 6;
        fireball.reposition(WIDTH * 2,
                rand.nextInt(HEIGHT - fireball.HEIGHT - MIN_Y_VALUE) + MIN_Y_VALUE);
        // Randomly arrange all crates to the right of the screen
        for (int j = 0; j < crates.size; j++) {
            crates.get(j).reposition((j + 1) * (rand.nextInt(CRATE_FLUCTUATION) + CRATE_MINIMUM_GAP) + WIDTH);
        }
        for (int i = 0; i < COIN_COUNT; i++) {
            coins.get(i).reposition((i + 1) * (rand.nextInt(COIN_FLUCTUATION) + COIN_MINIMUM_GAP) + WIDTH, 200 + rand.nextInt(COIN_FLUCTUATION));
        }
        for (int i = 0; i < POTION_COUNT; i++) {
            potions.get(i).reposition((i + 1) * (rand.nextInt(POTION_FLUCTUATION) + POTION_MINIMUM_GAP) + WIDTH);
        }
        if (highscores.size() < 10) {
            highscores.add(survivedFrames / 60);
            Collections.sort(highscores, Collections.<Integer>reverseOrder());
        } else {
            if (highscores.get(9) < survivedFrames / 60) {
                highscores.remove(9);
                highscores.add(survivedFrames / 60);
                Collections.sort(highscores, Collections.<Integer>reverseOrder());
            }
        }
        currentScreen = Screen.GAME_OVER;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
