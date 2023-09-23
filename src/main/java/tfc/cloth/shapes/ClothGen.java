package tfc.cloth.shapes;

import net.minecraft.world.phys.Vec3;
import tfc.cloth.phys.Constraint;
import tfc.cloth.phys.Point;
import tfc.cloth.phys.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClothGen {
    public static ArrayList<Point> generate(List<Vector3> vertices, List<Face> faces) {
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

        ArrayList<Point> finalList = new ArrayList<>();
        for (PointBuilder value : points.values()) {
            Point pt = value.build();
            if (pt == null) continue;
            finalList.add(pt);
        }

        return finalList;
    }

    public static ArrayList<Point> genSquare(int width, int height, double spacing, boolean structured) {
        HashMap<Vec3, Vector3> points = new HashMap<>();

        ArrayList<Point> clothPoints = new ArrayList<>();

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

                    if (true) {
                        refs.add(points.get(new Vec3(x + 2, y, 0)));
                        refs.add(points.get(new Vec3(x - 2, y, 0)));
                        refs.add(points.get(new Vec3(x, y + 2, 0)));
                        refs.add(points.get(new Vec3(x, y - 2, 0)));
                    }

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

    private static Vec3 cycleSide(int itr, Vec3 vec) {
        switch (itr) {
            case 0:
                return vec;
            case 1:
                return new Vec3(vec.x, vec.z, vec.y);
            case 2:
                return new Vec3(vec.z, vec.y, vec.x);
        }
        return null;
    }

    public static ArrayList<Point> genCube(int width, int height, int depth, double spacing, boolean structured, Vector3 centerOfMass) {
        HashMap<Vec3, Vector3> points = new HashMap<>();

        ArrayList<Point> clothPoints = new ArrayList<>();

        Vector3 origin = new Vector3((width / 2d) * spacing, (height / 2d) * spacing, (depth / 2d) * spacing);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    points.put(new Vec3(x, y, z), new Vector3(x * spacing, y * spacing, z * spacing));
                }
            }
        }

        ArrayList<Vector3> center = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && x == y && y == z) {
                        Vector3 cent = new Vector3(origin.x + x * spacing, origin.y + y * spacing, origin.z + z * spacing);
                        center.add(cent);
                    }
                }
            }
        }
        ArrayList<Vector3> surface = new ArrayList<>();

        HashMap<Vec3, ArrayList<Vector3>> ptsRefs = new HashMap<>();

        for (int l = 0; l < 3; l++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    for (int i = 0; i < 2; i++) {
                        boolean top = i == 1;

                        boolean attach = false;

                        ArrayList<Vector3> refs = new ArrayList<>();
                        Vec3 vec = cycleSide(l, new Vec3(x, y, top ? depth - 1 : 0));
                        if (ptsRefs.containsKey(vec)) {
                            refs = ptsRefs.get(vec);
                            attach = true;
                        } else {
                            ptsRefs.put(vec, refs);
                        }

                        if (attach) {
                            surface.add(points.get(vec));
                            for (Vector3 vector3 : center) {
                                refs.add(vector3);
                            }
                        }

                        if (x > 0) refs.add(points.get(cycleSide(l, new Vec3(x - 1, y, top ? depth - 1 : 0))));
                        if (x < width - 1) refs.add(points.get(cycleSide(l, new Vec3(x + 1, y, top ? depth - 1 : 0))));
                        if (y > 0) refs.add(points.get(cycleSide(l, new Vec3(x, y - 1, top ? depth - 1 : 0))));
                        if (y < height - 1) refs.add(points.get(cycleSide(l, new Vec3(x, y + 1, top ? depth - 1 : 0))));

                        if (structured) {
                            refs.add(points.get(cycleSide(l, new Vec3(x + 1, y + 1, top ? depth - 1 : 0))));
                            refs.add(points.get(cycleSide(l, new Vec3(x - 1, y - 1, top ? depth - 1 : 0))));
                            refs.add(points.get(cycleSide(l, new Vec3(x - 1, y + 1, top ? depth - 1 : 0))));
                            refs.add(points.get(cycleSide(l, new Vec3(x + 1, y - 1, top ? depth - 1 : 0))));

//                            refs.add(points.get(cycleSide(l, new Vec3(x + 2, y, top ? depth - 1 : 0))));
//                            refs.add(points.get(cycleSide(l, new Vec3(x - 2, y, top ? depth - 1 : 0))));
//                            refs.add(points.get(cycleSide(l, new Vec3(x, y + 2, top ? depth - 1 : 0))));
//                            refs.add(points.get(cycleSide(l, new Vec3(x, y - 2, top ? depth - 1 : 0))));

                            int prevNull = 0;
                            while (refs.contains(null)) {
                                int i1;
                                for (i1 = prevNull; i1 < refs.size(); i1++)
                                    if (refs.get(i1) == null)
                                        break;
                                refs.remove(i1);
                                prevNull = i1;
                            }
                        }
                    }
                }
            }
        }

        for (Vec3 vec3 : ptsRefs.keySet()) {
            Vector3 pos = points.get(vec3);

            clothPoints.add(new Point(
                    pos, ptsRefs.get(vec3).toArray(new Vector3[0]),
                    origin
            ));

//            surface.add(pos);
        }

        for (Vector3 vector3 : center) {
            Vector3 pos = vector3;

            Point pt;
            clothPoints.add(pt = new Point(
                    pos, surface.toArray(new Vector3[0]),
                    origin
            ));

            pt.constraint = point -> {
//                pt.pos.set(centerOfMass);
//                pt.lastPos.set(centerOfMass);

//                pt.setRefObjects(new Point[0]);
//                pt.setRefs(new Vector3[0]);
            };
        }

//		for (int z = 0; z < depth; z++) {
//			for (int x = 0; x < width; x++) {
//				for (int y = 0; y < height; y++) {
//					ArrayList<Vector3> refs = new ArrayList<>();
//
//					if (x > 0) refs.add(points.get(new Vec3(x - 1, y, z)));
//					if (x < width - 1) refs.add(points.get(new Vec3(x + 1, y, z)));
//					if (y > 0) refs.add(points.get(new Vec3(x, y - 1, z)));
//					if (y < height - 1) refs.add(points.get(new Vec3(x, y + 1, z)));
//					if (z > 0) refs.add(points.get(new Vec3(x, y, z - 1)));
//					if (z < depth - 1) refs.add(points.get(new Vec3(x, y, z + 1)));
//
//					if (structured) {
//						refs.add(points.get(new Vec3(x + 1, y + 1, z)));
//						refs.add(points.get(new Vec3(x - 1, y - 1, z)));
//						refs.add(points.get(new Vec3(x - 1, y + 1, z)));
//						refs.add(points.get(new Vec3(x + 1, y - 1, z)));
//
//						refs.add(points.get(new Vec3(x - 1, y, z + 1)));
//						refs.add(points.get(new Vec3(x + 1, y, z - 1)));
//						refs.add(points.get(new Vec3(x - 1, y + 1, z + 1)));
//						refs.add(points.get(new Vec3(x + 1, y - 1, z - 1)));
//						refs.add(points.get(new Vec3(x, y + 1, z + 1)));
//						refs.add(points.get(new Vec3(x, y - 1, z - 1)));
//
//						if (true) {
//							refs.add(points.get(new Vec3(x + 2, y, z)));
//							refs.add(points.get(new Vec3(x - 2, y, z)));
//							refs.add(points.get(new Vec3(x, y + 2, z)));
//							refs.add(points.get(new Vec3(x, y - 2, z)));
//							refs.add(points.get(new Vec3(x, y, z + 2)));
//							refs.add(points.get(new Vec3(x, y, z - 2)));
//						}
//
//						int prevNull = 0;
//						while (refs.contains(null)) {
//							int i;
//							for (i = prevNull; i < refs.size(); i++)
//								if (refs.get(i) == null)
//									break;
//							refs.remove(i);
//							prevNull = i;
//						}
//					}
//
//					Vector3 pos = points.get(new Vec3(x, y, z));
//
//					clothPoints.add(new Point(
//							pos, refs.toArray(new Vector3[0]),
//							origin
//					));
//				}
//			}
//		}

        return clothPoints;
    }
}
