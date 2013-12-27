
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Node;

public class gbl {
	
	public static boolean debugmode = true;

	public static float EPS = 0.0001f;
	public static ProfileMode mode;
	public static String dischargetype;
	
	// time/space
	private static float dt;						// [hr] time step
	private static float T;							// [hr] total simulation time.
	private static float fin;						// [veh/hr] incoming flow
	
	// traffic demand
	private static float avgvehlength;				// [ft]
	private static float avgplatoonsize; 			// [veh]
	private static float followerheadway;			// [sec]
	
	// downstream density
	private static ScalarFunction rhodown = new ScalarFunction();	// [veh/mile]

	// GETTERS .............................................................
	public static float getDt() {
		return dt;
	}
	
	public static ScalarFunction getRhoDown() {
		return rhodown;
	}

	public static float getT() {
		return T;
	}

	public static float getFin() {
		return fin;
	}

	public static float getAvgvehlength() {
		return avgvehlength;
	}

	public static float getAvgplatoonsize() {
		return avgplatoonsize;
	}

	public static float getFollowerheadway() {
		return followerheadway;
	}
	
	// DOM .........................................................................
	public static boolean initFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return false;
		try  
		{
			dt  = Float.parseFloat(p.getAttributes().getNamedItem("dt").getNodeValue());
			T   = Float.parseFloat(p.getAttributes().getNamedItem("T").getNodeValue());
			fin = Float.parseFloat(p.getAttributes().getNamedItem("fin").getNodeValue());
			avgvehlength = Float.parseFloat(p.getAttributes().getNamedItem("avgvehlength").getNodeValue());
			avgplatoonsize = Float.parseFloat(p.getAttributes().getNamedItem("avgplatoonsize").getNodeValue());
			followerheadway = Float.parseFloat(p.getAttributes().getNamedItem("followerheadway").getNodeValue());
			String tmp = p.getAttributes().getNamedItem("regionmode").getNodeValue();
			if(tmp.equalsIgnoreCase("shocksonly"))
				mode = ProfileMode.ShocksOnly;
			else if(tmp.equalsIgnoreCase("entropyincreasing"))
				mode = ProfileMode.IncreasingEntropy;
			else
				throw new Exception("Incorrect entry for regionmode");

			tmp = p.getAttributes().getNamedItem("dischargetype").getNodeValue();
			if(tmp.equalsIgnoreCase("uncongested") | tmp.equalsIgnoreCase("congested"))
				dischargetype = tmp;
			else
				throw new Exception("Incorrect entry for dischargetype");
			
			for (int i = 0; i < p.getChildNodes().getLength(); i++){
				if (p.getChildNodes().item(i).getNodeName().equals("DownstreamDensity")) {
					res &= rhodown.initFromDOM(p.getChildNodes().item(i));		
					rhodown.sec2hr();
				}
			}
		}
		catch(Exception e) {
			throw new Exception(e.getMessage());
		}
		return res;
	}

	public static boolean validate() throws Exception {
		rhodown.validate();
		if(dt<=0 || T<=0 || avgvehlength<0 || avgplatoonsize<0){
			System.out.println("Globals must be positive");
			return false;
		}
		return true;
	}
	
	public static void initialize() throws Exception { }
	
	public static Vector<Float> readvec(String buf)
	{
		Vector<Float> v = new Vector<Float>();
		StringTokenizer st = new StringTokenizer(buf, ", \t");
		while (st.hasMoreTokens()) {
			v.add(Float.parseFloat(st.nextToken()));
		}
		return v;
	}
	
}