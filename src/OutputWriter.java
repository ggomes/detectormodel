import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;

public class OutputWriter {

	private BufferedWriter out_t;
	private BufferedWriter out_regL;
	private BufferedWriter out_regrhoup;
	private BufferedWriter out_regrhodn;
	private BufferedWriter out_regtype;
	private BufferedWriter out_regrho;
	private BufferedWriter out_rho0;
	private BufferedWriter out_xout;
	private BufferedWriter out_xcom;
	private BufferedWriter out_delay;
	
	public void open() throws Exception{
		out_t = new BufferedWriter(new FileWriter("out_t.txt"));
		out_regL = new BufferedWriter(new FileWriter("out_regL.txt"));
		out_regrhodn = new BufferedWriter(new FileWriter("out_regrhodn.txt"));
		out_regrhoup = new BufferedWriter(new FileWriter("out_regrhoup.txt"));
		out_regtype = new BufferedWriter(new FileWriter("out_regtype.txt"));	
		out_regrho = new BufferedWriter(new FileWriter("out_regrho.txt"));
		out_rho0 = new BufferedWriter(new FileWriter("out_rho0.txt"));
		out_xout = new BufferedWriter(new FileWriter("out_xout.txt"));
		out_xcom = new BufferedWriter(new FileWriter("out_xcom.txt"));
		out_delay = new BufferedWriter(new FileWriter("out_delay.txt"));	
	}
	
	public void write(float t,Link L,Pair xout,Pair xcom) throws Exception{
		
		float tt = Math.round(t/gbl.getDt())*gbl.getDt();
		Vector<Float> l = new Vector<Float>();
		Vector<Float> rhoup = new Vector<Float>();
		Vector<Float> rhodn = new Vector<Float>();
		Vector<Float> regrho = new Vector<Float>();
		Vector<Integer> rtype = new Vector<Integer>();
		for(int i=0;i<L.R.size();i++){
			l.add(L.R.get(i).getL());
			rhodn.add(L.R.get(i).getRhodn());
			rhoup.add(L.R.get(i).getRhoup());
			regrho.add(L.R.get(i).getRho());
			if(L.R.get(i).getType()==QRegion.RegionType.ConstantDensity)
				rtype.add(1);
			if(L.R.get(i).getType()==QRegion.RegionType.Fan)
				rtype.add(2);
		}
		out_t.write( tt + "\n");
		out_regL.write( l.toString() + "\n");
		out_regrhodn.write( rhodn.toString() + "\n");
		out_regrhoup.write( rhoup.toString() + "\n");
		out_regtype.write( rtype.toString() + "\n");
		out_regrho.write( regrho.toString() + "\n");
		out_rho0.write( L.getrho0() + "\n");
		out_xout.write( xout.rho + "\t" + xout.f + "\n");
		out_xcom.write( xcom.rho + "\t" + xcom.f + "\n");
		out_delay.write( L.getD() + "\n");
	}
	
	public void close() throws Exception{
		out_t.close();	
		out_regL.close();	
		out_regrhodn.close();	
		out_regrhoup.close();	
		out_regtype.close();	
		out_regrho.close();	
		out_rho0.close();	
		out_xout.close();		
		out_xcom.close();			
		out_delay.close();
	}
	
}
