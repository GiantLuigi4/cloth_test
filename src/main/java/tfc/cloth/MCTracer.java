package tfc.cloth;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.cloth.phys.Cloth;
import tfc.cloth.phys.Tracer;
import tfc.cloth.phys.Vector3;

public class MCTracer implements Tracer {
	Level level;
	
	Long2ObjectOpenHashMap<LevelChunk> chunks = new Long2ObjectOpenHashMap<>();
	
	protected LevelChunk getChunk(ChunkPos chunkPos) {
		LevelChunk chunk = chunks.get(chunkPos.toLong());
		if (chunk == null)
			chunks.put(
					chunkPos.toLong(),
					chunk = (LevelChunk) level.getChunk(
							chunkPos.x, chunkPos.z,
							ChunkStatus.FULL, false
					)
			);
		return chunk;
	}
	
	protected BlockHitResult trace(Vector3 start, Vector3 end) {
		double dist = start.distance(end);
		
		if (dist == 0) {
			BlockPos bp = new BlockPos(start.x, start.y, start.z);
			ChunkPos chunkPos = new ChunkPos(bp);
			LevelChunk chunk = getChunk(chunkPos);
			BlockState state = chunk.getBlockState(bp);
			
			if (!state.isAir()) {
				VoxelShape sp = state.getShape(level, bp);
				for (AABB aabb : sp.toAabbs()) {
					if (aabb.contains(
							start.x, start.y, start.z
					)) return new BlockHitResult(
							new Vec3(start.x, start.y, start.z),
							Direction.UP,
							bp, true
					);
				}
			}
			return BlockHitResult.miss(
					new Vec3(start.x, start.y, start.z),
					Direction.UP,
					bp
			);
		}
		
		BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos last = new BlockPos.MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		Vector3 worker = new Vector3(start);
		
		Vec3 dir = new Vec3(
				end.x - start.x,
				end.y - start.y,
				end.z - start.z
		).normalize();
		
		LevelChunk currentChunk = null;
		
		for (double i = 0; i < dist; i += Math.min(dist, 0.1)) {
			worker.set(start).scl(0.5).add(
					end.x / 2,
					end.y / 2,
					end.z / 2
			);
			
			current.set(worker.x, worker.y, worker.z);
			if (current.equals(last)) continue;
			last.set(current);
			
			// get chunk
			ChunkPos ckPos = new ChunkPos(current);
			if (currentChunk == null || !ckPos.equals(currentChunk.getPos()))
				currentChunk = getChunk(ckPos);
			if (currentChunk == null || currentChunk.isEmpty()) continue;
			// get section
			int sectionNumber = SectionPos.blockToSectionCoord(current.getY());
			if (sectionNumber < currentChunk.getMinSection() || sectionNumber > currentChunk.getMaxSection())
				continue;
			LevelChunkSection section = currentChunk.getSection(currentChunk.getSectionIndexFromSectionY(sectionNumber));
			// skip if it has no blocks
			if (section.hasOnlyAir()) continue;
			
			BlockState state = section.getBlockState(
					current.getX() & 15,
					current.getY() & 15,
					current.getZ() & 15
			);
			if (state.isAir()) continue;
			
			VoxelShape shape = state.getShape(level, current);
			
			BlockHitResult res = null;
			
			if (!shape.isEmpty()) {
				BlockHitResult result = shape.clip(
						new Vec3(
								worker.x,
								worker.y,
								worker.z
						),
						new Vec3(
//								worker.x + dir.x,
//								worker.y + dir.y,
//								worker.z + dir.z
								end.x,
								end.y,
								end.z
						),
						current.immutable()
				);
				
				if (result == null || result.getType() == HitResult.Type.MISS)
					continue;
				
				res = result;
			}
			
			if (res != null && res.getType() != HitResult.Type.MISS)
				return res;
		}
		
		return BlockHitResult.miss(
				new Vec3(worker.x, worker.y, worker.z),
				Direction.UP,
				current
		);
	}
	
	@Override
	public double traceDist(Vector3 start, Vector3 end, Direction[] dir) {
		BlockHitResult result = trace(start, end);
		if (result.getType() != HitResult.Type.MISS) {
			end.set(
					result.getLocation().x,
					result.getLocation().y,
					result.getLocation().z
			);
			dir[0] = result.getDirection();
			// TODO: figure out where the "surface" of the block is
		}
		
		return start.distance(end);
//		return 0;
	}
	
	@Override
	public void recenter(Cloth cloth) {
		chunks.clear();
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}
}
