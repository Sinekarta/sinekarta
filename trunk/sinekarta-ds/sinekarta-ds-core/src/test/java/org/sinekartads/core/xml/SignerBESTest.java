/*
 * XAdES4j - A Java library for generation and verification of XAdES signatures.
 * Copyright (C) 2010 Luis Goncalves.
 *
 * XAdES4j is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or any later version.
 *
 * XAdES4j is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with XAdES4j. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sinekartads.core.xml;

import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.sinekartads.util.x509.X509Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.production.DataObjectReference;
import xades4j.production.EnvelopedXmlObject;
import xades4j.production.SignatureAppendingStrategies;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.production.XadesSigningProfile;
import xades4j.properties.AllDataObjsCommitmentTypeProperty;
import xades4j.properties.CommitmentTypeProperty;
import xades4j.properties.DataObjectDesc;
import xades4j.properties.DataObjectFormatProperty;
import xades4j.providers.KeyingDataProvider;

/**
 *
 * @author LuÃ­s
 */
public class SignerBESTest extends SignerTestBase
{
	Logger tracer = Logger.getLogger(getClass());
	
	KeyingDataProvider kp;
	XadesSigner signer;
	
    public SignerBESTest()
    {
    	try {
	    	String alias = "Alessandro De Prato";
	        kp = kdpFactory.getKeyRingDataProvider(alias);
	        
	        XadesSigningProfile p = new XadesBesSigningProfile(kp);
	        signer = p.newSigner();
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    

    @Test
    public void testSignBES() throws Exception
    {
        tracer.info("signBES");
        try {
        Document doc1 = getTestDocument();
        Document doc2 = getDocument("document.xml");
        Node objectContent = doc1.importNode(doc2.getDocumentElement(), true);
        Element elemToSign = doc1.getDocumentElement();
//        SignerBES signer = (SignerBES)new XadesBesSigningProfile(keyingProviderMy).newSigner();

//        IndividualDataObjsTimeStampProperty dataObjsTimeStamp = new IndividualDataObjsTimeStampProperty();
        AllDataObjsCommitmentTypeProperty globalCommitment = AllDataObjsCommitmentTypeProperty.proofOfApproval();
        CommitmentTypeProperty commitment = CommitmentTypeProperty.proofOfCreation();
        DataObjectDesc obj1 = new DataObjectReference('#' + elemToSign.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform()).withDataObjectFormat(new DataObjectFormatProperty("text/xml", "MyEncoding").withDescription("Isto Ã© uma descriÃ§Ã£o do elemento raiz").withDocumentationUri("http://doc1.txt").withDocumentationUri("http://doc2.txt").withIdentifier("http://elem.root")).withCommitmentType(commitment);
        DataObjectDesc obj2 = new EnvelopedXmlObject(objectContent, "text/xml", null).withDataObjectFormat(new DataObjectFormatProperty("text/xml", "MyEncoding").withDescription("Isto Ã© uma descriÃ§Ã£o do elemento dentro do object").withDocumentationUri("http://doc3.txt").withDocumentationUri("http://doc4.txt").withIdentifier("http://elem.in.object")).withCommitmentType(commitment);
        SignedDataObjects dataObjs = new SignedDataObjects(obj1, obj2).withCommitmentType(globalCommitment);
//      DataObjectDesc obj1 = new DataObjectReference('#' + elemToSign.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform()).withDataObjectFormat(new DataObjectFormatProperty("text/xml", "MyEncoding").withDescription("Isto Ã© uma descriÃ§Ã£o do elemento raiz").withDocumentationUri("http://doc1.txt").withDocumentationUri("http://doc2.txt").withIdentifier("http://elem.root")).withCommitmentType(commitment).withDataObjectTimeStamp(dataObjsTimeStamp);
//      DataObjectDesc obj2 = new EnvelopedXmlObject(objectContent, "text/xml", null).withDataObjectFormat(new DataObjectFormatProperty("text/xml", "MyEncoding").withDescription("Isto Ã© uma descriÃ§Ã£o do elemento dentro do object").withDocumentationUri("http://doc3.txt").withDocumentationUri("http://doc4.txt").withIdentifier("http://elem.in.object")).withCommitmentType(commitment).withDataObjectTimeStamp(dataObjsTimeStamp);
//      SignedDataObjects dataObjs = new SignedDataObjects(obj1, obj2).withCommitmentType(globalCommitment).withDataObjectsTimeStamp();
        

        signer.sign(dataObjs, elemToSign);

        outputDocument(doc1, DOCUMENT_BASE+"_bes.xml");
	    } catch(Exception e) {
	    	tracer.error(e.getMessage(), e);
	    	throw e;
	    }
    }

    @Test
    public void testSignBESAndAppendAsFirstChild() throws Exception
    {
    	try {
        tracer.info("signBESAndAppendAsFirstChild");

        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
//        SignerBES signer = (SignerBES)new XadesBesSigningProfile(keyingProviderMy).newSigner();

        DataObjectDesc obj1 = new DataObjectReference('#' + root.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform());
        SignedDataObjects dataObjs = new SignedDataObjects(obj1);

        signer.sign(dataObjs, root, SignatureAppendingStrategies.AsFirstChild);

        outputDocument(doc, DOCUMENT_BASE+"_firstchild.xml");
	    } catch(Exception e) {
	    	tracer.error(e.getMessage(), e);
	    	throw e;
	    }
    }

    @Test
    public void testSignBESExtrnlRes() throws Exception
    {
    	try {
        tracer.info("signBESExtrnlRes");

        Document doc = getNewDocument();
//        SignerBES signer = (SignerBES)new XadesBesSigningProfile(keyingProviderNist).newSigner();

        DataObjectDesc obj1 = new DataObjectReference("rfc3161.txt").withDataObjectFormat(new DataObjectFormatProperty("text/plain").withDescription("Internet X.509 Public Key Infrastructure - no Time-Stamp Protocol (TSP)"));
        //DataObjectDesc obj1 = new DataObjectReference("rfc3161.txt").withDataObjectFormat(new DataObjectFormatProperty("text/plain").withDescription("Internet X.509 Public Key Infrastructure Time-Stamp Protocol (TSP)")).withDataObjectTimeStamp(new IndividualDataObjsTimeStampProperty());
        signer.sign(new SignedDataObjects(obj1).withBaseUri("http://www.ietf.org/rfc/"), doc);

        outputDocument(doc, DOCUMENT_BASE+"_extrbes.xml");
	    } catch(Exception e) {
	    	tracer.error(e.getMessage(), e);
	    	throw e;
	    }
    }

    @Test
    public void testSignBESWithCounterSig() throws Exception
    {
        tracer.info("signBESWithCounterSig");
        try {
        Document doc = getTestDocument();
        Element elemToSign = doc.getDocumentElement();
/*
        XadesBesSigningProfile profile = new XadesBesSigningProfile(kp);/*
        final XadesSigner counterSigner = profile.newSigner();
        profile.withSignaturePropertiesProvider(new SignaturePropertiesProvider()
        {
            @Override
            public void provideProperties(
                    SignaturePropertiesCollector signedPropsCol)
            {
                signedPropsCol.addCounterSignature(new CounterSignatureProperty(counterSigner));
                signedPropsCol.setSignerRole(new SignerRoleProperty("CounterSignature maniac"));
            }
        });*/
//        SignerBES signer = (SignerBES)profile.newSigner();

        DataObjectDesc obj1 = new DataObjectReference('#' + elemToSign.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform());
        SignedDataObjects dataObjs = new SignedDataObjects().withSignedDataObject(obj1);

        signer.sign(dataObjs, elemToSign);

        outputDocument(doc, DOCUMENT_BASE+"_bescs.xml");
        } catch(Exception e) {
        	tracer.error(e.getMessage(), e);
        	throw e;
        }
    }
    
    static {
    	String base64String = "MIIFbjCCBFagAwIBAgIQeAJzQFnhaokUk1qQD/EoLDANBgkqhkiG9w0BAQsFADBsMQswCQYDVQQG"+
    	"EwJJVDEYMBYGA1UECgwPQXJ1YmFQRUMgUy5wLkEuMSEwHwYDVQQLDBhDZXJ0aWZpY2F0aW9uIEF1"+
    	"dGhvcml0eUMxIDAeBgNVBAMMF0FydWJhUEVDIFMucC5BLiBORyBDQSAzMB4XDTE0MTEwODAwMDAw"+
    	"MFoXDTE3MTEwNzIzNTk1OVowgeMxCzAJBgNVBAYTAklUMTswOQYDVQQKDDJKZW5pYSBTb2Z0d2Fy"+
    	"ZSBkaSBBbmRyZWEgVGVzc2FybyBQb3J0YS8wMzAxMjQwMTIwODEdMBsGA1UEAwwUVGVzc2FybyBQ"+
    	"b3J0YSBBbmRyZWExHDAaBgNVBAUTE0lUOlRTU05EUjcxTDE4QjM5M1IxDzANBgNVBCoMBkFuZHJl"+
    	"YTEWMBQGA1UEBAwNVGVzc2FybyBQb3J0YTERMA8GA1UELhMIMTM2MzUwNDgxHjAcBgNVBAwMFUxl"+
    	"Z2FsZSByYXBwcmVzZW50YW50ZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK6j3/tv"+
    	"JaZlLTOzHUcMyqnEGb5KW5jSvEiOPyJ+XHF5CRjuBvG2uTjk/Cq+s93sl3sfcvrDh7Qed8wL1Vsb"+
    	"FSCg9e/cB2RUu5lqkWfXCb3sHvRi9jeU2+dAxajX4IyI+85S0blzuvMMvM+2iJTXv3o031SYf9R2"+
    	"SzqjUYvCZl9r+PfJPx/S17/MQw5J04ZT5dfErCZkoFrps7ppND+Zx0KPEtQbU3bkeqty4M/TYiha"+
    	"8WSCDpuMZY2e5pbuZ8TUsXS49JI0ecYUSl652au4DbBlx83KqIXqvCWkw/Vsg/gi8Yxf7XnEY2z7"+
    	"q9d84Fnp1Th+/e331Di9U1+UKZVrgBUCAwEAAaOCAZIwggGOMA4GA1UdDwEB/wQEAwIGQDAdBgNV"+
    	"HQ4EFgQUozl01QhZx/qwPCD10ELnPmi0PSYwWAYDVR0gBFEwTzA8BgsrBgEEAYHoLQEBDjAtMCsG"+
    	"CCsGAQUFBwIBFh9odHRwczovL2NhLmFydWJhcGVjLml0L2Nwcy5odG1sMIEOBgwrBgEEAYHoLQEB"+
    	"CwEwWAYDVR0fBFEwTzBNoEugSYZHaHR0cDovL2NybC5hcnViYXBlYy5pdC9BcnViYVBFQ1NwQUNl"+
    	"cnRpZmljYXRpb25BdXRob3JpdHlDL0xhdGVzdENSTC5jcmwwLwYIKwYBBQUHAQMEIzAhMAgGBgQA"+
    	"jkYBATALBgYEAI5GAQMCARQwCAYGBACORgEEMCIGA1UdEQQbMBmBF2FuZHJlYS50ZXNzYXJvQGpl"+
    	"bmlhLml0MB8GA1UdIwQYMBaAFPDARbG2NbTqXyn6gwNK3C/1s33oMDMGCCsGAQUFBwEBBCcwJTAj"+
    	"BggrBgEFBQcwAYYXaHR0cDovL29jc3AuYXJ1YmFwZWMuaXQwDQYJKoZIhvcNAQELBQADggEBAIzw"+
    	"7IkJufuiMOPZRMnYfq4aHouNqt+MpByTAqJLcZpcV+1cG6ugGjGDWE44rljDqVo0SCQoD/IxYT/e"+
    	"9C7tVlLuj0Ky42rPvor/6yV9NW/tYD/pduQLuVazjw3FlI9+qlN+GIbdGFLdHirJbR0R1utYUDHM"+
    	"CayMJHMbTuvYWYeRjfMTW4ZR4ZXNrNldILAEQ6yPqBWtbAtZAihY9lUpm7xRev6WL36sNMC5pd3v"+
    	"phDMkOqY1BGyrBeBVoT+zZem8cai9oRTE7WT4nrgK4+ZjlzUOiHZqQMVaovQMimY0c9rOSsemBbs"+
    	"hEQA4JR3PnT5jdOwFtfcGiOPKH9YQFtFT1A=";
    	byte[] bytes = Base64.decodeBase64(base64String);
    	X509Certificate cert;
    	try {
    		cert = X509Utils.rawX509CertificateFromEncoded(bytes);
    	} catch(Exception e) {
    		
    	}
    }
}
