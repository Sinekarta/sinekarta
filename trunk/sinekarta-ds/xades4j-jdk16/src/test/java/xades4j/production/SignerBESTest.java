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
package xades4j.production;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.junit.Test;
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
import xades4j.properties.CounterSignatureProperty;
import xades4j.properties.DataObjectDesc;
import xades4j.properties.DataObjectFormatProperty;
import xades4j.properties.IndividualDataObjsTimeStampProperty;
import xades4j.properties.SignerRoleProperty;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SignaturePropertiesCollector;
import xades4j.providers.SignaturePropertiesProvider;
import xades4j.providers.impl.ExtKeyringDataProvider;
import xades4j.utils.X509Utils;

/**
 *
 * @author LuÃ­s
 */
public class SignerBESTest extends SignerTestBase
{
	Logger tracer = Logger.getLogger(getClass());

	static final String SIGN_ALIAS       = "Roberto Colombari"; 
	static final String SIGN_PRIVATE_KEY = "30820276020100300d06092a864886f70d0101010500048202603082025c02010002818100853c9950706ed4c495424cb1a739bab8639da5e4390e77c69d4de2e3d4ed1ceea2cf96cb784f7f9a0c7561ce471ed655e928ef56003e4dd8053bb3cafd83d665142529f249d29fdd730ec582a0320be028946f53e6a3603aebb8e68538837227c5922360790d8ffc1e5fec8e02592a1aafdeb938307516205a4f6d877f9e1c7d0203010001028180791347733ca8cabad5b449038ba63f52be5b8d5be6a98a18b7ec0649e9bd8b742409a6cbc1c9e477f5e85977dd535d8cf6739782bc77e1bf7389fc69739571a65af4279ecd2a8078e6ef9661a972f7eb233c10e03f735eb3fe4b48fa61a9a0a3c5f0ecb7e245a69331053a3e89b35b7b39b9bde9d4915fd00be10044a3beb281024100c1543dba4cce5af8a8f50b054a83343f412fc7cdcd46efdee7ec9c72fa06c059c689d6ed66ae50688a4985f8a8eb62ece5f3bb0a001c4e95b660ef60e71134dd024100b06d7c7b81ad598899de62bab7fbd731a4d799f3b3b82286297fd6a81d4f947a6f2b803b0a4d280ae86b3d7168cf39af741b2e60a90b7a1f774b9829aea4bc21024100b28903404ab1be9d281ab384bd5d1120e12828d24ba218deb73b70f755226afbfd3749fe8ef6a757036e0684ae2a427f1794cfc3da7a49b0446e9c61d6c1b31902405747428ec2df23ecccd9d413b4d2d4694db80f041d835928efbcbb4f5d78b1e643bacc6be8b3b4bc78b01cac4f023cf24c48ea0f8d710d1025eef2aea42400a1024001c63d53c6a22904e0bb08381a47510d4328ca8172392b104cee6afc476ebb46bd5945e4e445abcd223db96c03da57edde57219bc9839ff9ea7d32b657e2efac";
	static final String SIGN_CERTIFICATE = "308201a43082010d020101300d06092a864886f70d01010b050030193117301506035504030c0e4a656e696120536f667477617265301e170d3134313132333030303030305a170d3135313132343030303030305a301c311a301806035504030c11526f626572746f20436f6c6f6d6261726930819f300d06092a864886f70d010101050003818d0030818902818100853c9950706ed4c495424cb1a739bab8639da5e4390e77c69d4de2e3d4ed1ceea2cf96cb784f7f9a0c7561ce471ed655e928ef56003e4dd8053bb3cafd83d665142529f249d29fdd730ec582a0320be028946f53e6a3603aebb8e68538837227c5922360790d8ffc1e5fec8e02592a1aafdeb938307516205a4f6d877f9e1c7d0203010001300d06092a864886f70d01010b0500038181007e07a03eed95d8dd621bac2468959217e38202eddb7188fc2e674ec24f59952e12a272badd64f6ed360dd701b8266a1c5e15d489dc7af9e2ba7ca3e6068312356e0d8ce9a252cf304da4901d574325e231b77d19974ba969098d9386dcb0dd20f015b643d18edb251253a92e5b2401e4b25fd9536a0d1276c6a496f6ff3f277a";

	
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
        	// Source document and relative root element
        	Document doc = getTestDocument();
	        Element root = doc.getDocumentElement();
	        
	        // Creation of the reference to the root element
	        DataObjectDesc obj1 = new DataObjectReference('#' + root.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform());
	        SignedDataObjects dataObjs = new SignedDataObjects(obj1);
	        
	        // XML digital signature
	        SignerBES signer = (SignerBES)new XadesBesSigningProfile(kp).newSigner();
	        signer.sign(dataObjs, root, SignatureAppendingStrategies.AsFirstChild);
	
	        outputDocument(doc, DOCUMENT_BASE+"_bes.xml");
	    } catch(Exception e) {
	    	tracer.error(e.getMessage(), e);
	    	throw e;
	    }
    }
    
//    @Test
//    public void testSignBES() throws Exception
//    {
//        tracer.info("signBES");
//        try {
//        Document doc1 = getTestDocument();
//        Element elemToSign = doc1.getDocumentElement();
////        SignerBES signer = (SignerBES)new XadesBesSigningProfile(keyingProviderMy).newSigner();
//
//        IndividualDataObjsTimeStampProperty dataObjsTimeStamp = new IndividualDataObjsTimeStampProperty();
//        AllDataObjsCommitmentTypeProperty globalCommitment = AllDataObjsCommitmentTypeProperty.proofOfApproval();
//        CommitmentTypeProperty commitment = CommitmentTypeProperty.proofOfCreation();
//        DataObjectDesc obj1 = new DataObjectReference('#' + elemToSign.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform()).withDataObjectFormat(new DataObjectFormatProperty("text/xml", "MyEncoding").withDescription("Isto Ã© uma descriÃ§Ã£o do elemento raiz").withDocumentationUri("http://doc1.txt").withDocumentationUri("http://doc2.txt").withIdentifier("http://elem.root")).withCommitmentType(commitment).withDataObjectTimeStamp(dataObjsTimeStamp);
//        SignedDataObjects dataObjs = new SignedDataObjects(obj1).withCommitmentType(globalCommitment).withDataObjectsTimeStamp();
//
//        signer.sign(dataObjs, elemToSign);
//
//        outputDocument(doc1, "document.signed.bes.xml");
//	    } catch(Exception e) {
//	    	tracer.error(e.getMessage(), e);
//	    	throw e;
//	    }
//    }
//
//    @Test
//    public void testSignBESAndAppendAsFirstChild() throws Exception
//    {
//    	try {
//        tracer.info("signBESAndAppendAsFirstChild");
//
//        Document doc = getTestDocument();
//        Element root = doc.getDocumentElement();
////        SignerBES signer = (SignerBES)new XadesBesSigningProfile(keyingProviderMy).newSigner();
//
//        DataObjectDesc obj1 = new DataObjectReference('#' + root.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform());
//        SignedDataObjects dataObjs = new SignedDataObjects(obj1);
//
//        signer.sign(dataObjs, root, SignatureAppendingStrategies.AsFirstChild);
//
//        outputDocument(doc, "document.signed.bes.firstchild.xml");
//	    } catch(Exception e) {
//	    	tracer.error(e.getMessage(), e);
//	    	throw e;
//	    }
//    }

//    @Test
//    public void testSignBESExtrnlRes() throws Exception
//    {
//    	try {
//        tracer.info("signBESExtrnlRes");
//
//        Document doc = getNewDocument();
////        SignerBES signer = (SignerBES)new XadesBesSigningProfile(keyingProviderNist).newSigner();
//
//        DataObjectDesc obj1 = new DataObjectReference("rfc3161.txt").withDataObjectFormat(new DataObjectFormatProperty("text/plain").withDescription("Internet X.509 Public Key Infrastructure Time-Stamp Protocol (TSP)")).withDataObjectTimeStamp(new IndividualDataObjsTimeStampProperty());
//        signer.sign(new SignedDataObjects(obj1).withBaseUri("http://www.ietf.org/rfc/"), doc);
//
//        outputDocument(doc, "document.signed.bes.extres.xml");
//	    } catch(Exception e) {
//	    	tracer.error(e.getMessage(), e);
//	    	throw e;
//	    }
//    }
//
//    @Test
//    public void testSignBESWithCounterSig() throws Exception
//    {
//        tracer.info("signBESWithCounterSig");
//        try {
//        Document doc = getTestDocument();
//        Element elemToSign = doc.getDocumentElement();
//
//        XadesBesSigningProfile profile = new XadesBesSigningProfile(kp);
//        final XadesSigner counterSigner = profile.newSigner();
//        profile.withSignaturePropertiesProvider(new SignaturePropertiesProvider()
//        {
//
////			public void provideProperties(
////					SignaturePropertiesCollector signedPropsCol) {
////				// TODO Auto-generated method stub
////				
////			}
//            public void provideProperties(
//                    SignaturePropertiesCollector signedPropsCol)
//            {
//                signedPropsCol.addCounterSignature(new CounterSignatureProperty(counterSigner));
//                signedPropsCol.setSignerRole(new SignerRoleProperty("CounterSignature maniac"));
//            }
//        });
////        SignerBES signer = (SignerBES)profile.newSigner();
//
//        DataObjectDesc obj1 = new DataObjectReference('#' + elemToSign.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform());
//        SignedDataObjects dataObjs = new SignedDataObjects().withSignedDataObject(obj1);
//
//        signer.sign(dataObjs, elemToSign);
//
//        outputDocument(doc, "document.signed.bes.cs.xml");
//        } catch(Exception e) {
//        	tracer.error(e.getMessage(), e);
//        	throw e;
//        }
//    }
}
