package org.sinekartads.dto.domain;

import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.formats.DateDTOProperty;
import org.sinekartads.model.domain.SecurityLevel.TimeStampVerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.HexUtils;

public class TimeStampDTO extends BaseDTO {

	private static final long serialVersionUID = -1202993324202213553L;
	
	private String signAlgorithm;
	private String digestAlgorithm;
	private String disposition;
	private String hexTimeStampToken;
	private DigestDTO messageImprint;
//	private CertificateDTO certificate;
	private String[] hextCertificateChain;
	private String hexDigitalSignature;
	private String verifyResult;
	private String tsaName;
	@DateDTOProperty
	private String signingTime;
	private String reason;
	private String location;
	
	@Override
    public boolean isEmpty ( ) {
    	return StringUtils.isBlank ( hexTimeStampToken );
    }
	
	
	public String getSigAlgorithm() {
		return signAlgorithm;
	}

	public void setSigAlgorithm(String sigAlgorithm) {
		this.signAlgorithm = sigAlgorithm;
	}
	
	public String getDigestAlgorithm() {
		return digestAlgorithm;
	}

	public void setDigestAlgorithm(String digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm;
	}
	
	public void setHexCertificateChain(String[] hextCertificateChain) {
		this.hextCertificateChain = hextCertificateChain;
	}
	
	public String[] getHexCertificateChain() {
		return hextCertificateChain;
	}
	
//	public void setCertificate(CertificateDTO certificate) {
//		this.certificate = certificate;
//	}
//	
//	public CertificateDTO getCertificate() {
//		return certificate;
//	}
	
	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}

	public String getTsaName() {
		return tsaName;
	}

	public void setTsaName(String tsaName) {
		this.tsaName = tsaName;
	}
	
	public void setHexTimeStampToken(String hexTimeStampToken) {
		this.hexTimeStampToken = hexTimeStampToken;
	}
	public String getHexTimeStampToken() {
		return hexTimeStampToken;
	}

	public void setMessageImprint(DigestDTO messageImprintDto) {
		this.messageImprint = messageImprintDto;
	}
	public DigestDTO getMessageImprint() {
		return messageImprint;
	}
	
	public void setHexDigitalSignature(String hexDigitalSignature) {
		this.hexDigitalSignature = hexDigitalSignature;
	}
	public String getHexDigitalSignature() {
		return hexDigitalSignature;
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

	public void setVerifyResult(String verifyResult) {
		this.verifyResult = verifyResult;
	}
	public String getVerifyResult() {
		return verifyResult;
	}
	
	
	
	// -----
	// --- Formatted properties
	// -
	
	public SignatureAlgorithm signAlgorithmFromString() {
		return SignatureAlgorithm.getInstance(signAlgorithm);
	}
	
	public void signAlgorithmToString(SignatureAlgorithm signatureAlgorithm) {
		this.signAlgorithm = signatureAlgorithm.getName();
	}
	
	public DigestAlgorithm digestAlgorithmFromString() {
		return DigestAlgorithm.getInstance(digestAlgorithm);
	}
	
	public void digestAlgorithmToString(DigestAlgorithm digestAlgorithm) {
		this.digestAlgorithm = digestAlgorithm.getName();
	}
	
	public SignDisposition.TimeStamp dispositionFromString() {
		return SignDisposition.TimeStamp.valueOf(disposition);
	}
	
	public void dispositionToString(SignDisposition.TimeStamp disposition) {
		this.disposition = disposition.name();
	}

	public void encTimeStampTokenToHex(byte[] encTimeStampToken) {
    	try {
			if(encTimeStampToken != null) {
				hexTimeStampToken= HexUtils.encodeHex(encTimeStampToken);
			} else {
				hexTimeStampToken = null;
			}
    	} catch(Exception e) {
    		// it should be never thrown
    		throw new RuntimeException(e);
    	}
	}
	
	public byte[] encTimeStampTokenFromHex() throws CertificateException {
		byte[] encTimeStampToken = null;
		try {
			if(hexTimeStampToken != null) {
				encTimeStampToken = HexUtils.decodeHex(hexTimeStampToken);
			}
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
		
		return encTimeStampToken;
	}
	
	public void digitalSignatureToHex(byte[] digitalSignature) {
		hexDigitalSignature = HexUtils.encodeHex(digitalSignature);
	}
	
	public byte[] digitalSignatureFromHex() {
		return HexUtils.decodeHex(hexDigitalSignature);
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
			this.signingTime = null;
		} else {
			this.signingTime = timeFormat.format(signingTime);
		}
	}
	
	public TimeStampVerifyResult verifyResultFromString() {
		return TimeStampVerifyResult.valueOf(verifyResult);
	}
	
	public void verifyResultToString(TimeStampVerifyResult verifyResult) {
		this.verifyResult = verifyResult.name();
	}
}

