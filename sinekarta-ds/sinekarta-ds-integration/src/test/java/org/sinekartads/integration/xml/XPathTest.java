package org.sinekartads.integration.xml;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import xades4j.xml.sign.DOMUtils;
 
public class XPathTest extends TestCase {
	
	public static final String XML_SAMPLE = 
		"	<?xml version=\"1.0\"?>\n" +
		"	<Employees>\n" +
		"	    <Employee emplid=\"1111\" type=\"admin\">\n" +
		"	        <firstname>John</firstname>\n" +
		"	        <lastname>Watson</lastname>\n" +
		"	        <age>30</age>\n" +
		"	        <email>johnwatson@sh.com</email>\n" +
		"	    </Employee>\n" +
		"	    <Employee emplid=\"2222\" type=\"admin\">\n" +
		"	        <firstname>Sherlock</firstname>\n" +
		"	        <lastname>Homes</lastname>\n" +
		"	        <age>32</age>\n" +
		"	        <email>sherlock@sh.com</email>\n" +
		"	    </Employee>\n" +
		"	    <Employee emplid=\"3333\" type=\"user\">\n" +
		"	        <firstname>Jim</firstname>\n" +
		"	        <lastname>Moriarty</lastname>\n" +
		"	        <age>52</age>\n" +
		"	        <email>jim@sh.com</email>\n" +
		"	    </Employee>\n" +
		"	    <Employee emplid=\"4444\" type=\"user\">\n" +
		"	        <firstname>Mycroft</firstname>\n" +
		"	        <lastname>Holmes</lastname>\n" +
		"	        <age>41</age>\n" +
		"	        <email>mycroft@sh.com</email>\n" +
		"	    </Employee>\n" +
		"	</Employees>";
	
	public static final String XML_SIGNATURE = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<xx:pippo>" +		
		"	<ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"" +
		"		Id=\"xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a\">" +
		"		<ds:SignedInfo>" +
		"			<ds:CanonicalizationMethod" +
		"				Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\" />" +
		"			<ds:SignatureMethod" +
		"				Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\" />" +
		"			<ds:Reference" +
		"				Id=\"reference-xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a-keyinfo\"" +
		"				Type=\"http://www.w3.org/2001/04/xmlenc#sha256\" URI=\"#xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a-keyinfo\">" +
		"				<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />" +
		"				<ds:DigestValue>nwg8YJsA05ELOon3a2/bkozIDJ+IS3RD9rDk3zeGSHU=" +
		"				</ds:DigestValue>" +
		"			</ds:Reference>" +
		"			<ds:Reference Id=\"xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a-ref0\"" +
		"				URI=\"#\">" +
		"				<ds:Transforms>" +
		"					<ds:Transform" +
		"						Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\" />" +
		"				</ds:Transforms>" +
		"				<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />" +
		"				<ds:DigestValue />" +
		"			</ds:Reference>" +
		"			<ds:Reference" +
		"				URI=\"#xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a-signedprops\">" +
		"				<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />" +
		"				<ds:DigestValue>0zO0aAJ1BJpYfUBi4y1FC7llrZlcENu4huBQqs+ptgw=" +
		"				</ds:DigestValue>" +
		"			</ds:Reference>" +
		"			<ds:Reference Type=\"http://uri.etsi.org/01903#SignedProperties\"" +
		"				URI=\"#xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a-signedprops\">" +
		"				<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />" +
		"				<ds:DigestValue>0zO0aAJ1BJpYfUBi4y1FC7llrZlcENu4huBQqs+ptgw=" +
		"				</ds:DigestValue>" +
		"			</ds:Reference>" +
		"		</ds:SignedInfo>" +
		"		<ds:SignatureValue>" +
		"			u+RwVNU4REkRFIYmGbBArtvj2B6fQxR9LiLtkjR/H0Ebd0cieae5yFkMOnvVM4TemiMtaij1d1e1" +
		"			U4EZBb43apdOUkm65UravIOauZfaFv5rXATolIltckl1G3JCoKeWcvt7wnJyCfNAMfiuMGMHLGmL" +
		"			VHyWRzvNqFdu/7A/hVG49W2QAc51yIE5W/xDUGu9TQc31x73ZAjY1hOAzasPZsGn+jpr1I4OwEtF" +
		"			zFYZ/MfSZNEzm58gXFYx/46FDq6UffjgOI40cJ1OzLPhqNIO9iMVBLil6mdSY3fCZDxGJK058HVg" +
		"			ythqnBJBioHjAimhljEDAR9X+LLdjsiUTzdypw==" +
		"		</ds:SignatureValue>" +
		"		<ds:KeyInfo Id=\"xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a-keyinfo\">" +
		"			<ds:X509Data>" +
		"				<ds:X509Certificate>" +
		"					MIIF7zCCBNegAwIBAgIDL2SMMA0GCSqGSIb3DQEBCwUAMIGDMQswCQYDVQQGEwJJVDEVMBMGA1UE" +
		"					CgwMSU5GT0NFUlQgU1BBMRQwEgYDVQQFEwswNzk0NTIxMTAwNjEiMCAGA1UECwwZQ2VydGlmaWNh" +
		"					dG9yZSBBY2NyZWRpdGF0bzEjMCEGA1UEAwwaSW5mb0NlcnQgRmlybWEgUXVhbGlmaWNhdGEwHhcN" +
		"					MTMwMjExMDczMzA4WhcNMTYwMjExMDAwMDAwWjCBojELMAkGA1UEBhMCSVQxFTATBgNVBAoMDE5P" +
		"					TiBQUkVTRU5URTEWMBQGA1UEBAwNVEVTU0FSTyBQT1JUQTEPMA0GA1UEKgwGQU5EUkVBMRwwGgYD" +
		"					VQQFExNJVDpUU1NORFI3MUwxOEIzOTNSMRYwFAYDVQQuEw0yMDEwMTExMjU1NzE4MR0wGwYDVQQD" +
		"					DBRBbmRyZWEgVGVzc2FybyBQb3J0YTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAOqa" +
		"					a1GQCvbATVflq+t9lHn/JUJrJpqWbs7Q6jjOIoY15E8OoSParx1wZcqv9owfTGrapkt2wgUTgC0U" +
		"					dIEZXsROqanULoibzcfrca5+bylhfcbNPiSMJt/CBhFjaU9q4iS7jgcCHMgfiMdCQAwiA7s0lRVk" +
		"					jsLayahWvuW1UF3vQOikG2IcCjBTotE+rb/cmwUFnFqLdmXG4z4p7hAmrGvs+foD/E6WBMj6zVW0" +
		"					8pMXK+lUZ33XLki8HzGih9/VX8SJLftdwKQHsZOfGomZPFyvoQctW2R2UyiikbnEiCH4Srep24tN" +
		"					oxetdwu+oLEd+holGjHD5R+I04E38DzFbkkCAwEAAaOCAkkwggJFMAkGA1UdEwQCMAAwZAYDVR0g" +
		"					BF0wWzBPBgYrTCQBAQEwRTBDBggrBgEFBQcCARY3aHR0cDovL3d3dy5maXJtYS5pbmZvY2VydC5p" +
		"					dC9kb2N1bWVudGF6aW9uZS9tYW51YWxpLnBocDAIBgYrTBgBAQIwLwYIKwYBBQUHAQMEIzAhMAgG" +
		"					BgQAjkYBATALBgYEAI5GAQMCARQwCAYGBACORgEEMCgGA1UdCQQhMB8wHQYIKwYBBQUHCQExERgP" +
		"					MTk3MTA3MTgwMDAwMDBaME4GCCsGAQUFBwEBBEIwQDA+BggrBgEFBQcwAYYyaHR0cDovL29jc3Au" +
		"					aW5mb2NlcnQuaXQvT0NTUFNlcnZlcl9JQ0UvT0NTUFNlcnZsZXQwDgYDVR0PAQH/BAQDAgZAMCUG" +
		"					A1UdEgQeMByBGmZpcm1hLmRpZ2l0YWxlQGluZm9jZXJ0Lml0MB8GA1UdIwQYMBaAFDD8IXx80nxt" +
		"					vIzDuhNQ93qgK8W2MIGvBgNVHR8EgacwgaQwgaGggZ6ggZuGgZhsZGFwOi8vbGRhcC5pbmZvY2Vy" +
		"					dC5pdC9jbiUzZEluZm9DZXJ0JTIwRmlybWElMjBRdWFsaWZpY2F0YSUyMENSTDAyLG91JTNkQ2Vy" +
		"					dGlmaWNhdG9yZSUyMEFjY3JlZGl0YXRvLG8lM2RJTkZPQ0VSVCUyMFNQQSxjJTNkSVQ/Y2VydGlm" +
		"					aWNhdGVSZXZvY2F0aW9uTGlzdDAdBgNVHQ4EFgQUHvz/Ut8OL5yZtrnpwi0e+OIe774wDQYJKoZI" +
		"					hvcNAQELBQADggEBAGMg9zxQSXlILzUu86457p3VeWCnmbnXruMfVorctRHxv5bdVELpkYx/6xvf" +
		"					YwxbqCNC+MP9R6+LE+2JNpBLq/gFXJI6EZZvhdTdCPcN21sLP2gSqLvGqTCZmX9VKQxeTF4I/i2l" +
		"					koVgk6s0xRw994e3HO6ZajF/JGnZ/9VT/e7ZPRyJl8lWI+VLnaBT6cN0IrYRi/Xab06CkqNjH1hG" +
		"					3p/5STpPzsBoFKOm3Fxb8Wfro29mPw5K3H24xSVKk1Npt3A2z1TGjMqip+XOzwE9pMbfzo+Xfc+/" +
		"					CtiAMUNSZ+tkP3K1orPSvo4N5aRCqaiNa2JzdmdYckOCQOmB20PqFCM=" +
		"				</ds:X509Certificate>" +
		"			</ds:X509Data>" +
		"		</ds:KeyInfo>" +
		"		<ds:Object>" +
		"			<xades:QualifyingProperties xmlns:xades=\"http://uri.etsi.org/01903/v1.3.2#\"" +
		"				xmlns:xades141=\"http://uri.etsi.org/01903/v1.4.1#\" Target=\"#xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a\">" +
		"				<xades:SignedProperties" +
		"					Id=\"xmldsig-b1330edb-d995-45b6-8a6f-e041fc192a3a-signedprops\">" +
		"					<xades:SignedSignatureProperties>" +
		"						<xades:SigningTime>2014-12-18T16:13:15.642+01:00" +
		"						</xades:SigningTime>" +
		"						<xades:SigningCertificate>" +
		"							<xades:Cert>" +
		"								<xades:CertDigest>" +
		"									<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />" +
		"									<ds:DigestValue>nCgDr31xbiTcB/10XNdMRyU7KxjtvDiKunro4Oy/WtM=" +
		"									</ds:DigestValue>" +
		"								</xades:CertDigest>" +
		"								<xades:IssuerSerial>" +
		"									<ds:X509IssuerName>CN=InfoCert Firma" +
		"										Qualificata,OU=Certificatore" +
		"										Accreditato,2.5.4.5=#130b3037393435323131303036,O=INFOCERT" +
		"										SPA,C=IT</ds:X509IssuerName>" +
		"									<ds:X509SerialNumber>3105932</ds:X509SerialNumber>" +
		"								</xades:IssuerSerial>" +
		"							</xades:Cert>" +
		"						</xades:SigningCertificate>" +
		"					</xades:SignedSignatureProperties>" +
		"				</xades:SignedProperties>" +
		"			</xades:QualifyingProperties>" +
		"		</ds:Object>" +
		"	</ds:Signature>" +
		"</xx:pippo>"; 
	
	@Test
    public static void testWithSample() {
 
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
             
            DocumentBuilder builder =  builderFactory.newDocumentBuilder();
             
            Document xmlDocument = builder.parse ( new ByteArrayInputStream(XML_SAMPLE.getBytes()) );
 
            XPath xPath =  XPathFactory.newInstance().newXPath();
 
            System.out.println("*************************");
            String expression = "/Employees/Employee[@emplid='3333']/email";
            System.out.println(expression);
            String email = xPath.compile(expression).evaluate(xmlDocument);
            System.out.println(email);
 
            System.out.println("*************************");
            expression = "/Employees/Employee/firstname";
            System.out.println(expression);
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
            }
 
            System.out.println("*************************");
            expression = "/Employees/Employee[@type='admin']/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
            }
 
            System.out.println("*************************");
            expression = "/Employees/Employee[@emplid='2222']";
            System.out.println(expression);
            Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
            if(null != node) {
                nodeList = node.getChildNodes();
                for (int i = 0;null!=nodeList && i < nodeList.getLength(); i++) {
                    Node nod = nodeList.item(i);
                    if(nod.getNodeType() == Node.ELEMENT_NODE)
                        System.out.println(nodeList.item(i).getNodeName() + " : " + nod.getFirstChild().getNodeValue());
                }
            }
             
            System.out.println("*************************");
 
            expression = "/Employees/Employee[age>40]/firstname";
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            System.out.println(expression);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
            }
         
            System.out.println("*************************");
            expression = "/Employees/Employee[1]/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
            }
            System.out.println("*************************");
            expression = "/Employees/Employee[position() <= 2]/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
            }
 
            System.out.println("*************************");
            expression = "/Employees/Employee[last()]/firstname";
            System.out.println(expression);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
            }
 
            System.out.println("*************************");
 
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }      
    }
	
	
	@Test
    public static void testWithSignature() {
 
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
             
            DocumentBuilder builder =  builderFactory.newDocumentBuilder();
             
            Document xmlDocument = builder.parse ( new ByteArrayInputStream(XML_SIGNATURE.getBytes()) );
            Element  docElement  = xmlDocument.getDocumentElement();
 
            String expression;
 
            System.out.println("*************************");
            expression = "*[local-name() = 'Signature']";
            System.out.println(expression);
            Element signature = DOMUtils.searchElement(docElement, expression);
            System.out.println(DOMUtils.toXML(signature));
            
            System.out.println("*************************");
            expression = "*[local-name() = 'Signature']/*[local-name() = 'SignedInfo']";
            System.out.println(expression);
            Element signedInfo = DOMUtils.searchElement(docElement, expression);
            System.out.println(DOMUtils.toXML(signedInfo));
            
            System.out.println("*************************");
            expression = "*[local-name() = 'Signature']/*[local-name() = 'SignedInfo']/*[local-name() = 'Reference']";
            System.out.println(expression);
            List<Element> references = DOMUtils.searchElements(docElement, expression);
            for ( Element reference : references ) {
            	System.out.println(DOMUtils.toXML(reference));
            }
            
            
            System.out.println("*************************");
            expression = "*[local-name() = 'Signature']/*[local-name() = 'SignedInfo']/*[local-name() = 'Reference' and @URI='#']";
            System.out.println(expression);
            Element bodyReference = DOMUtils.searchElement(docElement, expression);
            System.out.println(DOMUtils.toXML(bodyReference));
            
            System.out.println("*************************");
            expression = "*[local-name() = 'Signature']/*[local-name() = 'SignedInfo']/*[local-name() = 'Reference' and @URI='#']/*[local-name() = 'DigestValue']";
            System.out.println(expression);
            Element digestValueElement = DOMUtils.searchElement(docElement, expression);
            System.out.println(DOMUtils.toXML(digestValueElement));
 
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }      
    }
}