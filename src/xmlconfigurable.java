import org.w3c.dom.Node;

public interface xmlconfigurable {

	public boolean initFromDOM(Node p) throws Exception;

	public boolean validate() throws Exception;
	
}
