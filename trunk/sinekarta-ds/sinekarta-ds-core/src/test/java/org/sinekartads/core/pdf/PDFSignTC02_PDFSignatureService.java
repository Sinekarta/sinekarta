//package org.sinekartads.core.pdf;
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
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.security.KeyPair;
//import java.security.PrivateKey;
//import java.security.Signature;
//import java.security.cert.X509Certificate;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.FilenameUtils;
//import org.apache.commons.lang.math.RandomUtils;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.log4j.Logger;
//import org.sinekartads.SinekartaDsTestCase;
//import org.sinekartads.core.provider.ExternalDigester;
//import org.sinekartads.core.provider.ExternalSigner;
//import org.sinekartads.core.service.PDFSignatureService;
//import org.sinekartads.model.domain.CertificateInfo;
//import org.sinekartads.model.domain.DigestInfo;
//import org.sinekartads.model.domain.PDFSignatureInfo;
//import org.sinekartads.model.oid.DigestAlgorithm;
//import org.sinekartads.model.oid.SignatureAlgorithm;
//import org.sinekartads.util.HexUtils;
//import org.sinekartads.util.x509.X509CertificateDescriptor;
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
///**
// * 
// * @author andrea.tessaro
// *
// */
//public class PDFSignTC02_PDFSignatureService extends SinekartaDsTestCase {
//	
//	static Logger tracer = Logger.getLogger(PDFSignTC02_PDFSignatureService.class);
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
//			{DigestAlgorithm.SHA1, 	 CipherAlgorithm.DSA,  512, CryptoStandard.CMS,		null,	null},
//			{DigestAlgorithm.SHA256, CipherAlgorithm.DSA, 1024, CryptoStandard.CADES,	null,	null},
//			{DigestAlgorithm.SHA1, 	 CipherAlgorithm.RSA, 1024, CryptoStandard.CMS,		null,	null},
//			{DigestAlgorithm.SHA256, CipherAlgorithm.RSA, 2048, CryptoStandard.CADES,	null,	null}	};
//	
//	// other options
//	static final String SOURCE_FILE = "pippo.pdf";
//	static final String SUMMARY_FILE = "summary.txt";
//
//
//
//	// -----
//	// --- Termination closures
//	// -
//	
//	// by timeout
//	Thread timer = new Thread() {
//		public void run() {
//			try {
//				sleep(TESTER_TTL);
//				tracer.info("Timeout, the testers will be halted");				
//				while(true) {
//					// repeat many attempts if the testers didn't stop 
//					env.quitTesters();
//					sleep(1000);
//				}
//			} catch(InterruptedException e) {
//				// do nothing, just terminate
//			}
//		}
//	};
//	
//	// by generated file limit
//	long fileCounter = 0;
//	
//	synchronized void incrementFileCounter() {
//		fileCounter++;
//		if(GENERATED_FILE_LIMIT != null && fileCounter >= GENERATED_FILE_LIMIT) {
//			if(env.isRunning()) {
//				tracer.info("Generated file limit reached, the testers will quit.");
//			}
//			env.quitTesters();
//			timer.interrupt();
//		}
//	}
//	
//	
//	
//	// -----
//	// --- Test Environment configuration
//	// -
//	
//	class Environment extends Thread {
//		public void run() {
//			try {
//				File sourceFile = getTestResource(SOURCE_FILE);		
//				String src = sourceFile.getAbsolutePath();
//				
//				tracer.info(String.format("Identities generation..."));
//				Object[] testConf;
//				PrivateKey privateKey;
//				X509Certificate[] chain;
//				DigestAlgorithm digestAlgorithm;
//				CipherAlgorithm cipherAlgorithm;
//				String signatureAlgorithm;
//				CryptoStandard subfilter;
//				AdvancedKeyPairGenerator kpGen;
//				KeyPair keyPair;
//				int keySize;
//				for ( int i=0; i<confTemplate.length; i++ ) {
//					// configuration parsing
//					testConf = confTemplate[i];
//					digestAlgorithm 	= (DigestAlgorithm)testConf[0];
//					cipherAlgorithm 	= (CipherAlgorithm)testConf[1];
//					signatureAlgorithm 	= String.format("%swith%s", digestAlgorithm.getName(), cipherAlgorithm.getName());
//					keySize 			= (Integer)testConf[2];
//					// privateKey and chain generation
//					kpGen = X509Utils.getKeyPairGenerator(cipherAlgorithm);
//					keyPair = kpGen.createKeyPair(keySize, null);
//					privateKey = keyPair.getPrivate();
//					chain = ArrayUtils.toArray(X509Utils.createCertificate(
//							new X509CertificateDescriptor(signatureAlgorithm, "pippo", keyPair)));
//					// parameters update
//					testConf[4] = privateKey;
//					testConf[5] = chain;
//				}
//				tracer.info(String.format("Identities generated."));
//				
//				File workingDirectory = new File(FilenameUtils.getFullPath(src));
//				tracer.info(String.format("Working directory: %s.", workingDirectory.getAbsolutePath()));
//				tracer.info(String.format("Source file: %s.", FilenameUtils.getName(src)));
//				tracer.info(String.format("Clearing the working directory from the previous generated files..."));
//				int residualCount = 0;
//				int deletedCount = 0;
//				FilenameFilter filter = new FilenameFilter() {
//					@Override
//					public boolean accept(File dir, String name) {
//						return name.matches("\\w+_signed_\\d+_\\d+\\.pdf");
//					}
//				};
//				for ( String fileName : workingDirectory.list(filter) ) {
//					if ( new File(workingDirectory, fileName).delete() ) {
//						deletedCount++;
//					} else {
//						residualCount++;
//					}
//				}
//				File summaryFile = getTestResource(SUMMARY_FILE);
//				if(summaryFile.exists()) {
//					summaryFile.delete();
//				}
//				tracer.info(String.format("Working directory cleared, there have been deleted %d files", deletedCount));
//				if(residualCount > 0) {
//					tracer.warn(String.format("WARNING: it has been not possibile to delete %d files", residualCount));
//				}
//						
//				tracer.info(String.format("Generating %d tester instances...", GENERATED_FILE_LIMIT));
//				int number;
//	
//				// for each configuration, generate TESTER_INSTANCES instances
//				for ( number=0; number<TESTER_INSTANCES; number++ ) {
//					testConf 			= confTemplate [ RandomUtils.nextInt() % confTemplate.length ];
//					digestAlgorithm 	= (DigestAlgorithm)testConf[0];
//					cipherAlgorithm 	= (CipherAlgorithm)testConf[1];
//					keySize				= (Integer)testConf[2];
//					subfilter 			= (CryptoStandard)testConf[3];
//					privateKey 			= (PrivateKey)testConf[4];
//					chain 				= (X509Certificate[])testConf[5];
//					testers[number] 	= new ExternalSignatureTester (
//							number, 
//							digestAlgorithm.getName(), 
//							cipherAlgorithm.getName(), 
//							keySize, privateKey, chain, subfilter, src );
//				}
//				
//				tracer.info(String.format("Testers generated."));
//				
//				tracer.info(String.format("Starting all the testers..."));
//				for ( int i=0; i<testers.length; i++ ) {
//					testers[i].start();
//				}
//				tracer.info(String.format("Testers started."));
//									
//				try {
//					if ( TESTER_TTL != null ) {
//						tracer.info(String.format("Starting the timer from %dms", TESTER_TTL));
//						timer.start();
//					}
//					synchronized(this) {
//						wait();
//					}
//				} catch(InterruptedException e) {
//					// do nothing, just go to finally and quit the testers
//				} finally {
//					tracer.info(String.format("Stopping all the testers..."));
//					for ( int i=0; i<testers.length; i++ ) {
//						testers[i].quit();
//					}
//				}
//					
//				tracer.info(String.format("Waiting the testers to quit..."));
//				Thread.sleep(1000);
//				tracer.info(String.format("Testers stopped."));
//				
//				BufferedWriter writer = null;
//				try {
//					writer = new BufferedWriter(new FileWriter(getTestResource(SUMMARY_FILE)));
//					writer.write("Instances used during this test:\n");
//					writer.write(String.format("%17s: [%6s, %6s, %6s, %6s]\n", 
//							"tester", "digest", "cipher", "kiSize", "filter"));
//					for ( ExternalSignatureTester tester : testers ) {
//						writer.write(String.format(" - %s\n",tester.toString()));
//					}
//					writer.write(String.format("There have been generated %d files.\n", fileCounter));
//					tracer.info(String.format("There have been generated %d files.", fileCounter));
//				} finally {
//					IOUtils.closeQuietly(writer);
//				}
//			} catch(Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		boolean running = true;
//		public void quitTesters() {
//			synchronized(this) {
//				notify();
//				running = false;
//			}
//		}
//		
//		public boolean isRunning() {
//			return running;
//		}
//	}
//
//	
//	
//	// -----
//	// --- Tester implementation
//	// -
//	
//	static Map<String, ExternalSigner> remoteSignatures = new HashMap<String, ExternalSigner>();
//	static ExternalDigester remoteDigest = new ExternalDigester();
//	
//	class ExternalSignatureTester extends Thread {
//		final Logger log;
//		final int number;
//		final String digestAlgorithm;
//		final String cipherAlgorithm;
//		final int keySize;
//		final PrivateKey privateKey;		
//		final X509Certificate[] chain;
//		final CryptoStandard subfilter;
//		final String src;
//		final String signatureAlgorithm;
//		final ExternalSigner remoteSignature;
//		final Signature signature;
//		final long delay;
//		
//		boolean running;
//
//		public ExternalSignatureTester(int number, String digestAlgorithm, String cipherAlgorithm, int keySize,
//				PrivateKey privateKey, X509Certificate[] chain, CryptoStandard subfilter, String src) 
//						throws Exception {
//			this.number 				= number;
//			this.digestAlgorithm 		= digestAlgorithm;
//			this.cipherAlgorithm 		= cipherAlgorithm;
//			this.keySize				= keySize;
//			this.privateKey 			= privateKey;
//			this.chain 					= chain;
//			this.subfilter 				= subfilter;
//			this.src 					= src;
//			this.signatureAlgorithm 	= digestAlgorithm + "with" + cipherAlgorithm;
//			this.signature 				= Signature.getInstance(signatureAlgorithm);
//			this.log 					= Logger.getLogger(String.format("%s%02d - %13s",getClass().getName(), number, signatureAlgorithm));
//			this.delay 					= (RandomUtils.nextInt()%6) * 50 + 200;
//			
//			ExternalSigner remoteSignature = remoteSignatures.get(signatureAlgorithm);
//			if(remoteSignature == null) {
//				remoteSignature = ExternalSigner.getInstance(digestAlgorithm, cipherAlgorithm);
//				remoteSignatures.put(signatureAlgorithm, remoteSignature);
//			}
//			this.remoteSignature = remoteSignature;
//		}
//		
//		public void run() {
//			try {
//				log.info("started");
//				String dest;
//				String reason = String.format("test%02d - %swith%s", 
//						number, digestAlgorithm, cipherAlgorithm);
//				String location = "Casalecchio di Reno";
//			
//				byte[] fingerPrint;
//				byte[] digitalSignature;
//				fileCounter = 0;
//				running = true;
//				while(running) {
//					dest = String.format("%s%s_signed_%02d_%02d.pdf", 
//							FilenameUtils.getFullPath(src), FilenameUtils.getBaseName(src), number, fileCounter);
//					log.info("preSign: fingerPrint evaluation");
//					fingerPrint = this.preSign(src, reason, location);
//					log.info("external digitalSignature evaluation");
//					signature.initSign(privateKey);
//					signature.update(fingerPrint);
//					digitalSignature = signature.sign();
//					log.info(String.format("wait for %3dms", delay));
//					sleep(delay);
//					log.info("store the evaluated digitalSignature");
//					remoteSignature.setDigitalSignature(digitalSignature);
//					log.info("postSign: digitalSignature application");
//					postSign(src, dest, reason, location);
//					log.info("pdf stored at " + FilenameUtils.getName(dest));
//					incrementFileCounter();
//				}
//			} catch(InterruptedException e) {
//				log.info("interrupted by JRE");
//			} catch(Exception e) {
//				throw new RuntimeException(e);
//			} finally {
//				log.info("stopped");
//			}
//		}
//		
//		private byte[] preSign(String src, String reason, String location)
//				throws IOException, DocumentException, GeneralSecurityException {
//			// Create the stamper
//			File tmpFile = new File(HexUtils.randomHex(16)+".pdf");
//			PdfReader reader = new PdfReader(src);
//			PdfStamper stamper = PdfStamper.createSignature(reader, null, '\0', tmpFile);
//			// Create the appearance3
//			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//			appearance.setReason(reason);
//			appearance.setLocation(location);
//			appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
//			// Evaluate the digest
//			MakeSignature.signDetached(appearance, remoteDigest, remoteSignature, chain, null, null, null, 0, subfilter);
//			tmpFile.delete();
//			return remoteDigest.getFingerPrint();
//		}
//		
//		private void postSign(String src, String dest, String reason, String location)
//				throws IOException, DocumentException, GeneralSecurityException {
//			// Create the stamper
//			PdfReader reader = new PdfReader(src);
//			FileOutputStream os = new FileOutputStream(dest);
//			PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
//			// Create the appearance
//			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//			appearance.setReason(reason);
//			appearance.setLocation(location);
//			appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
//			// Evaluate the digest
//			MakeSignature.signDetached(appearance, remoteDigest, remoteSignature, chain, null, null, null, 0, subfilter);
//		}
//	
//		@Override
//		public String toString() {
//			return String.format("remoteTester%02d: [%6s, %6s, %6d, %6s]", 
//					number, digestAlgorithm, cipherAlgorithm, keySize, subfilter.name());
//		}
//		
//		public void quit() {
//			running = false;
//		}
//	}
//	
//
//	
//	// -----
//	// --- Test execution
//	// -
//	
//	ExternalSignatureTester[] testers = new ExternalSignatureTester[TESTER_INSTANCES];
//	Environment env = new Environment();
//	
//	public void test() throws Exception {
//		SignatureAlgorithm algorithm = SignatureAlgorithm.SHA256withRSA;
//		File sourceFile = getTestResource(SOURCE_FILE);		
//		String src = sourceFile.getAbsolutePath();
//		String dest;
//		
//		tracer.info(String.format("Identities generation..."));
//		PrivateKey privateKey;
//
//		CertificateInfo certificate = CentralKeyRing.getInstance().wrapCertificate(
//				getCertificate(Identity.ALESSANDRO));
//		privateKey = getPrivateKey(Identity.ALESSANDRO);
//		PDFSignatureInfo signatureInfo = new PDFSignatureInfo("test", algorithm, certificate);
//		signatureInfo.setDisposition(SignatureDisposition.PDF.DETACHED);
//		signatureInfo.setReason ( "PDFSignatureService test" );
//		signatureInfo.setLocation ( "Casalecchio di Reno" );
//		
//		PDFSignatureService pdfSignatureService = new PDFSignatureService();
//		byte[] content = FileUtils.readFileToByteArray ( sourceFile );
//		DigestInfo digestInfo;
//		byte[] digitalSignature;
//		
//		dest = String.format("%s%s_signed.pdf", 
//				FilenameUtils.getFullPath(src), FilenameUtils.getBaseName(src));
//		tracer.info("preSign: fingerPrint evaluation");
//		digestInfo = pdfSignatureService.preSign(content, signatureInfo);
//		tracer.info("external digitalSignature evaluation");
//		Signature signature = Signature.getInstance(algorithm.getName());
//		signature.initSign(privateKey);
//		signature.update(digestInfo.getFingerPrint());
//		digitalSignature = signature.sign();
//		signatureInfo = (PDFSignatureInfo)signatureInfo.setDigitalSignature(digitalSignature);  
//		tracer.info("store the evaluated digitalSignature");
//		tracer.info("postSign: digitalSignature application");
//		byte[] signedPdfEnc = pdfSignatureService.postSign(content, signatureInfo);
//		FileUtils.writeByteArrayToFile(new File(dest), signedPdfEnc);
//		tracer.info("pdf stored at " + FilenameUtils.getName(dest));
//		incrementFileCounter();
////		appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
//	}	
//}
