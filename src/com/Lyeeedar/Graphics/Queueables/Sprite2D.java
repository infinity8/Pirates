package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.AbstractModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.DecalBatcher;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Sprite2D implements Queueable {
	
	private final Decal decal;
	private final Vector3 position = new Vector3();
	
	private final Vector3 colour = new Vector3(1.0f, 1.0f, 1.0f);
	private float alpha = 1.0f;
	private final Vector3 finalColour = new Vector3();
	
	PositionalData pData = new PositionalData();
	
	public Sprite2D(Decal decal)
	{
		this.decal = decal;
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) 
	{
		decal.setColor(finalColour.x, finalColour.y, finalColour.z, alpha);
		((DecalBatcher) batches.get(DecalBatcher.class)).add(decal);
	}

	@Override
	public void set(Entity source, Vector3 offset) {
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			position.set(0, 0, 0).mul(source.readOnlyRead(PositionalData.class).composed).add(offset);
		}
		else
		{
			MinimalPositionalData data = source.readOnlyRead(MinimalPositionalData.class);
			position.set(data.position).add(offset);
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		decal.setPosition(position.x, position.y, position.z);
		
		lights.getLight(position, finalColour).scl(colour);

	}

	@Override
	public Queueable copy() {
		return new Sprite2D(decal);
	}

	@Override
	public void dispose() {
		pData.dispose();
	}

	@Override
	public void set(Matrix4 transform)
	{
		position.set(0, 0, 0).mul(transform);
		
	}

	@Override
	public void transform(Matrix4 mat)
	{
		position.mul(mat);
	}

}