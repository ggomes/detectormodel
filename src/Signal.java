import org.w3c.dom.Node;

public class Signal implements xmlconfigurable {

	public Link myLink;
	public ScalarFunction com;
	public boolean commandisdischarge;

	protected Signal(Link l){
		com = new ScalarFunction();
		myLink = l;
		commandisdischarge = true;
	}

	// .........................................................................
	
	public boolean update_xcom(float t,Pair xcom){
		
		boolean timetochange = com.isswitchtime(t);
		if(timetochange)
		{
			float comrho = Float.NaN;
			comrho = com.eval(t);
			if(comrho<myLink.FD.rhocrit){
				commandisdischarge = true;
				if(gbl.dischargetype.equalsIgnoreCase("uncongested")){
					comrho = myLink.FD.rhodisminus;	// uncongested discharge
					if(gbl.mode==ProfileMode.IncreasingEntropy){
						System.out.println("WARNING: Uncongested discharge command not permitted in the entropy-increasing mode.");
						comrho = myLink.FD.rhodisplus;
					}
				}
				if(gbl.dischargetype.equalsIgnoreCase("congested"))
					comrho = myLink.FD.rhodisplus;	// congested discharge
			}
			else{									// restrict
				commandisdischarge = false;
				comrho = Math.max( comrho , myLink.FD.rhodisplus + gbl.EPS );
			}
			xcom.set(comrho,myLink.FD.F(comrho));
		}		
		return timetochange;
	}
	
	// DOM .........................................................................
	public boolean initFromDOM(Node p) throws Exception {
		if (p == null)
			return false;
		com.initFromDOM(p);
		com.sec2hr();
		return true;
	}

	public boolean validate() throws Exception {
		
		if(!com.validate())
			return false;

		float rhobar = myLink.FD.FinvPlus(0);
		for(int i=1;i<com.t.size();i++){
			if(com.t.get(i)<0 || com.value.get(i)<0){
				System.out.println("Signal command must be non-negative.");
				return false;
			}
			
			if(com.value.get(i)>rhobar){
				System.out.println("Signal command exceeds jam density.");
				return false;
			}
			
		}
		return true;
	}
	
	public void initialize() throws Exception {
	}
	
}
