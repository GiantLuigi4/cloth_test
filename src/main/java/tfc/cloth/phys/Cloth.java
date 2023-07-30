package tfc.cloth.phys;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class Cloth {
	AABB bounds = new AABB(0, 0, 0, 1, 1, 1);
	
	Point[] orderedPoints;
	
	Vector3 centerOfClamping = null;
	
	protected double collisionStrength = 0;
	protected boolean strongCollisions = false;
	
	protected boolean extraStrongCollisions = false;
	
	public Cloth setCollisionStrength(double strength) {
		this.collisionStrength = strength;
		return this;
	}
	
	public boolean isExtraStrongCollisions() {
		return extraStrongCollisions;
	}
	
	public Cloth setExtraStrongCollisions(boolean extraStrongCollisions) {
		this.extraStrongCollisions = extraStrongCollisions;
		return this;
	}
	
	public Cloth setStrongCollisions(boolean strongCollisions) {
		this.strongCollisions = strongCollisions;
		return this;
	}
	
	public boolean isStrongCollisions() {
		return strongCollisions;
	}
	
	public double getCollisionStrength() {
		return collisionStrength;
	}
	
	protected void init() {
		HashMap<Vector3, Point> pointMap = null;
		
		for (Point orderedPoint : orderedPoints) {
			if (orderedPoint.isAware()) {
				if (pointMap == null) {
					pointMap = new HashMap<>();
					
					for (Point point : orderedPoints) {
						pointMap.put(point.pos, point);
					}
				}
				
				Point[] refObjs = new Point[orderedPoint.refs.length];
				
				for (int i = 0; i < orderedPoint.refs.length; i++) {
					refObjs[i] = pointMap.get(orderedPoint.refs[i]);
				}
				
				orderedPoint.refObjects = refObjs;
			}
		}
	}
	
	public Cloth(Point... orderedPoints) {
		this.orderedPoints = orderedPoints;
		init();
	}
	
	public Cloth(Collection<Point> points) {
		orderedPoints = new Point[points.size()];
		if (points instanceof List<Point> li) {
			for (int i = 0; i < points.size(); i++) {
				orderedPoints[i] = li.get(i);
			}
		} else {
			int i = 0;
			for (Point point : points) {
				orderedPoints[i] = point;
				i++;
			}
		}
		init();
	}
	
	public void tick(Tracer tracer, Vector3 worker, Function<Vector3, Vector3> gravityResolver) {
//		for (int i = 0; i < 2; i++) {
		tracer.recenter(this);
		centerOfClamping = null;
		for (Point point : orderedPoints) {
			point.setCloth(this);
			
			point.tick(tracer, worker, gravityResolver.apply(point.getPos()));
		}
		
		for (Point point : orderedPoints) {
			point.constraint.apply(point);
		}
		
		for (Point orderedPoint : orderedPoints) {
			orderedPoint.normalize();
		}
//		}
		
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;
		for (Point orderedPoint : orderedPoints) {
			minX = Math.min(orderedPoint.getPos().x, minX);
			minY = Math.min(orderedPoint.getPos().y, minY);
			minZ = Math.min(orderedPoint.getPos().z, minZ);
			maxX = Math.max(orderedPoint.getPos().x, maxX);
			maxY = Math.max(orderedPoint.getPos().y, maxY);
			maxZ = Math.max(orderedPoint.getPos().z, maxZ);
		}
		bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public Point[] getOrderedPoints() {
		return orderedPoints;
	}
	
	public void setOrderedPoints(Point[] points) {
		orderedPoints = points;
	}
	
	public Pair<Vector3, Double> calculateOffset(Entity entity) {
		double colliderSize = Math.max(
				2, Math.max(
						entity.getBbWidth(), entity.getBbHeight()
				)
		);
		
		Vector3 delta = new Vector3(0, 0, 0);
		Vector3 worker = new Vector3(0, 0, 0);
		Vector3 impulse = new Vector3(0, 0, 0);
		
		Vector3 pos = new Vector3(
				entity.position().x,
				entity.position().y + colliderSize / 2,
				entity.position().z
		);
		
		double count = 0;
		
		for (Point orderedPoint : orderedPoints) {
			if (pos.distance(
					orderedPoint.getPos().x,
					orderedPoint.getPos().y,
					orderedPoint.getPos().z
			) < colliderSize / 2) {
				worker.set(pos);
				
				worker.setDistance(orderedPoint.getPos(), colliderSize / 2);
				
				double length = entity.getDeltaMovement().length();
				double ln = length;
				if (ln < 0.4) ln = 0;
				else
					ln *= 3;
				
				impulse.set(
						worker.x - pos.x,
						worker.y - pos.y,
						worker.z - pos.z
				).scl(ln + 1);
				
				orderedPoint.push(impulse.scl(-0.6));
				
				delta.add(worker.sub(pos));
				
				ln = length;
				if (ln < 1) ln = 1;
				ln = Math.pow(ln + ln, 3);
				ln /= 8;
				
				count += (strongCollisions ? 1 : 2) / ln;
			}
		}
		
		return Pair.of(
				delta, count
		);
	}
	
	public void collide(Entity entity) {
		if (!bounds.intersects(entity.getBoundingBox()))
			return;
		
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
			
			if (extraStrongCollisions) {
				if (
						Math.signum(entity.getDeltaMovement().x) != Math.signum(delta.x)
				) entity.setDeltaMovement(
						entity.getDeltaMovement().x / 2, entity.getDeltaMovement().y, entity.getDeltaMovement().z
				);
				if (
						Math.signum(entity.getDeltaMovement().y) != Math.signum(delta.y)
				) entity.setDeltaMovement(
						entity.getDeltaMovement().x, entity.getDeltaMovement().y / 2, entity.getDeltaMovement().z
				);
				if (
						Math.signum(entity.getDeltaMovement().z) != Math.signum(delta.z)
				) entity.setDeltaMovement(
						entity.getDeltaMovement().x, entity.getDeltaMovement().y, entity.getDeltaMovement().z / 2
				);
			}
			
			delta.scl(collisionStrength);
			entity.push(delta.x, delta.y, delta.z);
			
			if (delta.y > 0) {
				entity.setOnGround(true);
				entity.fallDistance = 0;
			}
		}
	}
	
	public AABB getBounds() {
		return bounds;
	}
}
