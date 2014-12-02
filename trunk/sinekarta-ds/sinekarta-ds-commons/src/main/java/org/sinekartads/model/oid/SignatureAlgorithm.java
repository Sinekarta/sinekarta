package org.sinekartads.model.oid;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public enum SignatureAlgorithm implements Serializable, OidWrapper {
	
	// Signature instances, to be used with java.security.Signature
	MD4withRSA				(SinekartaDsAlgorithm.MD4withRSA),
    MD5withRSA				(SinekartaDsAlgorithm.MD5withRSA),
    MD2withRSA				(SinekartaDsAlgorithm.MD2withRSA),
    SHA1withRSA			(SinekartaDsAlgorithm.SHA1withRSA),
    SHA224withRSA			(SinekartaDsAlgorithm.SHA224withRSA),
    SHA256withRSA			(SinekartaDsAlgorithm.SHA256withRSA),
    SHA384withRSA			(SinekartaDsAlgorithm.SHA384withRSA),
    SHA512withRSA			(SinekartaDsAlgorithm.SHA512withRSA),
//    SHA1withRSAandMGF1	(SinekartaDsAlgorithm.SHA1withRSAandMGF1),
//    SHA256withRSAandMGF1	(SinekartaDsAlgorithm.SHA256withRSAandMGF1),
	SHA1withDSA			(SinekartaDsAlgorithm.SHA1withDSA),
    SHA224withDSA			(SinekartaDsAlgorithm.SHA224withDSA),
    SHA256withDSA			(SinekartaDsAlgorithm.SHA256withDSA),
    SHA384withDSA			(SinekartaDsAlgorithm.SHA384withDSA),
    SHA512withDSA			(SinekartaDsAlgorithm.SHA512withDSA),
//    SHA1withECDSA			(SinekartaDsAlgorithm.SHA1withECDSA),
//    SHA224withECDSA		(SinekartaDsAlgorithm.SHA224withECDSA),
//    SHA256withECDSA		(SinekartaDsAlgorithm.SHA256withECDSA),
//    SHA384withECDSA		(SinekartaDsAlgorithm.SHA384withECDSA),
//    SHA512withECDSA		(SinekartaDsAlgorithm.SHA512withECDSA),
    // WrappedCipher instances, to be used with java.security.Cipher
    RSAEncryption						(EncryptionAlgorithm.RSAEncryption),
//    DSA						(EncryptionAlgorithm.DSA),
	;
	
	final SinekartaDsAlgorithm algorithm;
	final EncryptionAlgorithm encryptionAlgorithm;
	final DigestAlgorithm digestAlgorithm;
	
	SignatureAlgorithm(OidWrapper algorithm) {
		this.algorithm 	= SinekartaDsAlgorithm.getInstance(algorithm);
		String sigAlgoName = algorithm.getName();
		Matcher mtc = Pattern.compile("(\\w+)with(\\w+)").matcher(sigAlgoName);
		DigestAlgorithm digestAlgorithm = null;
		EncryptionAlgorithm encryptionAlgorithm = null;
		try {
			if(mtc.find()) {
				digestAlgorithm = DigestAlgorithm.fromName(mtc.group(1));
				encryptionAlgorithm = EncryptionAlgorithm.fromName(mtc.group(2));
			} else {
				digestAlgorithm = null;
				encryptionAlgorithm = EncryptionAlgorithm.fromName(sigAlgoName);
			}
		} catch(IllegalArgumentException e) {
			Logger.getLogger(SignatureAlgorithm.class).warn(String.format (
					"unable to split the %s signatureAlgorithm - %s", algorithm, e.getMessage() ));
		} finally {
			this.digestAlgorithm = digestAlgorithm;
			this.encryptionAlgorithm = encryptionAlgorithm;
		}
	}
	
	@Override
	public String toString() {
		return algorithm.toString();
	}
	
	
	
	// -----
	// --- Instance retrieving
	// -
		
	/**
	 * Return the SignatureAlgorithm matching with the given algorithmDescriptor.
	 * Accepted values: \n" +
	 * <ul>
	 * <li> OidWrapper			  	oidWrapper
	 * <li> ASN1ObjectIdentifier 	oid
	 * <li> DERObjectIdentifier  	oid
	 * <li> String 			  		algorithmId or algorithmName
	 * </ul>
	 * @param algorithmDescriptors
	 * @return the matching SignatureAlgorithm
	 * @throws IllegalArgumentException if algorithmDescriptor is invalid or doesn't match  
	 * with any SignatureAlgorithm
	 */
	public static SignatureAlgorithm getInstance(Object algorithmDescriptor) throws IllegalArgumentException {
		// verify the descriptor validity
		SinekartaDsAlgorithm algorithm = SinekartaDsAlgorithm.getInstance(algorithmDescriptor);
		// search the SignatureAlgorithm by name 
		return fromName(algorithm.getName());
	}
	
	public static SignatureAlgorithm getInstance(
			DigestAlgorithm digest, 
			EncryptionAlgorithm cipher) throws IllegalArgumentException {
		
		String name = String.format("%swith%s", digest.getName(), cipher.getName());
		return fromName(name);
	}
	
	
	/**
	 * Return the SignatureAlgorithm matching with the given id.
	 * @param params
	 * @return the matching SignatureAlgorithm
	 * @throws IllegalArgumentException if algorithmId doesn't match with any SignatureAlgorithm
	 */
	public static SignatureAlgorithm fromId(String id) throws IllegalArgumentException {
	// end switch
		// verify the id validity
		SinekartaDsAlgorithm algorithm = SinekartaDsAlgorithm.fromId(id);
		// search the SignatureAlgorithm by name 
		return fromName(algorithm.getName());
	}
	
	/**
	 * Return the SignatureAlgorithm matching with the given name.
	 * @param algorithmName
	 * @return the matching SignatureAlgorithm
	 * @throws IllegalArgumentException if algorithmName doesn't match with any SignatureAlgorithm
	 */
	public static SignatureAlgorithm fromName(String algorithmName) throws IllegalArgumentException {
		if(StringUtils.isBlank(algorithmName)) {
			return null;
		}
		for(SignatureAlgorithm algorithm : SignatureAlgorithm.values()) {
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
	public boolean matchesWith(Object algorithmDescriptor) {
		return algorithm.matchesWith(algorithmDescriptor);
	}
	
	@Override
	public String getName() {
		return algorithm.getName();
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
	public boolean equals(OidWrapper oidWrapper) {
		return algorithm.equals(oidWrapper);
	}
	
	
	
	// -----
	// --- Embedded DigestAlgorithm and CipherAlgorithm recognition 
	// -
	
	/**
	 * Access to the embedded cipherAlgorithm
	 * @return the embedded cipherAlgorithm 
	 */
	public EncryptionAlgorithm getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}
	
	public DigestAlgorithm getDigestAlgorithm() {
		return digestAlgorithm;
	}

	public boolean isCipherWrapper() {
		return digestAlgorithm == null;
	}
	
	public boolean embedsDigest() {
		return digestAlgorithm != null;
	}
}
