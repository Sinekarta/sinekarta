package org.sinekartads.integration.xml;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.sinekartads.applet.AppletRequestDTO;
import org.sinekartads.applet.AppletResponseDTO;
import org.sinekartads.applet.AppletResponseDTO.ActionErrorDTO;
import org.sinekartads.applet.AppletResponseDTO.FieldErrorDTO;
import org.sinekartads.applet.SignNOApplet;
import org.sinekartads.core.service.XMLSignatureService;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.VerifyDTO;
import org.sinekartads.dto.jcl.JclResponseDTO;
import org.sinekartads.dto.jcl.PostSignResponseDTO;
import org.sinekartads.dto.jcl.PreSignResponseDTO;
import org.sinekartads.dto.jcl.VerifyResponseDTO;
import org.sinekartads.dto.tools.DTOConverter;
import org.sinekartads.integration.BaseIntegrationTC;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureInfo;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.EmptySignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.VerifyInfo;
import org.sinekartads.model.domain.XMLSignatureInfo;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SinekartaDsObjectIdentifiers;
import org.sinekartads.util.DNParser;
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.x509.X509Utils;
import org.sinekartads.utils.JSONUtils;

public class SignXMLwithSmartCardAndDTO extends BaseIntegrationTC {

	public static final String SOURCE_FILE 		= "document.xml";
	public static final String SIGNED_FILE 		= "document_bes.xml";
	public static final String MARKED_FILE 		= "document_t.xml";
	public static final String EXTRACTED_FILE 	= "document_cnt.xml";
	
	static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	static final byte[] SHA256_DIGEST_INFO_PREFIX = new byte[] { 
		0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte)0x86,  
		0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 };

	static Logger tracer = Logger.getLogger(SignXMLwithSmartCardAndDTO.class);
	
	
	
	@Test
	public void test() throws Exception {
		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null ) {
			Security.addProvider(new BouncyCastleProvider());
		}
		
		SignNOApplet applet = new SignNOApplet();
		try {
			
			// Main options
			String contentHex = HexUtils.encodeHex (
					FileUtils.readFileToByteArray ( 
							getTestResource ( SOURCE_FILE ) ) );
			boolean applyMark = false;
			boolean useFakeSmartCard = false;
			String driver;
			String scPin;
			if ( useFakeSmartCard ) {
				driver = "fake";
				scPin = "123";
			} else {
				driver = "libbit4ipki.so";
				scPin = "18071971";
			}
			
			// Test products
			String[] aliases;
			String alias;
			X509Certificate certificate;
			X509Certificate[] certificateChain;
			byte[] fingerPrint;
			byte[] digitalSignature;
			File envelopeFile;
			String envelopeHex; 
			
			// Communication unities
			DTOConverter converter = DTOConverter.getInstance(); 
			String jsonResp;
			PreSignResponseDTO preSignResp;
			PostSignResponseDTO postSignResp;
			VerifyResponseDTO verifyResp;
			AppletResponseDTO appletResponse;
			SignatureDTO chainSignatureDTO;
			SignatureDTO digestSignatureDTO;
			SignatureDTO signedSignatureDTO;
			SignatureDTO finalizedSignatureDTO;
			VerifyDTO verifyDTO;
			
			// Prepare the signature service
			XMLSignatureService signatureService = new XMLSignatureService();
			
			// Init the applet
			try {
				AppletRequestDTO req = new AppletRequestDTO();
				req.setDriver(driver);
				appletResponse = applet.selectDriver ( req );
			} catch(Exception e) {
				tracer.error("error during the applet initialization", e);
				throw e;
			}
			
			// Login with the smartCard
			try {
				AppletRequestDTO req = new AppletRequestDTO();
				req.setDriver(driver);
				req.setPin(scPin);
				appletResponse = applet.login ( req );
				aliases = (String[]) JSONUtils.deserializeJSON ( String[].class, extractJSON(appletResponse) );
			} catch(Exception e) {
				tracer.error("error during the applet login", e);
				throw e;
			}
			
			// Choose the signing alias
			StringBuilder buf = new StringBuilder();
			for ( String a : aliases ) {
				buf.append(a).append(" ");
			}
			alias = aliases[0];
			tracer.info(String.format ( "available aliases:   %s", buf ));
			tracer.info(String.format ( "signing alias:       %s", alias ));
			
			// Load the certificate chain from the applet
			try {
				AppletRequestDTO req = new AppletRequestDTO();
				req.setDriver(driver);
				req.setPin(scPin);
				req.setAlias(alias);
				appletResponse = applet.selectCertificate ( req );
				certificate = (X509Certificate) X509Utils.rawX509CertificateFromHex( extractJSON(appletResponse) );
				tracer.info(String.format ( "certificate:         %s", certificate ));
				certificateChain = new X509Certificate[] { certificate };
			} catch(Exception e) {
				tracer.error("error during the certificate selection", e);
				throw e;
			}
			
			// Declare the signature state variables
			EmptySignature 		  	  < SignCategory, SignDisposition.XML, 
										VerifyResult, XMLSignatureInfo 	   > emptySignature = null; 
			ChainSignature	  		  < SignCategory, SignDisposition.XML, 
										VerifyResult, XMLSignatureInfo 	   > chainSignature = null;
			DigestSignature	  		  < SignCategory, SignDisposition.XML, 
										VerifyResult, XMLSignatureInfo 	   > digestSignature = null;
			SignedSignature	  		  < SignCategory, SignDisposition.XML, 
										VerifyResult, XMLSignatureInfo 	   > signedSignature = null;
			FinalizedSignature		  < SignCategory, SignDisposition.XML, 
										VerifyResult, XMLSignatureInfo 	   > finalizedSignature = null;
			VerifyInfo verifyResult = null;
			
			// empty signature - initialized with the SHA256withRSA and RSA algorithms
			emptySignature = new XMLSignatureInfo ( conf.getSignatureAlgorithm(), conf.getDigestAlgorithm() ); 
			
			// Add to the empty signature the timeStamp request if needed
			TsRequestInfo tsRequest = null;
			if ( applyMark ) {
				tsRequest = new TsRequestInfo ( SignDisposition.TimeStamp.ENVELOPING,
											    DigestAlgorithm.SHA256,
											    BigInteger.TEN,
											    "http://ca.signfiles.com/TSAServer.aspx", "", "" );
			}
			emptySignature.setTsRequest(tsRequest);
			
			// chain signature - contains the certificate chain
			chainSignature = emptySignature.toChainSignature ( certificateChain );
			
			// Convert the chainSignature to a DTO
			try {
				chainSignatureDTO = (SignatureDTO) converter.fromSignatureInfo ( 
						(SignatureInfo < SignCategory, SignDisposition.XML, 
									     VerifyResult, XMLSignatureInfo >) chainSignature );
			} catch(Exception e) {
				tracer.error("unable to convert the chainSignature to the DTO", e);
				throw e;
			}
			
			// PreSign phase - join the content with the certificate chain and evaluate the digest
			try {
				jsonResp = signatureService.preSign(chainSignatureDTO.toBase64(), contentHex);
				digestSignatureDTO = extractResult ( SignatureDTO.class, jsonResp );
			} catch(Exception e) {
				tracer.error("error during the pre sign phase", e);
				throw e;
			}
			
			// digest signature - contains the envelope digest 
			try {
				digestSignature = (DigestSignature < SignCategory, SignDisposition.XML, 
					     							 VerifyResult, XMLSignatureInfo >) converter.toSignatureInfo(digestSignatureDTO);
			} catch(Exception e) {
				tracer.error("unable to obtain the digestSignature from the DTO", e);
				throw e;
			}
			
			// signed signature - sign the digest with the smartCard to obtain the digitalSignature
			try {
				DigestInfo digest = digestSignature.getDigest();
				fingerPrint = digest.getFingerPrint();
				tracer.info(String.format ( "fingerPrint:         %s", HexUtils.encodeHex(fingerPrint) ));
				AppletRequestDTO req = new AppletRequestDTO();
				req.setDriver(driver);
				req.setPin(scPin);
				req.setAlias(alias);
				req.setHexDigest(HexUtils.encodeHex(fingerPrint));
				appletResponse = applet.signDigest( req );
				digitalSignature = HexUtils.decodeHex ( (String) extractJSON(appletResponse) );
				tracer.info(String.format ( "digitalSignature:    %s", HexUtils.encodeHex(digitalSignature) ));
				signedSignature = digestSignature.toSignedSignature ( digitalSignature );
			} catch(Exception e) {
				tracer.error("error during the digital signature evaluation", e);
				throw e;
			}
			
//			 try {
//	            Signature signature = Signature.getInstance ( certificate.getSigAlgName() );
//	            signature.initVerify ( certificate.getPublicKey() );
//	            signature.update(fingerPrint);
//	            Assert.isTrue ( signature.verify(digitalSignature) );
//            } catch(Exception e) {
//            	throw new RuntimeException(e);
//            }
			
			// Convert the signedSignature to a DTO
			try {
				signedSignatureDTO = (SignatureDTO) converter.fromSignatureInfo ( 
						(SignatureInfo < SignCategory, SignDisposition.XML, 
									  	 VerifyResult, XMLSignatureInfo >) signedSignature );
			} catch(Exception e) {
				tracer.error("unable to convert the chainSignature to a DTO", e);
				throw e;
			}
			
			// PostSign phase - add the digitalSignature to the envelope and store the result into the JCLResultDTO
			try {
				jsonResp = signatureService.postSign ( signedSignatureDTO.toBase64(), contentHex );
				postSignResp = TemplateUtils.Encoding.deserializeBase64 ( PostSignResponseDTO.class, jsonResp );
				finalizedSignatureDTO = extractResult ( SignatureDTO.class, jsonResp );
			} catch(Exception e) {
				tracer.error("error during the envelope generation", e);
				throw e;
			}
			
			// finalized signature - enveloped signed and eventually marked, not modifiable anymore
			try {
				finalizedSignature = (FinalizedSignature < SignCategory, SignDisposition.XML, 
					     								   VerifyResult, XMLSignatureInfo >) converter.toSignatureInfo ( finalizedSignatureDTO );
			} catch(Exception e) {
				tracer.error("unable to obtain the digestSignature from the DTO", e);
				throw e;
			}

			// Store the signedData into the appropriated destFile
			try {
				String hexSignedData;
				byte[] signedData;
				if ( applyMark ) {
					hexSignedData = postSignResp.getMarkedSign();
					envelopeFile = getTestResource(MARKED_FILE);
				} else {
					hexSignedData = postSignResp.getEmbeddedSign();
					envelopeFile = getTestResource(SIGNED_FILE);
				}
				signedData = HexUtils.decodeHex(hexSignedData);
				FileUtils.writeByteArrayToFile ( envelopeFile, signedData );
			} catch(IOException e) {
				tracer.error("unable to obtain the digestSignature from the DTO", e);
				throw e;
			}
			tracer.info(String.format ( "signedContent stored into %s", envelopeFile.getAbsolutePath() ));
			
			// Convert the finalizedSignature to a DTO
			try {
				finalizedSignatureDTO = (SignatureDTO) converter.fromSignatureInfo ( 
						(SignatureInfo < SignCategory, SignDisposition.XML, 
									  	 VerifyResult, XMLSignatureInfo >) finalizedSignature );
			} catch(Exception e) {
				tracer.error("unable to convert the chainSignature to a DTO", e);
				throw e;
			}
			
			// Read the signed envelope
			try {
				envelopeHex = HexUtils.encodeHex ( FileUtils.readFileToByteArray(envelopeFile) );
			} catch(Exception e) {
				tracer.error("unable to read the signed envelope", e);
				throw e;
			}
			
			// Verify phase - load the envelope content and verify the nested signature 
//			try {
//				jsonResp = signatureService.verify ( envelopeHex, null, null, VerifyResult.VALID.name() );
//				verifyDTO = extractResult ( VerifyDTO.class, jsonResp );
//			} catch(Exception e) {
//				tracer.error("error during the envelope verification", e);
//				throw e;
//			}
//			
//			// finalized signature - enveloped signed and eventually marked, not modifiable anymore
//			try {
//				verifyResult = (VerifyInfo) converter.toVerifyInfo( verifyDTO );
//			} catch(Exception e) {
//				tracer.error("unable to obtain the verifyInfo from the DTO", e);
//				throw e;
//			}
//			
//			try {
//				for(VerifiedSignature < ?, ?, VerifyResult, ?> verifiedSignature : verifyResult.getSignatures() ) {
//					tracer.info(String.format ( "signature validity:  %s", verifiedSignature.getVerifyResult().name() ));
//					tracer.info(String.format ( "signature type:      %s", verifiedSignature.getSignType().name() ));
//					tracer.info(String.format ( "disposition:         %s", verifiedSignature.getDisposition().name() ));
//					tracer.info(String.format ( "digest algorithm:    %s", verifiedSignature.getDigest().getAlgorithm().name() ));
//					tracer.info(String.format ( "finger print:        %s", HexUtils.encodeHex(verifiedSignature.getDigest().getFingerPrint()) ));
//					tracer.info(String.format ( "counter signature:   %s", verifiedSignature.isCounterSignature() ));
//					tracer.info(String.format ( "signature algorithm: %s", verifiedSignature.getSignAlgorithm().name() ));
//					tracer.info(String.format ( "digital signature:   %s", HexUtils.encodeHex(verifiedSignature.getDigitalSignature()) ));
//					tracer.info(String.format ( "reason:              %s", verifiedSignature.getReason() ));
//					tracer.info(String.format ( "signing location:    %s", verifiedSignature.getLocation() ));
//					tracer.info(String.format ( "signing time:        %s", formatDate(verifiedSignature.getSigningTime()) ));
//					tracer.info(String.format ( "\n "));
//					tracer.info(String.format ( "signing certificate chain: "));
//					for ( X509Certificate cert : verifiedSignature.getRawX509Certificates() ) {
//						showCertificate(cert);
//					}
//					if ( verifiedSignature.getTimeStamps() != null ) {
//						tracer.info(String.format ( "\n "));
//						tracer.info(String.format ( "timestamps: "));
//						for ( TimeStampInfo mark : verifiedSignature.getTimeStamps() ) {
//							tracer.info(String.format ( "timestamp validity:  %s", mark.getVerifyResult().name() ));
//							tracer.info(String.format ( "timestamp authority: %s", mark.getTsaName() ));
//							tracer.info(String.format ( "timestamp authority: %s", mark.getTsaName() ));
//							tracer.info(String.format ( "message imprint alg: %s", mark.getMessageInprintInfo().getAlgorithm().name() ));
//							tracer.info(String.format ( "message imprint:     %s", HexUtils.encodeHex(mark.getMessageInprintInfo().getFingerPrint()) ));
//							tracer.info(String.format ( "digest algorithm:    %s", mark.getDigestAlgorithm().name() ));
//							tracer.info(String.format ( "digital signature:   %s", HexUtils.encodeHex(mark.getDigitalSignature()) ));
//							tracer.info(String.format ( "signature algorithm: %s", mark.getSignAlgorithm().name() ));
//							tracer.info(String.format ( "timestamp certificate: "));
//							for ( X509Certificate cert : mark.getRawX509Certificates() ) {
//								showCertificate(cert);
//							}
//						}
//					}
//				}
//			} catch(Exception e) {
//				tracer.error("unable to print the verify results", e);
//				throw e;
//			}
			
		} catch(Exception e) {
			tracer.error(e.getMessage(), e);
			throw e;
		} finally {
			applet.close();
		}
	}
	
	private <DTO extends BaseDTO> DTO extractResult(Class<DTO> dtoClass, String respHex) throws Exception {
		JclResponseDTO resp = TemplateUtils.Encoding.deserializeBase64(JclResponseDTO.class, respHex);
		ResultCode resultCode = resp.resultCodeFromString();
		DTO dto;
		if ( resultCode == ResultCode.SUCCESS ) {
			dto = TemplateUtils.Encoding.deserializeBase64(dtoClass, resp.getResult());
		} else {
			throw new Exception ( resp.getErrorMessage() );
		}
		return dto;
	}
	
	private String extractJSON(AppletResponseDTO resp) throws Exception {
		String json;
		if ( resp.checkSuccess() ) {
			json = resp.getResult();
		} else {
			StringBuilder buf = new StringBuilder();
			for ( FieldErrorDTO fieldError : resp.getFieldErrors() ) {
				for ( String errorMessage : fieldError.getErrors() ) {
					buf.append ( String.format("fieldError  - %s: %s\n", fieldError.getField(), errorMessage) );
				}
			}
			for ( ActionErrorDTO actionError : resp.getActionErrors() ) {
				buf.append ( String.format("actionError - %s\n", actionError.getErrorMessage()) );
			}
			throw new Exception ( buf.toString() );
		}
		return json;
	}

	private void showCertificate(X509Certificate certificate) {
		Map<String, String> dns = DNParser.parse ( certificate.getSubjectDN() );
		tracer.info(String.format ( "subject:             %s", dns.get(SinekartaDsObjectIdentifiers.dn_commonName) ));
		tracer.info(String.format ( "country:             %s", dns.get(SinekartaDsObjectIdentifiers.dn_countryName) ));
		tracer.info(String.format ( "organization:        %s", dns.get(SinekartaDsObjectIdentifiers.dn_organizationName) ));
		tracer.info(String.format ( "organization unit:   %s", dns.get(SinekartaDsObjectIdentifiers.dn_organizationUnitName) ));
		tracer.info(String.format ( "not before:          %s", formatDate(certificate.getNotBefore()) ));
		tracer.info(String.format ( "not after:           %s", formatDate(certificate.getNotAfter()) ));
		dns = DNParser.parse ( certificate.getIssuerDN() );
		tracer.info(String.format ( "issuer:              %s", dns.get(SinekartaDsObjectIdentifiers.dn_commonName) ));
	}
	
	private String formatDate(Date date) {
		if ( date == null ) 											return "";
		return dateFormat.format( date );
	}
	
}
