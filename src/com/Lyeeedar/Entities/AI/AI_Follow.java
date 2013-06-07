package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Vector3;

public class AI_Follow extends AI_Package {
	Entity follow;
	
	PositionalData entityPos = new PositionalData();
	PositionalData followPos = new PositionalData();
	
	AnimationData entityAnim = new AnimationData();
	
	Vector3 tmp = new Vector3();

	public AI_Follow(Entity entity) {
		super(entity);
	}
	
	public void setFollowTarget(Entity entity)
	{
		this.follow = entity;
	}

	@Override
	public void update(float delta) {
		
		entity.readData(entityPos, PositionalData.class);	
		follow.readData(followPos, PositionalData.class);
		
		entity.readData(entityAnim, AnimationData.class);	
		
		entityAnim.updateAnimations = true;
		entityAnim.animation = 3;
		entityAnim.anim = "move";
		
		double a = GLOBALS.angle(entityPos.rotation, tmp.set(entityPos.position).sub(followPos.position).nor(), up);

		if (Math.abs(a) < delta*100)
		{
			entityPos.rotate(0,  1, 0, (float) a);
		}
		else if (a > 0)
		{
			entityPos.rotate(0, 1, 0, (float) (-delta*100*Math.random()));
		}
		else
		{
			entityPos.rotate(0, 1, 0, (float) (delta*100*Math.random()));
		}
		
		entityPos.forward_backward(8);
		
		entityPos.applyVelocity(delta);
		entityPos.velocity.add(0, GLOBALS.GRAVITY*delta, 0);

		entity.writeData(entityPos, PositionalData.class);
		entity.writeData(entityAnim, AnimationData.class);
	}
	
	private final Vector3 up = new Vector3(0, 1, 0);

	@Override
	public void inform() {
		// TODO Auto-generated method stub
		
	}
}