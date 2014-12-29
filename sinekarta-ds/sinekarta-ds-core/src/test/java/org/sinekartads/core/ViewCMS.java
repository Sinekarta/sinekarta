package org.sinekartads.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.sinekartads.core.service.CMSSignatureService;
import org.sinekartads.core.service.TimeStampService;
import org.sinekartads.model.domain.CMSSignatureInfo;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.EmptySignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.test.SkdsTestCase;
import org.sinekartads.util.TemplateUtils;
import org.springframework.util.Assert;

public class ViewCMS extends SkdsTestCase {

	public static final String KEYSTORE_FILE	= "JENIA.p12";
	public static final String KEYSTORE_PIN 	= "skdscip";
	public static final String SOURCE_FILE 		= "pippo.txt";
	public static final String DESTINATION_FILE = "pippo.txt.p7m";
	
//	String tsaUrl = "https://marte.infocert.it/cdie/HttpService";
//	String tsaUsername = "andrea.tessaro@jenia.it";
//	String tsaPassword = "TdMiHTk5";
	
	static final CoreConfiguration conf;
	static {
		conf = new CoreConfiguration();
	}
	
	static final byte[] SHA256_DIGEST_INFO_PREFIX = new byte[] { 
		0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte)0x86,  
		0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 };

	
	static Logger tracer = Logger.getLogger(ViewCMS.class);
	
	
	@Test
	public void test() throws Exception {
		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null ) {
			Security.addProvider(new BouncyCastleProvider());
		}
		try {
			CMSSignatureService signatureService = new CMSSignatureService();
			
			String keyStorePin = "skdscip";
			char[] ksPwd = null;
			if ( StringUtils.isNotBlank(keyStorePin) ) {
				ksPwd = keyStorePin.toCharArray();
			}
			
			InputStream is = FileUtils.openInputStream(getTestResource("JENIA.jks"));
			KeyStoreType type = KeyStoreType.JKS;
			KeyStore keyStore = KeyStore.getInstance ( type.getType(), type.getProvider() );
			keyStore.load ( is, ksPwd );
			
			String userAlias = Identity.ALESSANDRO.alias;
			
			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
					keyStore.getEntry(userAlias, new PasswordProtection(ksPwd));
			X509Certificate[] certificateChain = TemplateUtils.Cast.cast ( 
					X509Certificate.class, privateKeyEntry.getCertificateChain() );
			PrivateKey privateKey = privateKeyEntry.getPrivateKey();
			
			File srcFile = getTestResource(SOURCE_FILE);
			File destFile = getTestResource(DESTINATION_FILE);
			
			EmptySignature 		  	  < SignCategory, SignDisposition.CMS, 
										VerifyResult, CMSSignatureInfo 	   > emptySignature = null; 
			ChainSignature	  		  < SignCategory, SignDisposition.CMS, 
										VerifyResult, CMSSignatureInfo 	   > chainSignature = null;
			DigestSignature	  		  < SignCategory, SignDisposition.CMS, 
										VerifyResult, CMSSignatureInfo 	   > digestSignature = null;
			SignedSignature	  		  < SignCategory, SignDisposition.CMS, 
										VerifyResult, CMSSignatureInfo 	   > signedSignature = null;
			FinalizedSignature		  < SignCategory, SignDisposition.CMS, 
										VerifyResult, CMSSignatureInfo 	   > finalizedSignature = null;
			
			InputStream contentIs;
			OutputStream detachedSignOs;
			OutputStream embeddedSignOs;
			OutputStream tsResultOs;
			OutputStream markedSignOs;
			
			emptySignature = new CMSSignatureInfo ( conf.getSignatureAlgorithm(), conf.getDigestAlgorithm() ); 
			chainSignature = emptySignature.toChainSignature ( certificateChain );
			
			contentIs = new FileInputStream(srcFile);
			digestSignature = signatureService.doPreSign(chainSignature, contentIs);
			DigestInfo digest = digestSignature.getDigest();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			byte[] digestInfoValue = ArrayUtils.addAll(
					SHA256_DIGEST_INFO_PREFIX, digest.getFingerPrint());
			byte[] digitalSignature = cipher.doFinal ( digestInfoValue );
			signedSignature = digestSignature.toSignedSignature ( digitalSignature );
			
			contentIs 		= new FileInputStream ( srcFile );
			detachedSignOs  = new ByteArrayOutputStream ( );
			embeddedSignOs  = new FileOutputStream ( destFile );
			tsResultOs 		= new ByteArrayOutputStream ( );
			markedSignOs 	= new ByteArrayOutputStream ( );
			finalizedSignature = signatureService.doPostSign(signedSignature, contentIs, detachedSignOs, embeddedSignOs, tsResultOs, markedSignOs);
			Assert.isTrue ( finalizedSignature.isFinalized() );
		} catch(Exception e) {
			tracer.error(e.getMessage(), e);
			throw e;
		}
	}
	
    
}
