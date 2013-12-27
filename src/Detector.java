import java.io.BufferedWriter;
import java.io.FileWriter;

import org.w3c.dom.Node;

public class Detector implements xmlconfigurable {

	private int id;
	private Link myLink;
	private float position;	// [mile]
	private float length;	// [ft]
	private BufferedWriter out_p;
	
	protected Detector(Link l)
	{
		myLink = l;
	}

	// .........................................................................
	public void update_calls(float dt) throws Exception{
		float flow = myLink.getFlowAtPos(position);
		float dens = myLink.getDensAtPos(position);
		float p = probcall(dt,flow,dens);
		out_p.write( p + "\n");
	}
	
	public float probcall(float dt,float f,float rho){
		
		float tau = gbl.getFollowerheadway()/3600f;
		float Lv = gbl.getAvgvehlength() / 5280f;
		float Ld = length/5280f;
		float alpha = gbl.getAvgplatoonsize();
		
		if(f<0)
			return Float.NaN;
		
		// evaluate f*delta for positivity
		boolean deltaispositive;
		if( f*(tau-dt) - Ld*rho > 0 )
			deltaispositive = true;
		else
			deltaispositive = false;
		
		if(deltaispositive){
			return dt*f + (Ld+Lv)*rho;
		}
		else{
			float rhoLe = Lv*rho + tau*f;
			float lambdadelta = (f*(tau-dt)-rho*Ld) / alpha / (1-rhoLe);
			return 1 - (1-rhoLe) * ((float) Math.exp(lambdadelta));
		}
		
	}
	
	public void close() throws Exception{
		out_p.close();
	}
	
	// DOM .........................................................................
	public boolean initFromDOM(Node p) throws Exception {
		if (p == null)
			return false;
		try  
		{
			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
			position= Float.parseFloat(p.getAttributes().getNamedItem("pos").getNodeValue());
			length= Float.parseFloat(p.getAttributes().getNamedItem("length").getNodeValue());
		}
		catch(Exception e) {
			throw new Exception(e.getMessage());
		}
		return true;
	}

	public boolean validate() throws Exception {
		if(position<0 || length<0 ){
			System.out.println("Detector length and position must be non-negative.");
			return false;
		}
		return true;
	}
	

	public void initialize() throws Exception { 
		out_p = new BufferedWriter(new FileWriter("out_det_" + id + ".txt"));
	}
	
}