//package org.sinekartads.controller;
//
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//
//import org.apache.commons.codec.digest.DigestUtils;
//import org.apache.commons.lang.math.RandomUtils;
//import org.apache.log4j.Logger;
//import org.junit.Test;
//import org.sinekartads.SinekartaDsTestCase;
//import org.sinekartads.controller.ControllerTC.FakeSmartCardAccess.FakeScaCtrl;
//import org.sinekartads.keyring.AbstractKeyStore.FileSystemKeyStoreImpl;
//import org.sinekartads.model.CMSSignatureInfo;
//import org.sinekartads.model.CertificateInfo;
//import org.sinekartads.model.DigestInfo;
//import org.sinekartads.model.SignatureDisposition;
//import org.sinekartads.model.SignatureInfo;
//import org.sinekartads.model.SignatureType;
//import org.sinekartads.model.TrustedKeyRing;
//import org.sinekartads.model.TrustedKeyStore.KeyStoreType;
//import org.sinekartads.oid.DigestAlgorithm;
//import org.sinekartads.oid.SignatureAlgorithm;
//
//public class ControllerTC extends SinekartaDsTestCase {
//	
//	public static final String KEY_STORE_PATH = "JENIA.p12";
//	public static final String KEY_STORE_PASSWORD = "skdscip";
//	private static Logger tracer = Logger.getLogger(ControllerTC.class);
//	
//
//	
//	// -----
//	// --- 			state-safe SmartCardAccess - fake implementation
//	// --- The public protocol will only expose the FakeScaCtrl which will allow the user 
//	// --- to perform all the tasks.
//	// --- Using the controller have mainly two advantages: 
//	// ---  > better understanding of the internal state during the interface planning
//	// ---  > state-safe services implementation, without being concerned about the invocation order
//	// -
//	
//	class FakeSmartCardAccess {
//		
//		public int INIT_STEPS = 2;
//		public int STEP1_LOGIN = 0;
//		public int STEP2_CERTIFICATE_SELECTED = 1;
//		
//		class FakeScaCtrl extends Controller {
//
//			public FakeScaCtrl() {
//				super(INIT_STEPS);
//			}
//			
//			public void login(String pin) throws IllegalStateException, SmartCardAccessException {
//				tracer.info(String.format("logging in with pin '%s'...", pin));
//				
//				FakeSmartCardAccess.this.doLogin(pin);
//				goToInitStep(STEP1_LOGIN);
//			}
//
//			public List<CertificateInfo> certificateList()
//					throws SmartCardAccessException {
//				tracer.info("loading the certificateList inside the smartcard...");
//				verifyInitializationState(STEP1_LOGIN);
//				
//				return doCertificateList();
//			}
//
//			public void selectCertificate(CertificateInfo certificate) 
//					throws IllegalStateException, SmartCardAccessException {
//				tracer.info(String.format("selection of af the signing certificate '%s' ...", certificate.getSubjectDN()));
//				verifyInitializationState(STEP1_LOGIN);
//				
//				doSelectCertificate(certificate);
//				goToInitStep(STEP2_CERTIFICATE_SELECTED);
//			}
//
//			public CMSSignatureInfo signContent(SignatureAlgorithm sigAlgorithm, byte[] content) 
//					throws IllegalStateException, IllegalArgumentException, SmartCardAccessException {
//				tracer.info("applying a digital signature on a byte array...");
//				verifyControllerState();
//				
//				return doSignFingerPrint ( 
//						sigAlgorithm, 
//						DigestInfo.getInstance(DigestAlgorithm.SHA256, DigestUtils.sha256(content)) );
//			}
//			
//			public SignatureInfo<CMSSignatureInfo> signFingerPrint ( 
//					SignatureAlgorithm sigAlgorithm, 
//					DigestInfo digestInfo ) 
//							throws IllegalStateException, 
//									IllegalArgumentException, 
//									SmartCardAccessException {
//				tracer.info("applying a digital signature on a digestInfo...");
//				verifyControllerState();
//				
//				return doSignFingerPrint(sigAlgorithm, digestInfo);
//			}
//
//
//			public void close() throws SmartCardAccessException {
//				tracer.info("closing the sca...");
//				
//				closeController();
//				doClose();
//			}
//
//			public String getPKCS11Driver() {
//				tracer.info("retrieving the driver...");
//				
//				return FakeSmartCardAccess.this.driver;
//			}
//
//			public String getPin() {
//				tracer.info("retrieving the pin...");
//				verifyInitializationState(STEP1_LOGIN);
//				
//				return FakeSmartCardAccess.this.pin;
//			}
//		}
//		
//				
//		
//		// -----
//		// --- Fake smart card methods implementation
//		// -
//		
//		class SmartCardAccessException extends Exception {
//			private static final long serialVersionUID = -1224499242499666390L;
//			SmartCardAccessException() 					{ }
//			SmartCardAccessException(String message) 	{super(message);}
//			SmartCardAccessException(Throwable cause) 	{super(cause);}
//		}
//		
//		TrustedKeyRing source;
//		CertificateInfo certificate;
//		String pin;
//		
//		private void doLogin(String pin) throws IllegalStateException, SmartCardAccessException {
//			// fake implementation
//			try {
//				source = new FileSystemKeyStoreImpl(
//						"FakeSmartCardAccess", 
//						getTestResource(KEY_STORE_PATH), 
//						KeyStoreType.P12, pin);
//				this.pin = pin;
//			} catch(Exception e) {
//				throw new SmartCardAccessException(e);
//			}
//		}
//		
//		private void doSelectCertificate(CertificateInfo certificate) 
//				throws SmartCardAccessException {
//			this.certificate = certificate; 
//		}
//		
//		private List<CertificateInfo> doCertificateList() throws SmartCardAccessException {
//			// fake implementation
//			List<CertificateInfo> certificateList = new ArrayList<CertificateInfo>();
//			try {
//				Enumeration<String> aliasEnum = source.aliases();
//				while(aliasEnum.hasMoreElements()) {
//					certificateList.add(source.getCertificate(aliasEnum.nextElement()));
//				}
//				
//			} catch (Exception e) {
//				throw new SmartCardAccessException(e); 
//			}
//			return certificateList;
//		}
//		
//		private CMSSignatureInfo doSignFingerPrint(SignatureAlgorithm sigAlgorithm, DigestInfo digestInfo) 
//				throws IllegalArgumentException, SmartCardAccessException {
//			// fake implementation
//			CMSSignatureInfo signatureInfo;
//			try {
//				signatureInfo = new CMSSignatureInfo (
//						SignatureType.CMS.CAdES, 
//						sigAlgorithm, 
//						SignatureDisposition.CMS.EMBEDDED );
//			} catch(Exception e) {
//				throw new SmartCardAccessException(e);
//			}
//			return signatureInfo.setCertificate(certificate);
//		}
//		
//		private void doClose() throws SmartCardAccessException {
//			// fake implementation
//		}
//		
//		
//		
//		// -----
//		// --- Public interface: all the operations will be accessible through the controller
//		// -
//		
//		FakeScaCtrl scaCtrl = new FakeScaCtrl();
//		final String driver;
//		
//		public FakeSmartCardAccess(String driver) {
//			this.driver = driver;
//		}
//		
//		public FakeScaCtrl getController() {
//			return scaCtrl;
//		}
//		
//	}
//	
//		  
//	
//
//	@Test
//	public void test() throws Exception {
//		FakeScaCtrl sca = new FakeSmartCardAccess("fake").getController();
//		byte[] content = "pippo".getBytes();
//		DigestInfo digestInfo = DigestInfo.getInstance(DigestAlgorithm.SHA256, DigestUtils.sha256(content));
//		List<CertificateInfo> certificateList = null;
//				
//		tracer.info("trying to run randomly all the operations");
//		int random;
//		boolean signature = false;
//		for(int i=0; i<50 && !signature; i++) {
//			tracer.info(String.format("current state: %s", sca.state));
//			random = RandomUtils.nextInt() % 100;
//			try {
//				if( 	  between(random,  0, 14)) {							// login				15
//					sca.login(KEY_STORE_PASSWORD);
//				} else if(between(random, 15, 24)) {							// certificateList		10
//					certificateList = sca.certificateList();
//				} else if(between(random, 25, 39)) {							// selectCertificate	15			
//					if(certificateList != null) { 
//						sca.selectCertificate(
//								certificateList.get( RandomUtils.nextInt() % certificateList.size()) );
//					}
//				} else if(between(random, 40, 59)) {							// signContent			20
//					sca.signContent(SignatureAlgorithm.SHA256withRSA, content);
//					signature = true;
//				} else if(between(random, 60, 79)) {							// signFingerPrint		20
//					sca.signFingerPrint(SignatureAlgorithm.SHA256withRSA, digestInfo) ;
//					signature = true;
//				} else if(between(random, 80, 84)) {							// close				 5
//					sca.close();
//					certificateList = null;
//				} else if(between(random, 85, 90)) {							// driver				 5
//					tracer.info(String.format("driver: %s", sca.getPKCS11Driver())); 
//				} else if(between(random, 90, 99)) {							// pin					10
//					tracer.info(String.format("pin: %s", sca.getPin()));					
//				}
//			} catch(IllegalStateException e) {
//				tracer.error(e);
//			}
//			if(i > 1000) {
//				throw new Exception("the signature didn't work in 1000 attempts...");
//			}
//		}
//		
//	}
//	
//	static boolean between(int value, int min, int max) {
//		return value>=min && value<=max;
//	}
//}
