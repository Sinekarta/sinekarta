package org.sinekartads.model.domain;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.sinekartads.model.oid.DomainName;
import org.sinekartads.util.DNParser;

public class CertificateInfo 
		extends X509Certificate 
				implements Serializable {
	
	private static final long serialVersionUID = -1846515110310384156L;
	
	private final X509Certificate cert;
	private final X509Certificate[] certificateChain;
	
	public CertificateInfo (X509Certificate ... certificateChain) {		
		
		// Embed a certificate and keep a reference of the source which it belongs to
		this.cert = certificateChain[0];
		this.certificateChain = certificateChain;
	}
	
	public boolean equals( X509Certificate certificate ) {
		// Return true if the id built on this certificate matches with the given certificate
		return getId().match(certificate);
	}
	
	@Override
	public String toString() {
		return getSubjectAlias();
	}
	
	public X509Certificate getRawX509Certificate() {
		return cert;
	}
	
	public CertificateId getId() {
		// Return a CertificateId able to identify uniquely this certificate
		return CertificateIdFactory.getCertificateId ( this );
	}
	
	
	
	// -----
	// --- Direct access to credentials' domain names 
	// -

	public String getSubjectAlias() {
		return DNParser.parseName( getSubjectX500Principal() );
	}
	
	public String getEmailAddress() {
		return DNParser.parse( getSubjectX500Principal(), DomainName.emailAddress );
	}
	
	public String getOrganizationUnitName() {
		return DNParser.parse( getSubjectX500Principal(), DomainName.organizationUnitName );
	}

	public String getOrganizationName() {
		return DNParser.parse( getSubjectX500Principal(), DomainName.organizationName );
	}
	
	public String getLocalityName() {
		return DNParser.parse( getSubjectX500Principal(), DomainName.localityName );
	}	
	
	public String getStateOrProvinceName() {
		return DNParser.parse( getSubjectX500Principal(), DomainName.stateOrProvinceName );
	}
	
	public String getCountryName() {
		return DNParser.parse( getSubjectX500Principal(), DomainName.countryName );
	}
	
	public String getQcStatements() {
		return DNParser.parse( getSubjectX500Principal(), DomainName.qcStatements );
	}
	
	public String getIssuerAlias() {
		return DNParser.parseName( getIssuerX500Principal() );
	}
	
	
	
	// -----
	// --- Certificate validation
	// -
	
	public X509Certificate[] getChain() 
					throws IllegalStateException {
		
		return certificateChain;
	}
	
	

	// -----
	// --- X509Certificate methods wrapping
	// -

	 /**
     * Checks that the certificate is currently valid. It is if
     * the current date and time are within the validity period given in the
     * certificate.
     * <p>
     * The validity period consists of two date/time values: 
     * the first and last dates (and times) on which the certificate 
     * is valid. It is defined in
     * ASN.1 as:
     * <pre>
     * validity             Validity<p>
     * Validity ::= SEQUENCE {
     *     notBefore      CertificateValidityDate,
     *     notAfter       CertificateValidityDate }<p>
     * CertificateValidityDate ::= CHOICE {
     *     utcTime        UTCTime,
     *     generalTime    GeneralizedTime }
     * </pre>
     * 
     * @exception CertificateExpiredException if the certificate has expired.
     * @exception CertificateNotYetValidException if the certificate is not
     * yet valid.
     */
	@Override
    public void checkValidity()
        throws CertificateExpiredException, CertificateNotYetValidException {
    	cert.checkValidity();
    }

    /**
     * Checks that the given date is within the certificate's
     * validity period. In other words, this determines whether the 
     * certificate would be valid at the given date/time.
     *
     * @param date the Date to check against to see if this certificate
     *        is valid at that date/time.
     *
     * @exception CertificateExpiredException if the certificate has expired
     * with respect to the <code>date</code> supplied.
     * @exception CertificateNotYetValidException if the certificate is not
     * yet valid with respect to the <code>date</code> supplied.
     * 
     * @see #checkValidity()
     */
    @Override
    public void checkValidity(Date date)
        throws CertificateExpiredException, CertificateNotYetValidException {
    	cert.checkValidity(date);
    }

    /**
     * Gets the <code>version</code> (version number) value from the
     * certificate.
     * The ASN.1 definition for this is:
     * <pre>
     * version  [0] EXPLICIT Version DEFAULT v1<p>
     * Version ::=  INTEGER  {  v1(0), v2(1), v3(2)  }
     * </pre>
     * @return the version number, i.e. 1, 2 or 3.
     */
    @Override
    public int getVersion() {
    	return cert.getVersion();
    }

    /**
     * Gets the <code>serialNumber</code> value from the certificate.
     * The serial number is an integer assigned by the certification
     * authority to each certificate. It must be unique for each
     * certificate issued by a given CA (i.e., the issuer name and
     * serial number identify a unique certificate).
     * The ASN.1 definition for this is:
     * <pre>
     * serialNumber     CertificateSerialNumber<p>
     * 
     * CertificateSerialNumber  ::=  INTEGER
     * </pre>
     *
     * @return the serial number.
     */
    @Override
    public BigInteger getSerialNumber() {
    	return cert.getSerialNumber();
    }

    /**
     * <strong>Denigrated</strong>, replaced by {@linkplain
     * #getIssuerX500Principal()}. This method returns the <code>issuer</code>
     * as an implementation specific Principal object, which should not be
     * relied upon by portable code.
     *
     * <p>
     * Gets the <code>issuer</code> (issuer distinguished name) value from 
     * the certificate. The issuer name identifies the entity that signed (and
     * issued) the certificate. 
     * 
     * <p>The issuer name field contains an
     * X.500 distinguished name (DN).
     * The ASN.1 definition for this is:
     * <pre>
     * issuer    Name<p>
     *
     * Name ::= CHOICE { RDNSequence }
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     * RelativeDistinguishedName ::=
     *     SET OF AttributeValueAssertion
     *
     * AttributeValueAssertion ::= SEQUENCE {
     *                               AttributeType,
     *                               AttributeValue }
     * AttributeType ::= OBJECT IDENTIFIER
     * AttributeValue ::= ANY
     * </pre>
     * The <code>Name</code> describes a hierarchical name composed of
     * attributes,
     * such as country name, and corresponding values, such as US.
     * The type of the <code>AttributeValue</code> component is determined by
     * the <code>AttributeType</code>; in general it will be a 
     * <code>directoryString</code>. A <code>directoryString</code> is usually 
     * one of <code>PrintableString</code>,
     * <code>TeletexString</code> or <code>UniversalString</code>.
     * 
     * @return a Principal whose name is the issuer distinguished name.
     */
    @Override
    public Principal getIssuerDN() {
    	return cert.getIssuerDN();
    }

    /**
     * Returns the issuer (issuer distinguished name) value from the
     * certificate as an <code>X500Principal</code>. 
     * <p>
     * It is recommended that subclasses override this method.
     *
     * @return an <code>X500Principal</code> representing the issuer
     *		distinguished name
     * @since 1.4
     */
    @Override
    public X500Principal getIssuerX500Principal() {
        return cert.getIssuerX500Principal();
    }

    /**
     * <strong>Denigrated</strong>, replaced by {@linkplain
     * #getSubjectX500Principal()}. This method returns the <code>subject</code>
     * as an implementation specific Principal object, which should not be
     * relied upon by portable code.
     *
     * <p>
     * Gets the <code>subject</code> (subject distinguished name) value 
     * from the certificate.  If the <code>subject</code> value is empty,
     * then the <code>getName()</code> method of the returned
     * <code>Principal</code> object returns an empty string ("").
     *
     * <p> The ASN.1 definition for this is:
     * <pre>
     * subject    Name
     * </pre>
     * 
     * <p>See {@link #getIssuerDN() getIssuerDN} for <code>Name</code> 
     * and other relevant definitions.
     * 
     * @return a Principal whose name is the subject name.
     */
    @Override
    public Principal getSubjectDN() {
    	return cert.getSubjectDN();
    }

    /**
     * Returns the subject (subject distinguished name) value from the
     * certificate as an <code>X500Principal</code>.  If the subject value
     * is empty, then the <code>getName()</code> method of the returned
     * <code>X500Principal</code> object returns an empty string ("").
     * <p>
     * It is recommended that subclasses override this method.
     *
     * @return an <code>X500Principal</code> representing the subject
     *		distinguished name
     * @since 1.4
     */
    @Override
    public X500Principal getSubjectX500Principal() {
        return cert.getIssuerX500Principal();
    }

    /**
     * Gets the <code>notBefore</code> date from the validity period of 
     * the certificate.
     * The relevant ASN.1 definitions are:
     * <pre>
     * validity             Validity<p>
     * 
     * Validity ::= SEQUENCE {
     *     notBefore      CertificateValidityDate,
     *     notAfter       CertificateValidityDate }<p>
     * CertificateValidityDate ::= CHOICE {
     *     utcTime        UTCTime,
     *     generalTime    GeneralizedTime }
     * </pre>
     *
     * @return the start date of the validity period.
     * @see #checkValidity
     */
    @Override
    public Date getNotBefore() {
    	return cert.getNotBefore();
    }

    /**
     * Gets the <code>notAfter</code> date from the validity period of 
     * the certificate. See {@link #getNotBefore() getNotBefore}
     * for relevant ASN.1 definitions.
     *
     * @return the end date of the validity period.
     * @see #checkValidity
     */
    @Override
    public Date getNotAfter() {
    	return cert.getNotAfter(); 
    }

    /**
     * Gets the DER-encoded certificate information, the
     * <code>tbsCertificate</code> from this certificate.
     * This can be used to verify the signature independently.
     *
     * @return the DER-encoded certificate information.
     * @exception CertificateEncodingException if an encoding error occurs.
     */
    @Override
    public byte[] getTBSCertificate()
        throws CertificateEncodingException {
    	return cert.getTBSCertificate();
    }

    /**
     * Gets the <code>signature</code> value (the raw signature bits) from 
     * the certificate.
     * The ASN.1 definition for this is:
     * <pre>
     * signature     BIT STRING  
     * </pre>
     *
     * @return the signature.
     */
    @Override
    public byte[] getSignature() {
    	return cert.getSignature();
    }

    /**
     * Gets the signature algorithm name for the certificate
     * signature algorithm. An example is the string "SHA-1/DSA".
     * The ASN.1 definition for this is:
     * <pre>
     * signatureAlgorithm   AlgorithmIdentifier<p>
     * AlgorithmIdentifier  ::=  SEQUENCE  {
     *     algorithm               OBJECT IDENTIFIER,
     *     parameters              ANY DEFINED BY algorithm OPTIONAL  }
     *                             -- contains a value of the type
     *                             -- registered for use with the
     *                             -- algorithm object identifier value
     * </pre>
     * 
     * <p>The algorithm name is determined from the <code>algorithm</code>
     * OID string.
     *
     * @return the signature algorithm name.
     */
    @Override
    public String getSigAlgName() {
    	return cert.getSigAlgName();
    }

    /**
     * Gets the signature algorithm OID string from the certificate.
     * An OID is represented by a set of nonnegative whole numbers separated
     * by periods.
     * For example, the string "1.2.840.10040.4.3" identifies the SHA-1
     * with DSA signature algorithm, as per RFC 2459.
     * 
     * <p>See {@link #getSigAlgName() getSigAlgName} for 
     * relevant ASN.1 definitions.
     *
     * @return the signature algorithm OID string.
     */
    @Override
    public String getSigAlgOID() {
    	return cert.getSigAlgOID();
    }

    /**
     * Gets the DER-encoded signature algorithm parameters from this
     * certificate's signature algorithm. In most cases, the signature
     * algorithm parameters are null; the parameters are usually
     * supplied with the certificate's public key.
     * If access to individual parameter values is needed then use
     * {@link java.security.AlgorithmParameters AlgorithmParameters}
     * and instantiate with the name returned by
     * {@link #getSigAlgName() getSigAlgName}.
     * 
     * <p>See {@link #getSigAlgName() getSigAlgName} for 
     * relevant ASN.1 definitions.
     *
     * @return the DER-encoded signature algorithm parameters, or
     *         null if no parameters are present.
     */
    @Override
    public byte[] getSigAlgParams() {
    	return cert.getSigAlgParams();
    }

    /**
     * Gets the <code>issuerUniqueID</code> value from the certificate.
     * The issuer unique identifier is present in the certificate
     * to handle the possibility of reuse of issuer names over time.
     * RFC 2459 recommends that names not be reused and that
     * conforming certificates not make use of unique identifiers.
     * Applications conforming to that profile should be capable of
     * parsing unique identifiers and making comparisons.
     * 
     * <p>The ASN.1 definition for this is:
     * <pre>
     * issuerUniqueID  [1]  IMPLICIT UniqueIdentifier OPTIONAL<p>
     * UniqueIdentifier  ::=  BIT STRING
     * </pre>
     *
     * @return the issuer unique identifier or null if it is not
     * present in the certificate.
     */
    @Override
    public boolean[] getIssuerUniqueID() {
    	return cert.getIssuerUniqueID();
    }
   
    /**
     * Gets a boolean array representing bits of
     * the <code>KeyUsage</code> extension, (OID = 2.5.29.15).
     * The key usage extension defines the purpose (e.g., encipherment,
     * signature, certificate signing) of the key contained in the
     * certificate.
     * The ASN.1 definition for this is:
     * <pre>
     * KeyUsage ::= BIT STRING {
     *     digitalSignature        (0),
     *     nonRepudiation          (1),
     *     keyEncipherment         (2),
     *     dataEncipherment        (3),
     *     keyAgreement            (4),
     *     keyCertSign             (5),
     *     cRLSign                 (6),
     *     encipherOnly            (7),
     *     decipherOnly            (8) }
     * </pre>
     * RFC 2459 recommends that when used, this be marked
     * as a critical extension.
     *
     * @return the KeyUsage extension of this certificate, represented as
     * an array of booleans. The order of KeyUsage values in the array is
     * the same as in the above ASN.1 definition. The array will contain a
     * value for each KeyUsage defined above. If the KeyUsage list encoded
     * in the certificate is longer than the above list, it will not be
     * truncated. Returns null if this certificate does not
     * contain a KeyUsage extension.
     */
    @Override
    public boolean[] getKeyUsage() {
    	return cert.getKeyUsage();
    }
    
    /**
     * Gets an unmodifiable list of Strings representing the OBJECT
     * IDENTIFIERs of the <code>ExtKeyUsageSyntax</code> field of the
     * extended key usage extension, (OID = 2.5.29.37).  
     *
     * @return the ExtendedKeyUsage extension of this certificate,
     *         as an unmodifiable list of object identifiers represented
     *         as Strings. Returns null if this certificate does not
     *         contain an ExtendedKeyUsage extension.
     * @throws CertificateParsingException if the extension cannot be decoded
     * @since 1.4
     */
    @Override
    public List<String> getExtendedKeyUsage() throws CertificateParsingException {
    	return cert.getExtendedKeyUsage();
    }

    /**
     * Gets the certificate constraints path length from the
     * critical <code>BasicConstraints</code> extension, (OID = 2.5.29.19).
     *
     * @return the value of <code>pathLenConstraint</code> if the
     * BasicConstraints extension is present in the certificate and the
     * subject of the certificate is a CA, otherwise -1.
     * If the subject of the certificate is a CA and
     * <code>pathLenConstraint</code> does not appear,
     * <code>Integer.MAX_VALUE</code> is returned to indicate that there is no
     * limit to the allowed length of the certification path.
     */
    @Override
    public  int getBasicConstraints() {
    	return cert.getBasicConstraints();
    }

    /**
     * Gets an immutable collection of subject alternative names from the
     *
     * @return an immutable <code>Collection</code> of subject alternative 
     * names (or <code>null</code>)
     * @throws CertificateParsingException if the extension cannot be decoded
     * @since 1.4
     */
    @Override
    public Collection<List<?>> getSubjectAlternativeNames()
    		throws CertificateParsingException {
    	return cert.getSubjectAlternativeNames();
    }

    /**
     * Gets an immutable collection of issuer alternative names from the
     *
     * @return an immutable <code>Collection</code> of issuer alternative 
     * names (or <code>null</code>)
     * @throws CertificateParsingException if the extension cannot be decoded
     * @since 1.4
     */
    @Override
    public Collection<List<?>> getIssuerAlternativeNames()
    		throws CertificateParsingException {
    	return cert.getIssuerAlternativeNames();
    }
    
	@Override
    public boolean[] getSubjectUniqueID() {
    	return cert.getSubjectUniqueID();
    }

	@Override
	public boolean hasUnsupportedCriticalExtension() {
		return cert.hasUnsupportedCriticalExtension();
	}

	@Override
	public Set<String> getCriticalExtensionOIDs() {
		return cert.getCriticalExtensionOIDs();
	}

	@Override
	public Set<String> getNonCriticalExtensionOIDs() {
		return cert.getNonCriticalExtensionOIDs();
	}

	@Override
	public byte[] getExtensionValue(String oid) {
		return cert.getExtensionValue(oid);
	}

	@Override
	public byte[] getEncoded() throws CertificateEncodingException {
		return cert.getEncoded();
	}

	@Override
	public void verify(PublicKey key) throws CertificateException,
			NoSuchAlgorithmException, InvalidKeyException,
			NoSuchProviderException, SignatureException {
		cert.verify(key);
	}

	@Override
	public void verify(PublicKey key, String sigProvider)
			throws CertificateException, NoSuchAlgorithmException,
			InvalidKeyException, NoSuchProviderException, SignatureException {
		cert.verify(key, sigProvider);		
	}

	@Override
	public PublicKey getPublicKey() {
		return cert.getPublicKey();
	}

}
