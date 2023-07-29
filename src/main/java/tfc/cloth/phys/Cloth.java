package tfc.cloth.phys;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Cloth {
	AbstractPoint[] orderedPoints;
	
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
	
	public Cloth(AbstractPoint... orderedPoints) {
		this.orderedPoints = orderedPoints;
	}
	
	public Cloth(Collection<AbstractPoint> points) {
		orderedPoints = new AbstractPoint[points.size()];
		if (points instanceof List<AbstractPoint> li) {
			for (int i = 0; i < points.size(); i++) {
				orderedPoints[i] = li.get(i);
			}
		} else {
			int i = 0;
			for (AbstractPoint point : points) {
				orderedPoints[i] = point;
				i++;
			}
		}
	}
	
	public void tick(Tracer tracer, Vector3 worker, Function<Vector3, Vector3> gravityResolver) {
//		for (int i = 0; i < 2; i++) {
			tracer.recenter(this);
			centerOfClamping = null;
			for (AbstractPoint point : orderedPoints) {
				if (point instanceof Point pt)
					pt.setCloth(this);
				
				point.tick(tracer, worker, gravityResolver.apply(point.getPos()));
			}
			
			for (AbstractPoint point : orderedPoints) {
				point.constraint.apply(point);
			}
			
			for (AbstractPoint orderedPoint : orderedPoints) {
				orderedPoint.normalize();
			}
//		}
	}
	
	public AbstractPoint[] getOrderedPoints() {
		return orderedPoints;
	}
}
