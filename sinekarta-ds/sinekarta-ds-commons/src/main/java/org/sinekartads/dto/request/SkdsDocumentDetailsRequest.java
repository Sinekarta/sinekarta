package org.sinekartads.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SinekartaDsDocumentDetailsRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkdsDocumentDetailsRequest extends BaseRequest {
	
	private static final long serialVersionUID = 7884307217055017140L;
	
	
	
	private String[] nodeRefs;
	
	
	
	public String[] getNodeRefs() {
		return nodeRefs;
	}

	public void setNodeRefs(String[] nodeRefs) {
		this.nodeRefs = nodeRefs;
	}
}
