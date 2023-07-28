package tfc.cloth.phys;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Cloth {
	AbstractPoint[] orderedPoints;
	
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
		tracer.recenter(this);
		for (int i = 0; i < orderedPoints.length; i++) {
			orderedPoints[i].constraint.apply(orderedPoints[i]);
			orderedPoints[i].tick(tracer, worker, gravityResolver.apply(orderedPoints[i].getPos()));
		}
	}
	
	public AbstractPoint[] getOrderedPoints() {
		return orderedPoints;
	}
}
