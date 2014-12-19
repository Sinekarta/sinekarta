package xades4j.xml.sign;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
import org.apache.xml.security.utils.ElementProxy;
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
	
	public static byte[] toByteArray ( ElementProxy proxy ) {
		return toByteArray(elementFromProxy(proxy));
	}
	
	public static String toXML ( ElementProxy proxy ) {
		return toXML(elementFromProxy(proxy));
	}
	
	public static byte[] toByteArray ( Node node ) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
        	transformer.transform (	new DOMSource(node), new StreamResult(baos) );
        } catch(Exception e) {
        	throw new RuntimeException(e);
        }
        return baos.toByteArray();
	}
	
	public static String toXML ( Node node ) {
		String xml = null;
		try {
			xml = new String ( toByteArray(node) );
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
	

	public static Element elementFromProxy ( ElementProxy proxy ) {
		return proxy.getElement();
	}
	
	public static Element searchReplace(Element root, ElementProxy x, Element y) {
		return searchReplace ( root, 
							   elementFromProxy(x),
							   y );
	}
	
	public static Element searchReplace(Element root, ElementProxy x, ElementProxy y) {
		return searchReplace ( root, 
							   elementFromProxy(x),
							   elementFromProxy(y) );
	}
	
	public static Element searchReplace(ElementProxy root, Element x, Element y) {
		return searchReplace ( elementFromProxy(root), 
							   x,
							   y );
	}
	
	public static Element searchReplace(Element root, Element x, ElementProxy y) {
		return searchReplace ( root, 
							   x,
							   elementFromProxy(y) );
	}
	
	public static Element searchReplace(ElementProxy root, ElementProxy x, ElementProxy y) {
		return searchReplace ( elementFromProxy(root), 
							   elementFromProxy(x),
							   elementFromProxy(y) );
	}
	
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
	
	public static Element search(Element root, Node x) {
	    Queue<Node> q = new LinkedList<Node>();
	    q.add(root);
	    while (!q.isEmpty()) {
	        Node current = q.remove();
	        if (current == x) {
	        	return (Element) current;
	        }
	
	        NodeList children = current.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            q.add(children.item(i));
	        }
	    }
	    return root;
	}
	
	
	public static List<Element> searchElements(Element root, String expression) throws XPathExpressionException {
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(root, XPathConstants.NODESET);
		List<Element> elements = new ArrayList<Element>();
		for ( int i=0; i<nodeList.getLength(); i++ ) {
			elements.add ( (Element)nodeList.item(i) );
		}
		return elements;
	}
	
	public static Element searchElement(Element root, String expression) throws XPathExpressionException {
		return (Element)xPath.compile(expression).evaluate(root, XPathConstants.NODE);
	}
	
	public static Element replaceElement(Element root, String expression, Node y) throws XPathExpressionException {
		Node current = (Node)xPath.compile(expression).evaluate(root, XPathConstants.NODE);
		current.getParentNode().replaceChild(y, current);
	    return root;
	}
	
	public static Element searchReplace(Element root, String tagName, Node y) {
	    Queue<Node> q = new LinkedList<Node>();
	    q.add(root);
	    while (!q.isEmpty()) {
	        Node current = q.remove();
	        if (StringUtils.equalsIgnoreCase(current.getLocalName(), tagName)) {
	        	current.getParentNode().replaceChild(y, current);
	        } else {
		        NodeList children = current.getChildNodes();
		        for (int i = 0; i < children.getLength(); i++) {
		            q.add(children.item(i));
		        }
	        }
	    }
	    return root;
	}
	
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
	
	public static XMLSignature extSignatureToXML ( ExtXMLSignature extSignature, String signatureMethodURI ) {
		XMLSignature xmlSignature;
		try {
			xmlSignature = new XMLSignature ( extSignature.getElement(), signatureMethodURI );
		} catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
		return xmlSignature;
	}
	
	public static ExtXMLSignature xmlSignatureToExt ( XMLSignature xmlSignature, String signatureMethodURI ) {
    	ExtXMLSignature extSignature;
    	try {
    		extSignature = new ExtXMLSignature ( xmlSignature, signatureMethodURI );
    	} catch(Exception e) {
	    	throw new RuntimeException(e);
	    }
    	return extSignature;
    }
	
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
