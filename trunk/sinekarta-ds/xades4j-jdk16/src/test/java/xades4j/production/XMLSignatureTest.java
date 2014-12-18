//package xades4j.production;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
//import org.apache.xml.security.exceptions.XMLSecurityException;
//import org.apache.xml.security.signature.XMLSignature;
//import org.apache.xml.security.utils.Constants;
//import org.apache.xml.security.utils.XMLUtils;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//
//import xades4j.UnsupportedAlgorithmException;
//import xades4j.XAdES4jXMLSigException;
//import xades4j.algorithms.Algorithm;
//import xades4j.algorithms.CanonicalXMLWithoutComments;
//import xades4j.algorithms.GenericAlgorithm;
//import xades4j.xml.sign.ExtXMLSignature;
//
//public class XMLSignatureTest extends SignerTestBase {
//
//	static TransformerFactory tf = TransformerFactory.newInstance();
//	private static final org.slf4j.Logger tracer =
//	        org.slf4j.LoggerFactory.getLogger(XMLSignatureTest.class);
//	
//	static
//    {
//        Init.initXMLSec();
//    }
//	
//	public void test() throws Exception {
//		Document doc = getTestDocument();
//		ExtXMLSignature extXMLSignature = createExtSignature(doc, "www.pippo.com", "RSA");
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    	tf.newTransformer().transform ( new DOMSource(extXMLSignature.getElement()), new StreamResult(baos) );
//    	tracer.info(new String(baos.toByteArray()));
//    	doc = db.parse(new ByteArrayInputStream(baos.toByteArray()));
//    	XMLSignature xmlSignature = new XMLSignature(doc.getDocumentElement(), doc.getBaseURI(), false);
//    	baos = new ByteArrayOutputStream();
//    	tf.newTransformer().transform ( new DOMSource(xmlSignature.getElement()), new StreamResult(baos) );
//    	tracer.info(new String(baos.toByteArray()));
//	}
//	
//	private Element createElementForAlgorithm(Algorithm algorithm, String elementName, Document signatureDocument) throws UnsupportedAlgorithmException
//	    {
//	        Element algorithmElem = XMLUtils.createElementInSignatureSpace(signatureDocument, elementName);
//	        algorithmElem.setAttributeNS(null, Constants._ATT_ALGORITHM, algorithm.getUri());
//
//	        return algorithmElem;
//	    }
//	
//    private ExtXMLSignature createExtSignature(Document signatureDocument, String baseUri, String signingKeyAlgorithm) throws XAdES4jXMLSigException, UnsupportedAlgorithmException
//    {
//        Algorithm signatureAlg = new GenericAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
//        Element signatureAlgElem = createElementForAlgorithm(signatureAlg, Constants._TAG_SIGNATUREMETHOD, signatureDocument);
//
//
//        Algorithm canonAlg = new CanonicalXMLWithoutComments();
//        Element canonAlgElem = createElementForAlgorithm(canonAlg, Constants._TAG_CANONICALIZATIONMETHOD, signatureDocument);
//
//        try
//        {
//            return new ExtXMLSignature(signatureDocument, baseUri, signatureAlgElem, canonAlgElem);
//        } catch (XMLSecurityException ex)
//        {
//            // Following the code, doesn't seem to be thrown at all.
//            throw new XAdES4jXMLSigException(ex.getMessage(), ex);
//        }
//    }
//    
//}
