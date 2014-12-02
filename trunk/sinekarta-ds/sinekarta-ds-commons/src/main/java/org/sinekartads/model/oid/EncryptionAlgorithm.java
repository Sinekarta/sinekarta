package org.sinekartads.model.oid;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.util.HexUtils;

public enum EncryptionAlgorithm implements Serializable, OidWrapper {
	
	DSA					(SinekartaDsAlgorithm.DSA),	
	RSAEncryption		(SinekartaDsAlgorithm.RSAEncryption, // FIXME remove these prefixes if possible
								HexUtils.encodeHex(new byte[] { 0x30, 0x1f, 0x30, 0x07, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x04,	 0x14 }),
								HexUtils.encodeHex(new byte[] { 0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 }) ),

//	CMS3DESwrap		(SinekartaDsAlgorithm.CMS3DESwrap),
//	CMSRC2wrap			(SinekartaDsAlgorithm.CMSRC2wrap),
//	aes128_wrap		(SinekartaDsAlgorithm.aes128_wrap),
//	aes192_wrap		(SinekartaDsAlgorithm.aes192_wrap),
//	aes256_wrap		(SinekartaDsAlgorithm.aes256_wrap),
//	camellia128_wrap	(SinekartaDsAlgorithm.camellia128_wrap),
//	camellia192_wrap	(SinekartaDsAlgorithm.camellia192_wrap),
//	camellia256_wrap	(SinekartaDsAlgorithm.camellia256_wrap),
//	cmsSeed_wrap		(SinekartaDsAlgorithm.cmsSeed_wrap),
//	des_EDE3_CBC		(SinekartaDsAlgorithm.des_EDE3_CBC),
//	aes					(SinekartaDsAlgorithm.aes),
//	aes128_CBC			(SinekartaDsAlgorithm.aes128_CBC),
//	aes192_CBC			(SinekartaDsAlgorithm.aes192_CBC),
//	aes256_CBC			(SinekartaDsAlgorithm.aes256_CBC),
//	RC2_CBC				(SinekartaDsAlgorithm.RC2_CBC),
	;
    
	final SinekartaDsAlgorithm algorithm;
	
	private EncryptionAlgorithm(SinekartaDsAlgorithm algorithm) {
		this(algorithm, null, null);
	}
	
	private EncryptionAlgorithm(SinekartaDsAlgorithm algorithm, String prefixSHA1, String prefixSHA256) {
		this.algorithm 		= algorithm;
		this.prefixSHA1 	= HexUtils.decodeHex(prefixSHA1);
		this.prefixSHA256 	= HexUtils.decodeHex(prefixSHA256);
	}
	
	@Override
	public String toString() {
		return algorithm.toString();
	}
	
	
	
	// -----
	// --- Instance retrieving
	// -
		
	/**
	 * Return the CipherAlgorithm matching with the given algorithmDescriptor.
	 * Accepted values: \n" +
	 * <ul>
	 * <li> OidWrapper			  oidWrapper
	 * <li> ASN1ObjectIdentifier oid
	 * <li> DERObjectIdentifier  oid
	 * <li> String 			  algorithmId or algorithmName
	 * </ul>
	 * @param algorithmDescriptor
	 * @return the matching CipherAlgorithm
	 * @throws IllegalArgumentException if algorithmDescriptor is invalid or doesn't match  
	 * with any CipherAlgorithm
	 */
	public static EncryptionAlgorithm getInstance(Object algorithmDescriptor) throws IllegalArgumentException {
		// verify the descriptor validity
		SinekartaDsAlgorithm algorithm = SinekartaDsAlgorithm.getInstance(algorithmDescriptor);
		// search the CipherAlgorithm by name 
		return fromName(algorithm.getName());
	}
	
	/**
	 * Return the CipherAlgorithm matching with the given id.
	 * @param algorithmId
	 * @return the matching CipherAlgorithm
	 * @throws IllegalArgumentException if algorithmId doesn't match with any CipherAlgorithm
	 */
	public static EncryptionAlgorithm fromId(String algorithmId) throws IllegalArgumentException {
		// verify the id validity
		SinekartaDsAlgorithm algorithm = SinekartaDsAlgorithm.fromId(algorithmId);
		// search the CipherAlgorithm by name 
		return fromName(algorithm.getName());
	}
	
	/**
	 * Return the CipherAlgorithm matching with the given name.
	 * @param algorithmName
	 * @return the matching CipherAlgorithm
	 * @throws IllegalArgumentException if algorithmName doesn't match with any CipherAlgorithm
	 */
	public static EncryptionAlgorithm fromName(String algorithmName) throws IllegalArgumentException {
		if(StringUtils.isBlank(algorithmName)) {
			return null;
		}
		for(EncryptionAlgorithm algorithm : EncryptionAlgorithm.values()) {
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
	/*
	 * (non-Javadoc)
	 * @see org.sinekartads.oid.OidWrapper#matchesWith(java.lang.Object)
	 */
	public boolean matchesWith(Object argumentDescriptor) {
		return algorithm.matchesWith(argumentDescriptor);
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see org.sinekartads.oid.OidWrapper#equals(org.sinekartads.oid.OidWrapper)
	 */
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
	
	
	
	// -----
	// --- FingerPrint prefixes
	// -
	
	final byte[] prefixSHA1;
	final byte[] prefixSHA256;
	
	public byte[] getPrefixSHA1() {
		return prefixSHA1;
	}
	
	public byte[] getPrefixSHA256() {
		return prefixSHA256;
	}
}
