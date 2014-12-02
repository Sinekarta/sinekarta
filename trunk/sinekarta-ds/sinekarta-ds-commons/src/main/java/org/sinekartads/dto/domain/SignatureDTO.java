package org.sinekartads.dto.domain;

import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.formats.DateDTOProperty;
import org.sinekartads.dto.formats.FlagDTOProperty;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignatureStatus;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.x509.X509Utils;

public class SignatureDTO extends BaseDTO {

	private static final long serialVersionUID = -5238267083967757044L;
	
	public SignatureDTO() {
		timeStampRequest = new TimeStampRequestDTO();
		certificate = new CertificateDTO();
		digest = new DigestDTO();
		hexCertificateChain = new String[0];
		timeStamps = new TimeStampDTO[0];
	}
	
	private String signCategory;
	private String signDisposition;
	private String signAlgorithm;
	private String digestAlgorithm;
	@FlagDTOProperty
	private String finalized;
	
	@FlagDTOProperty
	private String counterSignature;
	@DateDTOProperty
	private String signingTime;
	private String reason;
	private String location;
	
	private TimeStampRequestDTO timeStampRequest;	
	private String[] hexCertificateChain;
	private CertificateDTO certificate;
	private DigestDTO digest;
	private String hexDigitalSignature;	
	private TimeStampDTO[] timeStamps;
	private String verifyResult;
	/**
	 * @deprecated ignore this field - fake field for serialization only proposes
	 */
	transient boolean empty;
	/**
	 * @deprecated ignore this field - fake field for serialization only proposes
	 */
	transient SignatureStatus status;
	
	@Override
    public boolean isEmpty ( ) {
    	return getStatus() == SignatureStatus.Stable.RAW;
    }
	
	
	
	// -----
	// --- Signature Status evaluation
	// -
	
	public SignatureStatus getStatus ( ) {
		
		if ( StringUtils.isNotEmpty(verifyResult) )			return SignatureStatus.Stable.VERIFIED;
		if ( finalizedFromString() )						return SignatureStatus.Stable.FINALIZED;
		if ( ArrayUtils.isNotEmpty(timeStamps) )			return SignatureStatus.SignProcess.MARKED;
		if ( StringUtils.isNotBlank(hexDigitalSignature) )	return SignatureStatus.SignProcess.SIGNED;
		if ( !BaseDTO.isEmpty(digest) )						return SignatureStatus.SignProcess.DIGEST;
		if ( ArrayUtils.isNotEmpty(hexCertificateChain))	return SignatureStatus.SignProcess.CHAIN;
		return SignatureStatus.Stable.RAW;
	}
	
	
	
	
	
	// -----
	// --- Conversion methods
	// -
	
	public static SignatureDTO fromBase64 ( String base64 ) {
		
		return BaseDTO.fromHex(base64, SignatureDTO.class);
	}
	
	
	
	// -----
	// --- Simple properties
	// -
	
	public DigestDTO getDigest() {
		return digest;
	}

	public void setDigest(DigestDTO digestInfo) {
		this.digest = digestInfo;
	}
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSigningTime() {
		return signingTime;
	}

	public void setSigningTime(String signingTimeValue) {
		this.signingTime = signingTimeValue;
	}
	
	public TimeStampDTO[] getTimeStamps() {
		return timeStamps;
	}
	
	public void setTimeStamps(TimeStampDTO[] timeStamps) {
		this.timeStamps = timeStamps;
	}
	
	public TimeStampRequestDTO getTimeStampRequest() {
		return timeStampRequest;
	}
	
	public void setTimeStampRequest(TimeStampRequestDTO timeStampRequest) {
		this.timeStampRequest = timeStampRequest; 
	}
	
	
	
	// -----
	// --- Formatted properties
	// -
	
	public SignCategory signCategoryFromString() {
		SignCategory signCat = null;
		if ( StringUtils.isNotBlank(signCategory) ) {
			signCat = SignCategory.valueOf(signCategory);
		} 
		return signCat;
	}
	
	public void signCategoryToString(SignCategory signCategory) {
		String signCat = null;
		if ( signCategory != null ) {
			signCat = signCategory.name();
		}
		this.signCategory = signCat; 
	}
	
	public X509Certificate[] certificateChainToHex() {
		return X509Utils.rawX509CertificatesFromHex(hexCertificateChain);
	}
	
	public void certificateChainToHex(X509Certificate[] certificateChain) {
		hexCertificateChain = X509Utils.rawX509CertificatesToHex(certificateChain);
	}
	
	public void digitalSignatureToHex(byte[] digitalSignature) {
		hexDigitalSignature = HexUtils.encodeHex(digitalSignature);
	}
	
	public byte[] digitalSignatureFromHex() {
		return HexUtils.decodeHex(hexDigitalSignature);
	}
	
	public SignatureAlgorithm signAlgorithmFromString() {
		return SignatureAlgorithm.getInstance(signAlgorithm);
	}
	
	public void signAlgorithmToString(SignatureAlgorithm signatureAlgorithm) {
		this.signAlgorithm = signatureAlgorithm.getName();
	}
	
	public DigestAlgorithm digestAlgorithmFromName() {
		return DigestAlgorithm.getInstance(digestAlgorithm);
	}
	
	public void digestAlgorithmToName(DigestAlgorithm digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm.getName();
	}
	
	public Boolean finalizedFromString() {
		return Boolean.valueOf(finalized); 
	}
	
	public void finalizedToString(Boolean finalized) {
		this.finalized = BooleanUtils.toString(finalized, "true", "false");
	}
	
	public Boolean counterSignatureFromString() {
		return Boolean.valueOf(counterSignature); 
	}
	
	public void counterSignatureToString(Boolean counterSignature) {
		counterSignature = BooleanUtils.isTrue(counterSignature);
	}
	
	public Date signingTimeFromString() {
		Date signingTime = null;
		if ( StringUtils.isNotBlank(this.signingTime) ) {
			try {
				signingTime = timeFormat.parse ( this.signingTime );
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return signingTime;
	}
	
	public void signingTimeToString(Date signingTime) {
		if ( signingTime == null) {
			this.signingTime = "";
		} else {
			this.signingTime = timeFormat.format(signingTime);
		}
	}
	
	public VerifyResult verifyResultFromString() {
		VerifyResult verRes = null;
		if ( StringUtils.isNotBlank(verifyResult) ) {
			verRes = VerifyResult.valueOf(verifyResult);
		}
		return verRes;
	}
	
	public void verifyResultToString(VerifyResult verifyResult) {
		this.verifyResult = "";
		if ( verifyResult != null ) {
			this.verifyResult = verifyResult.name();
		}
	}
	
	
	
	// -----
	// --- Simple properties
	// -

	public String getSignCategory() {
		return signCategory;
	}

	public void setSignCategory(String signCategory) {
		this.signCategory = signCategory;
	}
	
	public void setSignAlgorithm(String signatureAlgorithm) {
		this.signAlgorithm = signatureAlgorithm;
	}
	public String getSignAlgorithm() {
		return signAlgorithm;
	}
	
	public void setDigestAlgorithm(String digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}
	public String getDigestAlgorithm() {
		return digestAlgorithm;
	}
	
	public String getFinalized() {
		return finalized;
	}

	public void setFinalized(String finalized) {
		this.finalized = finalized;
	}


	public void setHexDigitalSignature(String hexDigitalSignature) {
		this.hexDigitalSignature = hexDigitalSignature;
	}
	public String getHexDigitalSignature() {
		return hexDigitalSignature;
	}
	
	public String[] getHexCertificateChain() {
		return hexCertificateChain;
	}

	public void setHexCertificateChain(String[] hexRawX509CertificateChain) {
		this.hexCertificateChain = hexRawX509CertificateChain;
	}

	public void setCertificate(CertificateDTO certificate) {
		this.certificate = certificate;
	}
	public CertificateDTO getCertificate() {
		return certificate;
	}
	
	public void setSignDisposition(String signDisposition) {
		this.signDisposition = signDisposition;
	}
	public String getSignDisposition() {
		return signDisposition;
	}

	public void setCounterSignature(String counterSignature) {
		this.counterSignature = counterSignature;
	}
	public String getCounterSignature() {
		return counterSignature;
	}

	public void setVerifyResult(String verifyResult) {
		this.verifyResult = verifyResult;
	}
	public String getVerifyResult() {
		return verifyResult;
	}


	
	// -----
	// --- PDF signature specific protocol
	// -
	
	private String pdfSignName;
	private String pdfRevision;
	@FlagDTOProperty
	private String pdfCoversWholeDocument;
	
	public String getPdfSignName() {
		return pdfSignName;
	}
	
	public void setPdfSignName(String name) {
		this.pdfSignName = name;
	}
	
	public String getPdfRevision() {
		return pdfRevision;
	}
	
	public void setPdfRevision(String revision) {
		this.pdfRevision = revision;
	}
	
	
	public Boolean pdfCoversWholeDocumentFromString() {
		return Boolean.valueOf(pdfCoversWholeDocument); 
	}
	
	public void pdfCoversWholeDocumentToString(Boolean pdfCoversWholeDocument) {
		if ( pdfCoversWholeDocument != null) {
			this.pdfCoversWholeDocument = BooleanUtils.toString(pdfCoversWholeDocument, "true", "false");
		} else {
			this.pdfCoversWholeDocument = "false";
		}
	}

	public void setPdfCoversWholeDocument(String coversWholeDocument) {
		this.pdfCoversWholeDocument = coversWholeDocument;
	}
	
	public String getPdfCoversWholeDocument() {
		return pdfCoversWholeDocument;
	}
}

