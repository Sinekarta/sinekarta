package org.sinekartads.dto.domain;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.util.HexUtils;

public class DigestDTO extends BaseDTO {

	private static final long serialVersionUID = -5238267083967757044L;

	private String digestAlgorithmName;
	
	private String hexFingerPrint;
	

	
	// -----
    // --- EntityDTO transmission protocol
    // -

	@Override
	public boolean isEmpty ( ) {
		return StringUtils.isBlank ( hexFingerPrint );
	}
	
	
	// -----
	// --- Formatted properties
	// -
	
	public byte[] fingerPrintFromHex() {
		return HexUtils.decodeHex(hexFingerPrint);
	}
	
	public void fingerPrintToHex(byte[] digitalDigest) {
		hexFingerPrint = HexUtils.encodeHex(digitalDigest);
	}
	
	public DigestAlgorithm digestAlgorithmFromName() {
		return DigestAlgorithm.getInstance(digestAlgorithmName);
	}
	
	public void digestAlgorithmToName(DigestAlgorithm digestAlgorithm) {
		digestAlgorithmName = digestAlgorithm.getName();
	}
	
	
	
	// -----
	// --- Direct access to formatted properties
	// -
	
	public void setDigestAlgorithmName(String digestAlgorithmName) {
		this.digestAlgorithmName = digestAlgorithmName;
	}
	public String getDigestAlgorithmName() {
		return digestAlgorithmName;
	}
	public void setHexFingerPrint(String hexFingerPrint) {
		this.hexFingerPrint = hexFingerPrint;
	}
	public String getHexFingerPrint() {
		return hexFingerPrint;
	}

}
