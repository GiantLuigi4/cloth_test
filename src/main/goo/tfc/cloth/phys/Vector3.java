package tfc.cloth.phys;

import java.util.Objects;

public class Vector3 {
	public double x, y, z;
	
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3(Vector3 vector3) {
		this(vector3.x, vector3.y, vector3.z);
	}
	
	public Vector3 set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vector3 set(Vector3 other) {
		return set(other.x, other.y, other.z);
	}
	
	public Vector3 add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vector3 add(Vector3 other) {
		return add(other.x, other.y, other.z);
	}
	
	public Vector3 sub(double x, double y, double z) {
		this.x += -x;
		this.y += -y;
		this.z += -z;
		return this;
	}
	
	public Vector3 sub(Vector3 other) {
		return sub(other.x, other.y, other.z);
	}
	
	public Vector3 mul(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}
	
	public Vector3 div(double x, double y, double z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;
		return this;
	}
	
	// https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
	public Vector3 calcNormal(Vector3 o1, Vector3 o2, Vector3 dst) {
		double ux = o1.x - x;
		double uy = o1.y - y;
		double uz = o1.z - z;
		
		double vx = o2.x - x;
		double vy = o2.y - y;
		double vz = o2.z - z;
		
		return dst.set(
				(uy * vz) - (uz * vy),
				(uz * vx) - (ux * vz),
				(ux * vy) - (uy * vx)
		);
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}
	
	public double distance(Vector3 end) {
		double dx = x - end.x;
		double dy = y - end.y;
		double dz = z - end.z;
		
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public Vector3 setDistance(Vector3 origin, double length) {
		x -= origin.x;
		y -= origin.y;
		z -= origin.z;
		
		double scl = length() / length;
		if (scl == 0) scl = 1;
		x /= scl;
		y /= scl;
		z /= scl;
		
		x += origin.x;
		y += origin.y;
		z += origin.z;
		
		return this;
	}
	
	public Vector3 setLength(double length) {
		double scl = length() / length;
		if (scl == 0) scl = 1;
		x /= scl;
		y /= scl;
		z /= scl;
		
		return this;
	}
	
	public Vector3 normalize() {
		double scl = length();
		if (scl == 0) scl = 1;
		x /= scl;
		y /= scl;
		z /= scl;
		
		return this;
	}
	
	public Vector3 scl(double v) {
		x *= v;
		y *= v;
		z *= v;
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vector3 vector3 = (Vector3) o;
		return Double.compare(vector3.x, x) == 0 && Double.compare(vector3.y, y) == 0 && Double.compare(vector3.z, z) == 0;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
	
	public Vector3 copy() {
		return new Vector3(this);
	}
}
