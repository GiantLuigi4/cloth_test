//package tfc.cloth.phys;
//
//import java.util.HashMap;
//
//public class SoftBodyPoint0 extends AbstractPoint {
//	public final Vector3 pos;
//	public final Vector3 veloc = new Vector3(0, 0, 0);
//	public final double chainSize0; // TODO: should be an array, based off the initial distances
//	public final Vector3 origin;
//	Vector3[] refs;
//
//	private static final Constraint defaultConstraint = (point) -> {
//	};
//
//	public SoftBodyPoint0(Vector3 pos, Vector3[] refs, Vector3 origin) {
//		this(pos, refs, origin, defaultConstraint);
//	}
//
//	public SoftBodyPoint0(Vector3 pos, Vector3[] refs, Vector3 origin, Constraint constraint) {
//		this.pos = pos;
//		this.refs = refs;
//		chainSize0 = 1 / 21d;
//		this.constraint = constraint;
//		this.origin = origin;
//	}
//
//	// TODO: some form of dampening based off cloth normal vector?
//	public void tick(Tracer tracer, Vector3 worker, Vector3 gravity) {
//		veloc.add(gravity.copy());
//		if (veloc.length() > 10) {
//			veloc.setLength(10);
//		}
//		worker.set(
//				pos.x + veloc.x,
//				pos.y + veloc.y,
//				pos.z + veloc.z
//		);
//		double expectedD = pos.distance(worker);
//		double d = tracer.traceDist(pos, worker);
//		if (d != expectedD) {
//			veloc.set(0, 0, 0);
////			pos.set(worker);
////			worker.sub(pos).scl(0.01);
////			pos.add(worker);
//			return;
//		}
//		pos.set(worker);
//
////		Vector3 cVec = new Vector3(pos.x, pos.y, pos.z);
//
////		normalize();
//
////		Vector3 vec1 = pos.copy();
////		pos.set(cVec);
////		vec1.sub(pos).scl(-0.5);
////		veloc.add(cVec);
//
//		veloc.scl(0.75);
//	}
//
//	public Vector3[] getRefs() {
//		return refs;
//	}
//
//	public void normalize() {
//		HashMap<Double, Vector3> vecsByDist = new HashMap<>();
//		for (int i = 0; i < refs.length; i += 1) {
//			Vector3 ref0 = refs[i];
//
//			double dr0 = pos.distance(ref0);
//			vecsByDist.put(dr0, pos);
//			if (dr0 != chainSize0) pos.setDistance(ref0, chainSize0);
//		}
////		for (Double aDouble : new TreeSet<>(vecsByDist.keySet())) {
////			pos.normalize(vecsByDist.get(aDouble), chainSize0);
////		}
//	}
//
//	@Override
//	public Vector3 getPos() {
//		return pos;
//	}
//
//	@Override
//	public Vector3 getVeloc() {
//		return veloc;
//	}
//}
