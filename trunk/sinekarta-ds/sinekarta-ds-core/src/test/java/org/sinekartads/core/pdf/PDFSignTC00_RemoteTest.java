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
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sinekartads.SinekartaDsTestCase;
import org.sinekartads.core.provider.ExternalDigester;
import org.sinekartads.core.provider.ExternalSigner;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.EncryptionAlgorithm;
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.TemplateUtils;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

public class PDFSignTC00_RemoteTest extends SinekartaDsTestCase {
	
	static final Logger tracer = Logger.getLogger(PDFSignTC00_RemoteTest.class); 
	static final String SOURCE_FILE = "pippo.pdf";
	static final String DESTINATION_FILE = "pippo_sgn.pdf";

	public void test() throws Exception {
		Security.addProvider ( new BouncyCastleProvider() );
		
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
		externalSign(src, String.format(dest, 1), certificateChain, privateKey, DigestAlgorithm.SHA256.getName(),
			EncryptionAlgorithm.RSAEncryption.getName(), provider.getName(), CryptoStandard.CMS, "Test 1", "Ghent", null, null, null);
		externalSign(src, String.format(dest, 2), certificateChain, privateKey, DigestAlgorithm.SHA512.getName(),
			EncryptionAlgorithm.RSAEncryption.getName(), provider.getName(), CryptoStandard.CMS, "Test 2", "Ghent", "http://ca.signfiles.com/TSAServer.aspx", "", "");
		directSign(src, String.format(dest, 3), certificateChain, privateKey, DigestAlgorithm.SHA256.getName(),
			EncryptionAlgorithm.RSAEncryption.getName(), provider.getName(), CryptoStandard.CADES, "Test 3", "Ghent");
	}
		
	private void directSign (
			String src, 
			String dest,
			X509Certificate[] chain, 
			PrivateKey pk, 
			String digestAlgorithm, 
			String encryptionAlgorithm,
			String provider,
			CryptoStandard subfilter, 
			String reason, 
			String location ) 
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
			ExternalSigner signature = new ExternalSigner(digestAlgorithm, encryptionAlgorithm);
			MakeSignature.signDetached(
					appearance, 	// visual aspect of the signature
					digest, 		// digest generator
					signature, 		// signature generator
					chain, 			// certification chain
					null, 			// Collection<CrlClient> crlList <- the CRL list
					null, 			// OcspClient ocspClient <- Online Certificate Status Protocol 
					null, 			// TSAClient tsaClient
					0, 				// the reserved size for the signature. It will be estimated if 0
					subfilter );	// Either Signature.CMS or Signature.CADES
			tracer.info(String.format("%s signed file stored at %s", reason, dest));
		} finally {
			IOUtils.closeQuietly(os);
			if(reader != null) {
				reader.close();
			}
			if( appearance == null || appearance.isPreClosed() ) {
				if (stamper != null) {
					stamper.close();
				}
			}
		}
	}
		
	
	private void externalSign (
			String src, 
			String dest,
			X509Certificate[] chain, 
			PrivateKey privateKey, 
			String digestAlgorithm, 
			String encryptionAlgorithm,
			String provider,
			CryptoStandard subfilter, 
			String reason, 
			String location,
			String tsUrl,
			String tsUsername,
			String tsPassword ) 
					throws GeneralSecurityException, 
							IOException, 
							DocumentException {
		
		PdfReader reader = null;
		PdfStamper stamper = null;
		FileOutputStream os = null;
		PdfSignatureAppearance appearance = null;
		String signatureAlgorithm = digestAlgorithm + "with" + encryptionAlgorithm;
		Signature signature = Signature.getInstance(signatureAlgorithm);
		ExternalDigester digester = new ExternalDigester();
		ExternalSigner signer = new ExternalSigner(digestAlgorithm, encryptionAlgorithm);
		byte[] fingerPrint;
		byte[] digitalSignature;
		try {
			tracer.info("preSign: fingerPrint evaluation");
			fingerPrint = this.preSign(src, chain, subfilter, reason, location, digester, signer);
			
			tracer.info("external digitalSignature evaluation");
			signature.initSign(privateKey);
			signature.update(fingerPrint);
			digitalSignature = signature.sign();
			tracer.info("store the evaluated digitalSignature");
			signer.setDigitalSignature(digitalSignature);
			tracer.info("postSign: digitalSignature application");
			postSign(src, dest, chain, subfilter, reason, location, tsUrl, tsUsername, tsPassword, digester, signer);
			tracer.info("pdf stored at " + FilenameUtils.getName(dest));
		} finally {
			IOUtils.closeQuietly(os);
			if(reader != null) {
				reader.close();
			}
			if( appearance == null || appearance.isPreClosed() ) {
				if (stamper != null) {
					stamper.close();
				}
			}
		}
	}
		
		
	private byte[] preSign ( String src, 
							 X509Certificate[] chain, 
							 CryptoStandard subfilter,
							 String reason, 
							 String location, 
							 ExternalDigester digester, 
							 ExternalSignature signer )	throws IOException, 
							 								   DocumentException, 
							 								   GeneralSecurityException {
		// Create the stamper
		File tmpFile = new File(HexUtils.randomHex(16)+".pdf");
		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = PdfStamper.createSignature(reader, null, '\0', tmpFile);
		// Create the appearance
		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
		appearance.setReason(reason);
		appearance.setLocation(location);
		appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
		// Evaluate the digest
		MakeSignature.signDetached(appearance, digester, signer, chain, null, null, null, 0, subfilter);
		tmpFile.delete();
		return digester.getFingerPrint();
	}
	
	private void postSign ( String src, 
							String dest,
							X509Certificate[] chain,
							CryptoStandard subfilter,
							String reason, 
							String location,
							String tsUrl,
							String tsUsername,
							String tsPassword,
							ExternalDigester digester, 
							ExternalSignature signer )	throws IOException, 
															   DocumentException, 
															   GeneralSecurityException {
		TSAClient tsaClient = null;
		if ( StringUtils.isNotBlank(tsUrl) ) {
			tsaClient = new TSAClientBouncyCastle ( tsUrl, tsUsername, tsPassword );
		}
		// Create the stamper
		PdfReader reader = new PdfReader(src);
		FileOutputStream os = new FileOutputStream(dest);
		PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
		// Create the appearance
		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
		appearance.setReason(reason);
		appearance.setLocation(location);
		appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
		// Evaluate the digest
		MakeSignature.signDetached(appearance, digester, signer, chain, null, null, tsaClient, 0, subfilter);
	}
}
