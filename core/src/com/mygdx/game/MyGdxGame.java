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

public class MyGdxGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Texture steveImage;
	private Texture stoneImage;
	private Rectangle steve;
	private Circle stone;
	private int steveAcc = -98;
	private int steveSpeed;


	@Override
	public void create () {
		batch = new SpriteBatch();

		steveImage = new Texture("character.png");
		stoneImage = new Texture("rock.png");

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1280, 720);

		steve = new Rectangle();
		steve.x = 1280 / 6;
		steve.y = 0;
		steve.height = 100;
		steve.width = 80;


	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(steveImage, steve.x, steve.y);
		batch.end();


		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
			steveSpeed = 1000;
		}

		steve.y += steveSpeed * Gdx.graphics.getDeltaTime();

		if(steve.y > 0){
			steveSpeed += steveAcc;
		}else if(steve.y == 0){
			steveSpeed = 0;
		}else if(steve.y < 0){
			steveSpeed = 0;
			steve.y = 0;
		}





	}

	@Override
	public void dispose () {
		batch.dispose();
	}
}
