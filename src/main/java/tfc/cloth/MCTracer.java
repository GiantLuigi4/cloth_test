package tfc.cloth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.cloth.phys.Cloth;
import tfc.cloth.phys.Tracer;
import tfc.cloth.phys.Vector3;

public class MCTracer implements Tracer {
	Level level;
	
	@Override
	public double traceDist(Vector3 start, Vector3 end, Direction[] dir) {
//		BlockState blockState = level.getBlockState(
//				new BlockPos(start.x, start.y, start.z)
//		);
//		if (!blockState.isAir()) {
//			VoxelShape shape = blockState.getShape(
//					level,
//					new BlockPos(start.x, start.y, start.z)
//			);
//			if (shape == Shapes.empty() || shape.isEmpty()) {
//				for (AABB aabb : shape.toAabbs()) {
//					if (aabb.contains(
//							new Vec3(start.x, start.y, start.z)
//					)) return 0;
//				}
//			}
//		}
		
		BlockHitResult result = level.clip(new ClipContext(
				new Vec3(start.x , start.y, start.z),
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
			dir[0] = result.getDirection();
			// TODO: figure out where the "surface" of the block is
		}
		
		return start.distance(end) ;
//		return 0;
	}
	
	@Override
	public void recenter(Cloth cloth) {
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}
}
