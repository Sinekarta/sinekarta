/*
 * Copyright (C) 2010 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.sinekartads.core.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.test.SkdsTestCase;

import xades4j.production.DataObjectReference;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.production.XadesSigningProfile;
import xades4j.properties.AllDataObjsCommitmentTypeProperty;
import xades4j.properties.DataObjectDesc;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider.KeyEntryPasswordProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider.KeyStorePasswordProvider;
import xades4j.providers.impl.KeyStoreKeyingDataProvider.SigningCertSelector;

public class XMLSignTC01_XAdES4J extends SkdsTestCase {
	
	static final Logger tracer = Logger.getLogger(XMLSignTC01_XAdES4J.class); 
	static final String SOURCE_FILE = "employeesalary.xml";

	public void test() throws Exception {
		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null ) {
			Security.addProvider(new BouncyCastleProvider());
		}
		if ( Security.getProvider(XMLDSigRIPrePost.PROVIDER_NAME) == null ) {
			Security.addProvider(new XMLDSigRIPrePost());
		}
		
		File sourceFile = getTestResource(SOURCE_FILE);
		
		String keyStorePin = "skdscip";
		char[] ksPwd = null;
		if ( StringUtils.isNotBlank(keyStorePin) ) {
			ksPwd = keyStorePin.toCharArray();
		}
		
		InputStream is = FileUtils.openInputStream(getTestResource("JENIA.p12"));
		KeyStoreType type = KeyStoreType.PKCS12_DEF;
		KeyStore keyStore = KeyStore.getInstance ( type.getType(), type.getProvider() );
		keyStore.load ( is, ksPwd );
		
		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
				keyStore.getEntry("CN=Alessandro De Prato", new PasswordProtection(ksPwd));
//		X509Certificate[] certificateChain = TemplateUtils.Cast.cast ( 
//				X509Certificate.class, privateKeyEntry.getCertificateChain() );
		X509Certificate certificate = (X509Certificate)privateKeyEntry.getCertificate();
		PrivateKey privateKey = privateKeyEntry.getPrivateKey();
		
		
		String src = sourceFile.getAbsolutePath();
		String dest = FilenameUtils.getFullPath(src) + FilenameUtils.getBaseName(SOURCE_FILE) + "_sgn.xml";
		InputStream contentIs = new FileInputStream(src);
		OutputStream signedContentOs = new FileOutputStream(dest);
		
		
	}
	
	
	public void xades4jSign( boolean applyMark ) throws Exception {
		KeyingDataProvider kp = new FileSystemKeyStoreKeyingDataProvider(
				"PKCS12-DEF",
				getTestResource("JENIA.p12").getAbsolutePath(),
	            new SigningCertSelector() {

					@Override
					public X509Certificate selectCertificate(
							List<X509Certificate> availableCertificates) {
						// TODO Auto-generated method stub
						return null;
					}
					
				},
	            new KeyStorePasswordProvider() {

					@Override
					public char[] getPassword() {
						return "skdscip".toCharArray();
					}
					
				},
	            new KeyEntryPasswordProvider() {

					@Override
					public char[] getPassword(String entryAlias,
							X509Certificate entryCert) {
						// TODO Auto-generated method stub
						return "skdscip".toCharArray();
					}
					
				},
	            true );
		
		XadesSigningProfile p;
		if ( applyMark ) {
//			p = new XadesTSigningProfile();
			throw new UnsupportedOperationException("XAdES-T signature not supported yet");
		} else {
			p = new XadesBesSigningProfile(kp);
		}
		XadesSigner signer = p.newSigner();
		
		DataObjectDesc obj = new DataObjectReference(getClass().getClassLoader().getResource("employeesalary.xml").getFile()).withDataObjectTimeStamp();
		SignedDataObjects dataObjs = new SignedDataObjects(obj).withCommitmentType(AllDataObjsCommitmentTypeProperty.proofOfOrigin());
	}
}
