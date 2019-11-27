public class Vec3float {
	private float x, y, z;
	private final double EPS = 1.0E-8;

	public Vec3float() {
		x = 0; y = 0; z = 0;		
	}

	public Vec3float(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;		
	}

	public Vec3float(float[] v) {
		if(v.length==3){
			this.x = v[0];
			this.y = v[1];
			this.z = v[2];
		} else {
			System.out.println("Vec3float initialization fail!!!");
		}
	}
	
	public Vec3float minus(Vec3float v){
		Vec3float ret = new Vec3float();
		ret.x = x - v.x;
		ret.y = y - v.y;
		ret.z = z - v.z;
		return ret;
	}

	public Vec3float minus(float f){
		Vec3float ret = new Vec3float();
		ret.x = x - f;
		ret.y = y - f;
		ret.z = z - f;
		return ret;
	}
	
	public Vec3float add(Vec3float v){
		Vec3float ret = new Vec3float();
		ret.x = x + v.x;
		ret.y = y + v.y;
		ret.z = z + v.z;
		return ret;
	}

	public Vec3float add(float f){
		Vec3float ret = new Vec3float();
		ret.x = x + f;
		ret.y = y + f;
		ret.z = z + f;
		return ret;
	}
	
	public float dot(Vec3float v){
		return x*v.x + y*v.y + z*v.z; 	
	}
	
	public Vec3float cross(Vec3float v){
		Vec3float ret = new Vec3float();
		ret.x = y * v.z - z*v.y;
		ret.y = z * v.x - x*v.z;
		ret.z = x * v.y - y*v.x;
		return ret;
	}
	
	
	public float len(){
		return (float) Math.sqrt(x*x + y*y + z*z); 	
	}

	public boolean normalize(){
		double s =1/ len();
		if(s>EPS){
			x = (float) s*x;
			y = (float) s*y;
			z = (float) s*z;
			return true;
		} else {
			x = 0; y = 0; z = 0;
			return false;
		}
	}
	
	public void setVector(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setVector(float[] fv){
		this.x = fv[0];
		this.y = fv[1];
		this.z = fv[2];
	}

	public void setVector(Vec3float v){
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	public float[] getVector(){
		float[] ret = {x,y,z};
		return ret;
	}

	public float getX(){
		return x;
	}

	public float getY(){
		return y;
	}
	
	public float getZ(){
		return z;
	}
}
