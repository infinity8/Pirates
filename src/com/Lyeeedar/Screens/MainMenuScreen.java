package com.Lyeeedar.Screens;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Terrain;
import com.Lyeeedar.Graphics.Clouds;
import com.Lyeeedar.Graphics.Sea;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Weather;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Pirates.PirateGame.Screen;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class MainMenuScreen extends AbstractScreen {
	
	private Table table;
	private PositionalData pData = new PositionalData();
	private SkyBox skybox;
	LightManager lightManager;
	
	public MainMenuScreen(PirateGame game)
	{
		super(game);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
		GLOBALS.SKYBOX = skybox;
		GLOBALS.LIGHTS = lightManager;
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void create() {
		
		BitmapFont font = FileUtils.getFont("data/skins/parchment.ttf", (int)GLOBALS.sclX(40), false);
		
		LabelStyle ls = new LabelStyle();
		ls.font = font;
		ls.fontColor = Color.BLACK;
		TextButtonStyle tbs = new TextButtonStyle();
		tbs.font = font;
		tbs.fontColor = Color.BLACK;
		
		LabelStyle lst = new LabelStyle();
		lst.font = FileUtils.getFont("data/skins/parchment.ttf", (int)GLOBALS.sclX(40), false);
		lst.fontColor = Color.BLACK;
		
		Label lblTitle = new Label("Pirates! Arrrrrr!", lst);
		lblTitle.setFontScale(5);
		
		TextButton btnContinue = new TextButton("Continue", tbs);
		btnContinue.getLabel().setAlignment(Align.left, Align.left);
		btnContinue.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.switchScreen(Screen.GAME);
				return false;
			}
		});
		
		TextButton btnNewGame = new TextButton("New Game", tbs);
		btnNewGame.getLabel().setAlignment(Align.left, Align.left);
		btnNewGame.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.screens.get(Screen.GAME).create();
				game.switchScreen(Screen.GAME);
				return false;
			}
		});

		
		TextButton btnOptions = new TextButton("Options", tbs);
		btnOptions.getLabel().setAlignment(Align.left, Align.left);
		btnOptions.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return false;
			}
		});
		
		TextButton btnCredits = new TextButton("Credits", tbs);
		btnCredits.getLabel().setAlignment(Align.left, Align.left);
		btnCredits.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return false;
			}
		});
		
		TextButton btnExit = new TextButton("Exit", tbs);
		btnExit.getLabel().setAlignment(Align.left, Align.left);
		btnExit.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Gdx.app.exit();
				return false;
			}
		});
		
		TextButton btnInventory = new TextButton("Inventory", tbs);
		btnInventory.getLabel().setAlignment(Align.left, Align.left);
		btnInventory.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.switchScreen(Screen.INVENTORY);
				return false;
			}
		});
		
		TextButton btnCharacter = new TextButton("Character", tbs);
		btnCharacter.getLabel().setAlignment(Align.left, Align.left);
		btnCharacter.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.switchScreen(Screen.CHARACTER);
				return false;
			}
		});



		int width = 300;
		int height = 50;
		int lpad = 30;
		int bpad = 15;
		
		table = new Table();
		
		Table btable = new Table();
		//table.debug();
		//btable.debug();
		
		table.add(lblTitle).center().pad(50).expandX().top();
		table.row();
		
		btable.add(btnContinue).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnNewGame).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnOptions).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnCredits).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnExit).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnInventory).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnCharacter).width(width).height(height).padBottom(bpad).padLeft(lpad);
		
		table.add(btable).left().expandY();
		table.row();
		
		table.add(new Label("Version: Alpha 01", ls)).padTop(25).bottom();

		table.setFillParent(true);
		stage.addActor(table);
		
		Texture seatex = new Texture(Gdx.files.internal("data/textures/water.png"));
		seatex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Weather weather = new Weather(new Vector3(0.4f, 0.6f, 0.6f), new Vector3(-0.3f, -0.3f, 0), new Vector3(0.05f, 0.03f, 0.08f), new Vector3(-0.05f, 0.03f, 0.08f), new Clouds());
		Sea sea = new Sea(seatex, new Vector3(0.0f, 0.3f, 0.5f), new Terrain(new Texture[]{}, -100.0f, new Terrain.HeightMap[]{}));
		skybox = new SkyBox(sea, weather);
		
		lightManager = new LightManager();
		lightManager.addLight(weather.sun);
		
		pData.position.set(0, 10, 0);
		((FollowCam)cam).setYAngle(0);
		
	}

	@Override
	public void queueRenderables(float delta, HashMap<Class, Batch> batches) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
		Table.drawDebug(stage);

	}

	@Override
	public void update(float delta) {
		GLOBALS.SKYBOX.update(delta, cam);
		((FollowCam)cam).updateBasic(pData);
		
	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resized(int width, int height) {
		// TODO Auto-generated method stub
		
	}

}
