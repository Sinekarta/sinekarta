package org.sinekartads.model.oid;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.TextUtils;

/**
 * a full list of algorithms can be found at
 * 		https://github.com/henryk/cyberflex-shell/blob/master/oids.txt
 * @author adeprato
 *
 */
public enum SinekartaDsAlgorithm implements Serializable, OidWrapper {
	
	// Digest algorithms
	MD2						("MD2",						SinekartaDsObjectIdentifiers.adig_MD2),
	MD4						("MD4",						SinekartaDsObjectIdentifiers.adig_MD4),
    MD5						("MD5",						SinekartaDsObjectIdentifiers.adig_MD5),
    SHA1					("SHA1", 					SinekartaDsObjectIdentifiers.adig_SHA1),
    SHA224					("SHA224", 					SinekartaDsObjectIdentifiers.adig_SHA224),
    SHA256					("SHA256", 					SinekartaDsObjectIdentifiers.adig_SHA256),
    SHA384					("SHA384", 					SinekartaDsObjectIdentifiers.adig_SHA384),
    SHA512					("SHA512",					SinekartaDsObjectIdentifiers.adig_SHA256),
	// Cipher algorithms
	DSA						("DSA",						SinekartaDsObjectIdentifiers.aenc_DSA),	
	// Digital signature algorithms
	RSAEncryption			("RSA", 					SinekartaDsObjectIdentifiers.asgn_RSAEnchription),
    MD4withRSA				("MD4withRSA", 				SinekartaDsObjectIdentifiers.asgn_MD4withRSA, "1.3.14.3.2.2"),
    MD5withRSA				("MD5withRSA", 				SinekartaDsObjectIdentifiers.asgn_MD5withRSA, "1.3.14.3.2.3"),
    MD2withRSA				("MD2withRSA", 				SinekartaDsObjectIdentifiers.asgn_MD2withRSA),
    SHA1withRSA				("SHA1withRSA", 				SinekartaDsObjectIdentifiers.asgn_SHA1withRSA, "1.3.14.3.2.29", "0.4.0.127.0.7.2.2.2.1"),
    SHA1withECDSA			("SHA1withECDSA", 			SinekartaDsObjectIdentifiers.asgn_SHA1withECDSA),
    SHA224withRSA			("SHA224withRSA", 			SinekartaDsObjectIdentifiers.asgn_SHA224withRSA),
    SHA256withRSA			("SHA256withRSA", 			SinekartaDsObjectIdentifiers.asgn_SHA256withRSA, "0.4.0.127.0.7.2.2.2.2"),
    SHA384withRSA			("SHA384withRSA", 			SinekartaDsObjectIdentifiers.asgn_SHA384withRSA),
    SHA512withRSA			("SHA512withRSA", 			SinekartaDsObjectIdentifiers.asgn_SHA512withRSA),
    
    SHA224withECDSA		("SHA224withECDSA", 		SinekartaDsObjectIdentifiers.asgn_SHA224withECDSA),
    SHA256withECDSA		("SHA256withECDSA", 		SinekartaDsObjectIdentifiers.asgn_SHA256withECDSA),
    SHA384withECDSA		("SHA384withECDSA", 		SinekartaDsObjectIdentifiers.asgn_SHA384withECDSA),
    SHA512withECDSA		("SHA512withECDSA", 		SinekartaDsObjectIdentifiers.asgn_SHA512withECDSA),
    
	SHA1withDSA			("SHA1withDSA", 			SinekartaDsObjectIdentifiers.asgn_SHA1WithDSA),
    SHA224withDSA			("SHA224withDSA", 			SinekartaDsObjectIdentifiers.asgn_SHA224withDSA),
    SHA256withDSA			("SHA256withDSA", 			SinekartaDsObjectIdentifiers.asgn_SHA256withDSA),
    SHA384withDSA			("SHA384withDSA", 			SinekartaDsObjectIdentifiers.asgn_SHA384withDSA),
    SHA512withDSA			("SHA512withDSA", 			SinekartaDsObjectIdentifiers.asgn_SHA512withDSA), 
	// Wrapper algorithms
//	CMS3DESwrap			("DESEDEWrap",				PKCSObjectIdentifiers.id_alg_CMS3DESwrap),
//	CMSRC2wrap				("RC2Wrap",					PKCSObjectIdentifiers.id_alg_CMSRC2wrap),
//	aes128_wrap			("AESWrap",					NISTObjectIdentifiers.id_aes128_wrap),
//	aes192_wrap			("AESWrap",					NISTObjectIdentifiers.id_aes192_wrap),
//	aes256_wrap			("AESWrap",					NISTObjectIdentifiers.id_aes256_wrap),
//	camellia128_wrap		("CamelliaWrap",			NTTObjectIdentifiers.id_camellia128_wrap),
//	camellia192_wrap		("CamelliaWrap",			NTTObjectIdentifiers.id_camellia192_wrap),
//	camellia256_wrap		("CamelliaWrap",			NTTObjectIdentifiers.id_camellia256_wrap),
//	cmsSeed_wrap			("SEEDWrap",				KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap),
//	des_EDE3_CBC			("DESede",					PKCSObjectIdentifiers.des_EDE3_CBC),
//	aes						("AES",						NISTObjectIdentifiers.aes),
//	aes128_CBC				("AES",						NISTObjectIdentifiers.id_aes128_CBC),
//	aes192_CBC				("AES",						NISTObjectIdentifiers.id_aes192_CBC),
//	aes256_CBC				("AES",						NISTObjectIdentifiers.id_aes256_CBC),
//	RC2_CBC					("RC2",						PKCSObjectIdentifiers.RC2_CBC),
/* ----------------------------------------------------------------------------
 * --- FIXME verify and remove 
 * ----------------------------------------------------------------------------
 * RSA/ECB/PKCS1Padding equals to RSA since PKCS1Padding is the default padding for PKCS1
 * rsaEncryption		("RSA/ECB/PKCS1Padding", 	PKCSObjectIdentifiers.rsaEncryption), 		
 * ---------------------------------------------------------------------------- 
 */	;
	
	final String name;	
	final String oid;
	final String[] oids;
    
	private SinekartaDsAlgorithm(String name, String oid,  String... otherOids) {
		this.name = name;
		
		// populate the oid (always the first added to the constructor)
		this.oid = oid;
		
		// take the other oids and grants they are unique
		Set<String> oidSet = new TreeSet<String>();
		oidSet.add(oid);
		oidSet.addAll(TemplateUtils.Conversion.arrayToList(otherOids));
		this.oids = oidSet.toArray(new String[oidSet.size()]);
	}
	
	@Override
	public String toString() {
		return String.format("%s: %s -> %s", name, TextUtils.fromArray((Object[])oids), oid);
	}
	
	
	
	// -----
	// --- Instance retrieving
	// -
	
	/**
	 * Return the SinekartaDsAlgorithm matching with the given algorithmDescriptor.
	 * Accepted values: \n" +
	 * <ul>
	 * <li> OidWrapper			  	oidWrapper
	 * <li> ASN1ObjectIdentifier 	oid
	 * <li> DERObjectIdentifier  	oid
	 * <li> String 			  		algorithmId 
	 * <li> String 			  		algorithmName 
	 * </ul>
	 * @param algorithmDescriptor
	 * @return the matching SinekartaDsAlgorithm
	 * @throws IllegalArgumentException if algorithmDescriptor is invalid or doesn't match with
	 * any SinekartaDsAlgorithm
	 */
	public static SinekartaDsAlgorithm getInstance(Object algorithmDescriptor) {
		SinekartaDsAlgorithm algorithm;
		if(algorithmDescriptor instanceof String) {
			// try to recognize the algorithm by id
			try {
				algorithm = fromId((String)algorithmDescriptor);
			} catch(IllegalArgumentException e) {
				// try to recognize the algorithm by name
				try {
					algorithm = fromName((String)algorithmDescriptor);
				} catch(IllegalArgumentException e1) {
					// error if there are no matches with either id or name
					throw new IllegalArgumentException(
							String.format("algorithmDescriptor %s doesn't match with any algorithmId or algorithmName", algorithmDescriptor));
				}							 
			}
			// proceed with a valid algorithm
		} else if(algorithmDescriptor instanceof OidWrapper) {
			algorithm = fromId(((OidWrapper)algorithmDescriptor).getId());
		} else {
			throw new IllegalArgumentException(String.format(
					"Invalid algorithmDescriptor: %s. Accepted values: \n" +
					" - [OidWrapper			  oidWrapper] \n" +
					" - [ASN1ObjectIdentifier oid] \n" +
					" - [DERObjectIdentifier  oid] \n" +
					" - [String 			  algorithmId] \n" +
					" - [String 			  algorithmName] ",
					algorithmDescriptor));
		}
		return algorithm;
	}
		
	/**
	 * Return the SinekartaDsAlgorithm matching with the given id. <br>
	 * The match will happen if the given algorithmId appears among the algorithm oids.
	 * @param algorithmId
	 * @return the matching SinekartaDsAlgorithm
	 * @throws IllegalArgumentException if algorithmId doesn't match with any SinekartaDsAlgorithm
	 */
	public static SinekartaDsAlgorithm fromId(String algorithmId) {
		if(StringUtils.isBlank(algorithmId)) {
			return null;
		}
		for(SinekartaDsAlgorithm algorithm : SinekartaDsAlgorithm.values()) {
			for(String curOid : algorithm.oids) {
				if(StringUtils.equals(curOid, algorithmId)) {
					return algorithm;
				}
			}
		}
		throw new IllegalArgumentException("algorithm not found: " + algorithmId);
	}
	
	/**
	 * Return the SinekartaDsAlgorithm matching with the given name.
	 * @param algorithmName
	 * @return the matching SinekartaDsAlgorithm
	 * @throws IllegalArgumentException if algorithmName doesn't match with any SinekartaDsAlgorithm
	 */
	public static SinekartaDsAlgorithm fromName(String algorithmName) {
		if(StringUtils.isBlank(algorithmName)) {
			return null;
		}
		for(SinekartaDsAlgorithm algorithm : SinekartaDsAlgorithm.values()) {
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
	public boolean matchesWith(Object algorithmDescriptor) throws IllegalArgumentException {
		// take the algorithmId related to the given algorithmDescriptor
		String algorithmId = getInstance(algorithmDescriptor).getId();
		// the match will happen if the algorithmId appears among the algorithm oids  
		return ArrayUtils.contains(oids, algorithmId);
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.sinekartads.oid.OidWrapper#equals( org.sinekartads.oid.OidWrapper)
	 */
	public boolean equals(OidWrapper oidWrapper) {
		// to be equivalent they have at least to contain the same number of elements
		if(oids.length != oidWrapper.getIds().length) return false; 
		// all the items in the first must be contained in the second
		Set<String> wrappedOids = new HashSet<String>(Arrays.asList(oidWrapper.getIds()));
		for(String oid : oids) {
			if( !wrappedOids.contains(oid) ) {
				return false;
			}
		}
		// if they have the same number of elements the first must now contain all the items in the second
		return true;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override 
	public String getId() {
		return oid;
	}
	
	@Override
	public String[] getIds() {
		return oids;
	}

}
