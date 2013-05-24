package Entities;

import Entities.AI.AI_Package;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.lyeeedar.Pirates.GLOBALS;

public class Entity {
	
	private final EntityData data = new EntityData();	
	private AI_Package ai;
	private EntityRunnable runnable = new EntityRunnable();
	
	public Entity()
	{
	}
	
	public void setAI(AI_Package ai)
	{
		this.ai = ai;
	}
	
	public Runnable getRunnable(float delta)
	{
		runnable.set(delta, this);
		return runnable;
	}
	
	public void update(float delta)
	{
		ai.update(delta);
	}
	
	public void writeData(EntityData data)
	{
		synchronized(this.data)
		{
			this.data.write(data);
		}
	}
	
	public EntityData readData(EntityData data)
	{
		synchronized(this.data)
		{
			this.data.cpy(data);
		}
		
		return data;
	}
	
	public static class EntityRunnable implements Runnable
	{
		float delta;
		Entity entity;
		
		public void set(float delta, Entity entity)
		{
			this.entity = entity;
			this.delta = delta;
		}
		
		@Override
		public void run() {
			entity.update(delta);
		}
	}
	
	public static class EntityData
	{
		public float radius = 0.5f;
		public float radius2 = radius*radius;
		public float radius2y = (radius+GLOBALS.STEP)*(radius+GLOBALS.STEP);
		
		public void updateRadius(float radius)
		{
			this.radius = radius;
			this.radius2 = radius * radius;
			this.radius2y = (radius+GLOBALS.STEP)*(radius+GLOBALS.STEP);
		}
		
		public final Vector3 position = new Vector3();
		public final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
		public final Vector3 up = new Vector3(GLOBALS.DEFAULT_UP);	
		public final Vector3 velocity = new Vector3();
		
		public int jumpToken = 0;
		public float scale = 1;
		
		private final Vector3 tmpVec = new Vector3();
		private final Matrix4 tmpMat = new Matrix4();
		private final Vector3 nPos = new Vector3();
		private final Vector3 v = new Vector3();
		private final Vector3 dest = new Vector3();
		private final Ray ray = new Ray(new Vector3(), new Vector3());
		private final Vector3 collision = new Vector3();
		private final float[] min_dist = {Float.MAX_VALUE};
		private final Vector3[] tmp = {new Vector3(), new Vector3(), new Vector3(), new Vector3()};

		public void write(EntityData data)
		{
			position.set(data.position);
			rotation.set(data.rotation);		
			velocity.set(data.velocity);
			up.set(data.up);
			jumpToken = data.jumpToken;
			scale = data.scale;
			radius = data.radius;
			radius2 = data.radius2;
			radius2y = data.radius2y;
		}
		
		public void cpy(EntityData target)
		{
			target.position.set(position);
			target.rotation.set(rotation);		
			target.velocity.set(velocity);
			target.up.set(up);
			target.jumpToken = jumpToken;
			target.scale = scale;
			target.radius = radius;
			target.radius2 = radius2;
			target.radius2y = radius2y;
		}
		
		public void Yrotate (float angle) {	
			Vector3 dir = tmpVec.set(rotation).nor();
			if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
			{
				Vector3 localAxisX = dir.set(rotation);
				localAxisX.crs(up).nor();
				rotate(localAxisX.x, localAxisX.y, localAxisX.z, angle);
			}
		}

		public void Xrotate (float angle) {
			rotate(0, 1, 0, angle);
		}

		public void rotate (float x, float y, float z, float angle) {
			Vector3 axis = tmpVec.set(x, y, z);
			tmpMat.setToRotation(axis, angle);
			rotation.mul(tmpMat).nor();
			up.mul(tmpMat).nor();
		}
		
		public void left_right(float mag)
		{
			velocity.x += (float)Math.sin(rotation.z) * mag;
			velocity.z += -(float)Math.sin(rotation.x) * mag;
		}

		public void forward_backward(float mag)
		{
			velocity.x += (float)Math.sin(rotation.x) * mag;
			velocity.z += (float)Math.sin(rotation.z) * mag;
		}
		
		public void applyVelocity(float delta)
		{
			if (velocity.len2() == 0) return;
			
			if (velocity.x < -GLOBALS.MAX_SPEED_X) velocity.x = -GLOBALS.MAX_SPEED_X;
			else if (velocity.x > GLOBALS.MAX_SPEED_X) velocity.x = GLOBALS.MAX_SPEED_X;
			
			if (velocity.y < -GLOBALS.MAX_SPEED_Y) velocity.y = -GLOBALS.MAX_SPEED_Y;
			else if (velocity.y > GLOBALS.MAX_SPEED_Y) velocity.y = GLOBALS.MAX_SPEED_Y;
			
			if (velocity.z < -GLOBALS.MAX_SPEED_Z) velocity.z = -GLOBALS.MAX_SPEED_Z;
			else if (velocity.z > GLOBALS.MAX_SPEED_Z) velocity.z = GLOBALS.MAX_SPEED_Z;
			
			v.set(velocity.x, (velocity.y + GLOBALS.GRAVITY*delta), velocity.z);
			v.scl(delta);
			
			ray.origin.set(position).add(0, GLOBALS.STEP, 0);
			nPos.set(position).add(v);
			ray.direction.set(v.x, 0, 0).nor();
			min_dist[0] = Float.MAX_VALUE;

			if (v.x != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(ray, dest.set(nPos.x, position.y, position.z), collision, min_dist, tmp) && min_dist[0] < radius2)
			{
				velocity.x = 0;
				v.x = 0;
			}
			
			ray.origin.set(position).add(0, GLOBALS.STEP, 0);
			nPos.set(position).add(v);
			ray.direction.set(0, 0, v.z).nor();
			min_dist[0] = Float.MAX_VALUE;

			if (v.z != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(ray, dest.set(nPos.x, position.y, nPos.z), collision, min_dist, tmp) && min_dist[0]  < radius2)
			{
				velocity.z = 0;
				v.z = 0;
			}
			
			ray.origin.set(position).add(0, GLOBALS.STEP, 0);
			nPos.set(position).add(v);
			ray.direction.set(0, v.y, 0).nor();
			min_dist[0] = Float.MAX_VALUE;

			if (v.y != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(ray, dest.set(nPos.x, nPos.y, nPos.z), collision, min_dist, tmp) && min_dist[0]  < radius2y)
			{
				if (v.y < 0) jumpToken = 2;
				velocity.y = 0;
				v.y = 0;
				position.y = collision.y;
			}
			else if (nPos.y < -0.5f)
			{
				velocity.y = 0;
				v.y = 0;
				position.y = -0.5f;
				jumpToken = 2;
			}
			
			position.add(v.x, v.y, v.z);
			
			velocity.x = 0;
			velocity.z = 0;
		}
	}
	
}
