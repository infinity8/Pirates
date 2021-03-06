package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData.LOCATION;
import com.Lyeeedar.Pirates.GLOBALS.DIRECTION;

public class ActionUpdateAnimations extends Action
{
	private final AnimationData aData = new AnimationData();
	private final PositionalData pData = new PositionalData();
	
	private float lockCD = 0;

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) data.get("entity");
		float delta = (Float) getData("delta", 0);
		
		lockCD -= delta;
		
		entity.readData(aData);
		entity.readData(pData);
		DIRECTION dir = (DIRECTION) getData("direction", DIRECTION.NONE);
		
		if (aData.animationLock)
		{
			lockCD = 0.5f;
		}
		else if (lockCD > 0)
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
			
			if (dir == DIRECTION.FORWARD)
			{
				aData.animate_speed = 1f;
				aData.anim = "move_water_forward";
			}
			else if (dir == DIRECTION.BACKWARD)
			{
				aData.animate_speed = 1f;
				aData.anim = "move_water_forward";
			}
			else if (dir == DIRECTION.LEFT)
			{
				aData.animate_speed = 1f;
				aData.anim = "move_water_forward";
			}
			else if (dir == DIRECTION.RIGHT)
			{
				aData.animate_speed = 1f;
				aData.anim = "move_water_forward";
			}
			else if (dir == DIRECTION.NONE)
			{
				aData.animate_speed = 1f;
				aData.anim = "idle_water";
			}
			
			aData.base_anim = "idle";
		}
		else if (dir != DIRECTION.NONE) {
			if (aData.animate) aData.updateAnimations = true;
			else aData.updateAnimations = false;
			aData.animate = true;
			
			if (dir == DIRECTION.FORWARD)
			{
				aData.animate_speed = 1f;
				aData.anim = "move_ground_forward";
			}
			else if (dir == DIRECTION.BACKWARD)
			{
				aData.animate_speed = -1f;
				aData.anim = "move_ground_forward";
			}
			else if (dir == DIRECTION.LEFT)
			{
				aData.animate_speed = 1f;
				aData.anim = "move_ground_side";
			}
			else if (dir == DIRECTION.RIGHT)
			{
				aData.animate_speed = -1f;
				aData.anim = "move_ground_side";
			}
			
			aData.base_anim = "idle";
		}
		else {
			if (!aData.animate) aData.updateAnimations = true;
			else aData.updateAnimations = false;
			aData.animate = false;
			aData.anim = "idle_ground";
			aData.base_anim = "idle";
			aData.animate_speed = 0.5f;
		}
		
		entity.writeData(aData);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Action copy()
	{
		return new ActionUpdateAnimations();
	}

	@Override
	public void dispose()
	{
		aData.dispose();
		pData.dispose();
	}

}
