package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData.LOCATION;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private final FollowCam cam;
	private boolean jump = false;
	
	private final PositionalData pData = new PositionalData();
	private final AnimationData aData = new AnimationData();
	private final EquipmentData eData = new EquipmentData();
	private final StatusData sData = new StatusData();
		
	boolean activatecd = false;
	
	private final Vector3 nextRot = new Vector3();
	private final Vector3 tmpVec = new Vector3();
	
	public AI_Player_Control(Controls controls, FollowCam cam)
	{
		super();
		
		this.controls = controls;
		this.cam = cam;
	}

	@Override
	public void update(float delta) {
		
		entity.readData(pData, PositionalData.class);
		entity.readData(aData, AnimationData.class);
		entity.readData(eData, EquipmentData.class);
		entity.readData(sData, StatusData.class);
		
		evaluateDamage(sData, aData, delta);
		
		if (sData.currentHealth > 0)
		{
			if (!aData.animationLock)
			{
				// Evaluate controls
				int speed = 50;
				if (controls.sprint()) speed = 10;
				if (Gdx.input.isKeyPressed(Keys.ALT_LEFT))
				{
					speed = 1500;
				}
				
				nextRot.set(0, 0, 0);
				
				if (controls.up()) 
				{
					tmpVec.set(cam.direction.x, 0, cam.direction.z).nor();
					if (nextRot.isZero()) nextRot.add(tmpVec);
					else nextRot.add(tmpVec).scl(0.5f);
				}
				if (controls.down()) 
				{
					tmpVec.set(-cam.direction.x, 0, -cam.direction.z).nor();
					if (nextRot.isZero()) nextRot.add(tmpVec);
					else nextRot.add(tmpVec).scl(0.5f);
				}
				
				if (controls.left()) 
				{
					tmpVec.set(-cam.direction.x, 0, -cam.direction.z).nor();
					tmpVec.crs(GLOBALS.DEFAULT_UP);
					if (nextRot.isZero()) nextRot.add(tmpVec);
					else nextRot.add(tmpVec).scl(0.5f);
				}
				if (controls.right()) 
				{
					tmpVec.set(cam.direction.x, 0, cam.direction.z).nor();
					tmpVec.crs(GLOBALS.DEFAULT_UP);
					if (nextRot.isZero()) nextRot.add(tmpVec);
					else nextRot.add(tmpVec).scl(0.5f);
				}
				
				if (!nextRot.isZero())
				{
					pData.rotation.set(nextRot.nor());
					pData.up.set(GLOBALS.DEFAULT_UP);
					pData.forward_backward(speed);
				}
				
				if (controls.jump() && !jump) {
					pData.velocity.y = 70;
					pData.jumpToken--;
					jump = true;
				}
				else if (!controls.jump())
				{
					jump = false;
				}
				
			}

			if (controls.rightClick()) use(Equipment_Slot.RARM, eData);
			else 
			{
				stopUsing(Equipment_Slot.RARM, eData);
			}

//			if (activatecd && Gdx.input.isKeyPressed(Keys.E))
//			{
//				box.center.set(entityPos.rotation).scl(2).add(entityPos.position);
//				Entity e = activate(box, entityPos.graph, list, entityPos.position, pData);
//				if (e != null) e.activate(entity);
//				activatecd = false;
//			}
//			else if (!Gdx.input.isKeyPressed(Keys.E))
//			{
//				activatecd = true;
//			}
			
			// Update animations
			
			if (aData.animationLock)
			{
				
			}
			else if (pData.location == LOCATION.AIR)
			{
				if (!aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = false;
				aData.animate_speed = 1f;
				aData.anim = "fall";
				aData.base_anim = "idle";
			}
			else if (pData.location == LOCATION.SEA)
			{
				if (aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = true;
				aData.animate_speed = 1f;
				aData.anim = "swim";
				aData.base_anim = "idle";
			}
			else if (!nextRot.isZero()) {
				if (aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = true;
				aData.animate_speed = 1f;
				if (!controls.sprint()) aData.anim = "run";
				else aData.anim = "walk";
				aData.base_anim = "idle";
			}
			else {
				if (!aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = false;
				aData.anim = "idle";
				aData.base_anim = "idle";
				aData.animate_speed = 0.5f;
			}

		}
		
		pData.applyVelocity(delta);
		pData.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
				
		entity.writeData(pData, PositionalData.class);
		entity.writeData(aData, AnimationData.class);
		entity.writeData(eData, EquipmentData.class);
		entity.writeData(sData, StatusData.class);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
