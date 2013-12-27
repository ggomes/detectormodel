
public class Pair {
	public float rho;		// [veh/mile] density
	public float f;			// [veh/hr]	flow

	public Pair(){
		rho = Float.NaN;
		f = Float.NaN;
	}
	
	public Pair(float a,float b){
		rho = a;
		f = b;
	}
	
	public boolean islessthan(Pair x){
		if(f<x.f-gbl.EPS)
			return true;
		if(Math.abs(f-x.f)<gbl.EPS && x.rho<rho-gbl.EPS)
			return true;
		return false;
	}
	
	public boolean isequalto(Pair x){
		if(Math.abs(f-x.f)<gbl.EPS*65 && Math.abs(rho-x.rho)<gbl.EPS)
			return true;
		else
			return false;
	}
	
	public void set(float rhonew,float fnew){
		rho = rhonew;
		f = fnew;
	}
	
	public void set(Pair x){
		rho = x.rho;
		f = x.f;
	}
	
	public float chordslope(Pair x){
		if(Math.abs(this.rho-x.rho)<gbl.EPS){
			return Float.NaN;
		}
		else{
			return (this.f-x.f)/(this.rho-x.rho);
		}
	}
	
	public String toString() {
		return new String(rho + "\t" + f);
	}
	
}
