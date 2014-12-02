package org.sinekartads.dto.jcl;


public class PostSignResponseDTO extends JclResponseDTO {

	private static final long serialVersionUID = -1796910403169780972L;

	private String[] detachedSign = new String[1];
	private String[] embeddedSign = new String[1];
	private String[] tsResponse = new String[1];
	private String[] markedSign = new String[1];
	
	public String getDetachedSign() {
		return detachedSign[0];
	}
	
	public void setDetachedSign(String detachedSign) {
		this.detachedSign[0] = detachedSign;
	}
	
	public String getEmbeddedSign() {
		return embeddedSign[0];
	}
	
	public void setEmbeddedSign(String embeddedSign) {
		this.embeddedSign[0] = embeddedSign;
	}

	public String getTsResponse() {
		return tsResponse[0];
	}
	
	public void setTsResponse(String tsResponse) {
		this.tsResponse[0] = tsResponse;
	}
	
	public String getMarkedSign() {
		return markedSign[0];
	}

	public void setMarkedSign(String markedSign) {
		this.markedSign[0] = markedSign;
	}

}
