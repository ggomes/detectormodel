import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.text.resources.FormatData_sr_RS;

public class Link implements xmlconfigurable{

	public float L;							// [mile]
	// public float rhoinit;	GCG				// [veh/mile] initial denisity
	public Vector<QRegion> R;
	public Vector<Float> cummL;				// [mile] cummulative region length
	
	// fundamental diagram
	public AbstractFD FD;					// Fundamental diagram
	
	// Link variables 
	private float rho0;						// [veh/mile] upstream density
	private float rho;						// [veh/mile] average density
	private float D;						// [veh.hr] Instantaneous delay
	
	// Detectors
	public Vector<Detector> det;
	
	// Signal
	public Signal sig = null;
	
	protected Link()
	{
		R 	  = new Vector<QRegion>();
		det	  = new Vector<Detector>();
		sig	  = new Signal(this);
		cummL = new Vector<Float>();
	}

	// .........................................................................

	public float getD(){
		return D;
	}
	
	public float getAvgDens(){
		return rho;
	}
	
	public float getrho0(){
		return rho0;
	}
	
	public float getL0()
	{
		float L0 = L;
		for(int i=0;i<R.size();i++)
			L0 -= R.get(i).getL();
		return L0;
	}

	public float getFlowAtPos(float pos)
	{
		if(R.isEmpty())
			return FD.S(rho0);
		
		float down = 0.0f;
		for(int i=0;i<R.size();i++){
			if(i>0)
				down = cummL.get(i-1);
			if( pos>=down & pos<cummL.get(i)){
				float zeta = cummL.get(i) - pos;
				float r = R.get(R.size()-i-1).evalRho(zeta);
				return FD.F(r);
			}
		}
		return FD.F(rho0);
	}

	public float getDensAtPos(float pos)
	{
		if(R.isEmpty())
			return rho0;
		float down = 0.0f;
		for(int i=0;i<R.size();i++){
			if(i>0)
				down = cummL.get(i-1);
			if( pos>=down & pos<cummL.get(i)){
				float zeta = cummL.get(i) - pos;
				return R.get(R.size()-i-1).evalRho(zeta);
			}
		}
		return rho0;
	}

	public void update_region_rhoavg()
	{
		for(int i=0;i<R.size();i++)
			R.get(i).update_rhoavg();	
	}

	public void update_delay()
	{
		int i;
		D = 0;
		for(i=0;i<R.size();i++){
			QRegion X = R.get(i);
			D += X.getL()*(X.getRho() - FD.FinvMinus(gbl.getFin()));
		}	
	}
	
	public void update_cummL()
	{
		cummL.clear();
		float c = 0;
		for(int i=R.size()-1;i>=0;i--){
			c += R.get(i).getL();
			cummL.add( c );
		}	
	}

	public void update_rho0()
	{
		rho0 = FD.FinvMinus(gbl.getFin());
	}
	
	public void update_rhoavg(float fout) throws Exception
	{
		float N=rho*L;
		N += (gbl.getFin()-fout)*gbl.getDt();
		rho = N/L;
	}
	
	public void update_state()
	{
		if(R.isEmpty())
			return;
		
		int i;
		int removeregion;
		float vup,vdn;
		float tau;
		float rL;
		float movetime;
		float timeleft;
		float rhoup,rhodn;
		float r;
		
		Vector<Float> rhoup_mid = new Vector<Float>();
		Vector<Float> rhodn_mid = new Vector<Float>();
		Vector<Float> L_mid = new Vector<Float>();
		Vector<Float> vup_mid = new Vector<Float>();
		Vector<Float> vdn_mid = new Vector<Float>();
		Vector<Float> L_forward = new Vector<Float>();
		
		for(i=0;i<R.size();i++){
			vup_mid.add( Float.NaN );
			vdn_mid.add( Float.NaN );
			rhoup_mid.add( R.get(i).getRhoup() );
			rhodn_mid.add( R.get(i).getRhodn() );
			L_mid.add( R.get(i).getL() );
			L_forward.add( Float.NaN );
		}

		compute_boundaryspeed(rhoup_mid,rhodn_mid,vup_mid,vdn_mid);

		timeleft = gbl.getDt();
		while(timeleft>0){

			// advance all L by timeleft
			for(i=0;i<R.size();i++)
				L_forward.set( i , L_mid.get(i) + (vdn_mid.get(i)-vup_mid.get(i))*timeleft );

			// check if any are negative if so, find smallest vanishing time
			tau = 0;
			removeregion = -1;
			for(i=0;i<R.size();i++){
				if(L_forward.get(i)<0){
					rL  = L_forward.get(i);
					vup = vup_mid.get(i);
					vdn = vdn_mid.get(i);
					if( rL/(vdn-vup)>tau ){
						tau = rL/(vdn-vup);
						removeregion = i;
					}
				}
			}
			
			movetime = timeleft - tau;
			timeleft = tau;
			
			// update boundary densities and lengths by smallest vanishing time
			for(i=0;i<R.size();i++){
				vup = vup_mid.get(i);
				vdn = vdn_mid.get(i);
				rL  = L_mid.get(i);
				rhoup = rhoup_mid.get(i);
				rhodn = rhodn_mid.get(i);
				r = compute_boundarydensity(vup,rhoup,rhodn,rL,movetime,true);
				rhoup_mid.set(i,r);
				r = compute_boundarydensity(vdn,rhoup,rhodn,rL,movetime,false);
				rhodn_mid.set(i,r);
				L_mid.set( i , L_mid.get(i) + (vdn_mid.get(i)-vup_mid.get(i))*movetime );
			}
			
			// eliminate regions
			if(removeregion>=0){
				rhoup_mid.remove(removeregion);
				rhodn_mid.remove(removeregion);
				L_mid.remove(removeregion);
				vup_mid.remove(removeregion);
				vdn_mid.remove(removeregion);
				L_forward.remove(removeregion);
				R.remove(removeregion);
			}
			
			// Compute boundary speeds
			compute_boundaryspeed(rhoup_mid,rhodn_mid,vup_mid,vdn_mid);
		}	
		
		// Record to R
		for(i=0;i<R.size();i++){
			QRegion X = R.get(i);		
			X.setAll(rhoup_mid.get(i),rhodn_mid.get(i),vup_mid.get(i),vdn_mid.get(i),L_mid.get(i));
		}
		
		update_cummL();
		
	}
	
	public void compute_boundaryspeed(Vector<Float> rhoup,Vector<Float> rhodn,Vector<Float> vup,Vector<Float> vdn){
		int i;
		float rhodnim1,rhoupi;
		for(i=0;i<R.size();i++){
			if(i==0)
				rhodnim1 = rho0;
			else 
				rhodnim1 = rhodn.get(i-1);
			rhoupi = rhoup.get(i);
			vup.set(i,FD.chordslope(rhodnim1,rhoupi));
		}	
		for(i=0;i<R.size();i++){
			if(i+1<R.size())
				vdn.set( i , vup.get(i+1));
			else
				vdn.set( i , 0f );
		}
	}
	
	public float compute_boundarydensity(float vboundary,float rhoup,float rhodn,float rL,float dt,boolean isup){
		float relspeed;
		float slope;
		float rhodot;
		float newrho;
		if(isup){
			relspeed = vboundary - FD.Fprime(rhoup);
			slope = FD.dPhidZeta(0,rhoup,rhodn);	
			if( Math.abs(rL)<gbl.EPS )
				rhodot = 0.0f;
			else
				rhodot = relspeed*slope/rL;
			newrho = rhoup + rhodot*dt;
		}
		else{
			relspeed = vboundary - FD.Fprime(rhodn);
			slope = FD.dPhidZeta(1,rhoup,rhodn);	
			if( Math.abs(rL)<gbl.EPS )
				rhodot = 0.0f;
			else
				rhodot = relspeed*slope/rL;
			newrho = rhodn + rhodot*dt;	
		}
		newrho = Math.min(newrho, FD.rhojam);
		newrho = Math.max(newrho, 0f);
		return newrho;
	}

	public void update_calls(float dt) throws Exception
	{
		int i;
		for(i=0;i<det.size();i++){
			det.get(i).update_calls(dt);
		}	
	}
	
	public void close() throws Exception{
		for(int i=0;i<det.size();i++)
			det.get(i).close();
	}

	// DOM .........................................................................
	public boolean initFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return false;
		try  
		{

			if(p.hasChildNodes()) {
				
				NodeList nl1 = p.getChildNodes();
				for (int i = 0; i < nl1.getLength(); i++)
				{

					Node p2 = nl1.item(i);
					
					if (p2.getNodeName().equals("parameters")){
						L		= Float.parseFloat(p2.getAttributes().getNamedItem("L").getNodeValue());
						// GCG rhoinit = Float.parseFloat(p2.getAttributes().getNamedItem("rhoinit").getNodeValue());
					}
					
					if (p2.getNodeName().equals("FD")){
						String classname = p2.getAttributes().getNamedItem("class").getNodeValue();
						Class c = Class.forName(classname);
						FD = (AbstractFD)c.newInstance();
						FD.initFromDOM(p2);
						FD.setRhoDis();
					}
					
					if (p2.getNodeName().equals("detectors")){
						if(p2.hasChildNodes()){
							NodeList nl2 = p2.getChildNodes();
							for (int j = 0; j < nl2.getLength(); j++) {
								if (nl2.item(j).getNodeName().equals("det")) {
									Detector d = new Detector(this);
									res &= d.initFromDOM(nl2.item(j));
									det.add(d);
								}
							}
						}
					}

					if (p2.getNodeName().equals("CommandDensity"))
						res &= sig.initFromDOM(p2);
					
				}
			}
		}
		catch(Exception e) {
			throw new Exception(e.getMessage());
		}
		return res;
	}

	public boolean validate() throws Exception {
		boolean res = true;
		
		res &= FD.validate();
		
		if( L<=0 ){ // GCG|| rhoinit<0 ){
			System.out.println("Link parameters must be positive.");
			res = false;
		}

		if(L<FD.maxspeed()*gbl.getDt()){
			System.out.println("Link too short.");
			res = false;
		}
		
		for(int i=0;i<det.size();i++)
			res &= det.get(i).validate();
		
		if(sig!=null)
			res &= sig.validate();
			
		return res;
	}

	public void initialize() throws Exception { 
		float r = FD.FinvMinus(gbl.getFin());
		rho0 = r;
		rho = r;		
		for(int i=0;i<det.size();i++)
			det.get(i).initialize();
		
		if(sig!=null)
			sig.initialize();
	}
	
}
