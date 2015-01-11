package org.sinekartads.core.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERGeneralString;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.PDFSignatureInfo;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.TimeStampInfo;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.EmptySignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.MarkedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.Transitions.VerifiedSignature;
import org.sinekartads.model.domain.Transitions.VerifiedTimeStamp;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.VerifyInfo;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.x509.X509Utils;
import org.springframework.util.Assert;

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
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

public class PDFSignatureService 
		extends AbstractSignatureService < SignCategory,
										   SignDisposition.PDF,
										   PDFSignatureInfo> {

	static final Logger tracer = Logger.getLogger ( PDFSignatureService.class );
	
	
	
	// -----
	// --- Pre-Sign phase
	// -
	
	@Override
	public DigestSignature < SignCategory, 
							 SignDisposition.PDF, 
							 VerifyResult, 		
							 PDFSignatureInfo > doPreSign (	ChainSignature < SignCategory, 
												 						 	 SignDisposition.PDF, 
													 						 VerifyResult, 		
													 						 PDFSignatureInfo >	chainSignature,
															InputStream 						contentIs ) 
																			throws SignatureException, IOException {
		
		Assert.notNull ( chainSignature );
		Assert.notNull ( contentIs );
		
		// Prepare the signature variables
		DigestSignature < SignCategory, 
						  SignDisposition.PDF,
						  VerifyResult,
						  PDFSignatureInfo > digestSignature	= null;
		try {
//			TSAClient tsaClient=null;
			
			
			TsRequestInfo tsRequest = chainSignature.getTsRequest();
			boolean applyMark = tsRequest!=null && StringUtils.isNotBlank(tsRequest.getTsUrl());
//			if ( applyMark ) {
//				tsaClient = new TSAClientBouncyCastle(tsRequest.getTsUrl(), tsRequest.getTsUsername(), tsRequest.getTsPassword());
//			}

			int estimatedSize=0;
			CryptoStandard sigtype = CryptoStandard.CADES;	
			PDFSignatureInfo signature = (PDFSignatureInfo) chainSignature;
			
			// creo il reader del pdf
			PdfReader reader = new PdfReader(contentIs);

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
	            if (applyMark)
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

			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, sigtype == CryptoStandard.CADES ? PdfName.ETSI_CADES_DETACHED : PdfName.ADBE_PKCS7_DETACHED);
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
	        InputStream data = sap.getRangeStream();
	        byte hash[] = DigestAlgorithms.digest(data, externalDigest.getMessageDigest(hashAlgorithm));
	        byte[] authenticatedAttributeBytes = sgn.getAuthenticatedAttributeBytes(hash, now, null, null, sigtype);

	        // calcolo dell'impronta
	        MessageDigest digester = MessageDigest.getInstance(signature.getDigestAlgorithm().getName());
	        byte[] fingerPrint = digester.digest(authenticatedAttributeBytes);
	        
	     	signature.setAuthenticatedAttributeBytes(authenticatedAttributeBytes);
	     	signature.setFileId(sap.getStamper().getFileId());
			signature.setUnicodeModDate(sap.getStamper().getUnicodeModDate());
			signature.setSigningTime(now.getTime());

			DigestInfo digestInfo = DigestInfo.getInstance(signature.getDigestAlgorithm(), fingerPrint);
			digestSignature = signature.toDigestSignature ( digestInfo );
//			digestSignature = PDFTools.calculateFingerPrint(chainSignature, contentIs);
		} catch (SignatureException e) {
			throw e;
		} catch (Exception e) {
			throw new SignatureException(e);
		}
				
		// Return the digestSignature
		return digestSignature;
	}
	

	
	// -----
	// --- Post-Sign phase
	// -
	
	@Override
	public FinalizedSignature < SignCategory,
							 	SignDisposition.PDF, 
							 	VerifyResult, 		
							 	PDFSignatureInfo > doPostSign (	SignedSignature	< SignCategory, 
													 	 					 	  SignDisposition.PDF, 
													 	 					 	  VerifyResult, 		
													 	 					 	  PDFSignatureInfo > signedSignature,
																InputStream 						 contentIs,
																OutputStream 						 detachedSignOs,
																OutputStream 						 embeddedSignOs,
																OutputStream 						 tsResultOs,
																OutputStream 						 markedSignOs )
																			
																			throws SignatureException, IOException 	{
		
		FinalizedSignature 	  	  <	SignCategory, 
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo >			finalizedSignature = null;
		
		Assert.notNull ( signedSignature );
		Assert.notNull ( contentIs );

		// Store the signed pdf, and eventually marked, into the markedSignOs
		TsRequestInfo tsRequest = signedSignature.getTsRequest();
		boolean applyMark = tsRequest!=null && StringUtils.isNotBlank(tsRequest.getTsUrl());
		OutputStream envelopedStream;
		if ( applyMark ) {
			envelopedStream = markedSignOs;
		} else {
			envelopedStream = embeddedSignOs;
		}
		
		try {
			PDFSignatureInfo signature = (PDFSignatureInfo) signedSignature;
			TSAClient tsaClient=null;
			
			if ( applyMark ) {
				tsaClient = new TSAClientBouncyCastle(tsRequest.getTsUrl(), tsRequest.getTsUsername(), tsRequest.getTsPassword());
			}

			int estimatedSize=0;
			CryptoStandard sigtype = CryptoStandard.CADES;
			
			// creo il reader del pdf
			PdfReader reader = new PdfReader(contentIs);

			// creo lo stamper (se il pdf e' gia' firmato, controfirma,
			// altrimenti firma
			PdfStamper stamper = null;
			if (isPdfSigned(reader)) {
				if (tracer.isDebugEnabled()) tracer.debug("document already signed, i will apply another sign");
				stamper = PdfStamper.createSignature(reader, envelopedStream, '\0', null, true);
			} else {
				if (tracer.isDebugEnabled()) tracer.debug("document never signed before, this is first");
				stamper = PdfStamper.createSignature(reader, envelopedStream, '\0');
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
	            if (applyMark)
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
	        
			PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, sigtype == CryptoStandard.CADES ? PdfName.ETSI_CADES_DETACHED : PdfName.ADBE_PKCS7_DETACHED);
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
	        
			finalizedSignature = signature.finalizeSignature();
//			finalizedSignature = PDFTools.sign(signedSignature, contentIs, envelopedStream);
		} catch (SignatureException e) {
			throw e;
		} catch (Exception e) {
			throw new SignatureException(e);
		} 
		
		return finalizedSignature;
	}
	
	
	
	// -----
	// --- TimeStamp application
	// -
	
	@Override
	public TimeStampInfo doApplyTimeStamp (
			TsRequestInfo emptyTsr,
			InputStream contentIs,
			InputStream signatureIs,
			InputStream embeddedSignIs,
			OutputStream timestampOs,
			OutputStream markedSignOs ) 
			throws SignatureException,
					IOException, 
					CertificateException {
		
//		// Verify that the digestInfo in the tsRequest does match with the signedData
//		DigestInfo digestInfo = tsRequest.getMessageImprintInfo();
//		digestInfo.validate(signedData);
//		
//		File tmpFile = new File ( conf.getUserSpaceTemporaryFolder(), HexUtils.randomHex(16) + ".pdf" );
//		byte[] signedPdf;
//		PdfReader reader = null;
//		PdfStamper stamper = null;
//		try {
//			// Create the stamper
//			reader = new PdfReader ( signedData );
//		
//			TSAClient tsaClient = new TSAClientBouncyCastle(
//					tsRequest.getTsUrl(), tsRequest.getTsUsername(), tsRequest.getTsPassword());
//			
//			PdfPKCS7 sgn = new PdfPKCS7(null, null, digestInfo.getAlgorithmName(), conf.getProviderName(), digester, false);
//			byte[] encodedSig = sgn.getEncodedPKCS7 (
//					digestInfo.getFingerPrint(), 
//					Calendar.getInstance(), 
//					tsaClient, null, null, null);
//			// ... will fail
//		} finally {
//			try {
//				if ( reader != null ) {
//					reader.close();
//				}
//				if ( stamper != null ) {
//					stamper.close();
//				}
//			} catch(Exception e) {
//				throw new GeneralSecurityException(e);
//			}
//			if ( tmpFile!=null && tmpFile.exists() ) {
//				tmpFile.delete();
//			}
//		}
		
		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

	
	
	// -----
	// --- Verify
	// -
	
	/**
	 * @deprecated use {@link #verify(InputStream)} instead
	 */
	@Override
	public VerifyInfo doVerify ( 
			InputStream contentIs,
			InputStream tsResponseIs,
			InputStream envelopeIs,
			VerifyResult requiredSecurityLevel,			
			OutputStream extractedOs ) 
					throws SignatureException,
							CertificateException,
							IOException {
		
		Assert.notNull ( envelopeIs );
		
		// Prepare the signature variables
		EmptySignature   		  < SignCategory, 
							 		SignDisposition.PDF,
							 		VerifyResult,
							 		PDFSignatureInfo >			emptySignature = null;		
		
		ChainSignature 	 	      < SignCategory, 
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo >			chainSignature = null;
							
		DigestSignature 	  	  < SignCategory, 
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo >			digestSignature	= null;		
							
		SignedSignature 	  	  < SignCategory, 
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo >			signedSignature	= null;
		
		MarkedSignature   		  <	SignCategory, 
							 		SignDisposition.PDF,
							 		VerifyResult,
							 		PDFSignatureInfo >			markedSignature	= null;

		FinalizedSignature 	  	  <	SignCategory, 
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo >			finalizedSignature = null;

		VerifiedSignature 	  	  <	SignCategory, 
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo >			verifiedSignature = null;
		
		byte[] envelope = IOUtils.toByteArray(envelopeIs);
		VerifyInfo verifyInfo = new VerifyInfo();
		VerifyResult signVerifyResult = VerifyResult.INVALID;
		PdfReader reader = null;
		try {
			reader = new PdfReader ( new ByteArrayInputStream(envelope) );
			AcroFields acroFields = reader.getAcroFields();
			
//			SignDisposition.PDF signDisposition;
//			SignatureType.PDF signType;
			PdfPKCS7 pdfPkCs7;
			TimeStampToken rawTimeStampToken;
			VerifiedTimeStamp timeStamp;
			DigestAlgorithm digestAlgorithm;
			SignatureAlgorithm signatureAlgorithm;
			DigestInfo digest;
//			byte[] digitalSignature;
			X509Certificate[] certificateChain;
			
	        for ( String signName : acroFields.getSignatureNames() ) {
	        	try {
		            pdfPkCs7 = acroFields.verifySignature(signName, "BC");
		            
		        	digestAlgorithm = DigestAlgorithm.getInstance(pdfPkCs7.getHashAlgorithm());
		        	signatureAlgorithm = SignatureAlgorithm.getInstance(pdfPkCs7.getDigestAlgorithm());
		        	rawTimeStampToken = pdfPkCs7.getTimeStampToken();
		        	if ( rawTimeStampToken != null ) {
		        		timeStamp = timeStampService.verify(rawTimeStampToken);
		        	} else {
		        		timeStamp = null;
		        	}
					// Instance the PDFSignatureInfo as emptySignature
					emptySignature = new PDFSignatureInfo ( signName, 
															signatureAlgorithm, 
															digestAlgorithm );
					((PDFSignatureInfo)emptySignature).setCoversWholeDocument (
							acroFields.signatureCoversWholeDocument(signName) );
					
					// Extract the untrusted signature chain and generate the untrustedChainSignature
					Certificate[] certificates = pdfPkCs7.getCertificates();
					certificateChain = new X509Certificate[certificates.length];
					for ( int i=0; i<certificates.length; i++ ) {
						certificateChain[i] = X509Utils.rawX509CertificateFromEncoded ( certificates[i].getEncoded() );
					}
					chainSignature = emptySignature.toChainSignature(certificateChain);

					// Extract the digest and generate the digestSignature
					digest = DigestInfo.getInstance(digestAlgorithm, "fingerPrint".getBytes());
					digestSignature = chainSignature.toDigestSignature(digest);
					
					// Extract the digitalSignature and generate the signedSignature 
//					digitalSignature = pdfPkCs7.getEncodedPKCS7();
					signedSignature = digestSignature.toSignedSignature("digitalSignature".getBytes());
					
					// Append the timeStamp
					if ( timeStamp != null ) {
						markedSignature = signedSignature.toMarkedSignature();
						markedSignature.appendTimeStamp(timeStamp, SignDisposition.TimeStamp.ATTRIBUTE);
						finalizedSignature = markedSignature.finalizeSignature();
					} else {
						finalizedSignature = signedSignature.finalizeSignature();
					}
					
					// Evaluate the securityLevel
		            Calendar cal = pdfPkCs7.getSignDate();
		            Certificate[] pkc = pdfPkCs7.getCertificates();
					boolean verified = pdfPkCs7.verify() && StringUtils.isBlank ( CertificateVerification.verifyCertificate((X509Certificate)pkc[0], new ArrayList<CRL>(), cal) );
					if ( verified ) { 
						signVerifyResult = minLevel ( signVerifyResult, VerifyResult.VALID );
					} else {
						signVerifyResult = minLevel ( signVerifyResult, VerifyResult.INVALID );
					}
					verifiedSignature = finalizedSignature.toVerifiedSignature(signVerifyResult);
					
				} catch(Exception e) {
					tracer.info(e.getMessage(), e);
					
					// Invalidate the signature if any error happen
					if ( markedSignature != null ) 					verifiedSignature = markedSignature.invalidateSignature();
					else if ( signedSignature != null ) 			verifiedSignature = signedSignature.invalidateSignature();
					else if ( digestSignature != null )		 		verifiedSignature = digestSignature.invalidateSignature();
					else if ( chainSignature != null ) 				verifiedSignature = chainSignature.invalidateSignature();
					else											verifiedSignature = emptySignature.invalidateSignature();
					 
				}
				verifyInfo.addSignature ( verifiedSignature );
			}
		} finally {
			
			if ( reader != null ) {
				reader.close();
			}
			if ( requiredSecurityLevel.compareTo(signVerifyResult) < 0 ) {
				IOUtils.write ( envelope, extractedOs );
			}
			
		}
		
		return verifyInfo;
	}
	
	
	
	// -----
	// --- Utility methods
	// -
	
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

}
