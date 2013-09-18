package com.Lyeeedar.Collision;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public final class SymbolicMesh extends CollisionShape<SymbolicMesh> {

	public final SymbolicMeshNode[] nodes;

	public final Triangle[] tris;

	private final Vector3 position = new Vector3();
	private final Vector3 rotationVec = new Vector3();
	private final Matrix4 rotation = new Matrix4();
	private final Matrix4 rotationTra = new Matrix4();
	private final Matrix4 combined = new Matrix4();
	private final Matrix4 inverse = new Matrix4();

	private final SymbolicMeshPartition partition;

	private final float minx;
	private final float miny;
	private final float minz;

	private final float maxx;
	private final float maxy;
	private final float maxz;

	public int count(SymbolicMeshPartition p)
	{
		int c = 0;
		for(SymbolicMeshPartition pp : p.children)
		{
			if (pp!=null) c+=count(pp);
		}

		return c+p.indices.length;
	}

	public void print()
	{
		try {

			String content = partition.print();

			File file = new File("tree.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SymbolicMesh(SymbolicMeshNode[] nodes, Triangle[] tris)
	{	
		this.tris = tris;

		List<SymbolicMeshNode> nodeList = new ArrayList<SymbolicMeshNode>(nodes.length);
		boolean[] duplicates = new boolean[nodes.length];
		for (int i = 0; i < nodes.length; i++)
		{
			if (duplicates[i]) continue;
			SymbolicMeshNode n1 = nodes[i];
			for (int j = 0; j < nodes.length; j++)
			{
				if (duplicates[j]) continue;
				if (j == i) continue;
				SymbolicMeshNode n2 = nodes[j];

				if (n1.x == n2.x && n1.y == n2.y && n1.z == n2.z)
				{
					duplicates[j] = true;
				}
			}

			nodeList.add(n1);
		}

		SymbolicMeshNode[] newNodes = new SymbolicMeshNode[nodeList.size()];
		nodeList.toArray(newNodes);

		this.nodes = newNodes;

		float minx = newNodes[0].x;
		float miny = newNodes[0].y;
		float minz = newNodes[0].z;

		float max = 0;

		for (SymbolicMeshNode node : newNodes)
		{
			if (node.x < minx) minx = node.x;
			if (node.y < miny) miny = node.y;
			if (node.z < minz) minz = node.z;

			if (node.x-minx > max) max = node.x-minx;
			if (node.y-miny > max) max = node.y-miny;
			if (node.z-minz > max) max = node.z-minz;

			node.calculate();
		}

		max++;

		this.minx = minx;
		this.miny = miny;
		this.minz = minz;
		this.maxx = minx+max;
		this.maxy = miny+max;
		this.maxz = minz+max;

		TempMeshPartition temp = new TempMeshPartition(null, this.minx, this.maxx, this.miny, this.maxy, this.minz, this.maxz, (byte) 0, tris);

		for (short i = 0; i < tris.length; i++)
		{			
			temp.addTriangle(i);
		}

		temp.finalize();

		partition = new SymbolicMeshPartition(null, temp);

		setPosition(new Vector3(0, 0, 0));
		setRotation(GLOBALS.DEFAULT_ROTATION);
		updateMatrixes();

		print();
	}

	/**
	 * Recalculate all the transformations for this mesh
	 */
	public void updateMatrixes()
	{
		Matrix4 tmpMat = Pools.obtain(Matrix4.class);
		combined.setToTranslation(position).mul(rotation);
		rotationTra.set(rotation).tra();
		inverse.set(rotationTra).mul(tmpMat.setToTranslation(-position.x, -position.y, -position.z));
		Pools.free(tmpMat);
	}

	public Matrix4 getCombined()
	{
		return combined;
	}

	@Override
	public void setPosition(Vector3 position)
	{
		if (this.position.x == position.x && this.position.y == position.y && this.position.z == position.z) return;

		this.position.set(position);
		updateMatrixes();
	}

	@Override
	public void setRotation(Vector3 rotation) {

		if (rotationVec.x == rotation.x && rotationVec.y == rotation.y && rotationVec.z == rotation.z) return;

		rotationVec.set(rotation);
		this.rotation.setToRotation(GLOBALS.DEFAULT_ROTATION, rotation);
		updateMatrixes();
	}

	@Override
	public boolean collide(Sphere sphere) {
		return checkCollision(sphere, true);
	}

	@Override
	public boolean collide(Cylinder cylinder) {
		return checkCollision(cylinder, true);
	}

	@Override
	public boolean collide(Box rect) {
		return checkCollision(rect, true);
	}

	@Override
	public boolean collide(Triangle tri) {
		return checkCollision(tri, true);
	}

	@Override
	public boolean collide(CollisionRay ray) {
		return checkCollision(ray, false);
	}

	@Override
	public SymbolicMesh set(SymbolicMesh other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SymbolicMesh copy() {
		return this;
	}

	@Override
	public void transformPosition(Matrix4 matrix) {
		position.mul(matrix);
		updateMatrixes();
	}

	@Override
	public void transformDirection(Matrix4 matrix) {
		rotation.mul(matrix);
		updateMatrixes();
	}

	@Override
	public SymbolicMesh obtain() {
		return this;
	}

	@Override
	public void free() {
		//throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		//throw new UnsupportedOperationException();
	}

	@Override
	protected String string() {
		return "SymbolicMesh";
	}

	public boolean checkCollision(CollisionShape<?> shape, boolean fast)
	{	
		shape.transformPosition(inverse);
		shape.transformDirection(rotationTra);

		CollisionShape<?> check = shape.obtain();

		System.out.println(check+"\n"+partition.shape+"\n"+check.collide(partition.shape)+"\n"+count(partition)+"\n");

		//		SymbolicMeshPartition p = partition.getPartition(check);
		//		if (p!= null) {
		//			System.out.println(p+"  "+p.depth+"\n"+p.shape);
		//			for (short i : p.indices)
		//			{
		//				System.out.println(tris[i]);
		//			}
		//		}

		int[] value = {0};
		if (!checkCollisionRecursive(partition, shape, check, fast, value)) {
			System.out.println("FAIL "+value[0]);
			System.out.println(shape+" \n "+check+"\n\n\n\n\n");
			check.free();
			return false;
		}
		System.out.println("SUCCEED "+value[0]);
		System.out.println(shape+" \n "+check+"\n\n\n\n\n");

		check.free();

		shape.transformPosition(combined);

		return true;
	}

	private boolean checkCollisionRecursive(SymbolicMeshPartition partition, CollisionShape<?> shape, CollisionShape<?> check, boolean fast, int[] value)
	{
		check.reset();
		//if (!partition.shape.collide(check)) return false;

		boolean collide = (partition.indices.length > 0) ? ThreadSafeIntersector.collideShapeList(shape, tris, partition.indices, fast) : false ;
		value[0] += partition.indices.length;

		if (collide) System.out.println("Collided at partition "+partition);

		if (partition.children != null) for (SymbolicMeshPartition p : partition.children)
		{
			if (fast && collide) break;
			if (checkCollisionRecursive(p, shape, check, fast, value)) collide = true;
		}

		return collide;
	}

	public static SymbolicMesh getSymbolicMesh(Mesh mesh, float maxArea)
	{
		final VertexAttributes attributes = mesh.getVertexAttributes();
		final int positionOffset = attributes.getOffset(Usage.Position);
		final int vertCount = mesh.getNumVertices();
		final int vertexSize = attributes.vertexSize / 4;
		final int triangles = mesh.getNumIndices() / 3;

		final float[] verts = new float[vertexSize * vertCount];
		mesh.getVertices(verts);	
		final short[] indices = new short[mesh.getNumIndices()];
		mesh.getIndices(indices);

		final SymbolicMeshNode[] vertexNodes = new SymbolicMeshNode[vertCount];

		for (int i = 0; i < vertCount; i++)
		{
			vertexNodes[i] = new SymbolicMeshNode(
					verts[(i*vertexSize)+positionOffset+0],
					verts[(i*vertexSize)+positionOffset+1],
					verts[(i*vertexSize)+positionOffset+2]
					);
		}

		final List<Triangle> tris = new ArrayList<Triangle>(triangles);

		for (int i = 0; i < triangles; i++)
		{
			final SymbolicMeshNode n1 = vertexNodes[indices[(i*3)+0]];
			final SymbolicMeshNode n2 = vertexNodes[indices[(i*3)+1]];
			final SymbolicMeshNode n3 = vertexNodes[indices[(i*3)+2]];

			n1.addNodes(n2, n3);
			n2.addNodes(n1, n3);
			n3.addNodes(n1, n2);

			Triangle t = new Triangle();
			t.v1.set(n1.x, n1.y, n1.z);
			t.v2.set(n2.x, n2.y, n2.z);
			t.v3.set(n3.x, n3.y, n3.z);

			tris.add(t);
		}

		Triangle[] triArray = new Triangle[triangles];
		tris.toArray(triArray);

		return new SymbolicMesh(vertexNodes, triArray);
	}

	public final static class SymbolicMeshNode
	{
		public final float x;
		public final float y;
		public final float z;

		public final List<SymbolicMeshNode> connectedNodes;		
		public float[] distances;
		public float[] angles;

		public SymbolicMeshNode(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;

			connectedNodes = new ArrayList<SymbolicMeshNode>();
		}

		public void calculate()
		{
			int size = connectedNodes.size();
			distances = new float[size];
			angles = new float[size];

			Vector3 tmp1 = new Vector3();
			Vector3 tmp2 = new Vector3();

			for (int i = 0; i < size; i++)
			{
				SymbolicMeshNode node = connectedNodes.get(i);

				tmp1.set(x, y, z);
				tmp2.set(node.x, node.y, node.z);

				distances[i] = tmp1.dst(tmp2);
				angles[i] = getAngle(tmp1, tmp2);
			}
		}

		private float getAngle(Vector3 n1, Vector3 n2)
		{
			double O = Math.abs(n1.y-n2.y);
			double H = Math.sqrt(Math.pow(n1.x-n2.x, 2) + Math.pow(n1.z-n2.z, 2));

			return (float) Math.sin(O/H);
		}

		public void addNodes(SymbolicMeshNode... nodes)
		{
			for (SymbolicMeshNode n : nodes)
			{
				connectedNodes.add(n);
			}
		}
	}

	public class TempMeshPartition
	{

		private static final int BUCKET_SIZE = 8;
		private static final float MIN_BOX_SIZE = 10f;

		public byte depth;

		public float minx;
		public float miny;
		public float minz;

		public float midx;
		public float midy;
		public float midz;

		public float maxx;
		public float maxy;
		public float maxz;

		public Box box;

		public Triangle[] tris;
		public List<Short> indices;

		public short[] indexArray;

		public TempMeshPartition parent;

		public TempMeshPartition[] children;

		public TempMeshPartition(TempMeshPartition parent, float minx, float maxx, float miny, float maxy, float minz, float maxz, byte depth, Triangle[] tris)
		{
			this.depth = depth;

			this.minx = minx;
			this.miny = miny;
			this.minz = minz;

			this.midx = minx+((maxx-minx)/2f);
			this.midy = miny+((maxy-miny)/2f);
			this.midz = minz+((maxz-minz)/2f);

			this.maxx = maxx;
			this.maxy = maxy;
			this.maxz = maxz;

			this.box = new Box(new Vector3(midx, midy, midz), (maxx-minx)/2, (maxy-miny)/2, (maxz-minz)/2);

			this.parent = parent;

			this.tris = tris;
			this.indices = new ArrayList<Short>();
		}

		public void addTriangle(short index)
		{			
			indices.add(index);
		}

		public boolean cascade (short index) 
		{			
			for (TempMeshPartition p : children)
			{
				if (
						ThreadSafeIntersector.testPointInBox(tris[index].v1, p.box) &&
						ThreadSafeIntersector.testPointInBox(tris[index].v2, p.box) &&
						ThreadSafeIntersector.testPointInBox(tris[index].v3, p.box)
						)
				{
					p.addTriangle(index);
					return true;
				}
			}
			return false;
		}

		public void minimize()
		{
			minx = tris[0].v1.x;
			miny = tris[0].v1.y;
			minz = tris[0].v1.z;
			maxx = tris[0].v1.x;
			maxy = tris[0].v1.y;
			maxz = tris[0].v1.z;

			for (short s : indices)
			{
				if (tris[s].v1.x < minx) minx = tris[s].v1.x;
				if (tris[s].v1.y < minx) miny = tris[s].v1.y;
				if (tris[s].v1.z < minx) minz = tris[s].v1.z;
				if (tris[s].v1.x > maxx) maxx = tris[s].v1.x;
				if (tris[s].v1.y > maxx) maxy = tris[s].v1.y;
				if (tris[s].v1.z > maxx) maxz = tris[s].v1.z;

				if (tris[s].v2.x < minx) minx = tris[s].v2.x;
				if (tris[s].v2.y < minx) miny = tris[s].v2.y;
				if (tris[s].v2.z < minx) minz = tris[s].v2.z;
				if (tris[s].v2.x > maxx) maxx = tris[s].v2.x;
				if (tris[s].v2.y > maxx) maxy = tris[s].v2.y;
				if (tris[s].v2.z > maxx) maxz = tris[s].v2.z;

				if (tris[s].v3.x < minx) minx = tris[s].v3.x;
				if (tris[s].v3.y < minx) miny = tris[s].v3.y;
				if (tris[s].v3.z < minx) minz = tris[s].v3.z;
				if (tris[s].v3.x > maxx) maxx = tris[s].v3.x;
				if (tris[s].v3.y > maxx) maxy = tris[s].v3.y;
				if (tris[s].v3.z > maxx) maxz = tris[s].v3.z;
			}

			if (maxx-minx < MIN_BOX_SIZE)
			{
				float diff = MIN_BOX_SIZE-(maxx-minx);
				minx-=diff/2;
				maxx+=diff/2;
			}
			if (maxy-miny < MIN_BOX_SIZE)
			{
				float diff = MIN_BOX_SIZE-(maxy-miny);
				miny-=diff/2;
				maxy+=diff/2;
			}
			if (maxz-minz < MIN_BOX_SIZE)
			{
				float diff = MIN_BOX_SIZE-(maxz-minz);
				minz-=diff/2;
				maxz+=diff/2;
			}

			midx = minx+((maxx-minx)/2f);
			midy = miny+((maxy-miny)/2f);
			midz = minz+((maxz-minz)/2f);

			box = new Box(new Vector3(midx, midy, midz), (maxx-minx)/2, (maxy-miny)/2, (maxz-minz)/2);
		}
		
		public ArrayList<ArrayList<float[]>> powerset (ArrayList<float[]> x, ArrayList<float[]> y, ArrayList<float[]> z)
		{
			ArrayList<ArrayList<float[]>> output = new ArrayList<ArrayList<float[]>>();
			
			for (int xi = 0; xi < x.size(); xi++)
			{
				for (int yi = 0; yi < y.size(); yi++)
				{
					for (int zi = 0; zi < z.size(); zi++)
					{
						ArrayList<float[]> division = new ArrayList<float[]>();
						
						division.add(x.get(xi));
						division.add(y.get(yi));
						division.add(z.get(zi));
						
						output.add(division);
					}
				}
			}
			
			return output;
		}

		public void finalize()
		{
			if (indices.size() == 0) return;

			minimize();
			boolean sx = maxx-minx > MIN_BOX_SIZE;
			boolean sy = maxy-miny > MIN_BOX_SIZE;
			boolean sz = maxz-minz > MIN_BOX_SIZE;
			if (indices.size() > BUCKET_SIZE && (sx || sy || sz))
			{
				ArrayList<float[]> x = new ArrayList<float[]>();
				if (sx)
				{
					x.add(new float[]{minx, midx});
					x.add(new float[]{midx, maxx});
				}
				else
				{
					x.add(new float[]{minx, maxx});
				}
				
				ArrayList<float[]> y = new ArrayList<float[]>();
				if (sy)
				{
					y.add(new float[]{miny, midy});
					y.add(new float[]{midy, maxy});
				}
				else
				{
					y.add(new float[]{miny, maxy});
				}
				
				ArrayList<float[]> z = new ArrayList<float[]>();
				if (sz)
				{
					z.add(new float[]{minz, midz});
					z.add(new float[]{midz, maxz});
				}
				else
				{
					z.add(new float[]{minz, maxz});
				}
				
				ArrayList<ArrayList<float[]>> all = powerset(x, y, z);
				this.children = new TempMeshPartition[all.size()];
				
				for (int i = 0; i < all.size(); i++)
				{
					ArrayList<float[]> division = all.get(i);
					children[i] = new TempMeshPartition(this, division.get(0)[0], division.get(0)[1], division.get(1)[0], division.get(1)[1], division.get(2)[0], division.get(2)[1], (byte) (depth+1), tris);
				}

				Iterator<Short> itr = indices.iterator();

				while(itr.hasNext())
				{
					short index = itr.next();
					boolean success = cascade(index);

					if (success) {
						itr.remove();
					}
				}
			}

			int bucket_size = indices.size();
			indexArray = new short[bucket_size];

			for (int i = 0; i < bucket_size; i++)
			{
				indexArray[i] = indices.get(i);
			}

			if (children != null) for (TempMeshPartition p : children) p.finalize();
		}
	}

	public final static class SymbolicMeshPartition
	{	
		public final byte depth;

		public final float minx;
		public final float miny;
		public final float minz;

		public final float midx;
		public final float midy;
		public final float midz;

		public final float maxx;
		public final float maxy;
		public final float maxz;

		public final short[] indices;

		public final SymbolicMeshPartition parent;

		public final SymbolicMeshPartition[] children;

		public final Box shape;

		public SymbolicMeshPartition(SymbolicMeshPartition parent, TempMeshPartition tree)
		{
			this.depth = (tree != null) ? tree.depth : 0;

			this.minx = (tree != null) ? tree.minx : 0;
			this.miny = (tree != null) ? tree.miny : 0;
			this.minz = (tree != null) ? tree.minz : 0;

			this.midx = (tree != null) ? tree.midx : 0;
			this.midy = (tree != null) ? tree.midy : 0;
			this.midz = (tree != null) ? tree.midz : 0;

			this.maxx = (tree != null) ? tree.maxx : 0;
			this.maxy = (tree != null) ? tree.maxy : 0;
			this.maxz = (tree != null) ? tree.maxz : 0;

			this.parent = parent;

			this.indices = (tree != null && tree.indexArray != null) ? tree.indexArray : new short[0];

			this.shape = (tree != null) ? tree.box : new Box();

			if (tree !=null && tree.children != null)
			{
				this.children = new SymbolicMeshPartition[tree.children.length];
				for (int i = 0; i < tree.children.length; i++)
				{
					children[i] = new SymbolicMeshPartition(this, tree.children[i]);
				}
			}
			else
			{
				this.children = new SymbolicMeshPartition[0];
			}
		}

		public SymbolicMeshPartition getPartition(CollisionShape<?> shape)
		{
			shape.reset();
			if (!this.shape.collide(shape)) return null;

			SymbolicMeshPartition pp = null;
			for (SymbolicMeshPartition p : children)
			{
				if (p == null) continue;

				if (pp != null && p.getPartition(shape)!=null) throw new RuntimeException("argh double match");
				pp = p.getPartition(shape);
				if (pp != null) return pp;
			}

			return this;
		}

		public String print()
		{
			String self = this.toString()+" depth: "+depth+" indices: "+indices.length+" min: "+minx+","+miny+","+minz+" max: "+maxx+","+maxy+","+maxz+"\n";

			for (SymbolicMeshPartition p : children)
			{
				if (p == null) continue;

				self += p.print();
			}

			return self;
		}
	}
}
