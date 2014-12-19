package org.sinekartads.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.TimeStampInfo;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.VerifyInfo;
import org.sinekartads.model.domain.XMLSignatureInfo;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.util.TemplateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xades4j.algorithms.XPath2FilterTransform.XPath2Filter;
import xades4j.production.DataObjectReference;
import xades4j.production.SignatureAppendingStrategies;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesExtBesSigningProfile;
import xades4j.production.XadesSignatureResult;
import xades4j.production.XadesSignerExt;
import xades4j.properties.DataObjectDesc;
import xades4j.providers.impl.ExtKeyringDataProvider;
import xades4j.utils.DOMHelper;
import xades4j.xml.sign.DOMUtils;

public class XMLSignatureService 
		extends AbstractSignatureService  <	SignCategory,
											SignDisposition.XML,
											XMLSignatureInfo > {

	public XMLSignatureService ( ) {
		try {		
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    	dbf.setNamespaceAware(true);
	    	db = dbf.newDocumentBuilder();
	    	tf = TransformerFactory.newInstance();
	    } catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
	}

	TransformerFactory tf;
	DocumentBuilder db;
	
	
	
	// -----
	// --- Pre-Sign phase
	// -
	
	public DigestSignature < SignCategory, 
							 SignDisposition.XML, 
							 VerifyResult, 		
							 XMLSignatureInfo > 		doPreSign (	ChainSignature < SignCategory, 
													 	 						 	 SignDisposition.XML, 
													 								 VerifyResult, 		
													 								 XMLSignatureInfo 		 >	chainSignature,
																	InputStream 								contentIs 		)
													 							 
													 										 throws SignatureException, IOException {
		
		DigestSignature < SignCategory, 
						  SignDisposition.XML, 
						  VerifyResult, 		
						  XMLSignatureInfo > digestSignature = null;
		try {
			Document doc = db.parse(contentIs);
	        // Apache Santuario now uses Document.getElementById; use this convention for tests.
	        Element root = doc.getDocumentElement();
	        DOMHelper.useIdAsXmlId(root);
	        
	        // External XAdES-BES Signer 
	        ExtKeyringDataProvider extKdp = new ExtKeyringDataProvider();
	        XadesSignerExt signer = (XadesSignerExt)new XadesExtBesSigningProfile(extKdp).newSigner();
	        
	        // Certificate chain injection
	        extKdp.setSigningCertificateChain ( 
	        		TemplateUtils.Conversion.arrayToList ( 
	        				chainSignature.getRawX509Certificates() ) );
	        
	        // Creation of the reference to the root element
	        String rootUri = DOMUtils.evalRootUri(root);
	        DataObjectDesc obj1 = new DataObjectReference(rootUri).withTransform ( XPath2Filter.subtract("/descendant::ds:") );
	        SignedDataObjects dataObjs = new SignedDataObjects(obj1);
	        
	        // Digest evaluation
	        byte[] digest = signer.digest(dataObjs, root);
	        
	        // Inject the digest into the signature
	        DigestAlgorithm digestAlgorithm = chainSignature.getDigestAlgorithm();
	        digestSignature = chainSignature.toDigestSignature ( 
	        		DigestInfo.getInstance(digestAlgorithm, digest) );
		} catch(Exception e) {
        	throw new SignatureException(e);
        }
        return digestSignature;
	}

	// -----
	// --- Post-Sign phase
	// -
	
	@Override
	public FinalizedSignature < SignCategory, 
							 	SignDisposition.XML, 
							 	VerifyResult, 		
							 	XMLSignatureInfo > 		doPostSign (	SignedSignature	  <	SignCategory, 
													 	 						 		SignDisposition.XML, 
													 	 						 	 	VerifyResult, 		
													 	 						 	 	XMLSignatureInfo 		 >	signedSignature,
																	InputStream 									contentIs,
																	OutputStream 									detachedSignOs,
																	OutputStream 									embeddedSignOs,
																	OutputStream 									tsResultOs,
																	OutputStream 									markedSignOs 	)
																			
																			throws SignatureException, IOException 			{
		
		FinalizedSignature < SignCategory, 
						 	 SignDisposition.XML, 
						 	 VerifyResult, 		
						 	 XMLSignatureInfo > finalizedSignature = null;
		
		try {
			Document doc = db.parse(contentIs);
	        // Apache Santuario now uses Document.getElementById; use this convention for tests.
	        Element root = doc.getDocumentElement();
	        DOMHelper.useIdAsXmlId(root);
	        
	        // External XAdES-BES Signer 
	        ExtKeyringDataProvider extKdp = new ExtKeyringDataProvider();
	        XadesSignerExt signer = (XadesSignerExt)new XadesExtBesSigningProfile(extKdp).newSigner();
	        
	        // Certificate chain injection
	        extKdp.setSigningCertificateChain ( 
	        		TemplateUtils.Conversion.arrayToList ( 
	        				signedSignature.getRawX509Certificates() ) );

	        // Creation of the reference to the root element
	        DataObjectDesc obj1 = new DataObjectReference('#' + root.getAttribute("Id")).withTransform( XPath2Filter.subtract("/descendant::ds:") );
	        SignedDataObjects dataObjs = new SignedDataObjects(obj1);
	        
	        // Inject the digitalSignature into the signer
	        signer.setDigest ( signedSignature.getDigest().getFingerPrint() );
	        signer.setDigitalSignature ( signedSignature.getDigitalSignature() );
	        
	        // Send the signed xml to the outputStream
	        XadesSignatureResult signResult = signer.sign(dataObjs, root, SignatureAppendingStrategies.AsFirstChild);
	        doc = signResult.getSignature().getDocument();
	    	tf.newTransformer().transform ( new DOMSource(doc), new StreamResult(embeddedSignOs) );
	    	
	    	// finalize the signature
	    	finalizedSignature = signedSignature.finalizeSignature();
		} catch(Exception e) {
        	throw new SignatureException(e);
        }
        
        return finalizedSignature;
	}

	@Override
	public TimeStampInfo doApplyTimeStamp (
			TsRequestInfo tsRequest,
			InputStream contentIs,
			InputStream detachedSignIs,
			InputStream embeddedSignIs,
			OutputStream timestampOs,
			OutputStream markedSignOs ) 
					throws SignatureException,
							CertificateException,
							IOException {

		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

	@Override
	public VerifyInfo doVerify ( 
			InputStream contentIs,
			InputStream tsResponseIs,
			InputStream envelopeIs,
			VerifyResult requiredSecurityLevel,
			OutputStream contentOs ) 
					throws 	CertificateException,
							SignatureException,
							IOException {

		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

}
