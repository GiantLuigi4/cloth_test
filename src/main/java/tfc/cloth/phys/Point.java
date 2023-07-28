package tfc.cloth.phys;

import net.minecraft.core.Direction;

public class Point extends AbstractPoint {
	Cloth myCloth = null;
	
	public final Vector3 pos;
	public final Vector3 lastPos;
	public final double[] chainSize; // TODO: should be an array, based off the initial distances
	public final Vector3 origin;
	Vector3[] refs;
	Vector3 impulse = new Vector3(0, 0, 0);
	double damping = 0.99;
	
	public Point setDamping(double damping) {
		this.damping = damping;
		return this;
	}
	
	private static final Constraint defaultConstraint = (point) -> {
	};
	
	public void setCloth(Cloth cloth) {
		myCloth = cloth;
	}
	
	public Point(Vector3 pos, Vector3[] refs, Vector3 origin) {
		this(pos, refs, origin, defaultConstraint);
	}
	
	public Point(Vector3 pos, Vector3[] refs, Vector3 origin, Constraint constraint) {
		this.pos = pos;
		this.lastPos = pos.copy();
		this.refs = refs;
		chainSize = new double[refs.length];
		for (int i = 0; i < refs.length; i++) {
			chainSize[i] = refs[i].distance(pos);
		}
		this.constraint = constraint;
		this.origin = origin;
	}
	
	
	// TODO: some form of dampening based off cloth normal vector?
	public void tick(Tracer tracer, Vector3 worker, Vector3 gravity) {
		worker.set(
				pos.x + (pos.x - lastPos.x) * damping + gravity.x + impulse.x,
				pos.y + (pos.y - lastPos.y) * damping + gravity.y + impulse.y,
				pos.z + (pos.z - lastPos.z) * damping + gravity.z + impulse.z
		);
		impulse.set(0, 0, 0);
		
		double td = worker.distance(pos);
		Direction[] dir = new Direction[1];
		double d = tracer.traceDist(
				pos, worker, dir
		);
		worker.sub(pos).scl(d / td).add(pos);
		if (d < td || td == 0) {
			
			if (myCloth.centerOfClamping == null)
				myCloth.centerOfClamping = new Vector3(pos);
			
			myCloth.centerOfClamping.scl(0.5).add(pos.copy().scl(0.5));

//			worker.sub(pos);
//			if (dir[0].getStepX() < 0) {
//				if (worker.x < 0) worker.x = 0;
//			} else if (dir[0].getStepX() > 0) {
//				if (worker.x > 0) worker.x = 0;
//			} else if (dir[0].getStepY() < 0) {
//				if (worker.y < 0) worker.y = 0;
//			} else if (dir[0].getStepY() > 0) {
//				if (worker.y > 0) worker.y = 0;
//			} else if (dir[0].getStepZ() < 0) {
//				if (worker.z < 0) worker.z = 0;
//			} else if (dir[0].getStepZ() > 0) {
//				if (worker.z > 0) worker.z = 0;
//			}
//			worker.scl(0.125);
//			worker.add(pos);
			
			return;
		}
		
		lastPos.set(pos);
		pos.set(worker);
	}
	
	public Vector3[] getRefs() {
		return refs;
	}
	
	public void normalize() {
		Vector3 srcPos = new Vector3(pos);
		
		for (int i = 0; i < refs.length; i++) {
			Vector3 worker = new Vector3(pos);
			worker.setDistance(refs[i], chainSize[i]);
			pos.scl(0.5).add(worker.scl(0.5));
			
			Vector3 impulse = srcPos.copy().sub(pos);
			this.impulse.add(impulse.scl(-2d / refs.length));
			this.pos.set(srcPos);
		}
	}
	
	@Override
	public Vector3 getPos() {
		return pos;
	}
	
	@Override
	public Vector3 getVeloc() {
		return new Vector3(
				(pos.x - lastPos.x) * damping + impulse.x,
				(pos.y - lastPos.y) * damping + impulse.y,
				(pos.z - lastPos.z) * damping + impulse.z
		);
	}
	
	@Override
	public void push(Vector3 vec) {
		impulse.add(vec);
	}
}
