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
package org.sinekartads.core.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sinekartads.core.provider.ExternalDigester;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.test.SkdsTestCase;
import org.sinekartads.util.TemplateUtils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

public class PDFSignTC00_HelloWorld extends SkdsTestCase {
	
	static final Logger tracer = Logger.getLogger(PDFSignTC00_HelloWorld.class); 
	static final String SOURCE_FILE = "pippo.pdf";
	static final String DESTINATION_FILE = "pippo_sgn.pdf";

	public void test() throws Exception {
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
		
		String userAlias = Identity.ALESSANDRO.alias;
		
		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
				keyStore.getEntry("CN="+userAlias, new PasswordProtection(ksPwd));
		X509Certificate[] certificateChain = TemplateUtils.Cast.cast ( 
				X509Certificate.class, privateKeyEntry.getCertificateChain() );
		PrivateKey privateKey = privateKeyEntry.getPrivateKey();
		
		
		String src = sourceFile.getAbsolutePath();
		String dest = FilenameUtils.getFullPath(src) + FilenameUtils.getBaseName(SOURCE_FILE) + "_signed_%2d.pdf";
		Provider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		
		tracer.info(String.format("Original document stored at %s", src));
		sign(src, String.format(dest, 1), certificateChain, privateKey, DigestAlgorithms.SHA256,
				provider.getName(), CryptoStandard.CMS, "Test 1", "Ghent");
		sign(src, String.format(dest, 2), certificateChain, privateKey, DigestAlgorithms.SHA512,
				provider.getName(), CryptoStandard.CMS, "Test 2", "Ghent");
		sign(src, String.format(dest, 3), certificateChain, privateKey, DigestAlgorithms.SHA256,
				provider.getName(), CryptoStandard.CADES, "Test 3", "Ghent");
		sign(src, String.format(dest, 4), certificateChain, privateKey, DigestAlgorithms.RIPEMD160,
				provider.getName(), CryptoStandard.CADES, "Test 4", "Ghent");
	}
		
	private void sign(
			String src, 
			String dest,
			X509Certificate[] chain, 
			PrivateKey pk, 
			String digestAlgorithm, 
			String provider,
			CryptoStandard sigType, 
			String reason, 
			String location) 
					throws GeneralSecurityException, 
							IOException, 
							DocumentException {
		
		PdfReader reader = null;
		PdfStamper stamper = null;
		FileOutputStream os = null;
		PdfSignatureAppearance appearance = null;
		try {
			// Creating the reader and the stamper
			reader = new PdfReader(src);
			os = new FileOutputStream(dest);
			// the third parameter, pdfVersion, at '\0' means "keep the same version of the original file"
			stamper = PdfStamper.createSignature(reader, os, '\0');
			// Creating the appearance
			appearance = stamper.getSignatureAppearance();
			appearance.setReason(reason);
			appearance.setLocation(location);
			// the signature will appear at the given place of the first page and stored into existing "sig"-named field 
			appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
			// Creating the signature
			ExternalDigest digest = new ExternalDigester();
			ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);
			MakeSignature.signDetached(
					appearance, 	// visual aspect of the signature
					digest, 		// digest generator
					signature, 		// signature generator
					chain, 			// certification chain
					null, 			// Collection<CrlClient> crlList <- the CRL list
					null, 			// OcspClient ocspClient <- Online Certificate Status Protocol 
					null, 			// TSAClient tsaClient
					0, 				// the reserved size for the signature. It will be estimated if 0
					sigType);		// Either Signature.CMS or Signature.CADES
			tracer.info(String.format("%s signed file stored at %s", reason, dest));
		} finally {
			IOUtils.closeQuietly(os);
//			if(appearance != null) {
//				appearance.close();
//			}
			if(stamper != null) {
				stamper.close();
			}
			if(reader != null) {
				reader.close();
			}
		}
	}

	
//	// FIXME: temporal override to easily test the signatures with Dike
//	public File getTestResource(String name) {
//		return new File("/home/adeprato/Scrivania/file firmati JENIA/"+name);
//	}
}
