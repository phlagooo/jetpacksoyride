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
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;
import static com.mygdx.game.Rock.*;

public class MyGdxGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture bgImage;
    private Rectangle steve;
    private Circle stone;
    private int steveAcc = -80;
    private int steve_y_speed;
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
    float elapsedTime;
    private int xStartPos = 100;
    private int yStartPos = FLOOR_Y;
    private int deathCount;
    private int survivedFrames;
    private BitmapFont font;
    // Movement textures / animations
    private Texture jump;
    Animation<TextureRegion> currentSteveAnimationState;
    Animation<TextureRegion> runAnimation;
    Animation<TextureRegion> fallAnimation;


    @Override
    public void create() {
        rocks = new Array<>();
        // Create all rocks to be used
        for (int i = 0; i < ROCK_COUNT; i++) {
            rocks.add(new Rock((i + 1) * (rand.nextInt(FLUCTUATION) + MINIMUM_GAP) + WIDTH));
        }
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
        // Reference: https://gamedev.stackexchange.com/questions/136659/is-it-possible-to-use-animated-gif-images-in-lbgdx
        runAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("run.gif").read());
        fallAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("fall.gif").read());
        currentSteveAnimationState = runAnimation;
        jump = new Texture("jump.png");
        mainMusic.setLooping(true);
        mainMusic.setVolume(0.6f);
        mainMusic.play();

        steve = new Rectangle();
        steve.x = xStartPos;
        steve.y = yStartPos;
        steve.width = 140;
        steve.height = 140;
    }

    @Override
    public void render() {
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

        if (steve.y > FLOOR_Y && steve_y_speed >= 0) {
            // Steve moving upwards
            batch.draw(jump, steve.x, steve.y, steve.width, steve.height);
        } else {
            // Steve is either running or falling
            batch.draw(currentSteveAnimationState.getKeyFrame(elapsedTime), steve.x, steve.y, steve.width, steve.height);
        }
        font.draw(batch, Integer.toString(survivedFrames / 60), WIDTH - 30, HEIGHT - 20);
        batch.end(); // Frame finished

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            jumpSound.play(0.5f);
            steve_y_speed = 1200;
        }

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

    private void collisionLogic() {
        survivedFrames++;
        for (int i = 0; i < rocks.size; i++) {
            // Check for collision
            if (steve.overlaps(rocks.get(i).bounds)) {
                deathSound.play();
                deathCount++;
                sourceX = 0;
                survivedFrames = 0;
                // Randomly arrange all crates to the right of the screen
                for (int j = 0; j < rocks.size; j++) {
                    rocks.get(j).reposition((j + 1) * (rand.nextInt(FLUCTUATION) + MINIMUM_GAP) + WIDTH);
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