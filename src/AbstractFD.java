import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class AbstractFD implements xmlconfigurable  {

	protected float fbar;			// [veh/hr] capacity
	protected float rhocrit;		// [veh/mile] critical density
	protected float rhojam;			// [veh/mile]
	
	protected float rhodisminus;	// [veh/mile] uncongested discharge density
	protected float rhodisplus;		// [veh/mile] congested discharge density
	protected float fdis;			// [veh/hr] discharge flow
	
	
	public AbstractFD() {
	}
	
	public float F(float dens){
		return Float.NaN;
	}
	
	public float Phi(float zeta,float rhoup,float rhodn){
		return Float.NaN;
	}

	public float integralPhi(float rhoup,float rhodn){
		return Float.NaN;
	}

	public float dPhidZeta(float zeta,float rhoup,float rhodn){
		return Float.NaN;
	}

	public float Fprime(float dens){
		return Float.NaN;
	}
	
	public float FinvMinus(float f){
		return Float.NaN;
	}

	public float FinvPlus(float f){
		return Float.NaN;	
	}
	
	public final float S(float dens){
		if(dens>rhocrit)
			return fdis;
		else
			return F(dens);
	}

	public final float R(float dens){
		if(dens>rhocrit)
			return F(dens);
		else
			return fbar;
	}
	
	public float maxspeed(){
		return Float.NaN;
	}
	
	public float chordslope(float rhoA,float rhoB){
		if( Math.abs(rhoA-rhoB)>gbl.EPS ) 	
			return (F(rhoA)-F(rhoB))/(rhoA-rhoB);	
		else{
			return Fprime(rhoA);
		}
	}

	public final void setRhoDis(){
		rhodisplus = FinvPlus(fdis);
		rhodisminus = FinvMinus(fdis);
		rhojam= FinvPlus(0);
	}

	public float evalAvgDensity(float rup,float rdn){
		return Float.NaN;
	}
	
	public boolean initFromDOM(Node p) throws Exception {
		if (p == null)
			return false;
		try  
		{
			NamedNodeMap p2 = p.getAttributes();
			fbar	= Float.parseFloat(p2.getNamedItem("fbar").getNodeValue());
			rhocrit	= Float.parseFloat(p2.getNamedItem("rhocrit").getNodeValue());
			fdis	= Float.parseFloat(p2.getNamedItem("fdis").getNodeValue());
		}
		catch(Exception e) {
			throw new Exception(e.getMessage());
		}
		return true;
	}
	
	public boolean validate() throws Exception  {

		if( fbar<=0 || rhocrit<=0 || fdis<=0 ){
			System.out.println("FD parameters must be positive");
			return false;
		}

		if(fbar<fdis){
			System.out.println("fdis must be less or equal to fbar");
			return false;
		}
		
		return true;	
	}
}
