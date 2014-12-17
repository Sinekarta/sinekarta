package org.sinekartads.core.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.tsp.TimeStampToken;
import org.sinekartads.core.provider.ExternalDigester;
import org.sinekartads.core.provider.ExternalSigner;
import org.sinekartads.model.domain.CertificateInfo;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.PDFSignatureInfo;
import org.sinekartads.model.domain.SecurityLevel.TimeStampVerifyResult;
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
import org.sinekartads.model.oid.EncryptionAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.TemplateUtils;
import org.springframework.util.Assert;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.MakeSignature;
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
	// --- PreSign-phase
	// -
	
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
		
		// Extract the signature options from the chainSignature
		SignatureAlgorithm signAlgorithm 	= chainSignature.getSignAlgorithm();
		SignDisposition.PDF signDisposition = chainSignature.getDisposition();
		X509Certificate[] certificateChain 	= chainSignature.getRawX509Certificates();

		// Evaluate the digestInfo as digestAlgorithm.evalDigest( <content + certificate.chain> )  
		PdfReader reader = null;
		PdfStamper stamper = null;
		try {
			// Convert the signedSignature to a PDFSignatureInfo to use its own protocol
			PDFSignatureInfo pdfSignature = (PDFSignatureInfo) chainSignature;
			
			// Create the stamper
			ByteArrayOutputStream os = new ByteArrayOutputStream(); 
			CryptoStandard subfilter = CryptoStandard.valueOf(pdfSignature.getSubfilter());
			reader = new PdfReader(contentIs);
			stamper = PdfStamper.createSignature(reader, os, '\0');
			
			// Create the appearance
			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
			
			// Evaluate and return the digest
			ExternalSigner signer = getExternalSigner ( signAlgorithm );
			if ( signDisposition == SignDisposition.PDF.DETACHED ) {
				MakeSignature.signDetached(
						appearance, 		// visual aspect of the signature
						digester, 			// digest generator
						signer, 			// signature generator
						certificateChain,	// certification chain
						null,	 			// Collection<CrlClient> crlList <- the CRL list
						null, 				// OcspClient ocspClient <- Online Certificate Status Protocol 
						null, 				// TSAClient tsaClient
						0, 					// the reserved size for the signature. It will be estimated if 0
						subfilter );		// Either Signature.CMS or Signature.CADES
			} else {
				// TODO DEFERRED pdf signature not implemented yet
				throw new UnsupportedOperationException ( "DEFERRED pdf signature not implemented yet" );
			}
		} catch (DocumentException e) {
			throw new SignatureException(e);
		} catch (InvalidKeyException e) {
			throw new SignatureException(e);
		} catch (GeneralSecurityException e) {
			throw new SignatureException(e);
		} finally {
			try {
				if ( reader != null ) {
					reader.close();
				}
//				if ( stamper != null ) {
//					stamper.close();
//				}
			} catch(Exception e) {
				throw new SignatureException(e);
			}
		}
		
		// Add the digestInfo to the trustedChainSignature and obtain the digestSignature
		digestSignature = chainSignature.toDigestSignature ( digester.getDigestInfo() );
		
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
		
		// Receive a signedSignature, the content to be signed and some outputStreams for storing the sign products to;  
		//			the digitalSignature has already been generated externally in some way
		// The only outputStream which will be used is markedSignOs: it has to be the only one not null. 
		// The DETACHED pdf signature means that it will applied outside the PDFDictionary, in opposition to the DEFERRED
		//			signature. To store the signature or the timeStamp into an external file, use a DETACHED CMS type instead.
		
		Assert.notNull ( signedSignature );
		Assert.notNull ( contentIs );
//		Assert.notNull ( markedSignOs );
//		Assert.isNull  ( detachedSignOs );
//		Assert.isNull  ( embeddedSignOs );
//		Assert.isNull  ( tsResultOs );

		// Extract the signature options from the chainSignature
		SignatureAlgorithm signAlgorithm 	= signedSignature.getSignAlgorithm();
		SignDisposition.PDF signDisposition = signedSignature.getDisposition();
		X509Certificate[] certificateChain 	= signedSignature.getRawX509Certificates();

		// Prepare the TSAClient if required by the signatureType
		TSAClient tsaClient = null;
		TsRequestInfo tsRequest = signedSignature.getTsRequest();
		if ( tsRequest!=null && StringUtils.isNotBlank(tsRequest.getTsUrl()) ) {
			tsaClient = new TSAClientBouncyCastle (
					tsRequest.getTsUrl(), tsRequest.getTsUsername(), tsRequest.getTsPassword() );
		}
		
		// Apply the digitalSignature to the PDF document
		PdfReader reader = null;
		PdfStamper stamper = null;
		byte[] signedPdfEnc;
		try {
			// Convert the signedSignature to a PDFSignatureInfo to use its own protocol
			PDFSignatureInfo pdfSignature = (PDFSignatureInfo) signedSignature;
			CryptoStandard subfilter = CryptoStandard.valueOf(pdfSignature.getSubfilter());
			
			// Create the stamper
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			reader = new PdfReader ( IOUtils.toByteArray(contentIs) );
			stamper = PdfStamper.createSignature ( reader, baos, '\0' );
			
			// Create the appearance
			PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
			
			// Create the signed file appending the external-generated digitalSignature
			ExternalSigner signer = getExternalSigner ( signAlgorithm );
			if ( signDisposition == SignDisposition.PDF.DETACHED ) {
				MakeSignature.signDetached(
						appearance, 		// visual aspect of the signature
						digester, 			// digest generator
						signer, 			// signature generator
						certificateChain,	// certification chain
						null,	 			// Collection<CrlClient> crlList <- the CRL list
						null, 				// OcspClient ocspClient <- Online Certificate Status Protocol 
						tsaClient, 			// TSAClient tsaClient
						0, 					// the reserved size for the signature. It will be estimated if 0
						subfilter );		// Either Signature.CMS or Signature.CADES

			} else {
//				PdfDictionary pdfDictionary = reader.getCatalog();
//				ExternalSignatureContainer externalSignatureContainer = new ExternalSignatureContainerImpl(pdfDictionary);
//				OutputStream ostream = null;
//				MakeSignature.signDeferred(reader, pdfSignature.getName(), ostream, externalSignatureContainer);
//				// ... will fail
				
				// TODO DEFERRED pdf signature not implemented yet
				throw new UnsupportedOperationException ( "DEFERRED pdf signature not implemented yet" );
			}
			// Return the temporary file content
			signedPdfEnc = baos.toByteArray();
		} catch (DocumentException e) {
			throw new SignatureException(e);
		} catch (GeneralSecurityException e) {
			throw new SignatureException(e);
		} finally {
			try {
				if ( reader != null ) {
					reader.close();
				}
//				if ( stamper != null ) {
//					stamper.close();
//				}
			} catch(Exception e) {
				throw new SignatureException(e);
			}
		} 
		
		// Store the signed pdf, and eventually marked, into the markedSignOs 
		if ( tsRequest!=null && StringUtils.isNotBlank(tsRequest.getTsUrl()) ) {
			IOUtils.write ( signedPdfEnc, markedSignOs );
		} else {
			IOUtils.write ( signedPdfEnc, embeddedSignOs );
		}

		finalizedSignature = signedSignature.finalizeSignature();
		
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
			AcroFields acroField = reader.getAcroFields();
			
//			SignDisposition.PDF signDisposition;
//			SignatureType.PDF signType;
			String signName;
			PdfPKCS7 pdfPkCs7;
			TimeStampToken rawTimeStampToken;
			VerifiedTimeStamp timeStamp;
			DigestAlgorithm digestAlgorithm;
			EncryptionAlgorithm encryptionAlgorithm;
			SignatureAlgorithm signatureAlgorithm;
			DigestInfo digest;
			byte[] digitalSignature;
			X509Certificate[] certificatesChain;
			CertificateInfo signingCertificate;
			TimeStampVerifyResult tsVerifyResult;
			
			// Iterate over the signatureNames
			List<String> signatureNames = acroField.getSignatureNames();
			for ( int idx=0 ; idx<signatureNames.size(); idx++ ) {
				signName = signatureNames.get(idx);
				pdfPkCs7 = null;
				encryptionAlgorithm = null;
				digestAlgorithm = null;
				certificatesChain = null;
				signingCertificate = null;
				signVerifyResult = null;
				
				try {
					pdfPkCs7 = acroField.verifySignature ( signName, conf.getProviderName() );
					
					// Evaluate the signatureAlgorithm
					digestAlgorithm = DigestAlgorithm.getInstance(pdfPkCs7.getDigestAlgorithm());
					encryptionAlgorithm = EncryptionAlgorithm.getInstance(pdfPkCs7.getEncryptionAlgorithm());
					signatureAlgorithm = SignatureAlgorithm.getInstance(digestAlgorithm, encryptionAlgorithm);
					
					// Extract and verify the timeStamp, if any
					rawTimeStampToken = pdfPkCs7.getTimeStampToken();
					timeStamp = timeStampService.verify(rawTimeStampToken);
					
					// Extract the CryptoStandard from the PdfPKCS7 
					// TODO pdfPkCs7.getFilterSubtype()
//					CryptoStandard subfilter = CryptoStandard.CADES;
					
					// Evaluate the signDisposition
					// TODO extract the signature disposition
//					signDisposition = SignDisposition.PDF.DETACHED;
					
					// Evaluate the signature type:
					//		- CMS   CryptoStandard ->   PDF / PDF_T
					//		- CAdES CryptoStandard -> PAdES / PAdES_T					
//					switch ( subfilter ) {
//						case CADES: {
//							if ( timeStamp != null ) {
//								signType = SignatureType.PDF.PAdES_T;
//							} else {
//								signType = SignatureType.PDF.PAdES;
//							}
//							break;
//						}
//						default: {
//							if ( timeStamp != null ) {
//								signType = SignatureType.PDF.PDF_T;
//							} else {
//								signType = SignatureType.PDF.PDF;
//							}
//						}
//					}
					
					// Instance the PDFSignatureInfo as emptySignature
					emptySignature = new PDFSignatureInfo ( signName, 
															signatureAlgorithm, 
															digestAlgorithm );
					
					// Extract the untrusted signature chain and generate the untrustedChainSignature
					certificatesChain = TemplateUtils.Cast.cast ( X509Certificate.class, pdfPkCs7.getCertificates() );
					chainSignature = emptySignature.toChainSignature(certificatesChain);

					// Extract the digest and generate the digestSignature
					digest = DigestInfo.getInstance(digestAlgorithm, "fingerPrint".getBytes());
					digestSignature = chainSignature.toDigestSignature(digest);
					
					// Extract the digitalSignature and generate the signedSignature 
					digitalSignature = pdfPkCs7.getEncodedPKCS7();
					signedSignature = digestSignature.toSignedSignature(digitalSignature);
					
					// Append the timeStamp
					if ( timeStamp != null ) {
						markedSignature = signedSignature.toMarkedSignature();
						markedSignature.appendTimeStamp(timeStamp, SignDisposition.TimeStamp.ATTRIBUTE);
						finalizedSignature = markedSignature.finalizeSignature();
					} else {
						finalizedSignature = signedSignature.finalizeSignature();
					}
					
					// Evaluate the securityLevel
					tsVerifyResult = timeStamp.getVerifyResult();
					if ( pdfPkCs7.verify() && (timeStamp == null || pdfPkCs7.verifyTimestampImprint()) ) { 
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
	
	ExternalDigester digester = new ExternalDigester();
	Map<SignatureAlgorithm, ExternalSigner> signers = new HashMap<SignatureAlgorithm, ExternalSigner>();

	ExternalSigner getExternalSigner(SignatureAlgorithm signatureAlgorithm) 
			throws 	InvalidKeyException, NoSuchAlgorithmException {
		ExternalSigner signer = signers.get ( signatureAlgorithm.getName() );
		if ( signer == null ) {
			signer = new ExternalSigner (
					signatureAlgorithm.getDigestAlgorithm().getName(), 
					signatureAlgorithm.getEncryptionAlgorithm().getName() );
			signers.put ( signatureAlgorithm, signer );
		}
		return signer;
	}
}
