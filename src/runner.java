import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;


public class runner {
	
	public static Vector<Link> Links = new Vector<Link>();

	public static void main(String[] args) {

		float t;			// [hr] time
		float rhoeps;
		boolean makeregion;
		boolean xcomhaschanged;
		boolean rhoRhaschanged;
		boolean lastregionisspillback;
		boolean queueisempty;
		Pair xout;			// outgoing state
		Pair xcom;			// command state
		Pair xF;			// (rhoS,F(rhoS))
		Pair xS;			// (rhoS,S(rhoS))
		Pair xSend;			// Sendable state
		Pair xR;			// Receivable state
		Pair xEps;			// Boundary state
		
		//ProfileMode mode = ProfileMode.ShocksOnly;
		
		OutputWriter out = new OutputWriter();
		
		try{

			out.open();
			
			// Read from xml
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("input.xml");
			if(!initFromDOM(doc.getChildNodes().item(0)))
				return;
			
			// validate the input
			if(!validate())
				return;
			
			initialize();
			xR    = new Pair();
			xS    = new Pair();
			xF    = new Pair();
			xcom  = new Pair();
			xSend = new Pair();
			xout  = new Pair();
			xEps  = new Pair();
			
			// Main loop ------------------------------------------------------------------------
			Link L = Links.get(0);
			lastregionisspillback = false;
			for(t=0;t<gbl.getT();t+=gbl.getDt()){
					
				if(gbl.debugmode)
					System.out.println(t + "\t" +  Math.round(t*3600.0f));
				
				// Set xS, xF, xEps ................................
				queueisempty = L.R.isEmpty();
				if(queueisempty){
					float rhoS = L.getAvgDens();
					xS.set(rhoS,L.FD.S(rhoS));
					xF.set(rhoS,L.FD.F(rhoS));
					rhoeps = xS.rho;
					xEps.set(rhoeps, L.FD.F(rhoeps));
				}
				else{
					xS.set(Float.NaN,Float.NaN);
					xF.set(Float.NaN,Float.NaN);
					rhoeps = L.R.lastElement().getRhodn();
					xEps.set(rhoeps, L.FD.F(rhoeps));
				}
				
				// xcom ..........................................
				xcomhaschanged = L.sig.update_xcom(t,xcom);

				if(gbl.debugmode & xcomhaschanged) System.out.println("xcomhaschanged");
				
				// xR ...............................................
				float rhoR = Float.NaN;
				rhoRhaschanged = gbl.getRhoDown().isswitchtime(t);
				rhoR = gbl.getRhoDown().eval(t);
				xR.set(rhoR,L.FD.R(rhoR));

				if(gbl.debugmode & rhoRhaschanged) System.out.println("rhoRhaschanged");				
				
				// xSend .............................................
				if(!queueisempty){						// Yes Queue
					xSend.set( xcom );
					makeregion = xcomhaschanged | rhoRhaschanged;
				}
				else{									// No Queue
					if(L.sig.commandisdischarge){				// discharge command
						xSend.set( xS );
						makeregion = false;
					}
					else{									// restrict command
						if( !xF.isequalto(xcom) && xF.chordslope(xcom)<0 ){
							xSend.set( xcom );
							makeregion = true;
						}
						else{
							xSend.set( xS );
							makeregion = false;	
						}
					}
				}
				
				// xOut ..............................................
				if(xR.islessthan(xSend)){
					xout.set(xR);
					makeregion = (lastregionisspillback & rhoRhaschanged) | !lastregionisspillback;
					makeregion = makeregion & !xout.isequalto(xEps);
					if(makeregion)
						lastregionisspillback = true;
				}
				else{
					xout.set(xSend);
					makeregion = makeregion & !xout.isequalto(xEps);
					if(makeregion)
						lastregionisspillback = false;
				}
					
				// create new regions ..................................
				if( makeregion )
				{
					float rhoout = xout.rho;
					if(gbl.mode==ProfileMode.IncreasingEntropy & rhoout<=rhoeps)
						L.R.add( new QRegion(QRegion.RegionType.Fan,rhoeps,rhoout,L.FD) );
					L.R.add( new QRegion(QRegion.RegionType.ConstantDensity,rhoout,rhoout,L.FD) );	
					L.update_cummL();	
				}
			
				// compute rho0 (time t)
				L.update_rho0();

				// compute average region densities (time t)
				L.update_region_rhoavg();

				// delay (time t)
				L.update_delay();
				
				// Call events (time t)
				L.update_calls(gbl.getDt());

				// STATE UPDATE **********************************
				L.update_state();

				// Update rho0 (time t->t+1) 
				L.update_rhoavg(xout.f);
				
				if(L.R.isEmpty())
					lastregionisspillback = false;
				
				// export 
				out.write(t, L, xout, xcom);

			}
			
			out.close();
			Links.get(0).close();
			System.out.println("done");	
			
		
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
	}

	public static boolean initFromDOM(Node p) throws Exception {
		if ((p == null) || (!p.hasChildNodes()))
			return false;
		boolean res = true;
		try {
			for (int i = 0; i < p.getChildNodes().getLength(); i++){
				
				if (p.getChildNodes().item(i).getNodeName().equals("Globals")) 
					res &= gbl.initFromDOM(p.getChildNodes().item(i));
				if (p.getChildNodes().item(i).getNodeName().equals("LinkList")) {
					if(p.getChildNodes().item(i).hasChildNodes()){
						NodeList pp2 = p.getChildNodes().item(i).getChildNodes();
						for (int j = 0; j < pp2.getLength(); j++) {
							if (pp2.item(j).getNodeName().equals("link")) {
								Link lk = new Link();
								res &= lk.initFromDOM(pp2.item(j));
								Links.add(lk);
							}
						}
					}
				}
				
			}
		}
		catch(Exception e) {
			throw new Exception(e.getMessage());
		}
		return res;
	}

	public static boolean validate() throws Exception {
		boolean res = true;
		res &= gbl.validate();
		for(int i=0;i<Links.size();i++)
			res &= Links.get(i).validate();
		return res;
	}

	public static void initialize() throws Exception {
		gbl.initialize();
		for(int i=0;i<Links.size();i++)
			Links.get(i).initialize();
	}

}
