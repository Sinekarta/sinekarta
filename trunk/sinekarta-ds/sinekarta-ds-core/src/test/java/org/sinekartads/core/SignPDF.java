package org.sinekartads.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.Test;
import org.sinekartads.core.service.PDFSignatureService;
import org.sinekartads.core.service.TimeStampService;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.model.domain.PDFSignatureInfo;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.EmptySignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.test.SkdsTestCase;
import org.sinekartads.util.TemplateUtils;
import org.springframework.util.Assert;

public class SignPDF extends SkdsTestCase {

	public static final String KEYSTORE_FILE	= "JENIA.p12";
	public static final String KEYSTORE_PIN 	= "skdscip";
	public static final String SOURCE_FILE 		= "pippo.pdf";
	public static final String EMBEDDED_FILE 	= "pippo_sgn.pdf";
	public static final String MARKED_FILE 		= "pippo_mrk.pdf";
	
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

	
	static Logger tracer = Logger.getLogger(SignPDF.class);
	
	
	@Test
	public void test() throws Exception {
		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null ) {
			Security.addProvider(new BouncyCastleProvider());
		}
		
		InputStream contentIs = null;
		OutputStream detachedSignOs = null;
		OutputStream embeddedSignOs = null;
		OutputStream tsResultOs = null;
		OutputStream markedSignOs = null;
		try {
			PDFSignatureService signatureService = new PDFSignatureService();
			boolean applyMark = false;
			
			String keyStorePin = "skdscip";
			char[] ksPwd = null;
			if ( StringUtils.isNotBlank(keyStorePin) ) {
				ksPwd = keyStorePin.toCharArray();
			}
			
			InputStream is = FileUtils.openInputStream(getTestResource("sinekarta.jks"));
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
			
			EmptySignature 		  	  < SignCategory, SignDisposition.PDF, 
										VerifyResult, PDFSignatureInfo 	   > emptySignature = null; 
			ChainSignature	  		  < SignCategory, SignDisposition.PDF, 
										VerifyResult, PDFSignatureInfo 	   > chainSignature = null;
			DigestSignature	  		  < SignCategory, SignDisposition.PDF, 
										VerifyResult, PDFSignatureInfo 	   > digestSignature = null;
			SignedSignature	  		  < SignCategory, SignDisposition.PDF, 
										VerifyResult, PDFSignatureInfo 	   > signedSignature = null;
			FinalizedSignature		  < SignCategory, SignDisposition.PDF, 
										VerifyResult, PDFSignatureInfo 	   > finalizedSignature = null;
			
			emptySignature = new PDFSignatureInfo ( "firma", conf.getSignatureAlgorithm(), conf.getDigestAlgorithm() );
			if ( applyMark ) {
				TsRequestInfo tsRequest = new TsRequestInfo ( SignDisposition.TimeStamp.ATTRIBUTE,
															  DigestAlgorithm.SHA256,
															  BigInteger.TEN,
															  "http://ca.signfiles.com/TSAServer.aspx", "", "");
				emptySignature.setTsRequest(tsRequest);
			}
			chainSignature = emptySignature.toChainSignature ( certificateChain );
			
			contentIs = new FileInputStream(srcFile);
			digestSignature = signatureService.doPreSign(chainSignature, contentIs);
			DigestInfo digest = digestSignature.getDigest();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			byte[] digestInfoValue = ArrayUtils.addAll (
					SHA256_DIGEST_INFO_PREFIX, digest.getFingerPrint() );
			byte[] digitalSignature = cipher.doFinal ( digestInfoValue );
			signedSignature = digestSignature.toSignedSignature ( digitalSignature );
			
			contentIs 		= new FileInputStream ( srcFile );
			detachedSignOs  = new ByteArrayOutputStream ( );
			if ( applyMark ) {
				embeddedSignOs 	= new ByteArrayOutputStream ( );
				markedSignOs  = new FileOutputStream ( getTestResource(MARKED_FILE) );
			} else {
				embeddedSignOs  = new FileOutputStream ( getTestResource(EMBEDDED_FILE) );
				markedSignOs 	= new ByteArrayOutputStream ( );
			}
			tsResultOs 		= new ByteArrayOutputStream ( );
			finalizedSignature = signatureService.doPostSign(signedSignature, contentIs, detachedSignOs, embeddedSignOs, tsResultOs, markedSignOs);
			Assert.isTrue ( finalizedSignature.isFinalized() );
		} catch(Exception e) {
			tracer.error(e.getMessage(), e);
			throw e;
		} finally {
			IOUtils.closeQuietly(contentIs);
			IOUtils.closeQuietly(detachedSignOs);
			IOUtils.closeQuietly(embeddedSignOs);
			IOUtils.closeQuietly(tsResultOs);
			IOUtils.closeQuietly(markedSignOs);
		}
	}
	
	protected KeyPair makeKeyPair() {
		try {
			KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
			return kg.generateKeyPair();
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}	
	
	protected X509Certificate makeCertificate(KeyPair pair, String name, String issuerName) {
		try {
			long DAY = 1000*60*60*24l;
			long now = System.currentTimeMillis();
			long midnight = now - now%DAY;
			Date notBefore = new Date(midnight - DAY);                // time before which the certificate isn't valid yet
			Date notAfter = new Date(midnight + DAY*365);             // time after which the certificate is no longer valid		
			ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withRSA").setProvider(conf.getProviderName()).build(pair.getPrivate());
			JcaX509v1CertificateBuilder v1CertGen = new JcaX509v1CertificateBuilder(
		              new X500Name(issuerName), 
		              BigInteger.ONE, 
		              notBefore, 
						notAfter, 
		              new X500Name(name), 
		              pair.getPublic());
			X509CertificateHolder certHolder = v1CertGen.build(sigGen);
			X509Certificate cert = new JcaX509CertificateConverter().setProvider(conf.getProviderName()).getCertificate(certHolder);
	        cert.checkValidity(new Date());
	        cert.verify(pair.getPublic());	        
	        return cert;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}
