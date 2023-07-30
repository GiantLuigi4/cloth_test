package tfc.cloth.shapes;

import tfc.cloth.phys.Cloth;
import tfc.cloth.phys.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClothGen {
	public static Cloth generate(List<Vector3> vertices, List<Face> faces, boolean softBody) {
		HashMap<Vector3, Vector3> vertexMap = new HashMap<>();
		for (Vector3 vertex : vertices) vertexMap.put(vertex, vertex);
		
		HashMap<Vector3, PointBuilder> points = new HashMap<>();
		for (Face face : faces) {
			Vector3[] vecs = new Vector3[]{
					vertexMap.get(vertices.get(face.v0())),
					vertexMap.get(vertices.get(face.v1())),
					vertexMap.get(vertices.get(face.v2()))
			};
			PointBuilder[] builders = new PointBuilder[3];
			for (int i = 0; i < vecs.length; i++) {
				Vector3 vec = vecs[i];
				PointBuilder builder = points.get(vec);
				if (builder == null) points.put(vec, builder = new PointBuilder(vec));
				builders[i] = builder;
			}
			
			PointBuilder addTo = builders[0];
			int maxC = addTo.refCount();
			for (int i = 1; i < builders.length; i++) {
				int rc = builders[i].refCount();
				if (rc < maxC) {
					addTo = builders[i];
					maxC = rc;
				}
			}
			
			for (Vector3 vec : vecs) {
				addTo.addRef(vec);
			}
		}
		
		ArrayList<AbstractPoint> finalList = new ArrayList<>();
		for (PointBuilder value : points.values()) {
			AbstractPoint pt = value.build(softBody);
			if (pt == null) continue;
			finalList.add(pt);
		}
		
		//stackoverflow.com/a/18115837/8840278
		return new Cloth(finalList);
	}
}
