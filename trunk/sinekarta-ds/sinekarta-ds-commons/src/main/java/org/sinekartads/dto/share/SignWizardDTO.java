package org.sinekartads.dto.share;

import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.model.domain.SignatureType.SignCategory;

public class SignWizardDTO extends WizardDTO {

	public static enum TsSelection {
		NONE,
		DEFAULT,
		CUSTOM;
	}
	
	private static final long serialVersionUID = 775568680966263562L;
	
	public SignWizardDTO ( ) {
		nodeRefs = new String[0];
		documents = new DocumentDTO[0];
		signature = new SignatureDTO();
		setKsAliases(new String[0]);
		setScAliases(new String[0]);
	}
	
	private String[] nodeRefs;
	private String tsSelection;
	
	private String sessionId;
	private DocumentDTO[] documents;
	private SignatureDTO signature;
	private String mimetype;
	
	private String signCategory;
	
	private String clientType;
	
	private String ksPin;
	private String[] ksAliases;
	private String ksUserAlias;
	private String ksUserPassword;
	
	private String scPin;
	private String[] scAliases;
	private String scUserAlias;
	private String scUserPassword;
	

	
	// ------
	// --- Simple properties
	// -
	
	public String[] getNodeRefs() {
		return nodeRefs;
	}
	
	public void setNodeRefs ( String[] nodeRefs ) {
		this.nodeRefs = nodeRefs;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public DocumentDTO[] getDocuments() {
		return documents;
	}
	
	public void setDocuments(DocumentDTO[] documents) {
		this.documents = documents;
	}
	
	public SignatureDTO getSignature() {
		return signature;
	}

	public void setSignature(SignatureDTO signature) {
		this.signature = signature;
	}
	
	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	
	public String getClientType() {
		return clientType;
	}
	
	public String getKsPin() {
		return ksPin;
	}

	public void setKsPin(String ksPin) {
		this.ksPin = ksPin;
	}
	
	public String[] getKsAliases() {
		return ksAliases;
	}

	public void setKsAliases(String[] keyStoreAliases) {
		this.ksAliases = keyStoreAliases;
	}

	public String getKsUserAlias() {
		return ksUserAlias;
	}

	public void setKsUserAlias(String ksUserAlias) {
		this.ksUserAlias = ksUserAlias;
	}
	
	public String getKsUserPassword() {
		return ksUserPassword;
	}

	public void setKsUserPassword(String ksUserPassword) {
		this.ksUserPassword = ksUserPassword;
	}
	
	public String getScUserAlias() {
		return scUserAlias;
	}

	public void setScUserAlias(String scUserAlias) {
		this.scUserAlias = scUserAlias;
	}
	
	public String getScUserPassword() {
		return scUserPassword;
	}

	public void setScUserPassword(String ksUserPassword) {
		this.scUserPassword = ksUserPassword;
	}

	public void setScPin(String smartCardPin) {
		this.scPin = smartCardPin;
	}
	
	public String getScPin() {
		return scPin;
	}

	public String[] getScAliases() {
		return scAliases;
	}

	public void setScAliases(String[] smartCardAliases) {
		this.scAliases = smartCardAliases;
	}
	
	
	// -----
	// --- Formatted properties
	// -

	public SignCategory signCategoryFromString() {
		return SignCategory.valueOf(signCategory);
	}
	public void signCategoryToString(SignCategory signCategory) {
		this.signCategory = signCategory.name();
	}
	
	
	
	// -----
	// --- Direct access to formatted properties
	// -

	public void setSignCategory(String signCategory) {
		this.signCategory = signCategory;
	}
	public String getSignCategory() {
		return signCategory;
	}

	public String getTsSelection() {
		return tsSelection;
	}

	public void setTsSelection(String tsSelection) {
		this.tsSelection = tsSelection;
	}
}
