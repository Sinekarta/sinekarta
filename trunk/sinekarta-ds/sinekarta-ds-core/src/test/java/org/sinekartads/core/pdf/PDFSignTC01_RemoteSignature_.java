///*
// * Copyright (C) 2010 Jenia Software.
// *
// * This file is part of Sinekarta
// *
// * Sinekarta is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Sinekarta is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// */
//package org.sinekartads.core.pdf;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.GeneralSecurityException;
//import java.security.KeyPair;
//import java.security.KeyStore;
//import java.security.PrivateKey;
//import java.security.Provider;
//import java.security.Security;
//import java.security.Signature;
//import java.security.KeyStore.PasswordProtection;
//import java.security.cert.X509Certificate;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.FilenameUtils;
//import org.apache.commons.lang.math.RandomUtils;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Logger;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.sinekartads.SinekartaDsTestCase;
//import org.sinekartads.model.domain.KeyStoreType;
//import org.sinekartads.model.oid.EncryptionAlgorithm;
//import org.sinekartads.model.oid.DigestAlgorithm;
//import org.sinekartads.core.provider.ExternalDigester;
//import org.sinekartads.core.provider.ExternalSigner;
//import org.sinekartads.core.provider.KeyPairFactory;
//import org.sinekartads.util.HexUtils;
//import org.sinekartads.util.TemplateUtils;
//import org.sinekartads.util.x509.X509Utils;
//
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfSignatureAppearance;
//import com.itextpdf.text.pdf.PdfStamper;
//import com.itextpdf.text.pdf.security.MakeSignature;
//import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
//
//public class PDFSignTC01_RemoteSignature_ extends SinekartaDsTestCase {
//	
//	static Logger tracer = Logger.getLogger(PDFSignTC01_RemoteSignature_.class);
//	
//	// -----
//	// --- General configuration
//	// -
//	
//	// main options, change to customize the test
//	static final int  TESTER_INSTANCES = 20;		
//	static final Long TESTER_TTL = 5000l;				// null to disable the timer
//	static final Long GENERATED_FILE_LIMIT = 200l;		// null to disable the limit
//	
//	// configuration templates, comment or add more to customize the test
//	Object[][] confTemplate =  new Object[][] {		// the last 3 arguments will be evaluated during the identities generation
//			{DigestAlgorithm.SHA1, 	 EncryptionAlgorithm.DSA,  512, CryptoStandard.CMS,		null,	null},
//			{DigestAlgorithm.SHA256, EncryptionAlgorithm.DSA, 1024, CryptoStandard.CADES,	null,	null},
//			{DigestAlgorithm.SHA1, 	 EncryptionAlgorithm.RSAEncryption, 1024, CryptoStandard.CMS,		null,	null},
//			{DigestAlgorithm.SHA256, EncryptionAlgorithm.RSAEncryption, 2048, CryptoStandard.CADES,	null,	null}	};
//	
//	// other options
//	static final String SOURCE_FILE = "pippo.pdf";
//	static final String SUMMARY_FILE = "summary.txt";
//
//
//
//
//	final Logger log;
//	final int number;
//	final String digestAlgorithm;
//	final String cipherAlgorithm;
//	final int keySize;
//	final PrivateKey privateKey;		
//	final X509Certificate[] chain;
//	final CryptoStandard subfilter;
//	final String src;
//	final String signatureAlgorithm;
//	final ExternalSigner remoteSignature;
//	final Signature signature;
//	final long delay;
//	
//	boolean running;
//
//	public ExternalSignatureTester(int number, String digestAlgorithm, String cipherAlgorithm, int keySize,
//			PrivateKey privateKey, X509Certificate[] chain, CryptoStandard subfilter, String src) 
//					throws Exception {
//		this.number 				= number;
//		this.digestAlgorithm 		= digestAlgorithm;
//		this.cipherAlgorithm 		= cipherAlgorithm;
//		this.keySize				= keySize;
//		this.privateKey 			= privateKey;
//		this.chain 					= chain;
//		this.subfilter 				= subfilter;
//		this.src 					= src;
//		this.signatureAlgorithm 	= digestAlgorithm + "with" + cipherAlgorithm;
//		this.signature 				= Signature.getInstance(signatureAlgorithm);
//		this.log 					= Logger.getLogger(String.format("%s%02d - %13s",getClass().getName(), number, signatureAlgorithm));
//		this.delay 					= (RandomUtils.nextInt()%6) * 50 + 200;
//		
//		ExternalSigner remoteSignature = remoteSignatures.get(signatureAlgorithm);
//		if(remoteSignature == null) {
//			remoteSignature = ExternalSigner.getInstance(digestAlgorithm, cipherAlgorithm);
//			remoteSignatures.put(signatureAlgorithm, remoteSignature);
//		}
//		this.remoteSignature = remoteSignature;
//	}
//	
//	public void sign() {
//		try {
//			log.info("started");
//			String dest;
//			String reason = String.format("test%02d - %swith%s", 
//					number, digestAlgorithm, cipherAlgorithm);
//			String location = "Casalecchio di Reno";
//		
//			byte[] fingerPrint;
//			byte[] digitalSignature;
//			running = true;
//			while(running) {
//				dest = String.format("%s%s_signed_%02d_%02d.pdf", 
//						FilenameUtils.getFullPath(src), FilenameUtils.getBaseName(src), number, fileCounter);
//				log.info("preSign: fingerPrint evaluation");
//				fingerPrint = this.preSign(src, reason, location);
//				log.info("external digitalSignature evaluation");
//				signature.initSign(privateKey);
//				signature.update(fingerPrint);
//				digitalSignature = signature.sign();
//				log.info(String.format("wait for %3dms", delay));
//				log.info("store the evaluated digitalSignature");
//				remoteSignature.setDigitalSignature(digitalSignature);
//				log.info("postSign: digitalSignature application");
//				postSign(src, dest, reason, location);
//				log.info("pdf stored at " + FilenameUtils.getName(dest));
//			}
//		} catch(InterruptedException e) {
//			log.info("interrupted by JRE");
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			log.info("stopped");
//		}
//	}
//	
//	private byte[] preSign(String src, String reason, String location)
//			throws IOException, DocumentException, GeneralSecurityException {
//		// Create the stamper
//		File tmpFile = new File(HexUtils.randomHex(16)+".pdf");
//		PdfReader reader = new PdfReader(src);
//		PdfStamper stamper = PdfStamper.createSignature(reader, null, '\0', tmpFile);
//		// Create the appearance3
//		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//		appearance.setReason(reason);
//		appearance.setLocation(location);
//		appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
//		// Evaluate the digest
//		MakeSignature.signDetached(appearance, remoteDigest, remoteSignature, chain, null, null, null, 0, subfilter);
//		tmpFile.delete();
//		return remoteDigest.getFingerPrint();
//	}
//	
//	private void postSign(String src, String dest, String reason, String location)
//			throws IOException, DocumentException, GeneralSecurityException {
//		// Create the stamper
//		PdfReader reader = new PdfReader(src);
//		FileOutputStream os = new FileOutputStream(dest);
//		PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
//		// Create the appearance
//		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//		appearance.setReason(reason);
//		appearance.setLocation(location);
//		appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
//		// Evaluate the digest
//		MakeSignature.signDetached(appearance, remoteDigest, remoteSignature, chain, null, null, null, 0, subfilter);
//	}
//
//}
