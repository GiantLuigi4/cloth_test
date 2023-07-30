package tfc.cloth.shapes;

import tfc.cloth.phys.Point;
import tfc.cloth.phys.Vector3;

import java.util.ArrayList;

public class PointBuilder {
	Vector3 origin;
	ArrayList<Vector3> refs = new ArrayList<>();
	
	public PointBuilder(Vector3 origin) {
		this.origin = origin;
	}
	
	public void addRef(Vector3 vec) {
		if (!vec.equals(origin) && !refs.contains(vec))
			refs.add(vec);
	}
	
	public int refCount() {
		return refs.size();
	}
	
	private static final Vector3[] EMPTY = new Vector3[0];
	
	public Point build() {
		if (refs.size() == 0) return null;
		return new Point(origin, refs.toArray(EMPTY), origin.copy());
	}
}
