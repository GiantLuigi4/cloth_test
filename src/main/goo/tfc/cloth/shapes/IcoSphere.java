package tfc.cloth.shapes;

import com.mojang.datafixers.util.Pair;
import tfc.cloth.phys.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IcoSphere {
	static List<Vector3> icosahedron_vertices;
	
	static {
		double phi = (1.0f + Math.sqrt(5.0f)) / 2.0f;
		
		List<Vector3> vertices = new ArrayList<>();
		
		vertices.add(new Vector3(-1, phi, 0));
		vertices.add(new Vector3(1, phi, 0));
		vertices.add(new Vector3(-1, -phi, 0));
		vertices.add(new Vector3(1, -phi, 0));
		
		vertices.add(new Vector3(0, -1, phi));
		vertices.add(new Vector3(0, 1, phi));
		vertices.add(new Vector3(0, -1, -phi));
		vertices.add(new Vector3(0, 1, -phi));
		
		vertices.add(new Vector3(phi, 0, -1));
		vertices.add(new Vector3(phi, 0, 1));
		vertices.add(new Vector3(-phi, 0, -1));
		vertices.add(new Vector3(-phi, 0, 1));
		
		icosahedron_vertices = vertices;
	}
	
	private static void bulkAdd(List<Face> faces, Face... facesAdd) {
		faces.addAll(Arrays.asList(facesAdd));
	}
	
	static List<Face> icosahedron_faces() {
		List<Face> faces = new ArrayList<>();
		bulkAdd(faces,
				new Face(0, 11, 5), new Face(0, 5, 1), new Face(0, 1, 7), new Face(0, 7, 10), new Face(0, 10, 11),
				new Face(1, 5, 9), new Face(5, 11, 4), new Face(11, 10, 2), new Face(10, 7, 6), new Face(7, 1, 8),
				new Face(3, 9, 4), new Face(3, 4, 2), new Face(3, 2, 6), new Face(3, 6, 8), new Face(3, 8, 9),
				new Face(4, 9, 5), new Face(2, 4, 11), new Face(6, 2, 10), new Face(8, 6, 7), new Face(9, 8, 1)
		);
		return faces;
	}
	
	static Pair<List<Vector3>, List<Face>> subdivide(List<Vector3> vertices, List<Face> faces) {
		List<Vector3> new_vertices = new ArrayList<>(vertices);
		List<Face> new_faces = new ArrayList<>();
		
		for (Face face : faces) {
			Vector3 v1 = vertices.get(face.v0());
			Vector3 v2 = vertices.get(face.v1());
			Vector3 v3 = vertices.get(face.v2());
			v1.normalize();
			v2.normalize();
			v3.normalize();
			
			Vector3 v12 = new Vector3((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, (v1.z + v2.z) / 2);
			Vector3 v23 = new Vector3((v2.x + v3.x) / 2, (v2.y + v3.y) / 2, (v2.z + v3.z) / 2);
			Vector3 v31 = new Vector3((v3.x + v1.x) / 2, (v3.y + v1.y) / 2, (v3.z + v1.z) / 2);
			
			int i12 = new_vertices.size();
			new_vertices.add(v12);
			int i23 = new_vertices.size();
			new_vertices.add(v23);
			int i31 = new_vertices.size();
			new_vertices.add(v31);
			
			new_faces.add(new Face(face.v0(), i12, i31));
			new_faces.add(new Face(face.v1(), i23, i12));
			new_faces.add(new Face(face.v2(), i31, i23));
			new_faces.add(new Face(i12, i23, i31));
		}
		return Pair.of(new_vertices, new_faces);
	}
	
	public static Pair<List<Vector3>, List<Face>> icosphere(int iterations) {
		List<Vector3> vertices = icosahedron_vertices;
		List<Face> faces = icosahedron_faces();
		
		for (int i = 0; i < iterations; i++) {
			Pair<List<Vector3>, List<Face>> output = subdivide(vertices, faces);
			vertices = output.getFirst();
			faces = output.getSecond();
		}
		
//		for (int i = 0; i < vertices.size(); i++) {
//			vertices.set(i, vertices.get(i).normalize());
//		}
		
		return Pair.of(vertices, faces);
	}
}
