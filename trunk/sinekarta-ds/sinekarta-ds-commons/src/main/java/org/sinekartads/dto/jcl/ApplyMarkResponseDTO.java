package org.sinekartads.dto.jcl;


public class ApplyMarkResponseDTO extends JclResponseDTO {

	private static final long serialVersionUID = 1246784972297798389L;
	
	private String[] tsResponse = new String[1];
	private String[] markedSign = new String[1];
	
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
