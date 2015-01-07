/*
 * Copyright (C) 2010 - 2012 Jenia Software.
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
 * Part of this code come from 
 * FirmaPdf version 0.0.x Copyright (C) 2006 Antonino Iacono (ant_iacono@tin.it)
 * and Roberto Resoli
 * See method description for more details
 * 
 * Part of this code come from 
 * com.itextpdf.text.pdf.security.MakeSignature
 * Paulo Soares
 * 
 * See method description for more details
 * 
 */
package org.sinekartads.core.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERGeneralString;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.PDFSignatureInfo;
import org.sinekartads.model.domain.SecurityLevel;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.TsRequestInfo;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

public class PDFTools {

	private static Logger tracer = Logger.getLogger(PDFTools.class);

	public static final String PDF = ".pdf";

	public static FinalizedSignature < SignatureType.SignCategory,
									   SignDisposition.PDF,
									   SecurityLevel.VerifyResult,
									   PDFSignatureInfo > sign ( SignedSignature < SignatureType.SignCategory,
																				   SignDisposition.PDF,
																								SecurityLevel.VerifyResult,
																								PDFSignatureInfo > signedSignature,
								//											  X509Certificate certificate, 
																			  InputStream is,
																			  OutputStream os ) throws SignatureException {
////		signAndMark(doc, certificate, is, os, null, null, null, null, null);
//		signAndMark(signatureInfo, certificate, is, os, null, null, null);
//	}
//
//	public static void signAndMark(PDFSignatureInfo doc,
//			X509Certificate certificate, InputStream is, OutputStream os,
//			String tsaUrl, String tsaUser, String tsaPassword) {
////		signAndMark(doc, certificate, is, os, tsaUrl, tsaUser, tsaPassword, null, null);
////	}
////	
////	public static void signAndMark(DigitalSignatureDocument doc,
////			X509Certificate certificate, InputStream is, OutputStream os,
////			String tsaUrl, String tsaUser, String tsaPassword, Collection<CrlClient> crlList, OcspClient ocspClient) {
		try {
			PDFSignatureInfo signature = (PDFSignatureInfo) signedSignature;
			TSAClient tsaClient=null;
			
			TsRequestInfo tsRequest = signature.getTsRequest(); 
			if (tsRequest!=null && StringUtils.isNotBlank(tsRequest.getTsUrl())) {
				tsaClient = new TSAClientBouncyCastle(tsRequest.getTsUrl(), tsRequest.getTsUsername(), tsRequest.getTsPassword());
			}
//			if (tsaUrl!=null) {
//				tsaClient = new TSAClientBouncyCastle(tsaUrl, tsaUser, tsaPassword);
//			}

			int estimatedSize=0;
			CryptoStandard sigtype = CryptoStandard.CMS;
			
			// creo il reader del pdf
			PdfReader reader = new PdfReader(is);

			// creo lo stamper (se il pdf e' gia' firmato, controfirma,
			// altrimenti firma
			PdfStamper stamper = null;
			if (isPdfSigned(reader)) {
				if (tracer.isDebugEnabled()) tracer.debug("document already signed, i will apply another sign");
				stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
			} else {
				if (tracer.isDebugEnabled()) tracer.debug("document never signed before, this is first");
				stamper = PdfStamper.createSignature(reader, os, '\0');
			}

			// questo e' il certificato su cui lavorare
			Certificate[] chain = signature.getRawX509Certificates();
//			Certificate[] chain = new Certificate[1];
//			chain[0] = certificate;

			// creo la signature apparence
			PdfSignatureAppearance sap = stamper.getSignatureAppearance();
			ExternalDigest externalDigest = new BouncyCastleDigest();
			 
			// inizio codice copiato da MakeSignature
			
//			Collection<byte[]> crlBytes = null;
//	        int i = 0;
//	        while (crlBytes == null && i < chain.length)
//	        	crlBytes = MakeSignature.processCrl(chain[i++], crlList);
	    	if (estimatedSize == 0) {
	            estimatedSize = 8192;
//	            if (crlBytes != null) {
//	                for (byte[] element : crlBytes) {
//	                    estimatedSize += element.length + 10;
//	                }
//	            }
//	            if (ocspClient != null)
	                estimatedSize += 4192;
//	            if (tsaClient != null)
	                estimatedSize += 4192;
	        }
	        sap.setCertificate(chain[0]);
	        sap.setReason(signature.getReason());
	        sap.setLocation(signature.getLocation());
	        
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(signature.getSigningTime());
			sap.setSignDate(cal);
			sap.getStamper().setUnicodeModDate(signature.getUnicodeModDate());
			sap.getStamper().setFileId(signature.getFileId());
	        
			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
	        dic.setReason(sap.getReason());
	        dic.setLocation(sap.getLocation());
	        dic.setContact(sap.getContact());
	        dic.setDate(new PdfDate(sap.getSignDate())); // time-stamp will over-rule this
	        sap.setCryptoDictionary(dic);

	        HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
	        exc.put(PdfName.CONTENTS, new Integer(estimatedSize * 2 + 2));
	        sap.preClose(exc);

	        String hashAlgorithm = signature.getDigestAlgorithm().getName();
	        PdfPKCS7 sgn = new PdfPKCS7(null, chain, hashAlgorithm, BouncyCastleProvider.PROVIDER_NAME, externalDigest, false);
	        InputStream data = sap.getRangeStream();
	        byte hash[] = DigestAlgorithms.digest(data, externalDigest.getMessageDigest(hashAlgorithm));
//	        byte[] ocsp = null;
//	        if (chain.length >= 2 && ocspClient != null) {
//	            ocsp = ocspClient.getEncoded((X509Certificate) chain[0], (X509Certificate) chain[1], null);
//	        }
	        sgn.setExternalDigest(signature.getDigitalSignature(), null, "RSA");

//	        byte[] encodedSig = sgn.getEncodedPKCS7(hash, _getSignDate(doc.getSignDate()), tsaClient, ocsp, crlBytes, sigtype);
	        byte[] encodedSig = sgn.getEncodedPKCS7(hash, cal, tsaClient, null, null, sigtype);

	        if (estimatedSize + 2 < encodedSig.length)
	            throw new IOException("Not enough space");

			ASN1EncodableVector extraDataVectorEncoding = new ASN1EncodableVector();
			// 
			extraDataVectorEncoding.add(new DERObjectIdentifier("1.2.840.114283")); // encoding attribute 
			extraDataVectorEncoding.add(new DERGeneralString("115.105.110.101.107.97.114.116.97"));

			// applico la firma al PDF
			byte[] extraDataVectorEncodingBytes = new DERSequence(new DERSequence(extraDataVectorEncoding)).getEncoded();

	        byte[] paddedSig = new byte[estimatedSize];
	        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
			System.arraycopy(extraDataVectorEncodingBytes, 0,paddedSig, encodedSig.length,extraDataVectorEncodingBytes.length); // encoding attribute

	        PdfDictionary dic2 = new PdfDictionary();
	        dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
	        sap.close(dic2);
	        
			// this should be already done, but ...
			// closing streams
			try {
				is.close();
			} catch (IOException e) {
				tracer.error("error on input stream", e);
			}
			try {
				os.flush();
			} catch (IOException e) {
				tracer.error("error on output stream", e);
			}
			try {
				os.close();
			} catch (IOException e) {
				tracer.error("error on output stream", e);
			}
			return signature.finalizeSignature();
//		} catch (MarkFailedException e) {
//			throw e;
		} catch (Exception e) {
			tracer.error("Unable to sign PDF.", e);
			throw new SignatureException("Unable to sign PDF.", e);
		}
	}

	public static DigestSignature < SignatureType.SignCategory,
									SignDisposition.PDF,
									SecurityLevel.VerifyResult,
									PDFSignatureInfo > calculateFingerPrint ( ChainSignature < SignatureType.SignCategory,
																							   SignDisposition.PDF,
																							   SecurityLevel.VerifyResult,
																							   PDFSignatureInfo > chainSignature,
//																			  X509Certificate certificate, 
																			  InputStream is) throws SignatureException {
//		calculateFingerPrint(doc, certificate, is, null, null, null, null, null);
//	}
//	
//	public static void calculateFingerPrint(DigitalSignatureDocument doc,
//			X509Certificate certificate, InputStream is, Collection<CrlClient> crlList, OcspClient ocspClient, String tsaUrl, String tsaUser, String tsaPassword) {
		try {

//			TSAClient tsaClient=null;
//			
//			if (tsaUrl!=null) {
//				tsaClient = new SinekartaTSAClient(tsaUrl, tsaUser, tsaPassword);
//			}
//
			int estimatedSize=0;
			CryptoStandard sigtype = CryptoStandard.CMS;	// FIXME qui c'era CMS
			PDFSignatureInfo signature = (PDFSignatureInfo) chainSignature;
			
			// creo il reader del pdf
			PdfReader reader = new PdfReader(is);

			// creo lo stamper (se il pdf e' gia' firmato, controfirma,
			// altrimenti firma
			PdfStamper stamper = null;
			if (isPdfSigned(reader)) {
				if (tracer.isDebugEnabled()) tracer.debug("calculating finger print for document already signed");
				stamper = PdfStamper.createSignature(reader, null, '\0', null, true);
			} else {
				if (tracer.isDebugEnabled()) tracer.debug("calculating finger print for document never signed before");
				stamper = PdfStamper.createSignature(reader, null, '\0');
			}
			
			// questo e' il certificato su cui lavorare
			Certificate[] chain = signature.getRawX509Certificates();
//			Certificate[] chain = new Certificate[1];
//			chain[0] = certificate;

			// creo la signature apparence
			PdfSignatureAppearance sap = stamper.getSignatureAppearance();
			ExternalDigest externalDigest = new BouncyCastleDigest();
			 
			// inizio codice copiato da MakeSignature
			
//			Collection<byte[]> crlBytes = null;
//	        int i = 0;
//	        while (crlBytes == null && i < chain.length)
//	        	crlBytes = MakeSignature.processCrl(chain[i++], crlList);
	    	if (estimatedSize == 0) {
	            estimatedSize = 8192;
//	            if (crlBytes != null) {
//	                for (byte[] element : crlBytes) {
//	                    estimatedSize += element.length + 10;
//	                }
//	            }
//	            if (ocspClient != null)
	                estimatedSize += 4192;
//	            if (tsaClient != null)
	                estimatedSize += 4192;
	        }
	    	Calendar now = Calendar.getInstance();
	    	PdfDate date = new PdfDate(now);
	    	
			sap.setSignDate(now);
			signature.setSigningTime(now.getTime());
			signature.setUnicodeModDate(date.toUnicodeString());

			sap.setCertificate(chain[0]);
			sap.setReason(signature.getReason());
			sap.setLocation(signature.getLocation());

			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
	        dic.setReason(sap.getReason());
	        dic.setLocation(sap.getLocation());
	        dic.setContact(sap.getContact());
	        dic.setDate(date); // time-stamp will over-rule this
	        sap.setCryptoDictionary(dic);

	        HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
	        exc.put(PdfName.CONTENTS, new Integer(estimatedSize * 2 + 2));
	        sap.preClose(exc);

	        String hashAlgorithm = signature.getDigestAlgorithm().getName();
	        PdfPKCS7 sgn = new PdfPKCS7(null, chain, hashAlgorithm, BouncyCastleProvider.PROVIDER_NAME, externalDigest, false);
//	        String hashAlgorithm = Constants.SHA256;
//	        PdfPKCS7 sgn = new PdfPKCS7(null, chain, hashAlgorithm, Constants.BC, externalDigest, false);
	        InputStream data = sap.getRangeStream();
	        byte hash[] = DigestAlgorithms.digest(data, externalDigest.getMessageDigest(hashAlgorithm));
//	        byte[] ocsp = null;
//	        if (chain.length >= 2 && ocspClient != null) {
//	            ocsp = ocspClient.getEncoded((X509Certificate) chain[0], (X509Certificate) chain[1], null);
//	        }
//	        byte[] authenticatedAttributeBytes = sgn.getAuthenticatedAttributeBytes(hash, now, ocsp, crlBytes, sigtype);
	        byte[] authenticatedAttributeBytes = sgn.getAuthenticatedAttributeBytes(hash, now, null, null, sigtype);

	        // calcolo dell'impronta
	        MessageDigest digester = MessageDigest.getInstance(signature.getDigestAlgorithm().getName());
	        byte[] fingerPrint = digester.digest(authenticatedAttributeBytes);
	        
//	     	byte[] fingerPrint = Util.digest256(authenticatedAttributeBytes);

	     	signature.setAuthenticatedAttributeBytes(authenticatedAttributeBytes);
	     	signature.setFileId(sap.getStamper().getFileId());
//	     	signature.setFileIDByteContent(TextUtil.byteToHex(sap.getStamper().getFileID().getBytes()));
			signature.setUnicodeModDate(sap.getStamper().getUnicodeModDate());
//			signature.setModDateUnicodeString(sap.getStamper().getModDate().toUnicodeString());
			signature.setSigningTime(now.getTime());
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
//			signature.setSignDate(sdf.format(now.getTime()));
			
			// this should be already done, but ...
			// closing streams
			try {
				is.close();
			} catch (IOException e) {
				tracer.error("error on input stream", e);
			}
			
			return signature.toDigestSignature ( 
					DigestInfo.getInstance(signature.getDigestAlgorithm(), fingerPrint) );
		} catch (Exception e) {
			tracer.error("Unable to calculate finger print of PDF.", e);
//			throw new PDFException("Unable calculate finger print of PDF.", e);
			throw new SignatureException("Unable calculate finger print of PDF.", e);
		}
	}

	/**
	 * metodo di utilita' che verifica se il pdf in input e' gia' firmato
	 * 
	 * @param reader
	 * @return
	 * @throws SignatureException 
	 */
	public static boolean isPdfSigned(InputStream is) throws SignatureException {
		if (tracer.isDebugEnabled())
			tracer.debug("chacking if PDF/A is signed");
		try {
			PdfReader reader = new PdfReader(is);
			boolean ret = false;
			if (PDFTools.isPdfSigned(reader)) {
				ret = true;
			}
			reader.close();
			return ret;
		} catch (Exception e) {
			tracer.error("Unable to read PDF. Unable to check if the pdf is signed.",e);
			throw new SignatureException("Unable to read PDF. Unable to check if the pdf is signed.",e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * metodo di utilita' che verifica se il pdf in input e' gia' firmato
	 * 
	 * @param reader
	 * @return
	 * @throws SignatureException 
	 */
	public static boolean isPdfSigned(PdfReader reader) throws SignatureException {
		if (tracer.isDebugEnabled())
			tracer.debug("chacking if PDF/A is signed");
		try {
			AcroFields af = reader.getAcroFields();

			// Search of the whole signature
			ArrayList<String> names = af.getSignatureNames();

			// For every signature :
			if (names.size() > 0) {
				if (tracer.isDebugEnabled())tracer.debug("yes, it is");
				return true;
			} else {
				if (tracer.isDebugEnabled())tracer.debug("no, it isn't");
				return false;
			}
		} catch (Exception e) {
			tracer.error("Unable to read PDF. Unable to check if the pdf is signed.",e);
			throw new SignatureException("Unable to read PDF. Unable to check if the pdf is signed.",e);
		}
	}

//	/**
//	 * metodo di utilita' che verifica se il pdf in input e' un PDF/A
//	 * 
//	 * @param reader
//	 * @return
//	 */
//	public static boolean isPdfa(InputStream is) {
//		if (tracer.isDebugEnabled()) tracer.debug("checking if PDF is PDF/A");
//		PdfReader reader = null;
//		ByteArrayInputStream bais = null;
//		XMLStreamReader sr = null;
//		try {
//			reader = new PdfReader(is);
//			byte[] metadata = reader.getMetadata();
//			if (metadata == null || metadata.length == 0)
//				return false;
//			bais = new ByteArrayInputStream(metadata);
//			sr = XMLInputFactory.newInstance().createXMLStreamReader(bais);
//			boolean isConformanceTag = false;
//			int eventCode;
//			while (sr.hasNext()) {
//				eventCode = sr.next();
//				String val = null;
//				switch (eventCode) {
//				case 1:
//					val = sr.getLocalName();
//					if (val.equals("conformance") && sr.getNamespaceURI().equals("http://www.aiim.org/pdfa/ns/id/"))
//						isConformanceTag = true;
//					break;
//				case 4:
//					val = sr.getText();
//					if (isConformanceTag) {
//						if (val.equals("A") || val.equals("B")) {
//							if (tracer.isDebugEnabled()) tracer.debug("yes, it is");
//							return true;
//						} else {
//							if (tracer.isDebugEnabled()) tracer.debug("no, it isn't");
//							return false;
//						}
//					}
//					break;
//				}
//			}
//		} catch (Exception e) {
//			tracer.error("Unable to read PDF. Unable to check if the pdf is a pdf/a.",e);
//			throw new PDFException("Unable to read PDF. Unable to check if the pdf is a pdf/a.",e);
//		} finally {
//			try {
//				if (reader != null)
//					reader.close();
//			} catch (Exception e) {
//				tracer.error("error on pdf reader", e);
//			}
//			try {
//				if (sr != null)
//					sr.close();
//			} catch (Exception e) {
//				tracer.error("error on stax reader", e);
//			}
//			try {
//				if (bais != null)
//					bais.close();
//			} catch (Exception e) {
//				tracer.error("error on input stream", e);
//			}
//			try {
//				if (is != null)
//					is.close();
//			} catch (Exception e) {
//				tracer.error("error on input stream", e);
//			}
//		}
//		if (tracer.isDebugEnabled())
//			tracer.debug("no, it isn't");
//		return false;
//	}
//
//	/**
//	 * metodo di utilita' che verifica se il pdf in input e' un PDF/A
//	 * 
//	 * @param reader
//	 * @return
//	 */
//	public static String getPrintableTimestampToken(InputStream is) {
//		if (tracer.isDebugEnabled()) tracer.debug("getting timestamp token");
//		String ret = null;
//		PdfReader reader = null;
//		try {
//			reader = new PdfReader(is);
//			AcroFields af = reader.getAcroFields();
//			for (String name : af.getSignatureNames()) {
//				PdfPKCS7 pk = af.verifySignature(name);
//				TimeStampToken tst = pk.getTimeStampToken();
//				if (tst != null) {
//					ret = Base64.encodeBytes(tst.getEncoded());
//				}
//			}
//			if (tracer.isDebugEnabled()) tracer.debug("timestamp token returned : " + ret);
//			return ret;
//		} catch (Exception e) {
//			tracer.error("Unable to read PDF. Unable to get timestamp token.", e);
//			throw new PDFException("Unable to read PDF. Unable to get timestamp token.", e);
//		} finally {
//			try {
//				if (reader != null)
//					reader.close();
//			} catch (Exception e) {
//				tracer.error("error on pdf reader", e);
//			}
//			try {
//				if (is != null)
//					is.close();
//			} catch (Exception e) {
//				tracer.error("error on input stream", e);
//			}
//		}
//	}
//
//	public static String calculatePdfName(String fileName) {
//		int dotIdx = fileName.lastIndexOf('.');
//		if (dotIdx >= 0) {
//			return fileName.substring(0, dotIdx)
//					+ Configuration.getInstance().getPdfaSuffix()
//					+ PDFTools.PDF;
//		} else {
//			return fileName + Configuration.getInstance().getPdfaSuffix()
//					+ PDFTools.PDF;
//		}
//	}
//
//	public static Calendar _getSignDate(String signDate) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
//		Date d = null;
//		try {
//			d = sdf.parse(signDate);
//		} catch (ParseException e) {
//			// not possible
//		}
//		Calendar c = Calendar.getInstance();
//		c.setTime(d);
//		return c;
//	}

}
