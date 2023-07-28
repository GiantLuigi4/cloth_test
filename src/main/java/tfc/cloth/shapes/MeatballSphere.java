package tfc.cloth.shapes;

import com.mojang.datafixers.util.Pair;
import tfc.cloth.phys.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// I messed up on vertex normalization and it produced an interesting shape
// so here we are
public class MeatballSphere {
	public static class Vertex {
		public final double x, y, z;
		
		public Vertex(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		Vertex normalize() {
			double sum = x * x + y * y + z * z;
			return new Vertex(x / sum, y / sum, z / sum);
		}
	}
	
	static List<Vertex> icosahedron_vertices;
	
	static {
		double phi = (1.0f + Math.sqrt(5.0f)) / 2.0f;
		
		List<Vertex> vertices = new ArrayList<>();
		
		vertices.add(new Vertex(-1, phi, 0));
		vertices.add(new Vertex(1, phi, 0));
		vertices.add(new Vertex(-1, -phi, 0));
		vertices.add(new Vertex(1, -phi, 0));
		
		vertices.add(new Vertex(0, -1, phi));
		vertices.add(new Vertex(0, 1, phi));
		vertices.add(new Vertex(0, -1, -phi));
		vertices.add(new Vertex(0, 1, -phi));
		
		vertices.add(new Vertex(phi, 0, -1));
		vertices.add(new Vertex(phi, 0, 1));
		vertices.add(new Vertex(-phi, 0, -1));
		vertices.add(new Vertex(-phi, 0, 1));
		
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
	
	static Pair<List<Vertex>, List<Face>> subdivide(List<Vertex> vertices, List<Face> faces) {
		List<Vertex> new_vertices = new ArrayList<>(vertices);
		List<Face> new_faces = new ArrayList<>();
		
		for (Face face : faces) {
			Vertex v1 = vertices.get(face.v0());
			Vertex v2 = vertices.get(face.v1());
			Vertex v3 = vertices.get(face.v2());
			
			Vertex v12 = new Vertex((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, (v1.z + v2.z) / 2);
			Vertex v23 = new Vertex((v2.x + v3.x) / 2, (v2.y + v3.y) / 2, (v2.z + v3.z) / 2);
			Vertex v31 = new Vertex((v3.x + v1.x) / 2, (v3.y + v1.y) / 2, (v3.z + v1.z) / 2);
			
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
		List<Vertex> vertices = icosahedron_vertices;
		List<Face> faces = icosahedron_faces();
		
		for (int i = 0; i < iterations; i++) {
			Pair<List<Vertex>, List<Face>> output = subdivide(vertices, faces);
			vertices = output.getFirst();
			faces = output.getSecond();
		}
		
		List<Vector3> out = new ArrayList<>();
		for (int i = 0; i < vertices.size(); i++) {
			Vertex vert = vertices.get(i).normalize();
			out.add(new Vector3(vert.x, vert.y, vert.z));
		}
		
		return Pair.of(out, faces);
	}
}
