package com.Lyeeedar.Util;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.ClosestRayResultSkippingCallback;
import com.Lyeeedar.Collision.Octtree.OcttreeFrustum;
import com.Lyeeedar.Collision.Octtree.OcttreeShape;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

public class FollowCam extends PerspectiveCamera {
	
	private static final float limit = 60;
	
	private final Controls controls;
	private final Vector3 tmp = new Vector3();
	public float followDist = 10.0f;
	public float followHeight = 4.0f;
	
	public OcttreeShape aiShape;
	public OcttreeFrustum renderFrustum;
	
	private final Matrix4 tmpMat = new Matrix4();
	private final Vector3 tmpVec = new Vector3();
	private final Vector3 tmpVec2 = new Vector3();
	
	public ClosestRayResultSkippingCallback ray;
	
	private Entity lockOn;
	
	public boolean underwater = false;
	private float underwaterCD = 0;
	
	public FollowCam(Controls controls, OcttreeShape aiShape, float sizeLim)
	{
		this.controls = controls;
		this.aiShape = aiShape;
		this.renderFrustum = new OcttreeFrustum(this, sizeLim);
	}
	
	private float Yangle = -15;
	private float Xangle = 0;
	
	public boolean isLockOn()
	{
		return lockOn != null;
	}
	
	public void lockOn(Entity entity)
	{
		this.lockOn = entity;
	}
	
	public void setYAngle(float angle)
	{
		this.Yangle = angle;
	}
	
	public void setXAngle(float angle)
	{
		this.Xangle = angle;
	}
	
	public void setFollowDist(float dist)
	{
		this.followDist = dist;
	}
	
	@Override
	public void update()
	{
		tmpVec.set(direction).scl(-1.0f);
		tmpMat.setToTranslation(position).rotate(GLOBALS.DEFAULT_ROTATION, tmpVec);
		if (aiShape != null) 
		{
			aiShape.setPosition(position);
			aiShape.setRotation(direction);
		}
		super.update();
	}
	
	public void updateBasic(PositionalData pData)
	{
		up.set(pData.up);
		direction.set(pData.rotation);
		Yrotate(Yangle);
		
		tmp.set(direction).scl(-1*followDist).add(pData.position).add(0, followHeight, 0);
		
		position.set(tmp);
		update();
	}
	
	public void update(Entity entity, float delta)
	{
		if (ray == null) ray = new ClosestRayResultSkippingCallback();
		
		followDist += controls.scrolled();
		followDist = MathUtils.clamp(followDist, 5, 50);
		
		Xangle -= controls.getDeltaX();
		Yangle -= controls.getDeltaY();
		
		if (Yangle > limit) Yangle = limit;
		if (Yangle < -limit) Yangle = -limit;
		
		PositionalData pData = entity.readOnlyRead(PositionalData.class);
		
		if (lockOn != null)
		{
			if (lockOn.DISPOSED) 
			{
				lockOn = null;
				update(entity, delta);
				return;
			}
			GLOBALS.needsSilhouette.add(lockOn);
			direction.set(lockOn.readOnlyRead(PositionalData.class).position).sub(pData.position).nor();
		}
		else
		{
			direction.set(GLOBALS.DEFAULT_ROTATION);
			direction.rotate(Xangle, 0, 1, 0);
			Yrotate(Yangle);
		}
		if (direction.isZero()) direction.set(GLOBALS.DEFAULT_ROTATION);
		//up.set(direction).crs(GLOBALS.DEFAULT_UP).crs(direction);
		up.set(pData.up);
		
		tmp.set(direction).scl(-1*followDist).add(pData.position).add(0, followHeight, 0);
		position.set(tmp);
		
		ray.setCollisionObject(null);
        ray.setClosestHitFraction(1f);

		tmpVec.set(pData.position).add(0, followHeight, 0);
		tmpVec2.set(position);
		
		ray.getRayFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
		ray.getRayToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
		ray.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
		ray.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
		ray.setSkipObject(pData.physicsBody);
		
		GLOBALS.physicsWorld.world.rayTest(tmpVec, tmpVec2, ray);
		if (ray.hasHit())
		{
			position.set(ray.getHitPointWorld().x(), ray.getHitPointWorld().y(), ray.getHitPointWorld().z());
		}
		
		update();
		
		for (int i = 0; i < 4; i++)
		{
			ray.setCollisionObject(null);
	        ray.setClosestHitFraction(1f);
			
			tmpVec.set(pData.position).add(0, followHeight, 0);
			tmpVec2.set(frustum.planePoints[i]);
			
			ray.getRayFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
			ray.getRayToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
			ray.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
			ray.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
			ray.setSkipObject(pData.physicsBody);
			
			GLOBALS.physicsWorld.world.rayTest(tmpVec, tmpVec2, ray);
			if (ray.hasHit())
			{
				tmpVec2.sub(ray.getHitPointWorld().x(), ray.getHitPointWorld().y(), ray.getHitPointWorld().z());
				position.sub(tmpVec2);
				update();
			}
		}
		
		underwaterCD -= delta;
		
		if (underwaterCD < 0)
		{
			int success = 0;
			int i = underwater ? 0 : 2 ;
			int in  = underwater ? 2 : 4 ;
			for (; i < in; i++)
			{
				float seaHeight = GLOBALS.SKYBOX.sea.waveHeight(frustum.planePoints[i].x, frustum.planePoints[i].z)+0.1f;
				float diff = seaHeight - frustum.planePoints[i].y;
				if (underwater) 
				{
					if (diff < 0) 
					{
						success++;
					}
				}
				else 
				{
					if (diff > 0) 
					{
						success++;
					}
				}
			}
			if (success == 2)
			{
				underwaterCD = 1.0f;
				underwater = !underwater;
				
//				if (underwater)
//				{
//					GLOBALS.FOG_MIN = 0;
//					GLOBALS.FOG_MAX = 2000;
//				}
//				else
//				{
//					GLOBALS.FOG_MIN = 1000;
//					GLOBALS.FOG_MAX = 6000;
//				}
			}
		}
		
		float seaY = 0;
		boolean sea = false;
		int i = underwater ? 2 : 0 ;
		int in  = underwater ? 4 : 2 ;
		for (; i < in; i++)
		{
			float seaHeight = GLOBALS.SKYBOX.sea.waveHeight(frustum.planePoints[i].x, frustum.planePoints[i].z);
			seaHeight += underwater ? -0.1f : 0.1f ;
			float diff = seaHeight - frustum.planePoints[i].y;
			
			if (underwater) 
			{
				if (diff < seaY) 
				{
					seaY = diff;
					sea = true;
				}
			}
			else 
			{
				if (diff > seaY) 
				{
					seaY = diff;
					sea = true;
				}
			}
		}
		
		if (sea)
		{
			position.y += seaY;
			update();
		}
	}

	public void Yrotate (float angle) {	
		Vector3 dir = tmp.set(direction).nor();
		if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
		{
			Vector3 localAxisX = dir;
			localAxisX.crs(up).nor();
			rotate(angle, localAxisX.x, localAxisX.y, localAxisX.z);
		}
	}
	
	
	public static btPairCachingGhostObject createFrustumObject(
			final Vector3... points)
	{
		final btPairCachingGhostObject result = new TestPairCachingGhostObject();
		final boolean USE_COMPOUND = true;
		// Using a compound shape is not necessary, but it's good practice to
		// create shapes around the center.
		Vector3 tmpV = new Vector3();
		Matrix4 tmpM = new Matrix4();
		if (USE_COMPOUND)
		{
			final Vector3 centerNear = new Vector3(points[2]).sub(points[0])
					.scl(0.5f).add(points[0]);
			final Vector3 centerFar = new Vector3(points[6]).sub(points[4])
					.scl(0.5f).add(points[4]);
			final Vector3 center = new Vector3(centerFar).sub(centerNear)
					.scl(0.5f).add(centerNear);
			final btConvexHullShape hullShape = new btConvexHullShape();
			for (int i = 0; i < points.length; i++)
				hullShape.addPoint(tmpV.set(points[i]).sub(center));
			final btCompoundShape shape = new btCompoundShape();
			shape.addChildShape(tmpM.setToTranslation(center), hullShape);
			result.setCollisionShape(shape);
		}
		else
		{
			final btConvexHullShape shape = new btConvexHullShape();
			for (int i = 0; i < points.length; i++)
				shape.addPoint(points[i]);
			result.setCollisionShape(shape);
		}
		result.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
		return result;
	}
	
	public static btGhostObject createSphere(float radius)
	{
		final btPairCachingGhostObject result = new TestPairCachingGhostObject();

		result.setCollisionShape(new btSphereShape(radius));
		result.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
		
		return result;
	}
	
	public static class TestPairCachingGhostObject extends btPairCachingGhostObject {
        public btCollisionShape shape;
        @Override
        public void setCollisionShape (btCollisionShape collisionShape) {
                shape = collisionShape;
                super.setCollisionShape(collisionShape);
        }
        @Override
        public void dispose () {
                super.dispose();
                if (shape != null)
                        shape.dispose();
                shape = null;
        }
}

}
