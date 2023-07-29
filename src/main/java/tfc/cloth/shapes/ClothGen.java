package tfc.cloth.shapes;

import net.minecraft.world.phys.Vec3;
import tfc.cloth.phys.AbstractPoint;
import tfc.cloth.phys.Point;
import tfc.cloth.phys.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClothGen {
	public static ArrayList<AbstractPoint> generate(List<Vector3> vertices, List<Face> faces) {
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
			AbstractPoint pt = value.build();
			if (pt == null) continue;
			finalList.add(pt);
		}
		
		return finalList;
	}
	
	public static ArrayList<AbstractPoint> genSquare(int width, int height, double spacing, boolean structured) {
		HashMap<Vec3, Vector3> points = new HashMap<>();
		
		ArrayList<AbstractPoint> clothPoints = new ArrayList<>();
		
		Vector3 origin = new Vector3((width / 2d) * spacing, 0, (height / 2d) * spacing);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				points.put(new Vec3(x, y, 0), new Vector3(x * spacing, 0, y * spacing));
			}
		}
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ArrayList<Vector3> refs = new ArrayList<>();
				
				if (x > 0) refs.add(points.get(new Vec3(x - 1, y, 0)));
				if (x < width - 1) refs.add(points.get(new Vec3(x + 1, y, 0)));
				if (y > 0) refs.add(points.get(new Vec3(x, y - 1, 0)));
				if (y < height - 1) refs.add(points.get(new Vec3(x, y + 1, 0)));
				
				if (structured) {
					refs.add(points.get(new Vec3(x + 1, y + 1, 0)));
					refs.add(points.get(new Vec3(x - 1, y - 1, 0)));
					refs.add(points.get(new Vec3(x - 1, y + 1, 0)));
					refs.add(points.get(new Vec3(x + 1, y - 1, 0)));
					
					int prevNull = 0;
					while (refs.contains(null)) {
						int i;
						for (i = prevNull; i < refs.size(); i++)
							if (refs.get(i) == null)
								break;
						refs.remove(i);
						prevNull = i;
					}
				}
				
				Vector3 pos = points.get(new Vec3(x, y, 0));
				
				clothPoints.add(new Point(
						pos, refs.toArray(new Vector3[0]),
						origin
				));
			}
		}
		
		return clothPoints;
	}
}
