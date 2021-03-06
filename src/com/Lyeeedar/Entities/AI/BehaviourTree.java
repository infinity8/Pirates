package com.Lyeeedar.Entities.AI;

import java.util.HashMap;
import java.util.Map;

import com.Lyeeedar.Entities.Entity.AI;

public class BehaviourTree implements AI
{
	private final Selector root;
	
	public BehaviourTree(Selector root)
	{
		this.root = root;
	}
	
	public void setData(String key, Object value)
	{
		root.setData(key, value);
	}
	
	@Override
	public void update(float delta)
	{
		setData("delta", delta);
		root.evaluate();
	}
	
	@Override
	public AI copy()
	{
		BehaviourTree nbt = new BehaviourTree((Selector)root.copy());
		
		for (Map.Entry<String, Object> entry : root.data.entrySet())
		{
			nbt.root.setData(entry.getKey(), entry.getValue());
		}
		
		return nbt;
	}
	
	@Override
	public void dispose()
	{
		root.dispose();
	}
	
	public enum BehaviourTreeState
	{
		FAILED,
		FINISHED,
		RUNNING
	}
	public static abstract class BehaviourTreeNode implements Comparable<BehaviourTreeNode>
	{
		public Selector parent;
		public int priority;
		public BehaviourTreeState state;
		
		protected final HashMap<String, Object> data = new HashMap<String, Object>();
		
		public abstract BehaviourTreeState evaluate();
		public abstract void cancel();
		
		public BehaviourTreeNode()
		{

		}
		
		@Override
		public int compareTo(BehaviourTreeNode o)
		{
			return o.priority-priority;
		}
		
		public void setData(String key, Object value)
		{
			data.put(key, value);
		}
		
		public Object getData(String key, Object fallback)
		{
			Object o = data.get(key);
			return o != null ? o : fallback;
		}
		
		public abstract BehaviourTreeNode copy();
		public abstract void dispose();
	}
	
	public static abstract class Conditional extends BehaviourTreeNode
	{

		public Conditional()
		{
			super();
		}
		
	}
	public static abstract class Action extends BehaviourTreeNode
	{
		public Action()
		{
			super();
		}
		
	}

}
