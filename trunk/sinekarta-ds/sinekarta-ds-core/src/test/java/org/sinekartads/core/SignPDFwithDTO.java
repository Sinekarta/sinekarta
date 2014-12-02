//package org.sinekartads.core;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.math.BigInteger;
//import java.security.KeyStore;
//import java.security.KeyStore.PasswordProtection;
//import java.security.PrivateKey;
//import java.security.Security;
//import java.security.cert.X509Certificate;
//
//import javax.crypto.Cipher;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Logger;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.eclipse.jetty.util.log.Log;
//import org.junit.Test;
//import org.sinekartads.SinekartaDsTestCase;
//import org.sinekartads.core.service.PDFSignatureService;
//import org.sinekartads.core.service.TimeStampService;
//import org.sinekartads.dto.BaseDTO;
//import org.sinekartads.dto.ResultCode;
//import org.sinekartads.dto.domain.SignatureDTO;
//import org.sinekartads.dto.jcl.JclResponseDTO;
//import org.sinekartads.dto.jcl.PostSignResponseDTO;
//import org.sinekartads.dto.jcl.PreSignResponseDTO;
//import org.sinekartads.dto.tools.DTOConverter;
//import org.sinekartads.model.domain.DigestInfo;
//import org.sinekartads.model.domain.KeyStoreType;
//import org.sinekartads.model.domain.PDFSignatureInfo;
//import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
//import org.sinekartads.model.domain.SignDisposition;
//import org.sinekartads.model.domain.SignatureInfo;
//import org.sinekartads.model.domain.SignatureType.SignCategory;
//import org.sinekartads.model.domain.Transitions.ChainSignature;
//import org.sinekartads.model.domain.Transitions.DigestSignature;
//import org.sinekartads.model.domain.Transitions.EmptySignature;
//import org.sinekartads.model.domain.Transitions.FinalizedSignature;
//import org.sinekartads.model.domain.Transitions.SignedSignature;
//import org.sinekartads.model.domain.TsRequestInfo;
//import org.sinekartads.model.oid.DigestAlgorithm;
//import org.sinekartads.util.HexUtils;
//import org.sinekartads.util.TemplateUtils;
//
//public class SignPDFwithDTO extends SinekartaDsTestCase {
//
//	public static final String KEYSTORE_FILE	= "JENIA.p12";
//	public static final String KEYSTORE_PIN 	= "skdscip";
//	public static final String SOURCE_FILE 		= "pippo.pdf";
//
//	static final Logger tracer = Logger.getLogger(SignPDFwithDTO.class);
//	static final DTOConverter converter = DTOConverter.getInstance(); 
//	static final CoreConfiguration conf;
//	static {
//		conf = new CoreConfiguration();
//	}
//	
//	static final byte[] SHA256_DIGEST_INFO_PREFIX = new byte[] { 
//		0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte)0x86,  
//		0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 };
//
//	public <DTO extends BaseDTO, RespDTO extends JclResponseDTO<DTO>> DTO extractResult(RespDTO resp) throws Exception {
//		ResultCode resultCode = resp.resultCodeFromString();
//		DTO dto;
//		if ( resultCode == ResultCode.SUCCESS ) {
//			dto = resp.getResult();
//		} else {
//			// FIXME this code doesn't work, understand why
////			Class<? extends Exception> errorClass = 
////					(Class<? extends Exception>) Class.forName(resp.getErrorType());
////			Exception e = TemplateUtils.Clone.deserializeFromHex ( errorClass, preSignResp.getError() );
////			throw(e);
//			throw new Exception ( resp.getErrorMessage() );
//		}
//		return dto;
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void test() throws Exception {
//		
//		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null ) {
//			Security.addProvider(new BouncyCastleProvider());
//		}
//		
//		boolean applyMark = false;
//		String hexResp;
//		PreSignResponseDTO preSignResp;
//		PostSignResponseDTO postSignResp;
//		SignatureDTO dto;
//		
//		// Create the signature service
//		PDFSignatureService signatureService = new PDFSignatureService();
//		TimeStampService timeStampService = new TimeStampService();
//		signatureService.setTimeStampService(timeStampService);
//		
//		// Load the privateKey and the relative certificate from the keystore
//		String keyStorePin = "skdscip";
//		char[] ksPwd = null;
//		if ( StringUtils.isNotBlank(keyStorePin) ) {
//			ksPwd = keyStorePin.toCharArray();
//		}
//		
//		InputStream is = FileUtils.openInputStream(getTestResource("JENIA.p12"));
//		KeyStoreType type = KeyStoreType.PKCS12_DEF;
//		KeyStore keyStore = KeyStore.getInstance ( type.getType(), type.getProvider() );
//		keyStore.load ( is, ksPwd );
//		
//		String userAlias = Identity.ALESSANDRO.alias;
//		
//		KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
//				keyStore.getEntry("CN="+userAlias, new PasswordProtection(ksPwd));
//		X509Certificate[] certificateChain = TemplateUtils.Cast.cast ( 
//				X509Certificate.class, privateKeyEntry.getCertificateChain() );
//		PrivateKey privateKey = privateKeyEntry.getPrivateKey();
//		
//		// Load and hexify the content to be signed
//		String contentHex = HexUtils.encodeHex (
//				FileUtils.readFileToByteArray ( 
//						getTestResource ( SOURCE_FILE ) ) );
//		
//		// Prepare the various stages of the signature
//		EmptySignature 		  	  < SignCategory, SignDisposition.PDF, 
//									VerifyResult, PDFSignatureInfo 	   > emptySignature = null; 
//		ChainSignature	  		  < SignCategory, SignDisposition.PDF, 
//									VerifyResult, PDFSignatureInfo 	   > chainSignature = null;
//		DigestSignature	  		  < SignCategory, SignDisposition.PDF, 
//									VerifyResult, PDFSignatureInfo 	   > digestSignature = null;
//		SignedSignature	  		  < SignCategory, SignDisposition.PDF, 
//									VerifyResult, PDFSignatureInfo 	   > signedSignature = null;
//		FinalizedSignature		  < SignCategory, SignDisposition.PDF, 
//									VerifyResult, PDFSignatureInfo 	   > finalizedSignature = null;
//		
//		// Generate the emptySignature 
//		emptySignature = new PDFSignatureInfo ( "signature", conf.getSignatureAlgorithm(), conf.getDigestAlgorithm() ); 
//		
//		// Add the tsRequest to the emptySignature (optional step) 
//		if ( applyMark ) {
//			TsRequestInfo tsRequest = new TsRequestInfo ( SignDisposition.TimeStamp.ATTRIBUTE,
//														  DigestAlgorithm.SHA256,
//														  BigInteger.TEN,
//														  "http://ca.signfiles.com/TSAServer.aspx", "", "" );
//			emptySignature.setTsRequest(tsRequest);
//		}
//		
//		// Add the certificateChain to the emptySignature -> chainSignature
//		chainSignature = emptySignature.toChainSignature ( certificateChain );
//		
//		// Convert the chainSignature to a DTO
//		try {
//			dto = converter.fromSignatureInfo ( (SignatureInfo < SignCategory, SignDisposition.PDF, 
//															     VerifyResult, PDFSignatureInfo >) chainSignature );
//		} catch(Exception e) {
//			tracer.error("unable to convert the chainSignature to the DTO", e);
//			throw e;
//		}
//		
//		// Perform the pre-sign phase
//		try {
//			hexResp = signatureService.preSign(dto.toBase64(), contentHex);
//			preSignResp = BaseDTO.deserializeBase64(PreSignResponseDTO.class, hexResp);
//			dto = extractResult ( preSignResp );
//		} catch(Exception e) {
//			tracer.error("error during the pre sign phase", e);
//			throw e;
//		}
//		
//		// Obtain the digestSignature from the DTO
//		try {
//			digestSignature = (DigestSignature < SignCategory, SignDisposition.PDF, 
//												 VerifyResult, PDFSignatureInfo >) converter.toSignatureInfo(dto);
//		} catch(Exception e) {
//			tracer.error("unable to obtain the digestSignature from the DTO", e);
//			throw e;
//		}
//		
//		// Evaluate the digitalSignature
//		try {
//			DigestInfo digest = digestSignature.getDigest();
//			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
//			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
//			byte[] digestInfoValue = ArrayUtils.addAll (
//					SHA256_DIGEST_INFO_PREFIX, digest.getFingerPrint() );
//			byte[] digitalSignature = cipher.doFinal ( digestInfoValue );
//			signedSignature = digestSignature.toSignedSignature ( digitalSignature );
//		} catch(Exception e) {
//			tracer.error("error during the digitalSignature evaluation", e);
//			throw e;
//		}
//		
//		// Convert the signedSignature to a DTO
//		try {
//			dto = converter.fromSignatureInfo ( (SignatureInfo < SignCategory, SignDisposition.PDF, 
//															  	 VerifyResult, PDFSignatureInfo >) signedSignature );
//		} catch(Exception e) {
//			tracer.error("unable to convert the chainSignature to a DTO", e);
//			throw e;
//		}
//
//		// Perform the post-sign phase
//		try {
//			hexResp = signatureService.postSign ( dto.toBase64(), contentHex );
//			postSignResp = BaseDTO.deserializeBase64 ( PostSignResponseDTO.class, hexResp );
//			dto = extractResult ( preSignResp );
//		} catch(Exception e) {
//			tracer.error("error during the digest evaluation", e);
//			throw e;
//		}
//		
//		// Obtain the finalizedSignature from the DTO
//		try {
//			finalizedSignature = (FinalizedSignature < SignCategory, SignDisposition.PDF, 
//				     VerifyResult, PDFSignatureInfo >) converter.toSignatureInfo ( dto );
//		} catch(Exception e) {
//			tracer.error("unable to obtain the digestSignature from the DTO", e);
//			throw e;
//		}
//		
//		// Display some signature defails
//		Log.info(String.format ( "digitalSignature: ", 
//				HexUtils.encodeHex(finalizedSignature.getDigitalSignature()) ));
//
//		// Store the signedData into the destFile
//		try {
//			File destFile;
//			String hexSignedData;
//			byte[] signedData;
//			if ( applyMark ) {
//				hexSignedData = postSignResp.getMarkedSign().getHex();
//				destFile = getTestResource("pippo.txt.m7m");
//			} else {
//				hexSignedData = postSignResp.getEmbeddedSign().getHex();
//				destFile = getTestResource("pippo.txt.p7m");
//			}
//			signedData = HexUtils.decodeHex(hexSignedData);
//			FileUtils.writeByteArrayToFile ( destFile, signedData );
//		} catch(IOException e) {
//			tracer.error("unable to obtain the digestSignature from the DTO", e);
//			throw e;
//		}
//		
//		// TODO add the verifySign phase
//	}
//}
