package org.sinekartads.util.x509;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.util.DNParser;
import org.sinekartads.util.TextUtils;

public class X509CertificateDescriptor {
	
	/**
	 * Self-signed certificate descriptor (the subject details will be used for the issuer as well)
	 * @param signatureAlgorithm
	 * @param subject
	 * @param subjectKeyPair
	 * @throws IllegalArgumentException
	 * @see X509CertificateDescriptor(String, String, KeyPair)
	 */
	public X509CertificateDescriptor(String signatureAlgorithm, String subject, KeyPair subjectKeyPair) throws IllegalArgumentException {
		this(signatureAlgorithm, subject, subjectKeyPair, subject, subjectKeyPair.getPublic() );
	}

	// FIXME issuerPulicKey may be not necessary
	/**
	 * Generic certificate descriptor (the issuer might even be the subject itself).
	 * Default initialization:
	 * <ul>
	 * <li>keyUsages: digitalSignature
	 * <li>random 16-chars-long hex challengePassowrd
	 * <li>renewing = false
	 * </ul>
	 * @param signatureAlgorithm
	 * @param subject a full X500Principal or simply the subject name
	 * @param subjectKeyPair
	 * @param issuer a full X500Principal or simply the issuer name
	 * @param issuerPublicKey
	 * @throws IllegalArgumentException
	 */
	public X509CertificateDescriptor(String signatureAlgorithm, String subject, KeyPair subjectKeyPair, String issuer, PublicKey issuerPublicKey) throws IllegalArgumentException {
		// verify that all the arguments are populated
		StringBuilder missing = new StringBuilder();
		if(StringUtils.isBlank(signatureAlgorithm)) {
			missing = TextUtils.appendToken(missing, "signatureAlgorithm");
		}
		if(StringUtils.isBlank(subject)) {
			missing = TextUtils.appendToken(missing, "subject");
		}
		if(subjectKeyPair == null) {
			missing = TextUtils.appendToken(missing, "subjectKeyPair");
		}
		if(StringUtils.isBlank(issuer)) {
			missing = TextUtils.appendToken(missing, "issuer");
		}
		if(issuerPublicKey == null) {
			missing = TextUtils.appendToken(missing, "issuerPublicKey");
		}
		if(StringUtils.isNotBlank(missing)) {
			throw new IllegalArgumentException(String.format("missing parameters: %s", missing));
		}
		// verify the presence and the format of mandatory CN value 
		Set<String> dns = DNParser.getDns(subject); 
		if(dns.size() > 0) {
			if ( !dns.contains("CN") ) {
				throw new IllegalArgumentException(String.format("missing CN=COMMON_NAME value into subject \"%s\"", subject));
			}
		} else {
			subject = "CN=" + subject;
		}
		dns = DNParser.getDns(issuer);
		if(dns.size() > 0) {
			if ( !dns.contains("CN") ) {
				throw new IllegalArgumentException(String.format("missing CN=COMMON_NAME value into issuer \"%s\"", issuer));
			}
		} else {
			issuer = "CN=" + issuer;
		}
		// populate the mandatory properties
		this.signatureAlgorithm 	= signatureAlgorithm; 
		this.subject 				= new X500Principal(subject);
		this.subjectKeyPair 		= subjectKeyPair;
		this.issuer 				= new X500Principal(issuer);
		this.issuerPublicKey 		= issuerPublicKey;
	}
	
	
	
	// -----
	// --- Basic bean properties
	// -
	
	// Mandatory
	final String 			signatureAlgorithm;
	final X500Principal 	subject;
	final KeyPair 			subjectKeyPair;
	final X500Principal 	issuer;
	final PublicKey			issuerPublicKey;
	// Optional
	String 					providerName;
	SecureRandom 			random;
	Date 					notBefore;
	Date 					notAfter;
	BigInteger 				serial;
	
	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public X500Principal getSubject() {
		return subject;
	}
	
	public KeyPair getSubjectKeyPair() {
		return subjectKeyPair;
	}
	
	public X500Principal getIssuer() {
		return issuer;
	}
	
	public PublicKey getIssuerPublicKey() {
		return issuerPublicKey;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public SecureRandom getRandom() {
		return random;
	}

	public void setRandom(SecureRandom random) {
		this.random = random;
	}

	public Date getNotBefore() {
		return notBefore;

	}

	public void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}
	
	public Date getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Date notAfter) {
		this.notAfter = notAfter;
	}

	public BigInteger getSerial() {
		return serial;
	}

	public void setSerial(BigInteger serial) {
		this.serial = serial;
	}
	 
}