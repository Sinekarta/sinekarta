package org.sinekartads.model.domain;

import java.io.Serializable;
import java.security.GeneralSecurityException;

import org.apache.commons.lang3.ArrayUtils;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.util.HexUtils;

public class DigestInfo implements Serializable {

	private static final long serialVersionUID = -7655254258250739676L;

	private DigestInfo(DigestAlgorithm algorithm, byte[] fingerPrint) { 
		this.algorithm = algorithm;
		this.fingerPrint = fingerPrint;
	}
	
	private final DigestAlgorithm algorithm;
	private final byte[] fingerPrint;

	public void validate ( 
			byte[] content ) 
					throws GeneralSecurityException {
		
		// Return true if the nested fingerPrint matches with the given content
		algorithm.validate ( content, fingerPrint );
	}
	
	public DigestAlgorithm getAlgorithm() {
		return algorithm;
	}

	public byte[] getFingerPrint() {
		return fingerPrint;
	}
	
	
	
	// -----
	// --- Factory methods
	// -
	
	public static DigestInfo getInstance(
			Object algorithmDescriptor, 
			byte[] fingerPrint) 
					throws IllegalArgumentException {
		
		DigestAlgorithm algorithm = DigestAlgorithm.getInstance(algorithmDescriptor);
		if ( ArrayUtils.isEmpty(fingerPrint) ) {
			throw new IllegalArgumentException ( "invalid fingerPrint - empty" );
		}
		return new DigestInfo(algorithm, fingerPrint);
	}
	
	
	
	// -----
	// --- Utility methods
	// -
	
	public boolean matchesAlgorithm(Object algorithmDescriptor) {
		DigestAlgorithm algorithm = DigestAlgorithm.getInstance(algorithmDescriptor);
		return algorithm.equals(this.algorithm);
	}
	
	public String getAlgorithmName() {
		return algorithm.getName();
	}
	
	public String getAlgorithmId() {
		return algorithm.getId();
	}
	
	public String toString() {
		return String.format("[\n\talgorithm: %s (%s)\n\tfingerPrint: %s\n]", 
				algorithm.getId(), algorithm.getName(), HexUtils.encodeHex(fingerPrint));
	}
	
}