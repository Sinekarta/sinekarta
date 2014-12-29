//package org.sinekartads.verification.j4ops;
//
//import it.j4ops.SignType;
//import it.j4ops.verify.CmsVerify;
//import it.j4ops.verify.PdfVerify;
//import it.j4ops.verify.XmlVerify;
//import it.j4ops.verify.bean.VerifyInfo;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.util.Properties;
//
//import org.sinekartads.integration.cms.SignCMSwithSmartCardAndDTO;
//import org.sinekartads.integration.xml.SignXMLwithSmartCardAndDTO;
//import org.sinekartads.test.SkdsTestCase;
//
//public class J4OpsValidation extends SkdsTestCase{
//	
//    public void testVerifySignCAdES () throws Exception { 
//        FileInputStream fis = new FileInputStream(getTestResource (
//        		SignCMSwithSmartCardAndDTO.class, 
//        		SignCMSwithSmartCardAndDTO.SIGNED_FILE) );
//        FileOutputStream fos = new FileOutputStream(getTestResource (
//        		SignCMSwithSmartCardAndDTO.class, 
//        		SignCMSwithSmartCardAndDTO.EXTRACTED_FILE) );    
//        CmsVerify p7xVerify = new CmsVerify (new Properties ());        
//        VerifyInfo vi = p7xVerify.verify(fis, null, fos);            
//        System.out.println (String.format("count signs:%d", vi.getCountSigns()));   
//        
//        if (vi.getCountSigns() != 1 || vi.getSignerInfos().get(0).getSignType() != SignType.CAdES_BES) {
//            throw new Exception ("Error on verify sign CAdES");
//        }
//    }
//	
//    public void testVerifySignPAdES () throws Exception { 
//        FileInputStream fis = new FileInputStream ("./test/PAdES/sign.pdf");    
//        PdfVerify pdfVerify = new PdfVerify (new Properties ());        
//        VerifyInfo vi = pdfVerify.verify(fis);            
//        System.out.println (String.format("count signs:%d", vi.getCountSigns()));   
//        
//        if (vi.getCountSigns() != 1 || vi.getSignerInfos().get(0).getSignType() != SignType.PAdES_BES) {
//            throw new Exception ("Error on verify sign PAdES");
//        }
//    }
//	
//    public void testVerifySignXAdES () throws Exception { 
//        FileInputStream fis = new FileInputStream (getTestResource (
//        		SignXMLwithSmartCardAndDTO.class, 
//        		SignXMLwithSmartCardAndDTO.SIGNED_FILE) );
//        XmlVerify xmlVerify = new XmlVerify (new Properties ());        
//        VerifyInfo vi = xmlVerify.verify(fis);          
//        System.out.println (String.format("count signs:%d", vi.getCountSigns()));   
//        
//        if (vi.getCountSigns() != 1 || vi.getSignerInfos().get(0).getSignType() != SignType.XAdES_BES) {
//            throw new Exception ("Error on verify sign XAdES");
//        }
//    }
//	
//}
//
//
