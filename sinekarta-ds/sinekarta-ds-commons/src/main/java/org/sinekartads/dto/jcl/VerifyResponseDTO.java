package org.sinekartads.dto.jcl;


public class VerifyResponseDTO extends JclResponseDTO {

	private static final long serialVersionUID = 1246784972297798389L;
	
	private String[] extractedContent = new String[1];

	public String getExtractedContent() {
		return extractedContent[0];
	}
	
	public void setExtractedContent(String extractedContent) {
		this.extractedContent[0] = extractedContent;
	}

}
