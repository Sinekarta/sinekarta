package org.sinekartads.dto.share;

import org.sinekartads.dto.domain.DocumentDTO;

public class VerifyWizardDTO extends WizardDTO {

	private static final long serialVersionUID = -8925771429236697347L;

//
//	public void validate() throws SinekartaDsBadClientRequestException {
//		if(StringUtils.isBlank(nodeRef)) {
//			throw new SinekartaDsBadClientRequestException(ErrorCode.MISSING_DTO_PARAMETERS, "nodeRef");
//		}
//	}

	private String nodeRef;
	private DocumentDTO document; 
	private String verifyOperation;
	private String destName;
	private String pathChoice;
	private String extractionParent;
	private String replaceFiles;

	
	public String getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(String nodeRef) {
		this.nodeRef = nodeRef;
	}

	public DocumentDTO getDocument() {
		return document;
	}

	public void setDocument(DocumentDTO document) {
		this.document = document;
	}

	public String getVerifyOperation() {
		return verifyOperation;
	}

	public void setVerifyOperation(String verifyOperation) {
		this.verifyOperation = verifyOperation;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String destName) {
		this.destName = destName;
	}

	public String getPathChoice() {
		return pathChoice;
	}

	public void setPathChoice(String pathChoice) {
		this.pathChoice = pathChoice;
	}

	public String getExtractionParent() {
		return extractionParent;
	}

	public void setExtractionParent(String extractionParent) {
		this.extractionParent = extractionParent;
	}

	public String getReplaceFiles() {
		return replaceFiles;
	}

	public void setReplaceFiles(String flagReplaceFile) {
		this.replaceFiles = flagReplaceFile;
	}

}
