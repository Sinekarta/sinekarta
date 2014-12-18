/*
 * XAdES4j - A Java library for generation and verification of XAdES signatures.
 * Copyright (C) 2010 Luis Goncalves.
 *
 * XAdES4j is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or any later version.
 *
 * XAdES4j is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with XAdES4j. If not, see <http://www.gnu.org/licenses/>.
 */
package xades4j.providers.impl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SigningCertChainException;
import xades4j.providers.SigningKeyException;
import xades4j.verification.UnexpectedJCAException;

/**
 * A ExtKeyStore-based implementation of {@code KeyingDataProvider}. The keystore is
 * loaded on first access (thread-safe).
 * <p>
 * The following procedure is done to get the signing certificate:
 * <ol>
 *  <li>Get all the X509Certificates in private key entries</li>
 *  <li>Invoke the supplied {@code SigningCertSelector} to choose the certificate and thus the entry</li>
 *  <li>Get the entry alias matching the selected certificate</li>
 *  <li>Get the certificate chain for that entry</li>
 * </ol>
 * <p>
 * The following procedure is done to get the signing key:
 * <ol>
 *  <li>Get the entry alias matching the provided certificate</li>
 *  <li>Get the protection to access that entry</li>
 *  <li>Return the entry's private key</li>
 * </ol>
 *
 * @see FileSystemExtKeyStoreKeyingDataProvider
 * @see PKCS11ExtKeyStoreKeyingDataProvider
 * @author Luís
 */
public class ExtKeyringDataProvider implements KeyingDataProvider
{

	List<X509Certificate> signingCertificateChain;
	
	public void setSigningCertificate ( X509Certificate signingCertificate ) {
		signingCertificateChain = new ArrayList<X509Certificate>();
		signingCertificateChain.add(signingCertificate);
	}
	
	public void setSigningCertificateChain ( List<X509Certificate> signingCertificateChain ) {
		this.signingCertificateChain = signingCertificateChain;
	}
    
    @Override
    public List<X509Certificate> getSigningCertificateChain() throws SigningCertChainException, UnexpectedJCAException {
        return signingCertificateChain;
    }

    @Override
    public PrivateKey getSigningKey(X509Certificate signingCert) throws SigningKeyException, UnexpectedJCAException
    {
        String signatureAlgorithm = signingCert.getSigAlgName();
        Matcher mtc = Pattern.compile("^(\\w+)with(\\w+)$").matcher ( 
        		signatureAlgorithm.replaceAll("WITH", "with") );
        
        Assert.isTrue ( mtc.find() );
        String encryptionAlgorithm = mtc.group(2);
        
        KeyPairFactory kpFactory = new KeyPairFactory ( encryptionAlgorithm );
        PrivateKey privateKey = kpFactory.createKeyPair().getPrivate();
        return privateKey;
    }
}
