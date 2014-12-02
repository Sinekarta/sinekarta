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
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sinekartads.model.domain.KeyStoreType;

public class XMLSignTC00_HelloWorld extends TestCase {
	
	static final Logger tracer = Logger.getLogger(XMLSignTC00_HelloWorld.class); 
	static final String SOURCE_FILE = "backup-svn.xml";

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
		
		Provider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		XmlDigitalSignatureGenerator xmlSignGen = new XmlDigitalSignatureGenerator();
		xmlSignGen.generateXMLDigitalSignature(contentIs, signedContentOs, privateKey, certificate);
//		xmlSignGen.generateXMLDigitalSignatureEnveloping(contentIs, signedContentOs, privateKey, certificate);
	}
	
	protected static File getTestResource(Class<? extends TestCase> clazz, String name) {
		String className = clazz.getName();
		className = className.substring(0, className.lastIndexOf("."));
		String path = "src/test/resources/"+className.replace(".", "/")+"/";		
		File file = new File(path + name);
		return file;
	}
	
	protected File getTestResource(String name) {
		return getTestResource(this.getClass(), name);
	}
}
