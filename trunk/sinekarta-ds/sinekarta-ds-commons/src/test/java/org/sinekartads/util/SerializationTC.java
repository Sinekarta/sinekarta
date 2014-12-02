package org.sinekartads.util;

import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.sinekartads.dto.domain.DigestDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.test.SkdsTestCase;
import org.sinekartads.util.x509.X509Utils;
import org.springframework.util.Assert;

public class SerializationTC extends SkdsTestCase {

	static final Logger tracer = Logger.getLogger ( SerializationTC.class );
	static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss zzz");
	static final String TEST_CERTIFICATE_HEX = "308206f4308205dca003020102020329f125300d06092a864886f70d0101050500308184310b300906035504061302495431153013060355040a0c0c494e464f4345525420535041311430120603550405130b3037393435323131303036311b3019060355040b0c12456e746520436572746966696361746f7265312b302906035504030c22496e666f436572742053657276697a69206469204365727469666963617a696f6e65301e170d3133303231313037333232335a170d3136303231313030303030305a30820114310b300906035504061302495431173015060355040a0c0e4e6f6e204469636869617261746f311e301c060355040b0c15432e432e492e412e412e20444920424f4c4f474e413147304506035504030c3e5453534e445237314c313842333933522f373432303032313830303338333331302e317656793858775046423431656a44514e51754b542b6e5254684d3d31193017060355040513105453534e445237314c313842333933523126302406092a864886f70d0109011617616e647265612e7465737361726f406a656e69612e6974310f300d060355042a0c06414e445245413116301406035504040c0d5445535341524f20504f52544131173015060355042e130e323031303131313235354137313230819f300d06092a864886f70d010101050003818d0030818902818100e86ac0f4e851153f5b515683b7232d3e0f14dfe6c3b3808d24c37a14af6ee29000227e1f416efe7f86314f33b6c3a0980ffe7d0078b27d3daa247ab062bbf9446a58d679e8053e2c56d6eb053f202bdcda1e34499228e3edc83cfc45ce157e1d157360d421f3d1b10f76c43b47f40cf424dfdc1d4a7f6ed0e2f86cff99e8a33f0203010001a382035e3082035a3082015d0603551d200482015430820150307b06062b4c240101043071302a06082b06010505070202301e0c1c496e666f436572742053504120434e53204365727469666963617465304306082b060105050702011637687474703a2f2f7777772e6669726d612e696e666f636572742e69742f646f63756d656e74617a696f6e652f6d616e75616c692e7068703081d006052b4c1002013081c630819e06082b060105050702023081910c818e4964656e74696669657320582e3530392061757468656e7469636174696f6e206365727469666963617465732069737375656420666f7220746865206974616c69616e204e6174696f6e616c205365727669636520436172642028434e53292070726f6a65637420696e206163636f7264696e6720746f20746865206974616c69616e20726567756c6174696f6e302306082b060105050702011617687474703a2f2f7777772e636e6970612e676f762e697430220603551d11041b30198117616e647265612e7465737361726f406a656e69612e6974304e06082b0601050507010104423040303e06082b060105050730018632687474703a2f2f6f6373702e696e666f636572742e69742f4f4353505365727665725f4943452f4f435350536572766c6574300e0603551d0f0101ff0404030205a0301d0603551d250416301406082b0601050507030206082b0601050507030430250603551d12041e301c811a6669726d612e6469676974616c6540696e666f636572742e6974301f0603551d230418301680144168e4ffc764e314437f35c7ec8f6039673d3fa63081ec0603551d1f0481e43081e13081a4a081a1a0819e86819b6c6461703a2f2f6c6461702e696e666f636572742e69742f636e253364496e666f4365727425323053657276697a6925323064692532304365727469666963617a696f6e6525323043524c30312c6f75253364456e7465253230436572746966696361746f72652c6f253364494e464f434552542532305350412c6325336449543f63657274696669636174655265766f636174696f6e4c6973743038a036a0348632687474703a2f2f7777772e6361726d2e696e666f636572742e69742f496e666f436572742f4155542f43524c30312e63726c301d0603551d0e04160414019e0d4b9248eb630c9462ff8d90ae637fae6d98300d06092a864886f70d010105050003820101005771aca28818b3207afb24e8ef67fc72bcae253f65a54704088c01bf342d332eb79f68c41f47b71454754d39f778caa151e44dde5f3e8c0da33f7c1c90d6a488cd8549d5ed4d34c519e289ff7bd7f55eaad59a18beeaf9aed8f684d6bd7274e704448741a7ef8f05d3f93cd0106ba92db6548e46091c9a86f2fdf45f35d196e7b9960ad1ffa439979650bcc3f1b6485902f27bc5eef7cdfc0622cf48899c7f259ecb8307753cc1a1b517618cb2ed9060ee8e5a861010c774756416eb590d9161c3309931233659238fc5cbbbd19e5291d3ebf31bb262f6003d122a7ed64bfa4de09bcb262ad067e3fcb59c25cda7f683b1a0eb99491319621478aa1963a9c559"; 
	static final X509Certificate TEST_CERTIFICATE;
	static {
		try {
			TEST_CERTIFICATE = X509Utils.rawX509CertificateFromHex ( TEST_CERTIFICATE_HEX );
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		}
	}

	
	@Test
	public void test() {
		
		// Prepare the base signature
		DigestDTO digest;
		
		digest = new DigestDTO();
		digest.setDigestAlgorithmName("SHA1");
		digest.setHexFingerPrint(HexUtils.randomHex(20));
		
		TimeStampDTO timeStamp0 = new TimeStampDTO ( );
		timeStamp0.setDisposition("ATTRIBUTE");
		timeStamp0.setMessageImprint(digest);
		timeStamp0.setTsaName("www.blia.com");
		timeStamp0.setSigningTime(dateFormat.format(new Date()));
		
		digest = new DigestDTO();
		digest.setDigestAlgorithmName("SHA256");
		digest.setHexFingerPrint(HexUtils.randomHex(64));
		
		TimeStampDTO timeStamp1 = new TimeStampDTO ( );
		timeStamp1.setDisposition("ENVELOPING");
		timeStamp1.setMessageImprint(digest);
		timeStamp1.setTsaName("www.infocer.it");
		timeStamp1.setSigningTime(dateFormat.format(new Date()));
		
		TimeStampDTO[] timeStamps = new TimeStampDTO[2];
		timeStamps[0] = timeStamp0;
		timeStamps[1] = timeStamp1;
		
		TimeStampRequestDTO timeStampRequest = new TimeStampRequestDTO();
		timeStampRequest.setMessageImprintAlgorithm("SHA256");
		timeStampRequest.setMessageImprintDigest(HexUtils.randomHex(64));
		timeStampRequest.setNounce(BigInteger.TEN.toString());
		timeStampRequest.setTimestampDisposition("ATTRIBUTE");
		timeStampRequest.setTsUrl("http://ca.signfiles.com/TSAServer.aspx");
		timeStampRequest.setTsUsername("");
		timeStampRequest.setTsPassword("");
		
		digest = new DigestDTO ( );
		digest.setDigestAlgorithmName("SHA256");
		digest.setHexFingerPrint ( HexUtils.randomHex(64) );
		
		SignatureDTO baseSignature = new SignatureDTO ( );
		baseSignature.setSignAlgorithm("SHA256withRSA");
		baseSignature.setDigest(digest);
		baseSignature.setHexCertificateChain(new String[] {TEST_CERTIFICATE_HEX});
		baseSignature.setHexDigitalSignature(HexUtils.randomHex(256));
		baseSignature.setLocation("Casalecchio di Reno");
		baseSignature.setReason("Signed with SineKarta");
		baseSignature.setSigningTime(dateFormat.format(new Date()));
		baseSignature.setTimeStamps(timeStamps);
		baseSignature.setTimeStampRequest(timeStampRequest);

		SignatureDTO clone;
		
		// Generate the cmsSignature by serialization 
		SignatureDTO cmsSignature = TemplateUtils.Encoding.deserialize ( 
				SignatureDTO.class, 
				TemplateUtils.Encoding.serialize(baseSignature) );
		cmsSignature.setSignCategory("CMS");
		cmsSignature.setSignDisposition("EMBEDDED");
		
		// Generate the cmsSignature by clonation
		SignatureDTO pdfSignature = TemplateUtils.Instantiation.clone ( baseSignature );
		pdfSignature.setSignCategory("PDF");
		pdfSignature.setSignDisposition("DETACHED");
		
		// Verify the JSON serialization
		String json = TemplateUtils.Encoding.serializeJSON ( cmsSignature, true );
		tracer.info ( String.format("cmsSignature as json: \n%s", json) );
		clone = TemplateUtils.Encoding.deserializeJSON ( SignatureDTO.class, json );
		Assert.isTrue ( clone.equals(cmsSignature) );

		// Verify the JSON serialization
		String base64 = TemplateUtils.Encoding.serializeBase64 ( cmsSignature );
		tracer.info ( String.format("cmsSignature as base64: \n%s", base64) );
		clone = TemplateUtils.Encoding.deserializeBase64 ( SignatureDTO.class, base64 );
		Assert.isTrue ( clone.equals(cmsSignature) );
		
		// Verify the hex serialization
		String hex = TemplateUtils.Encoding.serializeHex ( pdfSignature );
		tracer.info ( String.format("cmsSignature as hex: \n%s", hex) );
		clone = TemplateUtils.Encoding.deserializeHex ( SignatureDTO.class, hex );
		Assert.isTrue ( clone.equals(pdfSignature) );
		
		// TODO implement and verify the XML serialization
	}

}
