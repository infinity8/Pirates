package com.Lyeeedar.Pirates;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Spells.Spell;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Sound.Mixer;
import com.Lyeeedar.Util.Dialogue;
import com.badlogic.gdx.math.Vector3;

public final class GLOBALS {
	
	public enum GENDER
	{
		MALE,
		FEMALE
	}
	
	public enum ELEMENTS
	{
		TEMPERATURE, // Hot, cold
		LIGHT, // Shadow, lasers, illusions
		LIFE, // Healing, necromancy
		GAIA, // Earth, plants, weather
		FORCE // Gravity, physical buffs
	}

	public static final float STEP = 0.5f;

	public static final int MAX_SPEED_X = 1;
	public static final int MAX_SPEED_Y = 1;
	public static final int MAX_SPEED_Z = 1;

	public static final Vector3 DEFAULT_UP = new Vector3(0, 1, 0);
	public static final Vector3 DEFAULT_ROTATION = new Vector3(0, 0, 1);

	public static final float GRAVITY = -85;

	public static final int[] RESOLUTION = {1960, 1080};
	public static final int[] SCREEN_SIZE = {RESOLUTION[0], RESOLUTION[1]};
	
	public static final float FOG_MIN = 1000.0f;
	public static final float FOG_MAX = 6000.0f;

	public static boolean ANDROID = false;
	
	public static float PROGRAM_TIME = 0.0f;
	
	public static BulletWorld physicsWorld;
	public static BulletWorld renderOnlyWorld;
	
	public static SkyBox SKYBOX;
	public static LinkedList<Spell> SPELLS = new LinkedList<Spell>();
	public static LinkedList<Spell> pendingSPELLS = new LinkedList<Spell>();
	public static LightManager LIGHTS = new LightManager();
	public static Entity player;
	public static LinkedList<Dialogue> DIALOGUES = new LinkedList<Dialogue>();
	public static Mixer mixer;
	
	public static ExecutorService threadPool = Executors.newFixedThreadPool(1);//Runtime.getRuntime().availableProcessors());
	public static LinkedList<Future<?>> futureList = new LinkedList<Future<?>>();	
	public static void submitTask(Runnable run)
	{
		futureList.add(threadPool.submit(run));
	}
	public static void submitTasks(LinkedList<Runnable> list)
	{
		for (Runnable run : list) futureList.add(threadPool.submit(run));
	}
	public static void waitTillTasksComplete()
	{
		for (Future<?> future : futureList)
		{
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		futureList.clear();
	}

	public static double angle(Vector3 v1, Vector3 v2, Vector3 tmp)
	{
		Vector3 referenceForward = v1;
		Vector3 referenceRight = tmp.set(GLOBALS.DEFAULT_UP).crs(referenceForward);
		Vector3 newDirection = v2;
		float angle = (float) Math.toDegrees(Math.acos(v1.dot(v2) / (v1.len()*v2.len())));
		float sign = (newDirection.dot(referenceRight) > 0.0f) ? 1.0f : -1.0f;
		float finalAngle = sign * angle;
		return finalAngle;
	}
	
	public static int numDigits(int i)
	{
		for (int digits = 1; digits < 100; digits++)
		{
			if (i < Math.pow(10, digits)) return digits;
		}
		return 0;
	}
	
	public static float sclX(float val)
	{
		float tmp = val/1000.0f;
		return tmp*((float)RESOLUTION[0]);
	}
	
	public static float sclY(float val)
	{
		float tmp = val/1000.0f;
		return tmp*((float)RESOLUTION[1]);
	}
}
