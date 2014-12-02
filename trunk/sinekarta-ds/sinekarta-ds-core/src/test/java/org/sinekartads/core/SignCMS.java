package org.sinekartads.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.sinekartads.core.service.CMSSignatureService;
import org.sinekartads.core.service.TimeStampService;
import org.sinekartads.model.domain.CMSSignatureInfo;
import org.sinekartads.model.domain.KeyStoreType;
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
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.x509.X509Utils;
import org.springframework.util.Assert;

public class SignCMS extends SkdsTestCase {

	public static final String KEYSTORE_FILE	= "JENIA.p12";
	public static final String KEYSTORE_PIN 	= "skdscip";
	public static final String SOURCE_FILE 		= "pippo.txt";
	public static final String SIGNED_FILE 		= "pippo.txt.p7m";
	public static final String MARKED_FILE 		= "pippo.txt.m7m";
	
	
	static final CoreConfiguration conf;
	static {
		conf = new CoreConfiguration();
	}
	
	static final byte[] SHA256_DIGEST_INFO_PREFIX = new byte[] { 
		0x30, 0x2f, 0x30, 0x0b, 0x06, 0x09, 0x60, (byte)0x86,  
		0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x04, 0x20 };

	
	static Logger tracer = Logger.getLogger(SignCMS.class);
	
	
	
	@Test
	public void test() throws Exception {
		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null ) {
			Security.addProvider(new BouncyCastleProvider());
		}
		try {
			// Set the main options 
			File srcFile = getTestResource(SOURCE_FILE);
			boolean applyMark = false;
			
			// Prepare the signature service
			CMSSignatureService signatureService = new CMSSignatureService();
			TimeStampService timeStampService = new TimeStampService();
			signatureService.setTimeStampService(timeStampService);
			
			// Load the signing privateKey and relative certificate chain
			X509Certificate[] certificateChain;
//			PrivateKey privateKey;
//			String keyStorePin = "skdscip";
//			char[] ksPwd = null;
//			if ( StringUtils.isNotBlank(keyStorePin) ) {
//				ksPwd = keyStorePin.toCharArray();
//			}
//			InputStream is = FileUtils.openInputStream(getTestResource("JENIA.p12"));
//			KeyStoreType type = KeyStoreType.PKCS12_DEF;
//			KeyStore keyStore = KeyStore.getInstance ( type.getType(), type.getProvider() );
//			keyStore.load ( is, ksPwd );
//			String userAlias = Identity.ALESSANDRO.alias;
//			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
//					keyStore.getEntry("CN="+userAlias, new PasswordProtection(ksPwd));
//			certificateChain = TemplateUtils.Cast.cast ( 
//					X509Certificate.class, privateKeyEntry.getCertificateChain() );
//			privateKey = privateKeyEntry.getPrivateKey();
			certificateChain = new X509Certificate[] {
				X509Utils.rawX509CertificateFromHex ( "308206f4308205dca003020102020329f125300d06092a864886f70d0101050500308184310b300906035504061302495431153013060355040a0c0c494e464f4345525420535041311430120603550405130b3037393435323131303036311b3019060355040b0c12456e746520436572746966696361746f7265312b302906035504030c22496e666f436572742053657276697a69206469204365727469666963617a696f6e65301e170d3133303231313037333232335a170d3136303231313030303030305a30820114310b300906035504061302495431173015060355040a0c0e4e6f6e204469636869617261746f311e301c060355040b0c15432e432e492e412e412e20444920424f4c4f474e413147304506035504030c3e5453534e445237314c313842333933522f373432303032313830303338333331302e317656793858775046423431656a44514e51754b542b6e5254684d3d31193017060355040513105453534e445237314c313842333933523126302406092a864886f70d0109011617616e647265612e7465737361726f406a656e69612e6974310f300d060355042a0c06414e445245413116301406035504040c0d5445535341524f20504f52544131173015060355042e130e323031303131313235354137313230819f300d06092a864886f70d010101050003818d0030818902818100e86ac0f4e851153f5b515683b7232d3e0f14dfe6c3b3808d24c37a14af6ee29000227e1f416efe7f86314f33b6c3a0980ffe7d0078b27d3daa247ab062bbf9446a58d679e8053e2c56d6eb053f202bdcda1e34499228e3edc83cfc45ce157e1d157360d421f3d1b10f76c43b47f40cf424dfdc1d4a7f6ed0e2f86cff99e8a33f0203010001a382035e3082035a3082015d0603551d200482015430820150307b06062b4c240101043071302a06082b06010505070202301e0c1c496e666f436572742053504120434e53204365727469666963617465304306082b060105050702011637687474703a2f2f7777772e6669726d612e696e666f636572742e69742f646f63756d656e74617a696f6e652f6d616e75616c692e7068703081d006052b4c1002013081c630819e06082b060105050702023081910c818e4964656e74696669657320582e3530392061757468656e7469636174696f6e206365727469666963617465732069737375656420666f7220746865206974616c69616e204e6174696f6e616c205365727669636520436172642028434e53292070726f6a65637420696e206163636f7264696e6720746f20746865206974616c69616e20726567756c6174696f6e302306082b060105050702011617687474703a2f2f7777772e636e6970612e676f762e697430220603551d11041b30198117616e647265612e7465737361726f406a656e69612e6974304e06082b0601050507010104423040303e06082b060105050730018632687474703a2f2f6f6373702e696e666f636572742e69742f4f4353505365727665725f4943452f4f435350536572766c6574300e0603551d0f0101ff0404030205a0301d0603551d250416301406082b0601050507030206082b0601050507030430250603551d12041e301c811a6669726d612e6469676974616c6540696e666f636572742e6974301f0603551d230418301680144168e4ffc764e314437f35c7ec8f6039673d3fa63081ec0603551d1f0481e43081e13081a4a081a1a0819e86819b6c6461703a2f2f6c6461702e696e666f636572742e69742f636e253364496e666f4365727425323053657276697a6925323064692532304365727469666963617a696f6e6525323043524c30312c6f75253364456e7465253230436572746966696361746f72652c6f253364494e464f434552542532305350412c6325336449543f63657274696669636174655265766f636174696f6e4c6973743038a036a0348632687474703a2f2f7777772e6361726d2e696e666f636572742e69742f496e666f436572742f4155542f43524c30312e63726c301d0603551d0e04160414019e0d4b9248eb630c9462ff8d90ae637fae6d98300d06092a864886f70d010105050003820101005771aca28818b3207afb24e8ef67fc72bcae253f65a54704088c01bf342d332eb79f68c41f47b71454754d39f778caa151e44dde5f3e8c0da33f7c1c90d6a488cd8549d5ed4d34c519e289ff7bd7f55eaad59a18beeaf9aed8f684d6bd7274e704448741a7ef8f05d3f93cd0106ba92db6548e46091c9a86f2fdf45f35d196e7b9960ad1ffa439979650bcc3f1b6485902f27bc5eef7cdfc0622cf48899c7f259ecb8307753cc1a1b517618cb2ed9060ee8e5a861010c774756416eb590d9161c3309931233659238fc5cbbbd19e5291d3ebf31bb262f6003d122a7ed64bfa4de09bcb262ad067e3fcb59c25cda7f683b1a0eb99491319621478aa1963a9c559" )	
			};
			
			// Declare the signature state variables
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
			
			// Declare the streams involved by the sign process
			InputStream contentIs;
			OutputStream detachedSignOs;
			OutputStream embeddedSignOs;
			OutputStream tsResultOs;
			OutputStream markedSignOs;
			
			// Begin the process - an empty signature initialized with the SHA256withRSA and RSA algorithms
			emptySignature = new CMSSignatureInfo ( conf.getSignatureAlgorithm(), conf.getDigestAlgorithm() ); 
			
			// Add to the empty signature the timeStamp requested
			TsRequestInfo tsRequest = null;
			if ( applyMark ) {
				tsRequest = new TsRequestInfo ( SignDisposition.TimeStamp.ATTRIBUTE,
											    DigestAlgorithm.SHA256,
											    BigInteger.TEN,
											    "http://ca.signfiles.com/TSAServer.aspx", "", "" );
			}
			emptySignature.setTsRequest(tsRequest);
			
			// chain signature - populate the empty signature with the certificate chain
			chainSignature = emptySignature.toChainSignature ( certificateChain );
			
			// digest signature - join the content with the certificate chain and evaluate the digest   
			contentIs = new FileInputStream(srcFile);
			digestSignature = signatureService.doPreSign(chainSignature, contentIs);
			
			// signed signature - sign the digest with the privateKey to obtain the digitalSignature 
//			DigestInfo digest = digestSignature.getDigest();
//			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
//			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
//			byte[] digestInfoValue = ArrayUtils.addAll(
//					SHA256_DIGEST_INFO_PREFIX, digest.getFingerPrint());
//			byte[] digitalSignature = cipher.doFinal ( digestInfoValue );
			byte[] digitalSignature = HexUtils.decodeHex("e061e76516a249cbeffa13142969eb88e7f1fdfd4ae08b43fbf229c9ea9a10deff5c73a28bc921fefc79ae29250afbd5f338e9feebbaaeb0df46e2bbafe311b5310eb64da234829344975a5c85e347202849145e98375bfd8650880f617baeb77f6d8850df84cf0cdcc4f3f74d54bc420c7447d80050b42f602a35060467fabc");

			signedSignature = digestSignature.toSignedSignature ( digitalSignature );
			
			
			// finalized signature - generate the signedData, apply the timeStamp if any, and store it into the proper outputStream
			contentIs 		= new FileInputStream ( srcFile );
			detachedSignOs  = new ByteArrayOutputStream ( );
			if ( applyMark ) {
				embeddedSignOs  = new ByteArrayOutputStream ( );
				markedSignOs 	= new FileOutputStream ( getTestResource(MARKED_FILE) );
			} else {
				embeddedSignOs  = new FileOutputStream ( getTestResource(SIGNED_FILE) );
				markedSignOs 	= new ByteArrayOutputStream ( );
			}
			tsResultOs 		= new ByteArrayOutputStream ( );
			finalizedSignature = signatureService.doPostSign(signedSignature, contentIs, detachedSignOs, embeddedSignOs, tsResultOs, markedSignOs);
			
			Assert.isTrue ( finalizedSignature.isFinalized() );
		} catch(Exception e) {
			tracer.error(e.getMessage(), e);
			throw e;
		}
	}
}
