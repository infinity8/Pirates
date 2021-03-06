package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData.STATS;
import com.Lyeeedar.Pirates.GLOBALS;

public class ActionGravityAndMovement extends Action
{

	private final PositionalData pData = new PositionalData();
	private final StatusData sData = new StatusData();
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) data.get("entity");
		float delta = (Float) getData("delta", 0);
		
		entity.readData(pData);
		entity.readData(sData);
		
		pData.applyVelocity(delta, ((float)sData.stats.get(STATS.MASS)/100.0f));
		//pData.velocity.add(0, GLOBALS.GRAVITY*delta*((float)sData.stats.get(STATS.MASS)/100.0f), 0);
		
		entity.writeData(pData);
		
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
		return new ActionGravityAndMovement();
	}

	@Override
	public void dispose()
	{
		pData.dispose();
		sData.dispose();
	}

}
