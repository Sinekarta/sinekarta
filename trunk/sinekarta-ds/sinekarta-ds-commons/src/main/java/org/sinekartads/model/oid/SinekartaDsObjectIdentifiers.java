package org.sinekartads.model.oid;

public class SinekartaDsObjectIdentifiers {
	
	public static final String adig_MD2 						= "1.2.840.113549.2.2";
	public static final String adig_MD4 						= "1.2.840.113549.2.4";
	public static final String adig_MD5 						= "1.2.840.113549.2.5";
	public static final String adig_SHA1 						= "1.3.14.3.2.26";
	public static final String adig_SHA256 					= "2.16.840.1.101.3.4.2.1";
	public static final String adig_SHA384 					= "2.16.840.1.101.3.4.2.2";
	public static final String adig_SHA512 					= "2.16.840.1.101.3.4.2.3";
	public static final String adig_SHA224 					= "2.16.840.1.101.3.4.2.4";
	
	
	public static final String aenc_DSA 						= "1.2.840.10040.4.1";
	
	public static final String asgn_RSAEnchription 			= "1.2.840.113549.1.1.1";
    public static final String asgn_MD2withRSA      		= "1.2.840.113549.1.1.2";
    public static final String asgn_MD4withRSA 			   = "1.2.840.113549.1.1.3";
    public static final String asgn_MD5withRSA      = "1.2.840.113549.1.1.4";
    public static final String asgn_SHA1withRSA     = "1.2.840.113549.1.1.5";
    public static final String asgn_SHA256withRSA   = "1.2.840.113549.1.1.11";
    public static final String asgn_SHA384withRSA   = "1.2.840.113549.1.1.12";
    public static final String asgn_SHA512withRSA   = "1.2.840.113549.1.1.13";
    public static final String asgn_SHA224withRSA   = "1.2.840.113549.1.1.14";
    public static final String asgn_SHA1withECDSA   = "1.2.840.10045.4.1";
    public static final String asgn_SHA224withECDSA = "1.2.840.10045.4.3.1";
    public static final String asgn_SHA256withECDSA = "1.2.840.10045.4.3.2";
    public static final String asgn_SHA384withECDSA = "1.2.840.10045.4.3.3";
    public static final String asgn_SHA512withECDSA = "1.2.840.10045.4.3.4";
    public static final String asgn_SHA1WithDSA        	= "1.3.14.3.2.27";
    public static final String asgn_SHA224withDSA         = "2.16.840.1.101.3.4.3.1";
    public static final String asgn_SHA256withDSA         = "2.16.840.1.101.3.4.3.2";
    public static final String asgn_SHA384withDSA         = "2.16.840.1.101.3.4.3.3";
    public static final String asgn_SHA512withDSA         = "2.16.840.1.101.3.4.3.4";
	
	
	public static final String cnt_signedData 				= "1.2.840.113549.1.7.2";
	public static final String cnt_timestampedData			= "1.2.840.113549.1.9.16.1.31";
	
	public static final String attr_signingTime  				= "1.2.840.113549.1.9.5";
	public static final String attr_qcStatements	 			= "1.3.6.1.5.5.7.1.3";
	public static final String attr_subjectKeyIdentifier 		= "2.5.29.14";
	public static final String attr_keyUsage  				= "2.5.29.15";
	public static final String attr_privateKeyUsagePeriod 	= "2.5.29.16";
	public static final String attr_subjectAltName  			= "2.5.29.17";
	public static final String attr_issuerAltName  			= "2.5.29.18";
	public static final String attr_reason  					= "2.5.29.21";
	public static final String attr_cRLDistributionPoints 	= "2.5.29.31";
	public static final String attr_certificatePolicies 		= "2.5.29.32";
	public static final String attr_authorityKeyIdentifier 	= "2.5.29.35";
	public static final String attr_extKeyUsage 				= "2.5.29.37";
    public static final String attr_emailAddress 				= "1.2.840.113549.1.9.1";
    public static final String attr_timeStampToken 			= "1.2.840.113549.1.9.16.2.14";
    
    public static final String ext_qcStatements 				= "1.3.6.1.5.5.7.1.3";

    public static final String dn_commonName 					= "2.5.4.3";
	public static final String dn_surname  					= "2.5.4.4";
	public static final String dn_serialNumber  				= "2.5.4.5";
	public static final String dn_countryName  				= "2.5.4.6";
	public static final String dn_localityName  				= "2.5.4.7";
	public static final String dn_stateOrProvinceName  		= "2.5.4.8";
	public static final String dn_streetAddress  				= "2.5.4.9";
	public static final String dn_organizationName  			= "2.5.4.10";
	public static final String dn_organizationUnitName 		= "2.5.4.11";
	public static final String dn_title						= "2.5.4.12";
	public static final String dn_givenName 					= "2.5.4.42";
    public static final String dn_initials 					= "2.5.4.43";
    public static final String dn_generation 					= "2.5.4.44";
    public static final String dn_uniqueID 					= "2.5.4.45";
    public static final String dn_emailAddress 				= "1.2.840.113549.1.9.1";
    public static final String dn_qcStatements				= "1.3.6.1.5.5.7.1.3";

    // TODO unserstand if they have to be used as attributes or dns or both of them
    public static final String domainComponent 				= "0.9.2342.19200300.100.1.25";
    public static final String userID 							= "0.9.2342.19200300.100.1.1";
	
}
