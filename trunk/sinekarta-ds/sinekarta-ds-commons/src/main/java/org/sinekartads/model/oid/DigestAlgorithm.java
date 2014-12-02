package org.sinekartads.model.oid;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public enum DigestAlgorithm implements Serializable, OidWrapper {
	
	MD2					(SinekartaDsAlgorithm.MD2),
	MD4					(SinekartaDsAlgorithm.MD4),
	MD5					(SinekartaDsAlgorithm.MD5),
	SHA1				(SinekartaDsAlgorithm.SHA1),
	SHA224				(SinekartaDsAlgorithm.SHA224),
	SHA256				(SinekartaDsAlgorithm.SHA256),
	SHA384				(SinekartaDsAlgorithm.SHA384),
	SHA512				(SinekartaDsAlgorithm.SHA512),
	;
	
	final SinekartaDsAlgorithm algorithm;
	
	private DigestAlgorithm(SinekartaDsAlgorithm algorithm) {
		this.algorithm 	= algorithm;
	}
	
	public void validate ( 
			byte[] content, 
			byte[] fingerPrint ) 
					throws GeneralSecurityException {
		
		// Evaluate the expectedFingerPrint from the given content
		byte[] expectedFingerPrint;
		try {
			MessageDigest digester = MessageDigest.getInstance ( 
					algorithm.getName(), "BC" );
			digester.update(content);
			expectedFingerPrint = digester.digest();
		} catch (NoSuchAlgorithmException e) {
			// never thrown, algorithm provided by enum
			throw new RuntimeException(e);
		}
		
		// Return true if the fingerPrint matches with the content
		if( !Arrays.equals ( fingerPrint, expectedFingerPrint ) ) {
			throw new GeneralSecurityException(String.format(
					"the fingerPrint doesn't match with the given content \n" +
					"  - expected: %s\n  -    found: %s", fingerPrint, expectedFingerPrint ));
		}
	}
	
	@Override
	public String toString() {
		return algorithm.toString();
	}
	
	
	
	// -----
	// --- Factory methods
	// -
		
	/**
	 * Return the DigestAlgorithm matching with the given algorithmDescriptor.
	 * Accepted values: \n" +
	 * <ul>
	 * <li> OidWrapper			  oidWrapper
	 * <li> ASN1ObjectIdentifier oid
	 * <li> DERObjectIdentifier  oid
	 * <li> String 			  algorithmId or algorithmName
	 * </ul>
	 * @param algorithmDescriptor
	 * @return the matching DigestAlgorithm
	 * @throws IllegalArgumentException if algorithmDescriptor is invalid or doesn't match  
	 * with any DigestAlgorithm
	 */
	public static DigestAlgorithm getInstance(Object algorithmDescriptor) throws IllegalArgumentException {
		// verify the descriptor validity
		SinekartaDsAlgorithm algorithm = SinekartaDsAlgorithm.getInstance(algorithmDescriptor);
		// search the DigestAlgorithm by name 
		return fromName(algorithm.getName());
	}
	
	/**
	 * Return the DigestAlgorithm matching with the given id.
	 * @param algorithmId
	 * @return the matching DigestAlgorithm
	 * @throws IllegalArgumentException if algorithmId doesn't match with any DigestAlgorithm
	 */
	public static DigestAlgorithm fromId(String algorithmId) throws IllegalArgumentException {
		// verify the id validity
		SinekartaDsAlgorithm algorithm = SinekartaDsAlgorithm.fromId(algorithmId);
		// search the DigestAlgorithm by name 
		return fromName(algorithm.getName());
	}
	
	/**
	 * Return the DigestAlgorithm matching with the given name.
	 * @param algorithmName
	 * @return the matching DigestAlgorithm
	 * @throws IllegalArgumentException if algorithmName doesn't match with any DigestAlgorithm
	 */
	public static DigestAlgorithm fromName(String algorithmName) throws IllegalArgumentException {
		if(StringUtils.isBlank(algorithmName)) {
			return null;
		}
		for(DigestAlgorithm algorithm : DigestAlgorithm.values()) {
			if(StringUtils.equals(algorithm.getName(), algorithmName)) {
				return algorithm;
			}
		}
		throw new IllegalArgumentException("algorithm not found: " + algorithmName);
	}
	
	
	
	// -----
	// --- OidWrapper protocol: oid comparison and enquiry
	// -

	@Override
	public boolean matchesWith(Object argumentDescriptor) {
		return algorithm.matchesWith(argumentDescriptor);
	}

	@Override
	public boolean equals(OidWrapper oidWrapper) {
		return algorithm.equals(oidWrapper);
	}

	@Override
	public String getId() {
		return algorithm.getId();
	}

	@Override
	public String[] getIds() {
		return algorithm.getIds();
	}
	
	@Override
	public String getName() {
		return algorithm.getName();
	}

}
