package tfc.cloth.phys;

public abstract class AbstractPoint {
	public Constraint constraint;
	
	public abstract void tick(Tracer tracer, Vector3 worker, Vector3 gravity);
	public abstract Vector3 getPos();
	public abstract Vector3 getVeloc();
}
