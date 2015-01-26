package xades4j.xml.sign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DOMUtils {

	static XPath xPath;
	static Transformer transformer;
	static DocumentBuilder docBuilder;
	static {
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			xPath = XPathFactory.newInstance().newXPath();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * XML string representation of a generic DOM node  
	 * @param node
	 * @return the corresponding xml tree
	 */
	public static String toXML ( Node node ) {
		String xml = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        try {
	        	transformer.transform (	new DOMSource(node), new StreamResult(baos) );
	        } catch(Exception e) {
	        	throw new RuntimeException(e);
	        }
			xml = new String ( baos.toByteArray() );
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(node);
			transformer.transform(source, result);
			xml = result.getWriter().toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return xml;
	}
	
	/**
	 * search and replace the target element with a new one
	 * @param root containing the element to be replaced
	 * @param x element to be replaced
	 * @param y new element to be added
	 * @return the root element in which the first element has been replaced with the second
	 */
	public static Element searchReplace(Element root, Node x, Node y) {
	    Queue<Node> q = new LinkedList<Node>();
	    q.add(root);
	    while (!q.isEmpty()) {
	        Node current = q.remove();
	        if (current == x) {
	        	current.getParentNode().replaceChild(y, current);
	        }
	
	        NodeList children = current.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            q.add(children.item(i));
	        }
	    }
	    return root;
	}
	
	/**
	 * 
	 * @param root
	 * @param expression
	 * @param y
	 * @return
	 * @throws XPathExpressionException
	 */
	public static Element replaceElement(Element root, String expression, Node y) throws XPathExpressionException {
		Node current = (Node)xPath.compile(expression).evaluate(root, XPathConstants.NODE);
		current.getParentNode().replaceChild(y, current);
	    return root;
	}
	
	/**
	 * Conversion from an xml string to its DOM representation.
	 * @param xml
	 * @return the equivalent DOM element
	 */
	public static Element parseXML ( String xml ) {
		Document document;
		try {
			document = docBuilder.parse ( new ByteArrayInputStream(xml.getBytes()) );
		} catch (SAXException | IOException e) {
			// never thrown: byte array operation on a - supposed to be - valid XML string
			throw new RuntimeException(e);
		} 
		return document.getDocumentElement();
	}
	
	/**
	 * Conversion from ExtXMLSignature to XMLSignature.
	 * @param extSignature
	 * @param signatureMethodURI
	 * @return the XMLsignature equivalent to the input
	 */
	public static XMLSignature extSignatureToXML ( ExtXMLSignature extSignature, String signatureMethodURI ) {
		XMLSignature xmlSignature;
		try {
			xmlSignature = new XMLSignature ( extSignature.getElement(), signatureMethodURI );
		} catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
		return xmlSignature;
	}
	
	/**
	 * Conversion from XMLSignature to ExtXMLSignature.
	 * @param xmlSignature
	 * @param signatureMethodURI
	 * @return the ExtXMLsignature equivalent to the input
	 */
	public static ExtXMLSignature xmlSignatureToExt ( XMLSignature xmlSignature, String signatureMethodURI ) {
    	ExtXMLSignature extSignature;
    	try {
    		extSignature = new ExtXMLSignature ( xmlSignature, signatureMethodURI );
    	} catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
    	return extSignature;
    }
	
	/**
	 * Evaluation of the root URI. It will locate the root of the document containing the given element and
	 * checked its id. The URI will be equivalent to the id with the '#' prefix if not empty, or to a blank string 
	 * @param element 
	 * @return the URI for the root element
	 */
	public static String evalRootUri ( Element element ) {
		Element root = element.getOwnerDocument().getDocumentElement();
        String rootId = root.getAttribute("Id");
        String rootUri = "";
        if ( StringUtils.isNotBlank(rootId) ) {
        	rootUri = '#' + rootId;
        }
        return rootUri;
	}
}
