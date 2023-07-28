package tfc.cloth.phys;

import net.minecraft.core.Direction;

public interface Tracer {
	double traceDist(Vector3 start, Vector3 end, Direction[] sideHit);
	void recenter(Cloth cloth);
}
