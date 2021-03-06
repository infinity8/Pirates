package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public interface Queueable {

	public enum RenderType {
		SIMPLE,
		FORWARD,
		DEFERRED
	}
	
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches);
	
	public Matrix4 getTransform();
	
	public void set(Entity source, Matrix4 offset);
	public void set(Matrix4 transform);
	
	public void transform(Matrix4 mat);
	
	public void update(float delta, Camera cam, LightManager lights);
	
	public float[][] getVertexArray();
	
	public Vector3 getTransformedVertex(float[] values, Vector3 out);
	
	public Queueable copy();
	
	public void dispose();
}
