package com.Lyeeedar.Screens;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.Octtree;
import com.Lyeeedar.Collision.Octtree.OcttreeBox;
import com.Lyeeedar.Collision.Octtree.OcttreeEntry;
import com.Lyeeedar.Collision.Octtree.OcttreeFrustum;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData.COLLISION_TYPE;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Entity.StatusData.STATS;
import com.Lyeeedar.Entities.Terrain;
import com.Lyeeedar.Entities.AI.ActionApplySpellEffect;
import com.Lyeeedar.Entities.AI.ActionAttack;
import com.Lyeeedar.Entities.AI.ActionBuilder;
import com.Lyeeedar.Entities.AI.ActionEvaluateDamage;
import com.Lyeeedar.Entities.AI.ActionGravityAndMovement;
import com.Lyeeedar.Entities.AI.ActionKill;
import com.Lyeeedar.Entities.AI.ActionMove;
import com.Lyeeedar.Entities.AI.ActionMoveTo;
import com.Lyeeedar.Entities.AI.ActionPlayerControl;
import com.Lyeeedar.Entities.AI.ActionRandomWalk;
import com.Lyeeedar.Entities.AI.ActionSetParticleEffect;
import com.Lyeeedar.Entities.AI.ActionSetValue;
import com.Lyeeedar.Entities.AI.ActionStrafe;
import com.Lyeeedar.Entities.AI.ActionUpdateAnimations;
import com.Lyeeedar.Entities.AI.ActionWait;
import com.Lyeeedar.Entities.AI.BehaviourTree;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.AI.CheckBest.CheckClosest;
import com.Lyeeedar.Entities.AI.CheckBest.CheckHPThreshold;
import com.Lyeeedar.Entities.AI.CheckBest.CheckHasEquipment;
import com.Lyeeedar.Entities.AI.ConditionalAnimationLock;
import com.Lyeeedar.Entities.AI.ConditionalCollided;
import com.Lyeeedar.Entities.AI.ConditionalTimer;
import com.Lyeeedar.Entities.AI.DoAction.DoGiveItem;
import com.Lyeeedar.Entities.AI.DoAction.DoNothing;
import com.Lyeeedar.Entities.AI.DoAction.DoSetEntity;
import com.Lyeeedar.Entities.AI.GetEntities.GetAll;
import com.Lyeeedar.Entities.AI.GetEntities.GetEnemies;
import com.Lyeeedar.Entities.AI.GetEntities.GetSelf;
import com.Lyeeedar.Entities.AI.Selector;
import com.Lyeeedar.Entities.AI.Selector.ConcurrentSelector;
import com.Lyeeedar.Entities.AI.Selector.PrioritySelector;
import com.Lyeeedar.Entities.AI.Selector.RandomSelector;
import com.Lyeeedar.Entities.AI.Selector.SequenceSelector;
import com.Lyeeedar.Entities.Items.Armour;
import com.Lyeeedar.Entities.Items.Equipment.EquipmentGraphics;
import com.Lyeeedar.Entities.Items.Equipment.EquipmentModel;
import com.Lyeeedar.Entities.Items.Item;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Items.Item.ITEM_TYPE;
import com.Lyeeedar.Entities.Items.Weapon;
import com.Lyeeedar.Entities.Items.Weapon.ATTACK_STAGE;
import com.Lyeeedar.Entities.Items.Weapon.AttackActionParticleEffect;
import com.Lyeeedar.Entities.Items.Weapon.AttackMotionTrail;
import com.Lyeeedar.Entities.Items.Weapon.AttackSpellCast;
import com.Lyeeedar.Entities.Items.Spells.SpellEffect.InstantSpellEffect;
import com.Lyeeedar.Entities.Items.Spells.SpellEffect.SpellPayloadHP;
import com.Lyeeedar.Graphics.Clouds;
import com.Lyeeedar.Graphics.MessageList;
import com.Lyeeedar.Graphics.MessageList.Message;
import com.Lyeeedar.Graphics.MessageList.Text;
import com.Lyeeedar.Graphics.LineRenderer;
import com.Lyeeedar.Graphics.Sea;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Weather;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Graphics.Particles.TextParticle;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.Lyeeedar.Graphics.Queueables.Queueable;
import com.Lyeeedar.Graphics.Queueables.Sprite2D;
import com.Lyeeedar.Graphics.Queueables.TexturedMesh;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Pirates.ProceduralGeneration.SerkGenerator;
import com.Lyeeedar.Util.DetailController;
import com.Lyeeedar.Util.Dialogue;
import com.Lyeeedar.Util.Dialogue.DialogueAction;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.Lyeeedar.Util.ImageUtils;
import com.Lyeeedar.Util.Picker;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btHeightfieldTerrainShape;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends AbstractScreen {
	
	private SkyBox skybox;
	private Entity player;
	Terrain terrain;
	Entity ship;
	MessageList message = new MessageList(30, 10);
	Weapon weapon;
	
	private AnimatedModel pmodel;
	
	private final LinkedList<TextParticle> tParticles = new LinkedList<TextParticle>();
	private final LinkedList<ParticleEmitter> visibleEmitters = new LinkedList<ParticleEmitter>();
	private final Array<Entity> veggies = new Array<Entity>();
	
	private FollowCam veggieCam;
	
	private final SpriteBatch sB = new SpriteBatch();
	private final BitmapFont fB = new BitmapFont(true);
	
	private Octtree<Entity> veggieTree;

	public GameScreen(PirateGame game) {
		super(game);
	}

	AnimatedModel hair;
	TexturedMesh hair2;
	FloatBuffer fb;
	@Override
	public void create()
	{

		GLOBALS.picker = new Picker();
		
		BulletWorld bw = new BulletWorld(new Vector3(-100000, -100000, -100000), new Vector3(100000, 100000, 100000));
		GLOBALS.physicsWorld = bw;
		
		veggieTree = new Octtree<Entity>(null, new Vector3(-100000, -100000, -100000), new Vector3(100000, 100000, 100000));
		
		Octtree<Entity> rw = new Octtree<Entity>(null, new Vector3(-100000, -100000, -100000), new Vector3(100000, 100000, 100000));
		GLOBALS.renderTree = rw;
		
		//bw.add(cam.renderObject, BulletWorld.FILTER_GHOST, BulletWorld.FILTER_RENDER);
		//bw.add(cam.aiObject, BulletWorld.FILTER_GHOST, BulletWorld.FILTER_AI);
		
		veggieCam = new FollowCam(controls, null, 0);
		//rw.add(veggieCam.renderObject, BulletWorld.FILTER_GHOST, BulletWorld.FILTER_RENDER);
				
		// HEIGHT MAP
		Texture sand = FileUtils.loadTexture("data/textures/sand.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);	
		Texture grass = FileUtils.loadTexture("data/textures/grass.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);	
		Texture dirt = FileUtils.loadTexture("data/textures/road.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);	
		Texture rock = FileUtils.loadTexture("data/textures/rock.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);
		
		ArrayList<Entity> ae = new ArrayList<Entity>();
		
		SerkGenerator sg = new SerkGenerator(1000, 10000, 500, -50, 1337135);
		Pixmap hmpm = ImageUtils.arrayToPixmap(sg.generate(ae));
		Texture hm = ImageUtils.PixmapToTexture(hmpm);
		hm.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		terrain = new Terrain(new Texture[]{sand, grass, dirt, rock}, -50.0f, new Terrain.HeightMap[]{new Terrain.HeightMap(hm, new Vector3(0f, 0f, 0f), 500.0f, 10000, -50.0f)});
		
		terrain.readOnlyRead(PositionalData.class).calculateComposed();
		
		fb = ImageUtils.extractAlpha(hmpm);
		btHeightfieldTerrainShape hf = new btHeightfieldTerrainShape(1000, 1000, fb, 1.f, 0.f, 1.f, 1, false);
		hf.setLocalScaling(new Vector3(10f, 500f, 10f));
		
		bw.add(hf, new Matrix4().setToTranslation(5000f, 200f, 5000f), terrain, (short)(BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_WALKABLE), (short)(BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_WALKABLE));
		
		// END HEIGHTMAP
		
		System.out.println("heightmap done");
		
		// MAKE SHIP
		
		Texture shipTex = new Texture(Gdx.files.internal("data/textures/shipTex.png"));
        Mesh shipModel = FileUtils.loadMesh("data/models/shipMesh.obj");
		
		ship = new Entity(true, new PositionalData(), new StatusData());
		ship.readOnlyRead(PositionalData.class).calculateComposed();
		ship.readOnlyRead(StatusData.class).solid = true;
		ship.readOnlyRead(StatusData.class).stats.put(STATS.SPEED, 10);
		ship.readOnlyRead(StatusData.class).name = "Ship";
		
		Selector ssselect = new ConcurrentSelector();
		BehaviourTree sbtree = new BehaviourTree(ssselect);
		ssselect.addNode(new ActionGravityAndMovement());
		ssselect.addNode(new ActionMove(1));
		
		ship.setAI(sbtree);
		
		//ship.setActivationAction(new Action_AISwapper("THis is a ship woooot", new AI_Ship_Control(controls), new AI_RotOnly(controls)));
		
		TexturedMesh shipMesh = new TexturedMesh("data/models/shipMesh.obj", shipModel, GL20.GL_TRIANGLES, new Texture[]{shipTex}, new Vector3(1, 1, 1), 1);
		
//		for (int i = 0; i < 250; i++)
//		{
//			ParticleEffect sfire = FileUtils.loadParticleEffect("data/effects/fire.effect");
//			ship.addRenderable(sfire, new Vector3());
//			sfire.setEmission(shipMesh.getVertexArray(), shipMesh);
//		}
		
		ship.addRenderable(shipMesh, new Vector3());
		Array<MeshPart> parts = new Array<MeshPart>();
		parts.add(new MeshPart("", shipModel, 0, shipModel.getNumIndices(), GL20.GL_TRIANGLES));
		btCollisionShape shipShape = new btBvhTriangleMeshShape(parts);
		
		BoundingBox sbb = shipModel.calculateBoundingBox();
		OcttreeEntry<Entity> entry = rw.createEntry(ship, ship.readOnlyRead(PositionalData.class).position, sbb.getDimensions(), Octtree.MASK_AI | Octtree.MASK_RENDER | Octtree.MASK_ENTITY);
		ship.readOnlyRead(PositionalData.class).collisionType = COLLISION_TYPE.SHIP;
		ship.readOnlyRead(PositionalData.class).octtreeEntry = entry;
		ship.readOnlyRead(PositionalData.class).collisionShape = shipShape;
		rw.add(entry);
		bw.add(shipShape, new Matrix4().setToTranslation(ship.readOnlyRead(PositionalData.class).position), ship, (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI | BulletWorld.FILTER_WALKABLE), (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI | BulletWorld.FILTER_GHOST | BulletWorld.FILTER_WALKABLE));
		
		//cam.lockOn(ship);
		
		// END SHIP
		
		System.out.println("ship done");
		
		//  MAKE SKYBOX
		
		Texture seatex = new Texture(Gdx.files.internal("data/textures/water.png"));
		seatex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//seatex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		Weather weather = new Weather(new Vector3(0.4f, 0.6f, 0.6f), new Vector3(-0.3f, -0.3f, 0), new Vector3(0.05f, 0.03f, 0.08f), new Vector3(-0.05f, 0.03f, 0.08f), new Clouds());
		Sea sea = new Sea(seatex, new Vector3(0.0f, 0.3f, 0.5f), terrain);
		skybox = new SkyBox(sea, weather);
		
		// END SKYBOX
		
		System.out.println("skybox done");
		
		// MAKE PLAYER
		
		player = new Entity(false, new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());

		Selector sselect = new ConcurrentSelector();
		BehaviourTree btree = new BehaviourTree(sselect);
		sselect.addNode(new ActionUpdateAnimations());
		sselect.addNode(new ActionEvaluateDamage());
		sselect.addNode(new ActionGravityAndMovement());
		
		Selector pselect = new PrioritySelector();
		pselect.addNode(new ActionPlayerControl(controls, cam));
		pselect.addNode(new ConditionalAnimationLock(BehaviourTreeState.FINISHED, BehaviourTreeState.FAILED));
		
		sselect.addNode(pselect);
		
		player.setAI(btree);

		AnimatedModel am = new AnimatedModel("data/models/man2.g3db", FileUtils.loadModel("data/models/man2.g3db"), FileUtils.getTextureArray(new String[]{"data/textures/skin"}), new Vector3(0.7f, 0.7f, 0.7f), "idle_ground");
		player.addRenderable(am, new Vector3());
		pmodel = am;
		am.setDetailController(new DetailController());
		
		player.readOnlyRead(StatusData.class).factions.add("Player");
		player.readOnlyRead(StatusData.class).stats.put(STATS.MAXHEALTH, 100);
		player.readOnlyRead(StatusData.class).currentHealth = 100;
		player.readOnlyRead(StatusData.class).stats.put(STATS.MASS, 100);
		player.readOnlyRead(StatusData.class).stats.put(STATS.SPEED, 25);
		player.readOnlyRead(StatusData.class).blocking = true;
		
		EquipmentData eData = new EquipmentData();
		player.readData(eData);
		eData.am = am;
		eData.equip(Equipment_Slot.BODY, new Armour(new EquipmentGraphics("data/textures/skin", null), null));
		eData.equip(Equipment_Slot.HEAD, new Armour(new EquipmentGraphics(null, new EquipmentModel("data/models/hair1.g3db", new String[]{"data/textures/hair"}, null, new Vector3(1.0f, 1.0f, 1.0f), "DEF-head", new Matrix4().rotate(0, 0, 1, -90).translate(0.1f, 0.5f, 0))), null));
		ATTACK_STAGE[] wattacks = {
				
				new ATTACK_STAGE("attack_main_1", 1.0f, 1, null, new AttackMotionTrail(200, 70, 90), null, 0, 0),
				new ATTACK_STAGE("attack_main_2", 1.0f, 2, null, new AttackMotionTrail(200, 70, 100), null, 0, 0),
				new ATTACK_STAGE("attack_main_3", 1.0f, 1, null, new AttackMotionTrail(200, 81, 91), null, 0, 0)
		};
		EquipmentGraphics axeg = new EquipmentGraphics("data/textures/plate", new EquipmentModel("data/models/axe.g3db", new String[]{"data/textures/axe"}, "idle", new Vector3(1.0f, 1.0f, 1.0f), "DEF-palm_01_R", new Matrix4().rotate(1, 0, 0, 180).rotate(0, 0, 1, -20)));
		weapon = new Weapon(wattacks, axeg, new DESCRIPTION(null, null, null, null, null), 0.5f, 0.3f, null, new ATTACK_STAGE("recoil_main", 3, -1, new AttackActionParticleEffect("data/effects/sparks.effect"), null, null, 0, -1));
		weapon.statusModifier.setAttack(0, 0, 0, 0, 1000);
		eData.equip(Equipment_Slot.RARM, weapon);
		
		Entity spell2 = new Entity(false, new PositionalData(), new StatusData());
		Sprite2D orb2 = new Sprite2D(Decal.newDecal(new TextureRegion(FileUtils.loadTexture("data/textures/orb.png", true, null, null))), 1, 1);
		spell2.addRenderable(orb2, new Vector3());
		spell2.readOnlyRead(PositionalData.class).octtreeEntry = rw.createEntry(spell2, new Vector3(), new Vector3(10, 10, 10), Octtree.MASK_AI | Octtree.MASK_RENDER | Octtree.MASK_SPELL);
		spell2.readOnlyRead(PositionalData.class).collisionType = COLLISION_TYPE.SIMPLE;
		spell2.readOnlyRead(PositionalData.class).collisionShape = new btBoxShape(new Vector3(1, 1, 1));
		spell2.readOnlyRead(StatusData.class).stats.put(STATS.SPEED, 25);
		spell2.readOnlyRead(StatusData.class).currentHealth = 1;
		spell2.readOnlyRead(StatusData.class).stats.put(STATS.MAXHEALTH, 1);
		
		Selector ssssspselect = new PrioritySelector();
		ssssspselect.addNode(new ConditionalTimer(5, BehaviourTreeState.FINISHED, BehaviourTreeState.FAILED));
		ssssspselect.addNode(new ConditionalCollided(BehaviourTreeState.FINISHED, BehaviourTreeState.FAILED));
		
		Selector sssssselect = new SequenceSelector();
		sssssselect.addNode(new ActionKill());
		sssssselect.addNode(new ActionSetParticleEffect("data/effects/explosion.effect"));
		sssssselect.addNode(new ActionApplySpellEffect(new InstantSpellEffect(new SpellPayloadHP(50)), new OcttreeBox(new Vector3(), new Vector3(15, 15, 15), null)));
		sssssselect.addNode(ssssspselect);
		
		Selector ggsselect2 = new SequenceSelector();
		ggsselect2.addNode(new ActionMoveTo(true, 0, "enemy"));
		ggsselect2.addNode(new ActionBuilder(new GetEnemies(new OcttreeFrustum(new PerspectiveCamera(90, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]), 0)), new CheckClosest(), new DoSetEntity("enemy")));
		
		Selector sssssspselect = new PrioritySelector();
		sssssspselect.addNode(new ActionMove(5));
		sssssspselect.addNode(ggsselect2);
		sssssspselect.addNode(sssssselect);
		
		Selector ssssssselect = new ConcurrentSelector();
		BehaviourTree sssbtree = new BehaviourTree(ssssssselect);
		ssssssselect.addNode(new ActionGravityAndMovement());
		ssssssselect.addNode(sssssspselect);
		
		spell2.setAI(sssbtree);
		ATTACK_STAGE[] sattacks = {
				new ATTACK_STAGE("cast_off_2", 1.0f, 1, null, null, new AttackSpellCast(spell2), 0, 0),
				new ATTACK_STAGE("cast_off_1", 1.0f, 2, null, null, new AttackSpellCast(spell2), 0, 0),
				new ATTACK_STAGE("cast_off_2", 1.0f, -1, null, null, new AttackSpellCast(spell2), 0, 0)
		};
		eData.equip(Equipment_Slot.LARM, new Weapon(sattacks, new EquipmentGraphics(null, new EquipmentModel("data/models/bone-only.g3db", new String[]{"data/textures/blank"}, null, new Vector3(), "DEF-palm_01_L", new Matrix4())), new DESCRIPTION(null, null, null, null, null), 0.5f, 0.3f, null, null));
		
		player.writeData(eData);
		
		player.readOnlyRead(PositionalData.class).position.set(100, 5, 100);
		
		Vector3 dimensions = new Vector3(1, 2, 1);
		
		entry = rw.createEntry(player, player.readOnlyRead(PositionalData.class).position, dimensions, Octtree.MASK_AI | Octtree.MASK_RENDER | Octtree.MASK_ENTITY);
		player.readOnlyRead(PositionalData.class).octtreeEntry = entry;
		player.readOnlyRead(PositionalData.class).collisionShape = new btCapsuleShape(1, 2);
		rw.add(entry);
		bw.add(new btCapsuleShape(1, 2), new Matrix4().setToTranslation(player.readOnlyRead(PositionalData.class).position), player, (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI), (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI | BulletWorld.FILTER_GHOST));
		
		GLOBALS.player = player;
		
		// END PLAYER
		
		System.out.println("player done");
		
		// MAKE NPC 1
		
		Entity npc = new Entity(false, new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());
		//npc.setAI(new AI_Simple());
		Object[] actionTree = {
			DialogueAction.TEXT3D, "Test dialogue 123 This is Action 0. Text 3D. Advancing.", 1 ,
			new Object[]{ DialogueAction.WAIT, 5, 
			new Object[]{ DialogueAction.TEXT3D, "Waited 5 seconds. New dialogue shown.", 1 ,
			new Object[]{ DialogueAction.WAIT, 5,
			new Object[]{ DialogueAction.TEXT2D, "Now then, what do you want to do? 1: Stuff, 2: other stuff. Choose.", 
			new Object[]{ DialogueAction.INPUT, 
					Keys.NUM_1, 
						new Object[]{ DialogueAction.TEXT3D, "You chose 1.", 1 ,
						new Object[]{ DialogueAction.INPUT, Keys.E, null
						}
						},
					Keys.NUM_2,
						new Object[]{ DialogueAction.TEXT3D, "You chose 2.", 1 ,
						new Object[]{ DialogueAction.INPUT, Keys.E, null
						}
						},
			}
			}
			}
			}
			}
		};

		npc.readOnlyRead(PositionalData.class).position.set(-4, 12, 0);
		npc.readOnlyRead(StatusData.class).factions.add("Player");
		npc.readData(eData);
		//eData.equip(Equipment_Slot.BODY, new Armour(new SPRITESHEET("Human", Color.WHITE, 0, SpriteLayer.BODY), null));
		//eData.equip(Equipment_Slot.HEAD, new Armour(new SPRITESHEET("Hair1", new Color(0.9f, 0.5f, 0.7f, 1.0f), 0, SpriteLayer.HEAD), null));
		npc.writeData(eData);
		
		// END NPC 1
		
		System.out.println("npc1 done");
		
		// MAKE ENEMIES
		
		for (int i = 0; i < 50; i++)
		{
			
			Entity leader = makeEntity(null, rw, bw);
			
			for (int ii = 0; ii < 5; ii++)
			{
				makeEntity(leader, rw, bw);
			}
			
		}
		
		// END ENEMIES
		
		System.out.println("enemies done");
		
		// MAKE TREES
		
		Mesh grassMesh = FileUtils.loadMesh("data/models/pinet.obj");
		Texture pinetex = FileUtils.loadTexture("data/textures/pinet.png", true, TextureFilter.MipMapLinearLinear, null);
		terrain.vegetate(veggies, new ModelBatcher(grassMesh, GL20.GL_TRIANGLES, new Texture[]{pinetex}, new Vector3(1, 1, 1), false), 1, 2500, 50);
		btBoxShape tBox = new btBoxShape(new Vector3(10, 50, 10));
		BoundingBox bb = grassMesh.calculateBoundingBox();
		for (Entity v : veggies)
		{
			v.update(0);
			entry = rw.createEntry(v, v.readOnlyRead(MinimalPositionalData.class).position, bb.getDimensions(), Octtree.MASK_RENDER);
			rw.add(entry);
			bw.add(tBox, new Matrix4().setToTranslation(v.readOnlyRead(MinimalPositionalData.class).position), v, (short)(BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER), (short)(BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_GHOST));
		}
		veggies.clear();
		
		// END TREES
		
		System.out.println("trees done");
		
		// MAKE GRASS
		
		grassMesh = FileUtils.loadMesh("data/models/shr2.obj");
		Texture shrd = FileUtils.loadTexture("data/textures/shr2_d.png", true, TextureFilter.MipMapLinearLinear, null);
		Texture shrs = FileUtils.loadTexture("data/textures/shr2_s.png", true, TextureFilter.MipMapLinearLinear, null);
		Texture shre = FileUtils.loadTexture("data/textures/shr2_e.png", true, TextureFilter.MipMapLinearLinear, null);
		terrain.vegetate(veggies, new ModelBatcher(grassMesh, GL20.GL_TRIANGLES, new Texture[]{shrd, shrs, shre}, new Vector3(1, 1, 1), true), 1, 50000, 50);
		btBoxShape box = new btBoxShape(new Vector3(1, 1, 1));
		for (Entity v : veggies)
		{
			v.update(0);
			//rw.add(new btBoxShape(new Vector3(1, 1, 1)), new Matrix4().setToTranslation(v.readOnlyRead(MinimalPositionalData.class).position), v, BulletWorld.FILTER_RENDER, BulletWorld.FILTER_GHOST);
			btCollisionObject o = new btCollisionObject();
			o.setCollisionShape(box);
			o.setWorldTransform(new Matrix4().setToTranslation(v.readOnlyRead(MinimalPositionalData.class).position));
			OcttreeEntry<Entity> oe = veggieTree.createEntry(v, v.readOnlyRead(MinimalPositionalData.class).position, new Vector3(1, 1, 1), Octtree.MASK_RENDER);
			veggieTree.add(oe);
		}
		veggies.clear();
		
		// END GRASS
		
		System.out.println("grass done");
	}
	
	private Entity makeEntity(Entity leader, Octtree<Entity> rw, BulletWorld bw)
	{
		Entity ge = new Entity(false, new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());
		
		Selector ggrselect = new RandomSelector(new Random());
		ggrselect.addNode(new ActionWait(1));
		ggrselect.addNode(new ActionRandomWalk(new Random(), 1));
		
		Selector ggsselect3 = new SequenceSelector();
		ggsselect3.addNode(new ActionMoveTo(true, 50, "leader"));
		ggsselect3.addNode(new ActionSetValue("leader", leader));
		
		Selector ggpselect2 = new PrioritySelector();
		ggpselect2.addNode(new ActionMoveTo(true, 5, "enemy"));
		ggpselect2.addNode(new ActionAttack(5, false, true, "enemy"));
		
		Selector ggrselect3 = new RandomSelector(new Random());
		ggrselect3.addNode(new ActionWait(1));
		ggrselect3.addNode(new ActionStrafe(1, "enemy"));
		ggrselect3.addNode(ggpselect2);
		
		Selector ggsselect2 = new SequenceSelector();
		ggsselect2.addNode(ggrselect3);
		ggsselect2.addNode(new ActionBuilder(new GetEnemies(new OcttreeBox(new Vector3(), new Vector3(100, 100, 100), null)), new CheckClosest(), new DoSetEntity("enemy")));
		
		Selector ggsselect4 = new SequenceSelector();
		ggsselect4.addNode(new ActionMoveTo(false, 500, "enemy"));
		ggsselect4.addNode(new ActionBuilder(new GetEnemies(new OcttreeBox(new Vector3(), new Vector3(100, 100, 100), null)), new CheckClosest(), new DoSetEntity("enemy")));
		ggsselect4.addNode(new ActionBuilder(new GetSelf(), new CheckHPThreshold(false, 50), new DoNothing()));
		
		Selector ggpselect = new PrioritySelector();
		ggpselect.addNode(ggrselect);
		if (leader != null) ggpselect.addNode(ggsselect3);
		ggpselect.addNode(ggsselect2);
		ggpselect.addNode(ggsselect4);
		ggpselect.addNode(new ConditionalAnimationLock(BehaviourTreeState.FINISHED, BehaviourTreeState.FAILED));
		
		Selector ggsselect = new ConcurrentSelector();
		BehaviourTree ggbtree = new BehaviourTree(ggsselect);
		ggsselect.addNode(new ActionUpdateAnimations());
		ggsselect.addNode(new ActionEvaluateDamage());
		ggsselect.addNode(new ActionGravityAndMovement());
		ggsselect.addNode(ggpselect);
		
		ge.setAI(ggbtree);
		PositionalData pData = ge.readOnlyRead(PositionalData.class);
		if (leader != null)
		{
			PositionalData pData2 = leader.readOnlyRead(PositionalData.class);
			pData.position.set(pData2.position).add(ran.nextFloat()*500-250, 0, ran.nextFloat()*500-250);
		}
		else pData.position.set(ran.nextFloat()*10000, 0, ran.nextFloat()*10000);
		pData.position.y = terrain == null ? 0 : terrain.getHeight(pData.position.x,  pData.position.z);

		ge.readOnlyRead(StatusData.class).factions.add("Enemy");
		ge.readOnlyRead(StatusData.class).stats.put(STATS.SPEED, 15);
		ge.readOnlyRead(StatusData.class).stats.put(STATS.MASS, 100);
		ge.readOnlyRead(StatusData.class).stats.put(STATS.MAXHEALTH, 150);
		ge.readOnlyRead(StatusData.class).currentHealth = 100;
		ge.readOnlyRead(StatusData.class).blocking = false;
		
		AnimatedModel gam = new AnimatedModel("data/models/man2.g3db", FileUtils.loadModel("data/models/man2.g3db"), FileUtils.getTextureArray(new String[]{"data/textures/skin"}), new Vector3(0.7f, 0.7f, 0.7f), "idle_ground");
		ge.addRenderable(gam, new Vector3());
				
		ATTACK_STAGE[] gwattacks = {
				
				new ATTACK_STAGE("attack_main_1", 1.0f, 1, null, new AttackMotionTrail(20, 70, 90), null, 0, 0),
				new ATTACK_STAGE("attack_main_2", 1.0f, 2, null, new AttackMotionTrail(20, 70, 100), null, 0, 0),
				new ATTACK_STAGE("attack_main_3", 1.0f, 1, null, new AttackMotionTrail(20, 81, 91), null, 0, 0)
		};
		
		EquipmentData eData = ge.readOnlyRead(EquipmentData.class);
		eData.am = gam;
		
		eData.equip(Equipment_Slot.BODY, new Armour(new EquipmentGraphics("data/textures/skin", null), null));
		eData.equip(Equipment_Slot.HEAD, new Armour(new EquipmentGraphics(null, new EquipmentModel("data/models/hair1.g3db", new String[]{"data/textures/hair"}, null, new Vector3(1.0f, 1.0f, 1.0f), "DEF-head", new Matrix4().rotate(0, 0, 1, -90).translate(0.1f, 0.5f, 0))), null));
		
		EquipmentGraphics axeg = new EquipmentGraphics(null, new EquipmentModel("data/models/axe.g3db", new String[]{"data/textures/axe"}, "idle", new Vector3(1.0f, 1.0f, 1.0f), "DEF-palm_01_R", new Matrix4().rotate(1, 0, 0, 180).rotate(0, 0, 1, -20)));
		eData.equip(Equipment_Slot.RARM, new Weapon(gwattacks, axeg, new DESCRIPTION(null, null, null, null, null), 0.5f, 0.3f, null, new ATTACK_STAGE("recoil_main", 3, -1, null, null, null, 0, -1)));
		Item item = new Item(new DESCRIPTION("Orb", "An Orb", "An orbular thingymobob", ITEM_TYPE.MISC, "data/textures/orb.png"));
		item.dropRate = 50;
		eData.addItem(item);
		
		Vector3 dimensions = new Vector3(1, 2, 1);
		OcttreeEntry<Entity> entry = rw.createEntry(ge, pData.position, dimensions, Octtree.MASK_AI | Octtree.MASK_RENDER | Octtree.MASK_ENTITY);
		pData.octtreeEntry = entry;
		pData.collisionShape = new btCapsuleShape(1, 2);
		rw.add(entry);
		bw.add(new btCapsuleShape(1, 2), new Matrix4().setToTranslation(pData.position), ge, (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI), (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI | BulletWorld.FILTER_GHOST));

		return ge;
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
//		player.readData(sData, StatusData.class);
//		player.readData(pData, PositionalData.class);
//		batch.draw(blank, screen_width-80, screen_height-40, ((float)sData.currentHealth/(float)sData.MAX_HEALTH)*50, 10);
//		font.draw(spriteBatch, ""+pData.position, 20, screen_height-80);
//		font.draw(spriteBatch, ""+pData.rotation, 20, screen_height-120);
//		
//		player.readData(pData, PositionalData.class);
//		box.center.set(pData.rotation).scl(2).add(pData.position);
//		Entity e = activate(box, pData.graph, elist, pData.position, pData);
//		if (e != null)
//		{
//			font.draw(spriteBatch, ""+e.getActivationAction().getDesc(), 220, 220);
//		}
		for (Dialogue d : GLOBALS.DIALOGUES)
		{
			d.queue2D(batch);
		}
		message.render(batch, font, 20, 20);
	}
	
	ImmediateModeRenderer imr = new ImmediateModeRenderer20(false, false, 0);
	@Override
	public void drawSkybox(float delta)
	{		
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		GLOBALS.SKYBOX.weather.render(cam, GLOBALS.LIGHTS);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		if (terrain != null) terrain.render(cam, cam.position, GLOBALS.LIGHTS);
		GLOBALS.SKYBOX.sea.render(cam, cam.position, GLOBALS.LIGHTS);
	}
	@Override
	public void hide() {
		Gdx.input.setCursorCatched(false);

	}
	
	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
	float updateCooldown = 0;
	boolean update = false;
	Array<Entity> renderEntities = new Array<Entity>();
	Array<Entity> aiEntities = new Array<Entity>();
	@Override
	public void queueRenderables(float delta, HashMap<Class, Batch> batches) {
				
		if (update) 
		{
			renderEntities.clear();
			GLOBALS.renderTree.collectAll(renderEntities, cam.renderFrustum, Octtree.MASK_RENDER);
		}
		for (Entity e : renderEntities)
		{
			e.queueRenderables(cam, GLOBALS.LIGHTS, delta, batches);
		}
		
		Iterator<TextParticle> itr = tParticles.iterator();
		while (itr.hasNext())
		{
			TextParticle tp = itr.next();
			if (tp.lifeTime < 0)
			{
				itr.remove();
				tp.dispose();
			}
			else
			{
				tp.update(delta, cam);
				tp.render(decalBatch);
			}
		}
		
		if (update) 
		{
			veggies.clear();
			veggieTree.collectAll(veggies, veggieCam.renderFrustum, Octtree.MASK_RENDER);
		}
		for (Entity e : veggies)
		{
			e.queueRenderables(veggieCam, GLOBALS.LIGHTS, delta, batches);
		}
		
		for (Dialogue d : GLOBALS.DIALOGUES)
		{
			d.queue3D(decalBatch);
		}
		
		Iterator<ParticleEffect> peItr = GLOBALS.unanchoredEffects.iterator();
		while(peItr.hasNext())
		{
			ParticleEffect pe = peItr.next();
			pe.update(delta, cam, GLOBALS.LIGHTS);
			
			if (!pe.isPlaying())
			{
				peItr.remove();
				FileUtils.freeParticleEffect(pe);
			}
			else
			{
				pe.queue(delta, cam, batches);
			}
		}
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}
	@Override
	public void show() {
		Gdx.input.setCursorCatched(true);
		Gdx.input.setInputProcessor(controls.ip);
		GLOBALS.SKYBOX = skybox;
		GLOBALS.player = player;
	}

	@Override
	public void superDispose() {
		sB.dispose();
		fB.dispose();
	}

	LinkedList<Runnable> list = new LinkedList<Runnable>();
	boolean increase = true;
	
	float strike_time = 0.0f;
	Random ran = new Random();
	PositionalData pData = new PositionalData();
	boolean lockOn = false;
	boolean flip = false;
	boolean equipped = true;
	
	Comparator<Entity> aiComparator = new Comparator<Entity>(){
		@Override
		public int compare(Entity e1, Entity e2)
		{
			return e1.readOnlyRead(PositionalData.class).baseDepth-e2.readOnlyRead(PositionalData.class).baseDepth;
		}
	};
	
	@Override
	public void update(float delta) {
		
		message.update(delta);
		
		GLOBALS.needsSilhouette.clear();
		
		GLOBALS.needsSilhouette.add(ship);
		GLOBALS.needsSilhouette.add(player);
		
		if (update) update = false;
		updateCooldown -= delta;
		if (updateCooldown < 0)
		{
			updateCooldown = 0.1f;
			update = true;
		}
					
		if (update) 
		{
			GLOBALS.physicsWorld.update(delta);
			aiEntities.clear();
			GLOBALS.renderTree.collectAll(aiEntities, cam.aiShape, Octtree.MASK_AI);
			aiEntities.sort(aiComparator);
//			for (int i = 0; i < aiEntities.size; i++)
//			{
//				System.out.println(aiEntities.get(i).readOnlyRead(PositionalData.class).baseDepth);
//			}
//			System.out.println("");
		}
		for (int i = 0; i < aiEntities.size; i++)
		{
			Entity e = aiEntities.get(i);
			if (e.getAI() == null) continue;
			StatusData sData = e.readOnlyRead(StatusData.class);
			if (sData.DAMAGED > 0)
			{
				float mag = 1.0f - ((float)sData.DAMAGED) / ((float)sData.stats.get(STATS.MAXHEALTH)/2);
				if (mag > 1.0f) mag = 1.0f;
				
				e.readData(pData);
				Decal decal = ImageUtils.getTextDecal(1.0f*GLOBALS.numDigits(sData.DAMAGED), 3.2f, sB, fB, null, ""+sData.DAMAGED);
				tParticles.add(new TextParticle(decal, 3.0f, pData.position.add(0, 2, 0), new Vector3(0, 3.6f, 0), new Vector3(1.0f, mag, 0.0f)));
				sData.DAMAGED = 0;
			}
			
			if (!sData.ALIVE)
			{
				if (e.readOnlyRead(PositionalData.class).physicsBody != null) 
				{
					GLOBALS.physicsWorld.remove(e.readOnlyRead(PositionalData.class).physicsBody);
					ParticleEffect death = FileUtils.obtainParticleEffect("data/effects/death.effect");
					Queueable model = e.getRenderable(0);
					death.setEmission(model.getVertexArray(), model);
					death.play(false);
					death.setHomeTarget(player.getRenderable(0));
					GLOBALS.unanchoredEffects.add(death);
				}
				if (e.readOnlyRead(EquipmentData.class) != null)
				{
					for (ITEM_TYPE it : ITEM_TYPE.values())
					{
						for (Item item : e.readOnlyRead(EquipmentData.class).getItems(it))
						{
							float dropVal = ran.nextFloat()*100.0f;
							if (dropVal <= item.dropRate)
							{
								item.dropRate = 100;
								Entity drop = new Entity(false, new PositionalData(), new StatusData());
								
								Sprite2D icon = new Sprite2D(Decal.newDecal(new TextureRegion(FileUtils.loadTexture(item.description.icon, true, null, null))), 1, 1);
								drop.addRenderable(icon, new Vector3());
								drop.readOnlyRead(PositionalData.class).octtreeEntry = GLOBALS.renderTree.createEntry(drop, new Vector3(), new Vector3(1, 1, 1), Octtree.MASK_AI | Octtree.MASK_RENDER | Octtree.MASK_ITEM);
								drop.readOnlyRead(PositionalData.class).collisionType = COLLISION_TYPE.SIMPLE;
								drop.readOnlyRead(PositionalData.class).collisionShape = new btBoxShape(new Vector3(1, 1, 1));
								drop.readOnlyRead(PositionalData.class).position.set(e.readOnlyRead(PositionalData.class).position);
								drop.readOnlyRead(PositionalData.class).velocity.set(20*(ran.nextInt(2)-1)*ran.nextFloat(), 25+25*ran.nextFloat(), 20*(ran.nextInt(2)-1)*ran.nextFloat());
								drop.readOnlyRead(StatusData.class).stats.put(STATS.MASS, 100);

								Selector sselect2 = new SequenceSelector();
								sselect2.addNode(new ActionKill());
								sselect2.addNode(new ActionBuilder(new GetAll(new OcttreeBox(new Vector3(), new Vector3(1, 1, 1), null)), new CheckHasEquipment(), new DoGiveItem(item)));
								
								Selector sselect = new ConcurrentSelector();
								sselect.addNode(new ActionGravityAndMovement());
								sselect.addNode(sselect2);
								
								BehaviourTree tree = new BehaviourTree(sselect);
								
								drop.setAI(tree);
								
								GLOBALS.renderTree.add(drop.readOnlyRead(PositionalData.class).octtreeEntry);
								
								Message m = new Message();
								m.addLine(new Text("Something", Color.GREEN), new Text(" dropped an ", Color.BLACK), new Text("Orb", Color.YELLOW));
								message.addMessage(m);
							}
						}
					}
				}
				e.readOnlyRead(PositionalData.class).octtreeEntry.remove();
				e.dispose();
				aiEntities.removeIndex(i);
				i--;
			}
			else list.add(e.getRunnable(delta));
		}
		
		GLOBALS.submitTasks(list);
		list.clear();
		GLOBALS.waitTillTasksComplete();
		
		
		GLOBALS.SKYBOX.update(delta);
						
		((FollowCam)cam).update(player, delta);
		veggieCam.position.set(cam.position);
		veggieCam.direction.set(cam.direction);
		veggieCam.update();
		
		for (Dialogue d : GLOBALS.DIALOGUES)
		{
			d.update(delta, cam);
		}
		
		if (GLOBALS.picker.RUNNING)
		{
			GLOBALS.picker.update(delta);
			GLOBALS.picker.tint();
		}
		
		GLOBALS.proccessPendingEntities();
		
		if (Gdx.input.isKeyPressed(Keys.TAB))
		{
			if (lockOn)
			{
				
			}
			else
			{
				lockOn = true;
				
				if (cam.isLockOn())
				{
					cam.lockOn(null);
				}
				else
				{
					PositionalData pData = player.readOnlyRead(PositionalData.class);
					StatusData sData = player.readOnlyRead(StatusData.class);
					Entity closest = null;
					float DST = 10000;
					
					for (Entity e : renderEntities)
					{
						StatusData sData2 = e.readOnlyRead(StatusData.class);
						if (sData2 == null || sData2.factions.size == 0 || sData.isAlly(sData2))
						{
							continue;
						}
						
						float dst = pData.position.dst2(e.getPosition());
						if (dst < DST)
						{
							DST = dst;
							closest = e;
						}
					}
					
					cam.lockOn(closest);
				}
			}

		}
		else
		{
			lockOn = false;
		}
	}

	@Override
	public void resized(int width, int height) {
		veggieCam.viewportWidth = width;
		veggieCam.viewportHeight = height;
		veggieCam.near = 2f;
		veggieCam.far = 1502f;
		veggieCam.update();
	}

}
