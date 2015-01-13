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
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TimeStampToken;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.TimeStampInfo;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.MarkedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.TsResponseInfo;
import org.sinekartads.model.domain.VerifyInfo;
import org.sinekartads.model.domain.XMLSignatureInfo;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.util.TemplateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import xades4j.algorithms.Algorithm;
import xades4j.algorithms.ExclusiveCanonicalXMLWithoutComments;
import xades4j.algorithms.XPath2FilterTransform.XPath2Filter;
import xades4j.production.DataObjectReference;
import xades4j.production.SignatureAppendingStrategies;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesExtBesSigningProfile;
import xades4j.production.XadesExtTSigningProfile;
import xades4j.production.XadesSignatureResult;
import xades4j.production.XadesSignerExt;
import xades4j.production.XadesSigningProfile;
import xades4j.properties.DataObjectDesc;
import xades4j.providers.TimeStampTokenGenerationException;
import xades4j.providers.TimeStampTokenProvider;
import xades4j.providers.impl.DefaultAlgorithmsProviderEx;
import xades4j.providers.impl.ExtKeyringDataProvider;
import xades4j.providers.impl.ExtSignaturePropertiesProvider;
import xades4j.utils.DOMHelper;
import xades4j.xml.sign.DOMUtils;

public class XMLSignatureService 
		extends AbstractSignatureService  <	SignCategory,
											SignDisposition.XML,
											XMLSignatureInfo > {

	public static class ExtTimeStampTokenProvider implements TimeStampTokenProvider
	{
	    private TsRequestInfo tsRequest;
	    private TsResponseInfo tsResponse;
	    private TimeStampInfo timeStamp;

	    @Override
	    public final TimeStampTokenRes getTimeStampToken ( byte[] tsDigestInput,
	            										   String digestAlgUri ) 
	            												   throws TimeStampTokenGenerationException {
	    	try {
	    		// FIXME grant that the digestAlgUri matches with the digestAlgorithm in use within the tsResponse
	    		//		this should be automatic at the first version since only SHA256 is supported
	    		tsRequest = tsRequest.evaluateMessageImprint(tsDigestInput);
	            tsResponse = gblTsService.processTsTequest(tsRequest);
	            timeStamp = tsResponse.getTimeStamp();
	            byte[] tsTokenEnc = timeStamp.getEncTimeStampToken();
	            TimeStampToken tsToken = new TimeStampToken(new CMSSignedData(tsTokenEnc));
	            return new TimeStampTokenRes(tsTokenEnc, tsToken.getTimeStampInfo().getGenTime());
	    	} catch(Exception e) {
            	throw new TimeStampTokenGenerationException(e.getMessage(), e);
            }
	    }

	    public void setTsRequest(TsRequestInfo tsRequest) {
	    	this.tsRequest = tsRequest;
	    }

		public TimeStampInfo getTimeStamp() {
			return timeStamp;
		}

		public TsResponseInfo getTsResponse() {
			return tsResponse;
		}
	}


    static class ExclusiveC14nForTimeStampsAlgorithmsProvider extends DefaultAlgorithmsProviderEx
    {
        @Override
        public Algorithm getCanonicalizationAlgorithmForTimeStampProperties()
        {
            return new ExclusiveCanonicalXMLWithoutComments();
        }

        @Override
        public Algorithm getCanonicalizationAlgorithmForSignature()
        {
            return new ExclusiveCanonicalXMLWithoutComments();
        }
    }


	
	public XMLSignatureService ( ) {
		try {		
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    	dbf.setNamespaceAware(true);
	    	db = dbf.newDocumentBuilder();
	    	tf = TransformerFactory.newInstance();
	    } catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
		if ( gblTsService == null ) {
			gblTsService = timeStampService;
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
	        ExtSignaturePropertiesProvider extSignPropertiesProvider = new ExtSignaturePropertiesProvider();
	        XadesSigningProfile signingProfile;
	        TsRequestInfo tsRequest = chainSignature.getTsRequest(); 
	        if ( tsRequest != null && StringUtils.isNotBlank(tsRequest.getTsUrl() )) {
	        	ExtTimeStampTokenProvider extTsTokenProvider = new ExtTimeStampTokenProvider();
	        	extTsTokenProvider.setTsRequest(tsRequest);
	        	signingProfile = new XadesExtTSigningProfile(extKdp)
			        	.withTimeStampTokenProvider(extTsTokenProvider)
		                .withAlgorithmsProviderEx(ExclusiveC14nForTimeStampsAlgorithmsProvider.class);
	        } else {
	        	signingProfile = new XadesExtBesSigningProfile(extKdp);
	        }
	        XadesSignerExt signer = (XadesSignerExt) signingProfile
					.withSignaturePropertiesProvider ( extSignPropertiesProvider )
							.newSigner();
	        
	        // Certificate chain injection
	        extKdp.setSigningCertificateChain ( 
	        		TemplateUtils.Conversion.arrayToList ( 
	        				chainSignature.getRawX509Certificates() ) );
	        
	        // Creation of the reference to the root element
	        String rootUri = DOMUtils.evalRootUri(root);
	        DataObjectDesc obj1 = new DataObjectReference(rootUri).withTransform( XPath2Filter.subtract("/descendant::ds:Signature") );
	        SignedDataObjects dataObjs = new SignedDataObjects(obj1);
	        
	        // Digest evaluation
	        byte[] digest = signer.digest(dataObjs, root);
	        
	        // Inject the digest into the signature
	        DigestAlgorithm digestAlgorithm = chainSignature.getDigestAlgorithm();
	        digestSignature = chainSignature.toDigestSignature ( 
	        		DigestInfo.getInstance(digestAlgorithm, digest) );
	        
	        ((XMLSignatureInfo)digestSignature).setSignatureId(signer.getSignatureId());
	        digestSignature.setSigningTime ( extSignPropertiesProvider.getSigningTime() );
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
		
		MarkedSignature    < SignCategory, 
						 	 SignDisposition.XML, 
						 	 VerifyResult, 		
						 	 XMLSignatureInfo > markedSignature = null;
		
		FinalizedSignature < SignCategory, 
						 	 SignDisposition.XML, 
						 	 VerifyResult, 		
						 	 XMLSignatureInfo > finalizedSignature = null;
		
		try {
			Document doc = db.parse(contentIs);
	        // Apache Santuario now uses Document.getElementById; use this convention for tests.
	        Element root = doc.getDocumentElement();
	        DOMHelper.useIdAsXmlId(root);
	        
	        TsRequestInfo tsRequest = signedSignature.getTsRequest(); 
	        boolean applyMark = tsRequest != null && StringUtils.isNotBlank(tsRequest.getTsUrl());
	        
	        // External XAdES-BES Signer
	        ExtKeyringDataProvider extKdp = new ExtKeyringDataProvider();
	        ExtSignaturePropertiesProvider extSignPropertiesProvider = new ExtSignaturePropertiesProvider();
	        ExtTimeStampTokenProvider extTsTokenProvider = null;
	        XadesSigningProfile signingProfile;
	        if ( applyMark ) {
	        	extTsTokenProvider = new ExtTimeStampTokenProvider();
	        	extTsTokenProvider.setTsRequest(tsRequest);
	        	signingProfile = new XadesExtTSigningProfile(extKdp)
			        	.withTimeStampTokenProvider(extTsTokenProvider)
		                .withAlgorithmsProviderEx(ExclusiveC14nForTimeStampsAlgorithmsProvider.class);
	        } else {
	        	signingProfile = new XadesExtBesSigningProfile(extKdp);
	        }
	        XadesSignerExt signer = (XadesSignerExt) signingProfile
					.withSignaturePropertiesProvider ( extSignPropertiesProvider )
							.newSigner();
	        
	        // Certificate chain injection
	        extKdp.setSigningCertificateChain ( 
	        		TemplateUtils.Conversion.arrayToList ( 
	        				signedSignature.getRawX509Certificates() ) );
	        
	        // SigningTime injection
	        extSignPropertiesProvider.setSigningTime ( signedSignature.getSigningTime() );

	        // Creation of the reference to the root element
	        String rootUri = DOMUtils.evalRootUri(root);
	        DataObjectDesc obj1 = new DataObjectReference(rootUri).withTransform( XPath2Filter.subtract("/descendant::ds:Signature") );
	        SignedDataObjects dataObjs = new SignedDataObjects(obj1);
	        
	        // Inject the digitalSignature into the signer
	        signer.setSignatureId(((XMLSignatureInfo)signedSignature).getSignatureId());
	        signer.setDigest ( signedSignature.getDigest().getFingerPrint() );
	        signer.setDigitalSignature ( signedSignature.getDigitalSignature() );
	        
	        // Generate the signed xml
	        XadesSignatureResult signResult = signer.sign(dataObjs, root, SignatureAppendingStrategies.AsLastChild);
	        XMLSignature xmlSignature = signResult.getSignature();
	        String expression = "*[local-name() = 'Signature']";
	        DOMUtils.replaceElement(doc.getDocumentElement(), expression, xmlSignature.getElement());
	        
	        // Finalize the signature and send the signed xml to the outputStream
	        OutputStream targetStream;
	        if ( applyMark ) {
	        	markedSignature = signedSignature.toMarkedSignature();
	        	markedSignature.appendTimeStamp(extTsTokenProvider.getTimeStamp(), SignDisposition.TimeStamp.ATTRIBUTE);
	        	finalizedSignature = markedSignature.finalizeSignature();
	        	targetStream = markedSignOs;
	        } else {
	        	finalizedSignature = signedSignature.finalizeSignature();
	        	targetStream = embeddedSignOs;
	        }
	    	tf.newTransformer().transform ( new DOMSource(doc), new StreamResult(targetStream) );
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

		XMLSignature xmlSignature;
		try {
			Document doc = db.parse(envelopeIs);
			xmlSignature = new XMLSignature(doc.getDocumentElement(), null);
		} catch(SAXException e) {
			throw new IOException("unable to parse the xml document", e);
		} catch(XMLSecurityException e) {
			throw new SignatureException("unable to load the signature", e);
		}
		
//		xmlSignature.checkSignatureValue(cert)
		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

	
	
	private static TimeStampService gblTsService;

}
