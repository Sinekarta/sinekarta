package org.sinekartads.integration.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.sinekartads.test.SkdsTestCase;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.PdfPKCS7;

public class PDFVerify extends SkdsTestCase {
	
	String[] signedFiles = {
			"pippo_sgn.pdf",
			"pippo_mrk.pdf"
	};
	
	
	@Test
	public void test() throws Exception {
 
		Security.addProvider(new BouncyCastleProvider());

		File file;
		PrintWriter out = new PrintWriter(System.out);
		for ( String signedFile : signedFiles ) {
			out.println("SignedFile: " + signedFile);
			file = getTestResource(SignPDFwithSmartCardAndDTO.class, signedFile);
	        PdfReader reader = new PdfReader(new FileInputStream(file));
	        AcroFields af = reader.getAcroFields();
	        ArrayList<String> names = af.getSignatureNames();
	        for (String name : names) {
	            out.println("Signature name: " + name);
	            out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
	            out.println("Document revision: " + af.getRevision(name) + " of " + af.getTotalRevisions());
	            PdfPKCS7 pk = af.verifySignature(name, "BC");
	            out.println("verify : " + pk.verify());
	            Calendar cal = pk.getSignDate();
	            Certificate[] pkc = pk.getCertificates();
	            out.println("Subject: " + CertificateInfo.getSubjectFields(pk.getSigningCertificate()));
	            out.println("Revision modified: " + !pk.verify());
	            
	            String errors = CertificateVerification.verifyCertificate((X509Certificate)pkc[0], new ArrayList<CRL>(), cal);
	            if (errors == null)
	                out.println("Certificates verified against the KeyStore");
	            else
	                out.println(errors);
	            if(pk.verify()) {
	            	out.println("the signature has been verified");
	            } else {
	            	out.println("the signature is invalid");
	            }
	            
	            if(pk.getTimeStampToken() != null) {
	            	if(pk.verify()) {
	                	out.println("the timestamp has been verified");
	                } else {
	                	out.println("the timestamp is invalid");
	                }
	            }
	            
	            out.println("dictionary entries ");
	            PdfDictionary dict = af.getSignatureDictionary(name);
	            for(PdfName key : dict.getKeys()) {
	            	if ( key.equals(PdfName.CONTENTS) ) {
	            		out.println(key + "->" + "<CADES signature>");
	            	} else {
	            		out.println(key + "->" + dict.getDirectObject(key));
	            	}
	            }
	            
	            byte[] signEnc = dict.get(PdfName.CONTENTS).getBytes();
	            String destName = "enc_"+signedFile;
	            out.println("signed data stored into " + destName);
	            FileUtils.writeStringToFile(getTestResource(destName), Base64.encodeBytes(signEnc));
	        }
        }
        out.flush();
        out.close();
    }
	
}


