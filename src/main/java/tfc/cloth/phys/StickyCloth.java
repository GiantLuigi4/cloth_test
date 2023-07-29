package tfc.cloth.phys;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class StickyCloth extends Cloth {
	public StickyCloth(AbstractPoint... orderedPoints) {
		super(orderedPoints);
	}
	
	public StickyCloth(Collection<AbstractPoint> points) {
		super(points);
	}
	
	public void collide(Entity entity) {
		if (entity instanceof Spider) {
			super.collide(entity);
			return;
		}
		
		if (!entity.level.isClientSide) {
			if (entity instanceof Player)
				return;
		} else if (entity instanceof Player player)
			if (player.isSpectator())
				return;
		
		Pair<Vector3, Double> offset = calculateOffset(entity);
		Vector3 delta = offset.getFirst();
		double count = offset.getSecond();
		
		if (count != 0) {
			delta.scl((1d / count));
			
			entity.setDeltaMovement(
					entity.getDeltaMovement().x / 2,
					entity.getDeltaMovement().y / 2,
					entity.getDeltaMovement().z / 2
			);
			
			delta.scl(this.getCollisionStrength());
			entity.push(delta.x, delta.y, delta.z);
			
			if (delta.y > 0) {
				entity.setOnGround(true);
				entity.fallDistance = 0;
			}
		}
	}
}
