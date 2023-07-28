package tfc.cloth.phys;

public interface Tracer {
	double traceDist(Vector3 start, Vector3 end);
	void recenter(Cloth cloth);
}
