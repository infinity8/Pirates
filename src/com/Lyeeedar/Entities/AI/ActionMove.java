package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Vector3;

public class ActionMove extends Action
{
	private final PositionalData pData = new PositionalData();
	private final StatusData sData = new StatusData();
	public final float velocity;
	
	
	public ActionMove(float velocity)
	{
		this.velocity = velocity;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		
		entity.readData(pData);
		entity.readData(sData);
		
		pData.forward_backward(velocity*sData.speed);
		
		entity.writeData(pData);
		
		parent.setDataTree("moved", true);
		
		state = BehaviourTreeState.FINISHED;
		return state;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Action copy()
	{
		return new ActionMove(velocity);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
	}

}
