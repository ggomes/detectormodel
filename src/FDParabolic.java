import org.w3c.dom.Node;


public class FDParabolic extends AbstractFD {

	public FDParabolic() {
	}

	public float F(float dens) {
		if(dens<0 || dens>2*rhocrit)
			return Float.NaN;
		else
			return fbar*dens*(2*rhocrit-dens)/rhocrit/rhocrit;
	}
	
	public float Fprime(float dens){
		if(dens<0 || dens>2*rhocrit)
			return Float.NaN;
		return 2*fbar*(rhocrit-dens)/rhocrit/rhocrit;
	}
	
	public float Phi(float zeta, float rhoup, float rhodn) {
		return (1-zeta)*rhoup+zeta*rhodn;
	}

	public float dPhidZeta(float zeta, float rhoup, float rhodn) {
		return rhodn-rhoup;
	}

	public float integralPhi(float rhoup, float rhodn) {
		return 0.5f*(rhodn+rhoup);
	}
	
	public float FinvMinus(float f){
		if(f<0 | f>fbar)
			return Float.NaN;
		return rhocrit*(1.0f-(float)Math.sqrt(1.0f-f/fbar));
	}

	public float FinvPlus(float f){
		if(f<0 | f>fbar)
			return Float.NaN;
		return rhocrit*(1.0f+(float)Math.sqrt(1.0f-f/fbar));
	}

	public float maxspeed(){
		return 2*fbar/rhocrit;
	}
	
	public float evalAvgDensity(float rup,float rdn){
		return (rup+rdn)/2.0f;
	}
	
	public boolean initFromDOM(Node p) throws Exception {
		if(!super.initFromDOM(p))
			return false;
		return true;
	}
	
	public boolean validate() throws Exception  {
		if(!super.validate())
			return false;
		return true;
	}

	
}
