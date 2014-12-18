package xades4j.providers.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.Assert;

import xades4j.utils.DNParser;
import xades4j.utils.TemplateUtils;

public class ExtKeyStore {

	static final Provider bcProvider;
	
	static {
		bcProvider = new BouncyCastleProvider();
		if ( Security.getProvider(bcProvider.getName()) == null ) {
			Security.addProvider ( bcProvider );
		}
	}


	
    /**
     * Creates a KeyStore instance and injects the certificates inside it.
     */
    public ExtKeyStore ( X509Certificate ... certificates ) {
    	Assert.isTrue ( ArrayUtils.isNotEmpty(certificates) );
        this.provider = bcProvider;
        this.type = "ext";
        this.certificates = new HashMap<String, X509Certificate>();
        for ( X509Certificate cert : certificates ) {
        	this.certificates.put ( DNParser.parseAlias(cert.getSubjectDN()), cert );
        }
    }
    
    // The KeyStore certificates, mapped on their alias (CN subject principal attribute) 
	private final Map<String, X509Certificate> certificates;

	// KeyStore type and provider, in conformity with the KeyStore implementation
    private final String type;
    private final Provider provider;
    
    

    /**
     * Returns the provider of this keystore.
     *
     * @return the provider of this keystore.
     */
    public Provider getProvider()
    {
        return provider;
    }

    /**
     * Returns the type of this keystore.
     *
     * @return the type of this keystore.
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @deprecated do not use - no keys are expected to reside in this keyStore implementation
     */
    public Key getKey(String alias, char[] password)
        throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException
    {
        throw new UnsupportedOperationException ( 
        		"no keys are expected to reside in this keyStore implementation" );
    }

    /**
     * Returns the certificate chain associated with the given alias.
     *
     * @param alias the alias name, matching with the CN field of the target 
     * certificate's subject principal.
     *
     * @return the certificate chain (containing the only matching certificate itself
     * right now), or null if the given alias does not exist
     */
    public Certificate[] getCertificateChain(String alias)
        throws KeyStoreException
    {
        return new Certificate[] { certificates.get(alias) };
    }

    /**
     * Returns the certificate associated with the given alias.
     *
     * @param alias the alias name, matching with the CN field of the target 
     * certificate's subject principal.
     *
     * @return the certificate, or null if the given alias does not exist
     */
    public Certificate getCertificate(String alias)
        throws KeyStoreException
    {
    	 return certificates.get(alias);
    }

    /**
     * @deprecated do not use - method existing only for conformity with the KeyStore protocol
     */
    public Date getCreationDate(String alias)
        throws KeyStoreException
    {
        throw new UnsupportedOperationException ( 
        		"method existing only for conformity with the KeyStore protocol" );
    }

    /**
     * @deprecated do not use - no keys are expected to reside in this keyStore implementation
     */
    public void setKeyEntry(String alias, Key key, char[] password,
                                  Certificate[] chain)
        throws KeyStoreException
    {
    	 throw new UnsupportedOperationException ( 
         		"no keys are expected to reside in this keyStore implementation" );
    }

    /**
     * @deprecated do not use - no keys are expected to reside in this keyStore implementation
     */
    public void setKeyEntry(String alias, byte[] key,
                                  Certificate[] chain)
        throws KeyStoreException
    {
    	throw new UnsupportedOperationException ( 
         		"no keys are expected to reside in this keyStore implementation" );
    }

    /**
     * @deprecated do not use - this KeyStore implementation is static, 
     *                          the certificates are expected to be injected by the constructor
     */
    public void setCertificateEntry(String alias, Certificate cert)
        throws KeyStoreException
    {
    	throw new UnsupportedOperationException ( 
         		"this KeyStore implementation is static, the certificates are expected to be injected by the constructor" );
    }

    /**
     * @deprecated do not use - this KeyStore implementation is static, 
     *                          the certificates are expected to be injected by the constructor
     */
    public void deleteEntry(String alias)
        throws KeyStoreException
    {
    	throw new UnsupportedOperationException ( 
         		"this KeyStore implementation is static, the certificates are expected to be injected by the constructor" );
    }

    /**
     * Lists all the alias names of this keystore, they are intended to be the CN attribute of
     * the certificates' subject principal.
     *
     * @return enumeration of the alias names
     */
    public Enumeration<String> aliases()
        throws KeyStoreException
    {
        return TemplateUtils.Conversion.collectionToEnumeration( certificates.keySet() );
    }

    /**
     * Checks if the given alias exists in this keystore.
     *
     * @param alias the alias name
     *
     * @return true if the alias matches with any subject CN inside the keyStore, false otherwise
     */
    public boolean containsAlias(String alias)
        throws KeyStoreException
    {
        return certificates.containsKey(alias);
    }

    /**
     * Retrieves the number of entries in this keystore.
     *
     * @return the number of entries in this keystore
     */
    public int size()
        throws KeyStoreException
    {
        return certificates.size();
    }

    /**
     * @deprecated do not use - no keys are expected to reside in this keyStore implementation
     */
    public boolean isKeyEntry(String alias)
        throws KeyStoreException
    {
    	throw new UnsupportedOperationException ( 
         		"no keys are expected to reside in this keyStore implementation" );
    }

    /**
     * Returns true if the entry identified by the given alias
     * was created by a call to <code>setCertificateEntry</code>,
     * or created by a call to <code>setEntry</code> with a
     * <code>TrustedCertificateEntry</code>.
     *
     * @param alias the alias for the keystore entry to be checked
     *
     * @return true if the entry identified by the given alias contains a
     * trusted certificate, false otherwise.
     */
    public boolean isCertificateEntry(String alias)
        throws KeyStoreException
    {
        return certificates.containsKey(alias);
    }

    /**
     * Returns the (alias) name of the first keystore entry whose certificate
     * matches the given certificate.
     *
     * @param cert the certificate to match with.
     *
     * @return the alias name of the first entry with a matching certificate,
     * or null if no such entry exists in this keystore.
     */
    public String getCertificateAlias(Certificate cert)
        throws KeyStoreException
    {
    	Assert.isTrue ( StringUtils.equalsIgnoreCase(cert.getType(), "X509") );
    	
    	String alias = DNParser.parseAlias ( ((X509Certificate)cert).getSubjectDN() );
        if (certificates.containsKey ( alias )) {
        	alias = null;
        }
        return alias;
    }

    /**
     * @deprecated do not use - this KeyStore implementation is expected to be volatile
     */
    public void store(OutputStream stream, char[] password)
        throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException
    {
        throw new UnsupportedOperationException ( "this KeyStore implementation is expected to be volatile" );
    }

    /**
     * @deprecated do not use - this KeyStore implementation is expected to be volatile
     */
    public void store(LoadStoreParameter param)
                throws KeyStoreException, IOException,
                NoSuchAlgorithmException, CertificateException {
    	throw new UnsupportedOperationException ( "this KeyStore implementation is expected to be volatile" );
    }

    /**
     * @deprecated do not use - this KeyStore implementation is expected to be volatile
     */
    public void load(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
    	throw new UnsupportedOperationException ( "this KeyStore implementation is expected to be volatile" );
    }

    /**
     * @deprecated do not use - this KeyStore implementation is expected to be volatile
     */
    public void load(LoadStoreParameter param)
                throws IOException, NoSuchAlgorithmException,
                CertificateException {

    	throw new UnsupportedOperationException ( "this KeyStore implementation is expected to be volatile" );
    }

    /**
     * @deprecated do not use - use {@link #getCertificate(String)} or {@link #getCertificateChain(String)} instead
     */
    public Entry getEntry(String alias, ProtectionParameter protParam)
                throws NoSuchAlgorithmException, UnrecoverableEntryException,
                KeyStoreException {

    	throw new UnsupportedOperationException ( "use getCertificate(String) or getCertificateChain(String) instead" );
    }

    /**
     * @deprecated do not use - this KeyStore implementation is static, 
     * 							the certificates are expected to be injected by the constructor
     */
    public void setEntry(String alias, Entry entry,
                        ProtectionParameter protParam)
                throws KeyStoreException {
    	throw new UnsupportedOperationException ( 
         		"this KeyStore implementation is static, the certificates are expected to be injected by the constructor" );
    }

    /**
     * @deprecated do not use - this KeyStore implementation is intended to contain only certificates
     */
    public boolean
        entryInstanceOf(String alias,
                        Class<? extends KeyStore.Entry> entryClass)
        throws KeyStoreException
    {

    	throw new UnsupportedOperationException ( 
         		"this KeyStore implementation is intended to contain only certificates" );
    }

}
