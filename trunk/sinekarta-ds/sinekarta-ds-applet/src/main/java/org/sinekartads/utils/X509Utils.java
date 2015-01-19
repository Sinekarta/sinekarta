/*
 * Copyright (C) 2014 - 2015 Jenia Software.
 *
 * This file is part of Sinekarta-ds
 *
 * Sinekarta-ds is Open SOurce Software: you can redistribute it and/or modify
 * it under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sinekartads.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class X509Utils {
	
	// -----
	// --- Raw X509Certificate utility methods
	// -
	
	public static X509Certificate rawX509CertificateFromEncoded(byte[] encoded) throws CertificateException {
		
		// Return the X509Certificate encoded by the given byte array
		X509Certificate x509Certificate;
		if(ArrayUtils.isNotEmpty(encoded)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream is = new ByteArrayInputStream(encoded);
			x509Certificate = (X509Certificate) cf.generateCertificate(is);
		} else {
			x509Certificate = null;
		}
		return x509Certificate;
	}
	
	public static X509Certificate rawX509CertificateFromHex(String hex) throws CertificateException {
		
		// Return the X509Certificate encoded by the given hex string
		return rawX509CertificateFromEncoded(HexUtils.decodeHex(hex));
	}
	
	public static String rawX509CertificateToHex ( 
			X509Certificate certificate ) 
					throws CertificateEncodingException {
		
		// Return the hex string relative to the given X509Certificate
		String hex = null;
		if ( certificate != null ) {
			hex = HexUtils.encodeHex(certificate.getEncoded());
		}
		return hex;
	}
	
	public static X509Certificate[] rawX509CertificatesFromHex(String[] hexes) {
		try {
			List<X509Certificate> rawX509Certificates = new ArrayList<X509Certificate> ( );
			X509Certificate rawX509Certificate;
			for ( int i=0; i<hexes.length; i++) {
				rawX509Certificate = rawX509CertificateFromHex(hexes[i]);
				if ( rawX509Certificate != null ) {
					rawX509Certificates.add ( rawX509Certificate );
				}
			}
			return rawX509Certificates.toArray(new X509Certificate[rawX509Certificates.size()]);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String[] rawX509CertificatesToHex(X509Certificate[] rawX509Certificates) {
		List<String> hexes = new ArrayList<String>();
		try {
			String hex;
			for ( int i=0; i<rawX509Certificates.length; i++) {
				hex = rawX509CertificateToHex ( rawX509Certificates[i] );
				if ( StringUtils.isNotBlank(hex) ) {
					hexes.add ( hex );
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return hexes.toArray ( new String[hexes.size()] );
	}
	
	
	
	// -----
	// --- PrivateKey utility methods
	// -
	
	public static PrivateKey privateKeyFromHex (
			String privateKeyHex/*, EncryptionAlgorithm encryptionAlgorithm*/ ) {

		byte[] privateKeyEnc = HexUtils.decodeHex ( privateKeyHex );
		
		PrivateKey privateKey;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA", "SunJSSE");
	    	PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKeyEnc);
	    	privateKey = kf.generatePrivate(ks);
    	} catch (NoSuchAlgorithmException e) {
			// never thrown, algorithm granted by CipherAlgorithm
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// never thrown, using a system security provider
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			// never thrown, the algorithm must be the same for the certificate's public key
			throw new RuntimeException(e);
		} 
		
		return privateKey;
	}
	
	public static String privateKeyToHex ( PrivateKey privateKey ) {
		
		byte[] privateKeyEnc = privateKey.getEncoded();
		return HexUtils.encodeHex ( privateKeyEnc );
	}
}
