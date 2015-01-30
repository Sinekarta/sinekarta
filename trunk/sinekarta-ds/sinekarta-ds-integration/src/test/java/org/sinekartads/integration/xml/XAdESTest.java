//package org.sinekartads.integration.xml;
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//import static it.j4ops.PropertyConstants.DigestAlgName;
//import static it.j4ops.PropertyConstants.EncryptionAlgName;
//import static it.j4ops.PropertyConstants.EnvelopeSignType;
//import static it.j4ops.PropertyConstants.TSAPassword;
//import static it.j4ops.PropertyConstants.TSAURL;
//import static it.j4ops.PropertyConstants.TSAUser;
//import it.j4ops.XmlSignMode;
//import it.j4ops.sign.BaseSignHandler;
//import it.j4ops.sign.SignHandler;
//import it.j4ops.sign.XmlSign;
//import it.j4ops.sign.provider.ExternalProvider;
//import it.j4ops.sign.provider.IaikPKCS11Provider;
//import it.j4ops.test.XmlXMLDSIGTest;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.security.cert.X509Certificate;
//import java.util.Date;
//import java.util.Properties;
//
//import junit.framework.TestCase;
//import junit.textui.TestRunner;
//
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.Logger;
//import org.apache.log4j.xml.DOMConfigurator;
//import org.sinekartads.applet.AppletResponseDTO;
//import org.sinekartads.applet.SignApplet;
//import org.sinekartads.applet.AppletResponseDTO.ActionErrorDTO;
//import org.sinekartads.applet.AppletResponseDTO.FieldErrorDTO;
//import org.sinekartads.core.service.XMLSignatureService;
//import org.sinekartads.dto.domain.SignatureDTO;
//import org.sinekartads.dto.domain.VerifyDTO;
//import org.sinekartads.dto.jcl.PostSignResponseDTO;
//import org.sinekartads.dto.jcl.PreSignResponseDTO;
//import org.sinekartads.dto.jcl.VerifyResponseDTO;
//import org.sinekartads.dto.tools.DTOConverter;
//import org.sinekartads.integration.BaseIntegrationTC;
//import org.sinekartads.model.domain.DigestInfo;
//import org.sinekartads.util.HexUtils;
//import org.sinekartads.util.x509.X509Utils;
//import org.sinekartads.utils.JSONUtils;
//
///**
// *
// * @author fzanutto
// */
//public class XAdESTest extends BaseIntegrationTC {
//	Logger tracer = Logger.getLogger(getClass());
//	
//    private Properties prop = new Properties ();
//    private ExternalProvider signProvider = null;     
//    private SignHandler signHandler = null;    
//    
//    public XAdESTest () throws Exception {
//        signHandler = new BaseSignHandler("skdscip");            
//        signProvider = new ExternalProvider();   
//        
//        prop.setProperty(DigestAlgName.getLiteral(), "SHA256"); 
//        prop.setProperty(EncryptionAlgName.getLiteral(), "RSA"); 
//        prop.setProperty(EnvelopeSignType.getLiteral(), "XAdES_T");      
//        prop.setProperty(TSAURL.getLiteral(), "http://ca.signfiles.com/TSAServer.aspx");
//        prop.setProperty(TSAUser.getLiteral(), "");
//        prop.setProperty(TSAPassword.getLiteral(), "");
////        prop.setProperty(TSAURL.getLiteral(), "https://marte.infocert.it/cdie/HttpService");
////        prop.setProperty(TSAUser.getLiteral(), "andrea.tessaro@jenia.it");
////        prop.setProperty(TSAPassword.getLiteral(), "TdMiHTk5");
//    }
//    
//    public void testSignXAdES () throws Exception {
//    	
//    	// Main options
//    	SignApplet applet = new SignApplet();
//		String driver = "libbit4ipki.so";
//		String scPin = "18071971";
//			
//		// Test products
//		String[] aliases;
//		String alias;
//		X509Certificate certificate;
//		X509Certificate[] certificateChain;
//		byte[] fingerPrint;
//		byte[] digitalSignature;
//		File envelopeFile;
//		String envelopeHex; 
//		
//		// Communication unities
//		DTOConverter converter = DTOConverter.getInstance(); 
//		String jsonResp;
//		AppletResponseDTO appletResponse;
//		
//		// Prepare the signature service
//		XMLSignatureService signatureService = new XMLSignatureService();
//		
//		// Init the applet
//		try {
//			applet.init();
//			jsonResp = applet.selectDriver ( driver );
//			appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
//		} catch(Exception e) {
//			tracer.error("error during the applet initialization", e);
//			throw e;
//		}
//		
//		// Login with the smartCard
//		try {
//			jsonResp = applet.login ( scPin );
//			appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
//			aliases = (String[]) JSONUtils.deserializeJSON ( String[].class, extractJSON(appletResponse) );
//		} catch(Exception e) {
//			tracer.error("error during the applet login", e);
//			throw e;
//		}
//		
//		// Choose the signing alias
//		StringBuilder buf = new StringBuilder();
//		for ( String a : aliases ) {
//			buf.append(a).append(" ");
//		}
//		alias = aliases[0];
//		tracer.info(String.format ( "available aliases:   %s", buf ));
//		tracer.info(String.format ( "signing alias:       %s", alias ));
//		
//		// Load the certificate chain from the applet
//		try {
//			jsonResp = applet.selectCertificate ( alias );
//			appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
//			certificate = (X509Certificate) X509Utils.rawX509CertificateFromHex( extractJSON(appletResponse) );
//			tracer.info(String.format ( "certificate:         %s", certificate ));
//			certificateChain = new X509Certificate[] { certificate };
//		} catch(Exception e) {
//			tracer.error("error during the certificate selection", e);
//			throw e;
//		}
//    				
//    	
//        // set property     
//        XmlSign xmlSign = new XmlSign (signProvider, signHandler, prop);        
//        
//        // sign
//        File f = getTestResource("document.xml");
//        FileInputStream fis = new FileInputStream (f);              
//        FileOutputStream fos = new FileOutputStream (getTestResource("sign.xml"));                     
//        try {
//        	signProvider.setX509Certificate(certificate);
//            xmlSign.init();
//            fingerPrint = xmlSign.digest(new Date(), XmlSignMode.Enveloped, f.toURI().toURL().toString(), fis, fos);
//        } catch(Exception e) {
//        	tracer.error(e.getMessage(), e);
//        	throw e;
//        }
//        
//		try {
//			tracer.info(String.format ( "fingerPrint:         %s", HexUtils.encodeHex(fingerPrint) ));
//			jsonResp = applet.signDigest( HexUtils.encodeHex(fingerPrint) );
//			appletResponse = (AppletResponseDTO) JSONUtils.deserializeJSON(AppletResponseDTO.class, jsonResp);
//			digitalSignature = HexUtils.decodeHex ( (String) extractJSON(appletResponse) );
//			tracer.info(String.format ( "digitalSignature:    %s", HexUtils.encodeHex(digitalSignature) ));
//		} catch(Exception e) {
//			tracer.error("error during the digital signature evaluation", e);
//			throw e;
//		}
//		
//		try {
//			fis = new FileInputStream (f);              
//	        fos = new FileOutputStream (getTestResource("sign.xml"));
//			signProvider.setDigitalSignature(digitalSignature);
//            xmlSign.sign(new Date(), XmlSignMode.Enveloped, f.toURI().toURL().toString(), fis, fos);
//        } catch(Exception e) {
//        	tracer.error(e.getMessage(), e);
//        	throw e;
//        }
//
//        xmlSign.destroy();
//        fis.close();
//        fos.close();           
//    }
//    
////    public void testAddSignXAdES () throws Exception {
////        // set property     
////        XmlSign xmlSign = new XmlSign (signProvider, signHandler, prop);          
////        
////        // add sign
////        File f = new File ("./test/XAdES/sign.xml");
////        FileInputStream fis = new FileInputStream (f);      
////        FileOutputStream fos = new FileOutputStream ("./test/XAdES/add_sign.xml");                     
////        try {
////            xmlSign.init();
////            xmlSign.addSign(new Date(), XmlSignMode.Enveloped, f.toURI().toURL().toString(), fis, fos);
////        }
////        finally {
////            xmlSign.destroy();
////        }
////        fis.close();
////        fos.close();           
////    }    
////    
////    public void testVerifySignXAdES () throws Exception { 
////        FileInputStream fis = new FileInputStream ("./test/XAdES/sign.xml");    
////        XmlVerify xmlVerify = new XmlVerify (new Properties ());        
////        VerifyInfo vi = xmlVerify.verify(fis);          
////        System.out.println (String.format("count signs:%d", vi.getCountSigns()));   
////        
////        if (vi.getCountSigns() != 1 || vi.getSignerInfos().get(0).getSignType() != SignType.XAdES_BES) {
////            throw new Exception ("Error on verify sign XAdES");
////        }
////    }
////    
////    public void testVerifyAddSignXAdES () throws Exception { 
////        FileInputStream fis = new FileInputStream ("./test/XAdES/add_sign.xml");   
////        XmlVerify xmlVerify = new XmlVerify (new Properties ());        
////        VerifyInfo vi = xmlVerify.verify(fis);            
////        System.out.println (String.format("count signs:%d", vi.getCountSigns()));   
////        
////        if (vi.getCountSigns() != 2 || 
////            vi.getSignerInfos().get(0).getSignType() != SignType.XAdES_BES ||
////            vi.getSignerInfos().get(1).getSignType() != SignType.XAdES_BES) {
////            throw new Exception ("Error on verify sign XAdES");
////        }
////    }       
//
//    
//    @Override
//    protected void setUp() {
////        DOMConfigurator.configure("log4j.xml");
////        new File("./test/XAdES/").mkdirs();
//    }        
//
//    @Override
//    public void tearDown() {
//    }
//
//    
//    private String extractJSON(AppletResponseDTO resp) throws Exception {
//		String resultCode = resp.getResultCode();
//		String json;
//		if ( StringUtils.equals(resultCode, AppletResponseDTO.SUCCESS) ) {
//			json = resp.getResult();
//		} else {
//			StringBuilder buf = new StringBuilder();
//			for ( FieldErrorDTO fieldError : resp.getFieldErrors() ) {
//				for ( String errorMessage : fieldError.getErrors() ) {
//					buf.append ( String.format("fieldError  - %s: %s\n", fieldError.getField(), errorMessage) );
//				}
//			}
//			for ( ActionErrorDTO actionError : resp.getActionErrors() ) {
//				buf.append ( String.format("actionError - %s\n", actionError.getErrorMessage()) );
//			}
//			throw new Exception ( buf.toString() );
//		}
//		return json;
//	}
//
//    /**
//     * Test
//     * @param args null
//     * @throws Exception in caso di errore
//     */
//    public static void main(String[] args) throws Exception {
//        TestRunner.run(XAdESTest.class);
//    }    
//}
