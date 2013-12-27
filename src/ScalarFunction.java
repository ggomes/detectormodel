import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ScalarFunction implements xmlconfigurable {

	public Vector<Float> t;			// [sec or hours] time
	public Vector<Float> value;		// [?]
	private float currvalue;
	private boolean tinseconds;			// true->seconds, false->hours
	
	public ScalarFunction(){
		t = new Vector<Float>();
		value = new Vector<Float>();
		currvalue = 0.0f;
		tinseconds = true;
	}

	public boolean isswitchtime(float now){
		return !t.isEmpty() && Math.abs(now-t.firstElement())<gbl.getDt()/2;
	}
	
	public float eval(float now){
		if( isswitchtime(now) ){
			float newvalue = value.firstElement();
			t.remove(0);
			value.remove(0);
			currvalue = newvalue;
			return newvalue;
		}
		else{
			return currvalue;
		}
	}
	

	public void sec2hr(){
		if(tinseconds){
			for(int i=0;i<t.size();i++)
				t.set(i, t.get(i)/3600.0f);
			tinseconds = false;
		}
	}
	
	public boolean initFromDOM(Node p) throws Exception {
		if (p == null)
			return false;
		try  
		{
			if(p.hasChildNodes()){
				NodeList pp2 = p.getChildNodes();
				for (int j = 0; j < pp2.getLength(); j++) {
					if (pp2.item(j).getNodeName().equals("time")) 
						t = gbl.readvec(pp2.item(j).getTextContent());
					if (pp2.item(j).getNodeName().equals("value")) 
						value = gbl.readvec(pp2.item(j).getTextContent());
				}
			}
		}
		catch(Exception e) {
			throw new Exception(e.getMessage());
		}
		return true;
	}

	public boolean validate() throws Exception {
		if(t.size()!=value.size()){
			System.out.println("Scalar function formatting error.");
			return false;
		}
		
		float a = t.get(0);
		for(int i=1;i<t.size();i++){
			if(t.get(i)<=a){
				System.out.println("Scalar function time must be strictly increasing.");
				return false;
			}
		}
		return true;
	}

}
