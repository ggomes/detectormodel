
public class QRegion {
	
	private RegionType type;		// region type
	private float L;				// [mile] length
	private float rhoup; 		// [veh/mile] upstream density
	private float rhodn; 		// [veh/mile] downstream density
	private float vup;			// [mile/hr] upstream boundary speed
	private float vdn;			// [mile/hr] downstream boundary speed
	private float rho;			// [veh/mile] average density.
	private AbstractFD myF;
	
	public RegionType getType() {
		return type;
	}

	public float getL() {
		return L;
	}

	public float getRhoup() {
		return rhoup;
	}

	public float getRhodn() {
		return rhodn;
	}

	public float getVup() {
		return vup;
	}

	public float getVdn() {
		return vdn;
	}

	public void setVup(float vup) {
		this.vup = vup;
	}

	public void setVdn(float vdn) {
		this.vdn = vdn;
	}
	

	public void setAll(float rhoup,float rhodn,float vup,float vdn,float L) {
		this.vdn = vdn;
		this.vup = vup;
		this.L = L;
		this.rhodn = rhodn;
		this.rhoup = rhoup;
	}
	

	public void addtoL(float dl) {
		L += dl;
	}

	public float getRho() {
		return rho;
	}
	
	public float evalRho(float zeta){
		if(zeta>1.0f | zeta<0f)
			return Float.NaN;
		float r = myF.Phi(zeta,rhoup,rhodn);
		r = Math.min( r , myF.rhojam );
		r = Math.max( r , 0 );
		return r;
	}

	public AbstractFD getMyF() {
		return myF;
	}

	protected QRegion(RegionType t, float rup, float rdn, AbstractFD fd)
	{
		type = t;
		myF = fd;
		L = 0.0f;
		rhoup = rup;
		rhodn = rdn;
		vup = Float.NaN;
		rho = Float.NaN;
	}

	public void update_rhoup(){
		float relspeed = vup-myF.Fprime(rhoup);
		float slope = myF.dPhidZeta(0,rhoup,rhodn);
		float rhodot;
		if( Math.abs(L)<gbl.EPS )
			rhodot = 0.0f;
		else
			rhodot = relspeed*slope/L;
		rhoup += rhodot*gbl.getDt();
	}	
	
	public void update_rhodn(){
		float relspeed = vdn-myF.Fprime(rhodn);
		float slope = myF.dPhidZeta(1,rhoup,rhodn);
		float rhodot;
		if( Math.abs(L)<gbl.EPS )
			rhodot = 0.0f;
		else
			rhodot = relspeed*slope/L;
		rhodn += rhodot*gbl.getDt();
	}	
	
	public void update_L(){
		L += (vdn-vup)*gbl.getDt();
	}	
	
	public void update_rhoavg(){
		rho = myF.integralPhi(rhoup, rhodn);
	}
	
	@Override
	public String toString() {
		return (new Float(L)).toString();
	}

	public enum RegionType {
		ConstantDensity,Fan;
	}
	
}
