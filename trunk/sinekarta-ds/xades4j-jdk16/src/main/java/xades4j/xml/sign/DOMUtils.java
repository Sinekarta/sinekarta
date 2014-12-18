package xades4j.xml.sign;

import java.io.ByteArrayOutputStream;
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
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtils {

	static XPath xPath;
	static Transformer transformer;
	static DocumentBuilder docBuilder;
	static {
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			xPath =  XPathFactory.newInstance().newXPath();
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
	
	
	
	public static Element searchElement(Element root, String tagName) throws XPathExpressionException {
		Queue<Node> q = new LinkedList<Node>();
	    q.add(root);
	    while (!q.isEmpty()) {
	        Node current = q.remove();
	        if ( StringUtils.equalsIgnoreCase(current.getLocalName(), tagName) ) {
	        	return (Element) current;
	        }
	
	        NodeList children = current.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            q.add(children.item(i));
	        }
	    }
	    return root;
//		return (Element)xPath.compile(expression).evaluate(root, XPathConstants.NODE);
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
	        }
	
	        NodeList children = current.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            q.add(children.item(i));
	        }
	    }
	    return root;
	}
}
