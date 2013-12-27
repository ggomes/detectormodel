import org.w3c.dom.Node;

public class FDTriangular extends AbstractFD {

	//protected float rhobar;				// [veh/mile] jam density
	protected float w;					// [mph] congested wave speed
	protected float vf;					// [mph] freeflow speed
	
	public FDTriangular() {
	}

	public float F(float dens) {
		if(dens<0 || dens>rhojam)
			return Float.NaN;
		else
			return Math.min( vf*dens , w*(rhojam-dens) );
	}
	
	public float Fprime(float dens){
		if(dens<0 || dens>rhojam)
			return Float.NaN;
		if(dens<rhocrit)
			return vf;
		else 
			return -w;
	}

	public float Phi(float zeta, float rhoup, float rhodn) {
		return rhodn;
	}
	
	public float integralPhi(float rhoup, float rhodn) {
		return rhodn;
	}

	public float dPhidZeta(float zeta, float rhoup, float rhodn) {
		return 0f;
	}
	
	public float FinvMinus(float f){
		if(f<0 | f>fbar)
			return Float.NaN;
		return f/vf;
	}

	public float FinvPlus(float f){
		if(f<0 | f>fbar)
			return Float.NaN;
		return rhocrit+(fbar-f)/w;	
	}
	
	public float maxspeed(){
		return vf;
	}
	
	public float evalAvgDensity(float rup,float rdn){
		return rdn;
	}
	
	public boolean initFromDOM(Node p) throws Exception {
		if(!super.initFromDOM(p))
			return false;
		try  
		{
			rhojam	= Float.parseFloat(p.getAttributes().getNamedItem("rhobar").getNodeValue());
		}
		catch(Exception e) {
			throw new Exception(e.getMessage());
		}		
		w = fbar/(rhojam-rhocrit);
		vf = fbar/rhocrit;
		return true;
	}
	
	public boolean validate() throws Exception  {
		if(!super.validate())
			return false;
		if(rhojam<=rhocrit){
			System.out.println("rhocrit must be less than rhobar");
			return false;
		}
		return true;	
	}

	
}
