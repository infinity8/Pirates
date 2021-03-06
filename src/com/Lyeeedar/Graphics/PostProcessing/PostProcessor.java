/*******************************************************************************
 * Copyright (c) 2012 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.Lyeeedar.Graphics.PostProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.Lyeeedar.Graphics.PostProcessing.Effects.BloomEffect;
import com.Lyeeedar.Graphics.PostProcessing.Effects.BlurEffect;
import com.Lyeeedar.Graphics.PostProcessing.Effects.DepthOfFieldEffect;
import com.Lyeeedar.Graphics.PostProcessing.Effects.EdgeDetectionEffect;
import com.Lyeeedar.Graphics.PostProcessing.Effects.PostProcessingEffect;
import com.Lyeeedar.Graphics.PostProcessing.Effects.SSAOEffect;
import com.Lyeeedar.Graphics.PostProcessing.Effects.SilhouetteEffect;
import com.Lyeeedar.Graphics.PostProcessing.Effects.UnderwaterEffect;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class PostProcessor {
	
	public enum Effect {
		BLUR,
		BLOOM,
		EDGE_DETECT,
		DOF,
		SILHOUETTE,
		UNDERWATER,
		SSAO
	}
	
	
	private Format format;
	private int width;
	private int height;
	
	private FrameBuffer captureBuffer;
	
	private final SpriteBatch batch = new SpriteBatch();
	
	private final Array<Effect> effectChain = new Array<Effect>();
	private final HashMap<Effect, PostProcessingEffect> effects = new HashMap<Effect, PostProcessingEffect>();
	
	private final BufferChain bufferChain;
		
	private final FollowCam cam;

	public PostProcessor(Format format, int width, int height, FollowCam cam) {
		this.format = format;
		this.width = width;
		this.height = height;
		this.cam = cam;
		
		captureBuffer = new FrameBuffer(format, width, height, true);
		bufferChain = new BufferChain(format, width, height);
		
		setupEffects();
	}
	
	public Array<Effect> getEffectChain()
	{
		return effectChain;
	}
	
	public void setEffectChain(Effect... effects)
	{
		effectChain.clear();
		for (int i = 0; i < effects.length; i++)
		{
			effectChain.add(effects[i]);
		}
	}
	
	public void addEffect(Effect effect)
	{
		effectChain.add(effect);
	}
	
	public void setupEffects()
	{
		for (Map.Entry<Effect, PostProcessingEffect> entry : effects.entrySet())
		{
			entry.getValue().dispose();
		}
		effects.clear();
		effects.put(Effect.BLUR, new BlurEffect(1.0f, 2.0f, 800, 600));
		effects.put(Effect.BLOOM, new BloomEffect(GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]));
		effects.put(Effect.SSAO, new SSAOEffect(GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]));
		effects.put(Effect.EDGE_DETECT, new EdgeDetectionEffect(GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]));
		effects.put(Effect.DOF, new DepthOfFieldEffect(GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]));
		effects.put(Effect.SILHOUETTE, new SilhouetteEffect(GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1], cam));
		effects.put(Effect.UNDERWATER, new UnderwaterEffect(GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1], cam));
	}
	
	public void updateBufferSettings(Format format, int f, int g) {
		this.format = format;
		this.width = f;
		this.height = g;
		
		captureBuffer.dispose();
		captureBuffer = new FrameBuffer(format, f, g, true);
		bufferChain.updateBuffers(format, f, g);
		setupEffects();
		
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}
	
	public void begin()
	{
		captureBuffer.begin();
	}
	
	public void end()
	{
		captureBuffer.end();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);
		
		Texture texture = applyEffectChain();

		batch.begin();
		batch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		batch.end();
	}
	
	private Texture applyEffectChain()
	{
		bufferChain.begin(captureBuffer.getColorBufferTexture(), captureBuffer.getDepthBufferTexture());
		
		for (Effect effect : effectChain)
		{
			bufferChain.applyEffect(effects.get(effect));
		}
		
		return bufferChain.getFinalImage();
	}

	public void dispose()
	{
		captureBuffer.dispose();
		bufferChain.dispose();
		
		for (Map.Entry<Effect, PostProcessingEffect> entry : effects.entrySet())
		{
			entry.getValue().dispose();
		}
	}

	public Format getFormat() {
		return format;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public FrameBuffer getCaptureBuffer() {
		return captureBuffer;
	}
}
