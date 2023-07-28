package tfc.cloth;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tfc.cloth.phys.Cloth;
import tfc.cloth.phys.Tracer;
import tfc.cloth.phys.Vector3;

public class MCTracer implements Tracer {
	Level level;
	
	@Override
	public double traceDist(Vector3 start, Vector3 end) {
		Vector3 end1 = new Vector3(0, 0, 0);
		end1.set(end);
		end1.sub(start);
		end1.setDistance(new Vector3(0, 0, 0), 0.1);
		end1.scl(-1);
		BlockHitResult result = level.clip(new ClipContext(
				new Vec3(start.x + end1.x, start.y + end1.y, start.z + end1.z),
				new Vec3(end.x, end.y, end.z),
				ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE,
				null
		));
		if (result.getType() != HitResult.Type.MISS) {
			end.set(
					result.getLocation().x,
					result.getLocation().y,
					result.getLocation().z
			);
		}
		return start.distance(end);
	}
	
	@Override
	public void recenter(Cloth cloth) {
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}
}
