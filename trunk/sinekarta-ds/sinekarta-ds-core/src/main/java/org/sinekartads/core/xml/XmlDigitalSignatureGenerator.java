package org.sinekartads.core.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class is used to provide convenient methods to digitally sign an XML
 * document.
 * 
 * @author <a href="mailto:debadatta.mishra@gmail.com">Debadatta Mishra</a>
 * @since 2013
 */
public class XmlDigitalSignatureGenerator {

//	static final String providerName = System.getProperty(XMLDSigRIPrePost.PROVIDER_NAME, XMLDSigRIPrePost.class.getName());
//	static final String providerName = System.getProperty("jsr105Provider", XMLDSigRIPrePost.class.getName());
	static final String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
	static final XMLSignatureFactory xmlSigFactory;
	
	static {
		try {
			xmlSigFactory = XMLSignatureFactory.getInstance("DOM", (Provider) Class.forName(providerName).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			
		}
	}

	
	Logger tracer = Logger.getLogger(getClass());

	/**
	 * Method used to get the XML document by parsing
	 * 
	 * @param contentIs
	 *            , file path of the XML document
	 * @return Document
	 */
	private Document getXmlDocument(InputStream contentIs) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			doc = dbf.newDocumentBuilder().parse(contentIs);
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (SAXException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return doc;
	}

	/*
	 * Method used to store the signed XMl document
	 */
	private void storeSignedDoc(Document doc, OutputStream signedContentOs) {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer trans = null;
		try {
			trans = transFactory.newTransformer();
		} catch (TransformerConfigurationException ex) {
			ex.printStackTrace();
		}
		try {
			StreamResult streamRes = new StreamResult(signedContentOs);
			trans.transform(new DOMSource(doc), streamRes);
		} catch (TransformerException ex) {
			ex.printStackTrace();
		}
		tracer.info("XML file with attached digital signature generated successfully ...");
	}

	/**
	 * Method used to attach a generated digital signature to the existing
	 * document
	 * 
	 * @param contentIs
	 * @param signedContentOs
	 * @param privateKeyFilePath
	 * @param publicKeyFilePath
	 */
	public void generateXMLDigitalSignature(InputStream contentIs,
			OutputStream signedContentOs, PrivateKey privateKey,
			X509Certificate certificate, X509Certificate... certificateChain) {
		// Get the XML Document object
		Document doc = getXmlDocument(contentIs);
		// Create XML Signature Factory
		DOMSignContext domSignCtx = new DOMSignContext(privateKey,
				doc.getDocumentElement());
		Reference ref = null;
		SignedInfo signedInfo = null;
		try {
			List<Reference> references = new ArrayList<Reference>(); 
			ref = xmlSigFactory
					.newReference("", xmlSigFactory.newDigestMethod(
							DigestMethod.SHA256, null), Collections
							.singletonList(xmlSigFactory.newTransform(
									Transform.ENVELOPED,
									(TransformParameterSpec) null)), null, null);
			references.add(ref);
			signedInfo = xmlSigFactory.newSignedInfo(xmlSigFactory
					.newCanonicalizationMethod(
							CanonicalizationMethod.EXCLUSIVE,
							(C14NMethodParameterSpec) null), xmlSigFactory
					.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
					references);
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} catch (InvalidAlgorithmParameterException ex) {
			ex.printStackTrace();
		}
		// Pass the Public Key File Path
		KeyInfo keyInfo = getKeyInfo(certificate);
		// Create a new XML Signature
		XMLSignature xmlSignature = xmlSigFactory.newXMLSignature(signedInfo,
				keyInfo);
		try {
			// Sign the document
			xmlSignature.sign(domSignCtx);
		} catch (MarshalException ex) {
			ex.printStackTrace();
		} catch (XMLSignatureException ex) {
			ex.printStackTrace();
		}
		// Store the digitally signed document inta a location
		storeSignedDoc(doc, signedContentOs);
	}

//	/**
//	 * Method used to attach a generated digital signature to the existing
//	 * document
//	 * 
//	 * @param contentIs
//	 * @param signedContentOs
//	 * @param privateKeyFilePath
//	 * @param publicKeyFilePath
//	 */
//	public void generateXMLDigitalSignatureEnveloping(InputStream contentIs,
//			OutputStream signedContentOs, PrivateKey privateKey,
//			X509Certificate certificate, X509Certificate... certificateChain) {
//		// Get the XML Document object
//		Document doc = getXmlDocument(contentIs);
//		
//
//		XMLStructure content = new DOMStructure(doc.getDocumentElement());
//		XMLObject obj = xmlSigFactory.newXMLObject(
//				Collections.singletonList(content), "object", null, null);
//
//		DOMSignContext domSignCtx = new DOMSignContext(privateKey, doc);
//		Reference ref = null;
//		SignedInfo signedInfo = null;
//		try {
//			ref = xmlSigFactory.newReference("",
//					xmlSigFactory.newDigestMethod(DigestMethod.SHA256, null));
//			signedInfo = xmlSigFactory.newSignedInfo(xmlSigFactory
//					.newCanonicalizationMethod(
//							CanonicalizationMethod.EXCLUSIVE,
//							(C14NMethodParameterSpec) null), xmlSigFactory
//					.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
//					Collections.singletonList(ref));
//		} catch (NoSuchAlgorithmException ex) {
//			ex.printStackTrace();
//		} catch (InvalidAlgorithmParameterException ex) {
//			ex.printStackTrace();
//		}
//		// Pass the Public Key File Path
//		KeyInfo keyInfo = getKeyInfo(certificate);
//		// Create a new XML Signature
//		XMLSignature xmlSignature = xmlSigFactory.newXMLSignature(signedInfo,
//				keyInfo, Collections.singletonList(obj), null, null);
//		try {
//			// Sign the document
//			xmlSignature.sign(domSignCtx);
//		} catch (MarshalException ex) {
//			ex.printStackTrace();
//		} catch (XMLSignatureException ex) {
//			ex.printStackTrace();
//		}
//		// Store the digitally signed document inta a location
//		storeSignedDoc(doc, signedContentOs);
//	}

	private KeyInfo getKeyInfo(
			X509Certificate certificate, X509Certificate... certificateChain) {

		KeyInfoFactory keyInfoFact = xmlSigFactory.getKeyInfoFactory();

		// Create the KeyInfo containing the X509Data.
		List<Object> x509Content = new ArrayList<Object>();
		x509Content.add(certificate.getSubjectX500Principal().getName());
		x509Content.add(certificate);
		X509Data xd = keyInfoFact.newX509Data(x509Content);

		return keyInfoFact.newKeyInfo(Collections.singletonList(xd));
	}

}
