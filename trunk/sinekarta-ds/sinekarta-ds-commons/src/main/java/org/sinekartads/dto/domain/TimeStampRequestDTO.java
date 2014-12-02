package org.sinekartads.dto.domain;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.util.HexUtils;

public class TimeStampRequestDTO extends BaseDTO {

	private static final long serialVersionUID = -1202993324202213553L;
	
	public static TimeStampRequestDTO fromBase64 ( String base64 ) {
		return BaseDTO.fromHex ( base64, TimeStampRequestDTO.class );
	}
	
	public static TimeStampRequestDTO fromJSON ( String json ) {
		return BaseDTO.fromJSON ( json, TimeStampRequestDTO.class );
	}
	
	private String messageImprintAlgorithm;
	
	private String messageImprintDigest;
	
	private String nounce;
	
	private String tsUrl;
	
	private String tsUsername;
	
	private String tsPassword;
	
	private String timestampDisposition;
	
	@Override
    public boolean isEmpty ( ) {
    	return StringUtils.isBlank ( tsUrl );
    }
	
	
	
	// -----
	// --- Simple properties
	// -
	
	public String getTsUrl() {
		return tsUrl;
	}

	public void setTsUrl(String tsUrl) {
		this.tsUrl = tsUrl;
	}

	public String getTsUsername() {
		return tsUsername;
	}

	public void setTsUsername(String tsUsername) {
		this.tsUsername = tsUsername;
	}

	public String getTsPassword() {
		return tsPassword;
	}

	public void setTsPassword(String tsPassword) {
		this.tsPassword = tsPassword;
	}
	
	
	
    // -----
    // --- Formatted properties
    // -
	
	public DigestAlgorithm messageImprintAlgorithmFromString() {
		return DigestAlgorithm.fromName(messageImprintAlgorithm);
	}
	
	public void messageImprintAlgorithmToString(DigestAlgorithm messageImprintAlgorithm) {
		this.messageImprintAlgorithm = messageImprintAlgorithm.getName();
	}
	
	public byte[] messageImprintDigestFromHex() {
		return HexUtils.decodeHex(messageImprintDigest);
	}
	
	public void messageImprintDigestToHex(byte[] fingerPrint) {
		messageImprintDigest = HexUtils.encodeHex(fingerPrint);
	}
	
	public BigInteger nounceFromString() {
		return BigInteger.valueOf ( Long.parseLong(nounce) );
	}
	
	public void nounceToString(BigInteger nounce) {
		this.nounce = nounce.toString(); 
	}
	
	public SignDisposition.TimeStamp timestampDispositionFromString() {
		return SignDisposition.TimeStamp.valueOf(timestampDisposition); 	
	}
	
	public void timestampDispositionToString(SignDisposition.TimeStamp timestampDisposition) {
		this.timestampDisposition = timestampDisposition.name();
	}
	
	
	
	// -----
	// --- Direct access to formatted properties
	// -
	
	public void setMessageImprintAlgorithm(String messageImprintAlgorithm) {
		this.messageImprintAlgorithm = messageImprintAlgorithm;
	}
	public String getMessageImprintAlgorithm() {
		return messageImprintAlgorithm;
	}
	
	public void setMessageImprintDigest(String messageImprintDigest) {
		this.messageImprintDigest = messageImprintDigest;
	}
	public String getMessageImprintDigest() {
		return messageImprintDigest;
	}

	public void setNounce(String nounce) {
		this.nounce = nounce;
	}
	public String getNounce() {
		return nounce;
	}

	public void setTimestampDisposition(String timestampDisposition) {
		this.timestampDisposition = timestampDisposition;
	}
	public String getTimestampDisposition() {
		return timestampDisposition;
	}
	
}

