package com.Lyeeedar.Graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.ImageUtils;
import com.Lyeeedar.Util.Informable;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

public class Sprite3D implements Renderable {
	
	private static final String FILE_PREFIX = "data/sprites/";
	private static final String FILE_SEPERATOR = ".";
	private static final String FILE_SUFFIX = ".png";
	
	public enum SpriteLayer
	{
		BODY,
		FEET,
		FACE,
		BOTTOM,
		TOP,
		HEAD,
		OTHER
	}
	
	private final short NUM_ANIMS;
	private final short NUM_FRAMES;

	private final HashMap<SpriteLayer, SortedSet<SPRITESHEET>> layers;
	
	private final List<ANIMATION> animations;
	
	private String gender;
	
	private final HashMap<String, Texture> spritesheet;
	private float width;
	private short spriteWidth;
	private float height;
	private short spriteHeight;
	
	private final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
	private final Vector3 position = new Vector3();
	
	private Decal decal;
	private TextureRegion region;
	
	private float animationDelay;
	private float animationCD = 0;
	private boolean animate = false;
	private byte frame = 0;
	private byte animation = 0;
	private byte direction = 0;
	private boolean useDirection = true;
	private String currentAnimation = "";
	
	private final Vector3 tmp = new Vector3();
	private boolean update = false;	
	
	private boolean lock = false;
	private byte nextAnimation;
	private String nextAnim;
	private boolean animateStore;
	private boolean directionStore;
	private byte endFrame;
	private Informable informable;
	
	private final PositionalData pData = new PositionalData();
	private final AnimationData aData = new AnimationData();

	public Sprite3D(float width, float height, int num_anims, int num_frames)
	{
		this.width = width;
		this.height = height;
		this.NUM_ANIMS = (short) num_anims;
		this.NUM_FRAMES = (short) num_frames;
		
		layers = new HashMap<SpriteLayer, SortedSet<SPRITESHEET>>();
		
		for (SpriteLayer sl : SpriteLayer.values())
		{
			layers.put(sl, new TreeSet<SPRITESHEET>());
		}
		
		animations = new ArrayList<ANIMATION>();
		spritesheet = new HashMap<String, Texture>();
	}

	@Override
	public void queue(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {
		decalBatch.add(decal);
		
	}

	@Override
	public void set(Entity source) {
		
		source.readData(pData, PositionalData.class);
		source.readData(aData, AnimationData.class);
		
		setPosition(pData.position);
		setRotation(pData.rotation);
		if (aData.updateAnimations){
			playAnimationLoop(aData.anim, aData.animation, aData.useDirection);
			setAnimation(aData.animate, aData.animate_speed);
		}
		if (aData.animationLock)
		{
			playAnimationSingle(aData.playAnim, aData.playAnimation, aData.nextAnim, aData.nextAnimation, aData.startFrame, aData.endFrame, aData.useDirection, aData.informable);
		}
	}
	
	public void addAnimation(String animation, String base, String... extras)
	{
		this.animations.add(new ANIMATION(animation, base, extras));
	}
	public boolean removeAnimation(String animation)
	{
		for (int i = 0; i < animations.size(); i++)
		{
			if (animations.get(i).animationName.equals(animation))
			{
				animations.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public void setGender(boolean male)
	{
		if (male) gender = "male";
		else gender = "female";
	}
	public void addLayer(String spritesheet, Color tint, int priority, SpriteLayer layer)
	{
		layers.get(layer).add(new SPRITESHEET(spritesheet, tint, priority));
	}
	public boolean removeLayer(String spritesheet, SpriteLayer layer)
	{
		SortedSet<SPRITESHEET> ss = layers.get(layer);
		SPRITESHEET s = null;
		for (SPRITESHEET sps : ss) if (sps.filename.equals(spritesheet)) { s = sps; break;}
		if (s != null) ss.remove(s);
		else return false;
		return true;
	}
	public void clearLayer(SpriteLayer layer)
	{
		layers.get(layer).clear();
	}
	
	private String getFinalName(String file, String anim, String gender)
	{
		if (gender.equals("")) return FILE_PREFIX+file+FILE_SEPERATOR+anim+FILE_SUFFIX;
		return FILE_PREFIX+file+FILE_SEPERATOR+anim+FILE_SEPERATOR+gender+FILE_SUFFIX;
	}
	
	private Pixmap loadLayer(Pixmap sprite, ANIMATION animation, SortedSet<SPRITESHEET> layer)
	{
		for (SPRITESHEET s : layer)
		{
			if (sprite == null)
			{
				sprite = ImageUtils.copy(FileUtils.loadPixmap(getFinalName(s.filename, animation.animationBase, gender), false));
				ImageUtils.tint(sprite, s.colour);
			}
			else
			{
				merge(s, animation.animationBase, sprite);
			}
			for (String extra : animation.extras)
			{
				merge(s, animation.animationBase+extra, sprite);
			}
		}
		return sprite;
	}
	
	public void create()
	{
		for (Map.Entry<String, Texture> entry : spritesheet.entrySet())
		{
			entry.getValue().dispose();
		}
		spritesheet.clear();
		
		for (ANIMATION animation : animations)
		{
			Pixmap sprite = null;
			for (SpriteLayer layer : SpriteLayer.values())
			{
				sprite = loadLayer(sprite, animation, layers.get(layer));
			}
			
			spritesheet.put(animation.animationName, ImageUtils.PixmapToTexture(sprite));
		}
		
		spriteWidth = (short) (spritesheet.get(animations.get(0).animationName).getWidth()/NUM_FRAMES);
		spriteHeight = (short) (spritesheet.get(animations.get(0).animationName).getHeight()/NUM_ANIMS);
		
		region = new TextureRegion(spritesheet.get(animations.get(0).animationName), 0, 0, spriteWidth, spriteHeight);
		decal = Decal.newDecal(width, height, region, true);
	}
	
	private final <T> Pixmap merge(SPRITESHEET ss, String anim, Pixmap sprite)
	{
		String filename = getFinalName(ss.filename, anim, gender);
		Pixmap pix = FileUtils.loadPixmap(filename, false);
		if (pix != null)
		{
			ImageUtils.tint(pix, ss.colour);
			return ImageUtils.merge(sprite, pix);
		}
		
		filename = getFinalName(ss.filename, anim, "");
		pix = FileUtils.loadPixmap(filename, false);
		if (pix != null)
		{
			ImageUtils.tint(pix, ss.colour);
			return ImageUtils.merge(sprite, pix);
		}
		
		return sprite;
	}
	
	public void setPosition(Vector3 position)
	{
		this.position.set(position.x, position.y+(height/2), position.z);
	}
	public void setRotation(Vector3 rotation)
	{
		this.rotation.set(rotation);
	}
	
	public void playAnimationLoop(String animationName, byte animation, boolean useDirection)
	{
		if (lock) return;
		
		this.animate = true;
		if (!currentAnimation.equals(animationName))
		{
			Texture texture = spritesheet.get(animationName);
			region.setTexture(texture);
			update = true;
		}
		if (this.animation != animation)
		{
			this.animation = animation;
			update = true;
		}
		this.useDirection = useDirection;
	}
	public void playAnimationSingle(String anim, byte animation, String nextAnim, byte nextAnimation, byte startFrame, byte endFrame, boolean useDirection, Informable informable)
	{
		if (lock) return;
		
		this.directionStore = this.useDirection;
		this.animateStore = this.animate;
		this.nextAnim = nextAnim;
		this.nextAnimation = nextAnimation;
		this.endFrame = endFrame;
		this.informable = informable;
		
		playAnimationLoop(anim, animation, useDirection);
		this.frame = startFrame;
		
		lock = true;
	}
	private void finishAnimationSingle()
	{
		lock = false;
		informable.inform();
		frame = 0;
		playAnimationLoop(nextAnim, nextAnimation, directionStore);
		this.animate = animateStore;
		this.useDirection = directionStore;
	}
	
	public void setAnimation(boolean animate, float animate_speed)
	{
		if (lock) return;
		
		this.animationDelay = animate_speed;
		if (this.animate != animate)
		{
			this.animate = animate;
			frame = 0;
			update = true;
		}
	}

	public void update(float delta, Camera cam)
	{
		animationCD -= delta;
		
		if (!animate)
		{
			animationCD = 0;
		}
		else if (animationCD < 0)
		{
			animationCD = animationDelay;
			frame++;
			if (lock && frame == endFrame)
			{
				finishAnimationSingle();
			}
			else if (frame == NUM_FRAMES) {
				frame = 0;
			}
			update = true;
		}
		
		if (useDirection)
		{
			double angle = GLOBALS.angle(cam.direction, rotation, tmp);
			double abs_a = Math.abs(angle);
			
			byte d = 0;
			if (abs_a < 60)
			{
				d = 3;
			}
			else if (abs_a > 120)
			{
				d = 0;
			}
			else if (angle < 0)
			{
				d = 1;
			}
			else if (angle > 0)
			{
				d = 2;
			}
			
			if (d > NUM_ANIMS-1) d = (byte) (NUM_ANIMS-1);
			
			if (direction != d)
			{
				direction = d;
				update = true;
			}
		}
		else
		{
			direction = 0;
		}
		
		decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		decal.setPosition(position.x, position.y, position.z);
		
		if (update)
		{
			update = false;
			region.setRegion(frame*spriteWidth, (animation+direction)*spriteHeight, spriteWidth, spriteHeight);
			decal.setTextureRegion(region);
		}
	}

	private static final class SPRITESHEET implements Comparable<SPRITESHEET>
	{
		public final String filename;
		public final Color colour;
		public final int priority;
		
		public SPRITESHEET(String filename, Color colour, int priority)
		{
			this.filename = filename;
			this.colour = colour;
			this.priority = priority;
		}

		@Override
		public int compareTo(SPRITESHEET a) {
			return a.priority - priority;
		}
	}
	
	private static final class ANIMATION
	{
		public final String animationName;
		public final String animationBase;
		public final String[] extras;
		
		public ANIMATION(String animationName, String animationBase, String[] extras)
		{
			this.animationName = animationName;
			this.animationBase = animationBase;
			this.extras = extras;
		}
	}

	@Override
	public void dispose() {
		for (Map.Entry<String, Texture> entry : spritesheet.entrySet())
		{
			entry.getValue().dispose();
		}
	}
}
