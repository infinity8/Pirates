package com.Lyeeedar.Graphics.PostProcessing.Effects;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.TexturedMeshBatch;
import com.Lyeeedar.Graphics.Queueables.Queueable.RenderType;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class SilhouetteEffect extends PostProcessingEffect {

	private final BlurEffect blur;
	
	public final int BUFFER_WIDTH;
	public final int BUFFER_HEIGHT;
	
	private final FrameBuffer downsampleBuffer;
	
	protected HashMap<Class, Batch> batches;
	
	private final Camera cam;
	
	private final Color outline = new Color(1, 0, 0, 1);
	private final Color clear = new Color(0, 0, 0, 0);
	
	public SilhouetteEffect(int BUFFER_WIDTH, int BUFFER_HEIGHT, Camera cam) 
	{
		this.BUFFER_WIDTH = BUFFER_WIDTH;
		this.BUFFER_HEIGHT = BUFFER_HEIGHT;
		this.cam = cam;
		
		downsampleBuffer = new FrameBuffer(Format.RGBA8888, BUFFER_WIDTH, BUFFER_HEIGHT, false);
		blur = new BlurEffect(0.98f, 1.2f, BUFFER_WIDTH, BUFFER_HEIGHT);
	}

	@Override
	public void render(Texture texture, FrameBuffer buffer, Texture depthTexture)
	{
		downsampleBuffer.begin();
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(false);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		Gdx.gl20.glLineWidth(10);
		
		for (Entity e : GLOBALS.needsSilhouette)
		{
			e.queueRenderables(cam, GLOBALS.LIGHTS, 0, batches, false);
		}
		
		((TexturedMeshBatch) batches.get(TexturedMeshBatch.class)).render(cam, GL20.GL_LINES, outline);		
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(cam, GL20.GL_LINES, outline);
		
		for (Entity e : GLOBALS.needsSilhouette)
		{
			e.queueRenderables(cam, GLOBALS.LIGHTS, 0, batches, false);
		}
		
		((TexturedMeshBatch) batches.get(TexturedMeshBatch.class)).render(cam, GL20.GL_TRIANGLES, clear);
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(cam, GL20.GL_TRIANGLES, clear);

		downsampleBuffer.end();
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		//blur.render(downsampleBuffer.getColorBufferTexture(), downsampleBuffer, depthTexture);
		
		buffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setShader(null);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
		
		batch.begin();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		batch.draw(texture, 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		batch.draw(downsampleBuffer.getColorBufferTexture(), 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, downsampleBuffer.getColorBufferTexture().getWidth(), downsampleBuffer.getColorBufferTexture().getHeight(),
				false, true);
		
		batch.end();

		buffer.end();
	}
	
	@Override
	public void create() 
	{
		TexturedMeshBatch renderer = new TexturedMeshBatch(RenderType.SIMPLE);
		AnimatedModelBatch modelBatch = new AnimatedModelBatch(12, RenderType.SIMPLE);
		
		batches = new HashMap<Class, Batch>();
		batches.put(TexturedMeshBatch.class, renderer);
		batches.put(AnimatedModelBatch.class, modelBatch);
	}

}
