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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import xades4j.UnsupportedAlgorithmException;
import xades4j.XAdES4jException;
import xades4j.XAdES4jXMLSigException;
import xades4j.algorithms.Algorithm;
import xades4j.properties.DataObjectDesc;
import xades4j.properties.QualifyingProperties;
import xades4j.properties.QualifyingProperty;
import xades4j.properties.SignedSignatureProperty;
import xades4j.properties.SigningCertificateProperty;
import xades4j.properties.UnsignedSignatureProperty;
import xades4j.properties.data.SigAndDataObjsPropertiesData;
import xades4j.providers.AlgorithmsProviderEx;
import xades4j.providers.BasicSignatureOptionsProvider;
import xades4j.providers.DataObjectPropertiesProvider;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SignaturePropertiesProvider;
import xades4j.providers.SigningCertChainException;
import xades4j.utils.DOMHelper;
import xades4j.utils.ObjectUtils;
import xades4j.xml.marshalling.SignedPropertiesMarshaller;
import xades4j.xml.marshalling.UnsignedPropertiesMarshaller;
import xades4j.xml.marshalling.algorithms.AlgorithmsParametersMarshallingProvider;
import xades4j.xml.sign.DOMUtils;
import xades4j.xml.sign.ExtXMLSignature;

import com.google.inject.Inject;

/**
 * Base logic for producing XAdES signatures (XAdES-BES).
 * @author Luís
 */
class SignerExtBES implements XadesSigner, XadesSignerExt
{

	static DocumentBuilder db;
    static
    {
    	try {
	        Init.initXMLSec();
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	   	 	dbf.setNamespaceAware(true);
	   	 	db = dbf.newDocumentBuilder();
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    /**/
    private final KeyingDataProvider keyingProvider;
    private final AlgorithmsProviderEx algorithmsProvider;
    private final DataObjectDescsProcessor dataObjectDescsProcessor;
    private final PropertiesDataObjectsGenerator propsDataObjectsGenerator;
    private final SignedPropertiesMarshaller signedPropsMarshaller;
    private final UnsignedPropertiesMarshaller unsignedPropsMarshaller;
    private final AlgorithmsParametersMarshallingProvider algorithmsParametersMarshaller;
    /**/
    private final KeyInfoBuilder keyInfoBuilder;
    private final QualifyingPropertiesProcessor qualifPropsProcessor;

    @Inject
    protected SignerExtBES(
            KeyingDataProvider keyingProvider,
            AlgorithmsProviderEx algorithmsProvider,
            BasicSignatureOptionsProvider basicSignatureOptionsProvider,
            DataObjectDescsProcessor dataObjectDescsProcessor,
            SignaturePropertiesProvider signaturePropsProvider,
            DataObjectPropertiesProvider dataObjPropsProvider,
            PropertiesDataObjectsGenerator propsDataObjectsGenerator,
            SignedPropertiesMarshaller signedPropsMarshaller,
            UnsignedPropertiesMarshaller unsignedPropsMarshaller,
            AlgorithmsParametersMarshallingProvider algorithmsParametersMarshaller)
    {
        if (ObjectUtils.anyNull(
                keyingProvider, algorithmsProvider,
                signaturePropsProvider, dataObjPropsProvider, propsDataObjectsGenerator,
                signedPropsMarshaller, unsignedPropsMarshaller, algorithmsParametersMarshaller))
        {
            throw new NullPointerException("One or more arguments are null");
        }

        this.keyingProvider = keyingProvider;
        this.algorithmsProvider = algorithmsProvider;
        this.propsDataObjectsGenerator = propsDataObjectsGenerator;
        this.signedPropsMarshaller = signedPropsMarshaller;
        this.unsignedPropsMarshaller = unsignedPropsMarshaller;
        this.algorithmsParametersMarshaller = algorithmsParametersMarshaller;

        this.dataObjectDescsProcessor = dataObjectDescsProcessor;
        this.keyInfoBuilder = new KeyInfoBuilder(basicSignatureOptionsProvider, algorithmsProvider);
        this.qualifPropsProcessor = new QualifyingPropertiesProcessor(signaturePropsProvider, dataObjPropsProvider);
    }

    @Override
    public final byte[] digest(
            SignedDataObjects signedDataObjects,
            Node parent) throws XAdES4jException
    {
        return digest(signedDataObjects, parent, SignatureAppendingStrategies.AsLastChild);
    }

    @Override
    public final byte[] digest(
            SignedDataObjects signedDataObjects,
            Node referenceNode,
            SignatureAppendingStrategy appendingStrategy) throws XAdES4jException
    {
    	byte[] digest = null;
    	
        if (null == referenceNode)
        {
            throw new NullPointerException("Reference node node cannot be null");
        }
        if (null == signedDataObjects)
        {
            throw new NullPointerException("References cannot be null");
        }
        if (signedDataObjects.isEmpty())
        {
            throw new IllegalArgumentException("Data objects list is empty");
        }

        Document signatureDocument = DOMHelper.getOwnerDocument(referenceNode);

        // Generate unique identifiers for the Signature and the SignedProperties.
        String signatureId = String.format("xmldsig-%s", UUID.randomUUID());
        String signedPropsId = String.format("%s-signedprops", signatureId);

        // Signing certificate chain (may contain only the signing certificate).
        List<X509Certificate> signingCertificateChain = this.keyingProvider.getSigningCertificateChain();
        if (null == signingCertificateChain || signingCertificateChain.isEmpty())
        {
            throw new SigningCertChainException("Signing certificate not provided");
        }
        X509Certificate signingCertificate = signingCertificateChain.get(0);

        // The XMLSignature (ds:Signature).
        XMLSignature xmlSignature = createXMLSignature(
                signatureDocument,
                signedDataObjects.getBaseUri(),
                signingCertificate.getPublicKey().getAlgorithm());
        xmlSignature.setId(signatureId);
        
        /* ds:KeyInfo */
        this.keyInfoBuilder.buildKeyInfo(signingCertificate, xmlSignature);

        /* References */
        // Process the data object descriptions to get the References and mappings.
        // After this call all the signed data objects References and XMLObjects
        // are added to the signature.
        Map<DataObjectDesc, Reference> referenceMappings = this.dataObjectDescsProcessor.process(
                signedDataObjects,
                xmlSignature);

        /* QualifyingProperties element */
        // Create the QualifyingProperties element
        Element qualifyingPropsElem = ElementProxy.createElementForFamily(
                xmlSignature.getDocument(),
                QualifyingProperty.XADES_XMLNS, QualifyingProperty.QUALIFYING_PROPS_TAG);
        qualifyingPropsElem.setAttributeNS(null, QualifyingProperty.TARGET_ATTR, '#' + signatureId);
        // --- FIXME remove when stable ------------------------------------------------------------------------
        // xmlns:xades141 non è richiesto da Dike, lo togliamo per semplicità
        // -----------------------------------------------------------------------------------------------------
//        qualifyingPropsElem.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xades141", QualifyingProperty.XADESV141_XMLNS);
        // -----------------------------------------------------------------------------------------------------
        // ds:Object to contain QualifyingProperties
        ObjectContainer qPropsXmlObj = new ObjectContainer(xmlSignature.getDocument());
        qPropsXmlObj.appendChild(qualifyingPropsElem);
        try
        {
            xmlSignature.appendObject(qPropsXmlObj);
        } catch (XMLSignatureException ex)
        {
            // -> xmlSignature.appendObject(xmlObj): not thrown when signing.
            throw new IllegalStateException(ex);
        }

        /* Collect the properties */
        // Get the format specific signature properties.
        Collection<SignedSignatureProperty> fsssp = new ArrayList<SignedSignatureProperty>(2);
        Collection<UnsignedSignatureProperty> fsusp = new ArrayList<UnsignedSignatureProperty>(2);
//        getFormatSpecificSignatureProperties(fsssp, fsusp, signingCertificateChain);
        // Gather all the signature and data objects properties.
        QualifyingProperties qualifProps = qualifPropsProcessor.getQualifyingProperties(
                signedDataObjects, fsssp, fsusp);

        try
        {
            // The signature needs to be appended to the document from now on because
            // property data generation may need to dereference same-document data
            // object references.
            appendingStrategy.append(xmlSignature.getElement(), referenceNode);

            /* Signed properties */
            // Create the context for signed properties data objects generation.
            PropertiesDataGenerationContext propsDataGenCtx = new PropertiesDataGenerationContext(
                    signedDataObjects.getDataObjectsDescs(),
                    referenceMappings,
                    signatureDocument);
            // Generate the signed properties data objects. The data objects structure
            // is verifier in the process.
            SigAndDataObjsPropertiesData signedPropsData = this.propsDataObjectsGenerator.generateSignedPropertiesData(
                    qualifProps.getSignedProperties(),
                    propsDataGenCtx);
            // Marshal the signed properties data to the QualifyingProperties node.
            this.signedPropsMarshaller.marshal(signedPropsData, qualifyingPropsElem);
            Element signedPropsElem = DOMHelper.getFirstChildElement(qualifyingPropsElem);
            DOMHelper.setIdAsXmlId(signedPropsElem, signedPropsId);

            // SignedProperties reference
            // XAdES 6.3.1: "In order to protect the properties with the signature,
            // a ds:Reference element MUST be added to the XMLDSIG signature (...)
            // composed in such a way that it uses the SignedProperties element (...)
            // as the input for computing its corresponding digest. Additionally,
            // (...) use the Type attribute of this particular ds:Reference element,
            // with its value set to: http://uri.etsi.org/01903#SignedProperties."

            String digestAlgUri = algorithmsProvider.getDigestAlgorithmForDataObjsReferences();
            if (null == digestAlgUri)
            {
                throw new NullPointerException("Digest algorithm URI not provided");
            }

            try
            {
                xmlSignature.addDocument('#' + signedPropsId, null, digestAlgUri, null, QualifyingProperty.SIGNED_PROPS_TYPE_URI);
            } catch (XMLSignatureException ex)
            {
                // Seems to be thrown when the digest algorithm is not supported. In
                // this case, if it wasn't thrown when processing the data objects it
                // shouldn't be thrown now!
                throw new UnsupportedAlgorithmException(
                        "Digest algorithm not supported in the XML Signature provider",
                        digestAlgUri, ex);
            }
            
            String signatureMethodURI = this.algorithmsProvider.getSignatureAlgorithm ( 
            		signingCertificate.getPublicKey().getAlgorithm() ).getUri();
            ExtXMLSignature extSignature = DOMUtils.xmlSignatureToExt ( xmlSignature, signatureMethodURI );

            // Apply the signature
            try
            {
                PrivateKey signingKey = keyingProvider.getSigningKey(signingCertificate);
                digest = extSignature.digest(signingKey);
            }
            catch (XMLSignatureException ex)
            {
                throw new XAdES4jXMLSigException(ex.getMessage(), ex);
            }
            
            // Set the ds:SignatureValue id.
            Element sigValueElem = DOMHelper.getFirstDescendant(
                    xmlSignature.getElement(),
                    Constants.SignatureSpecNS, Constants._TAG_SIGNATUREVALUE);
            DOMHelper.setIdAsXmlId(sigValueElem, String.format("%s-sigvalue", signatureId));

            /* Marshal unsigned properties */
            // Generate the unsigned properties data objects. The data objects structure
            // is verifier in the process.
            propsDataGenCtx.setTargetXmlSignature(xmlSignature);
            SigAndDataObjsPropertiesData unsignedPropsData = this.propsDataObjectsGenerator.generateUnsignedPropertiesData(
                    qualifProps.getUnsignedProperties(),
                    propsDataGenCtx);
            // Marshal the unsigned properties to the final QualifyingProperties node.
            this.unsignedPropsMarshaller.marshal(unsignedPropsData, qualifyingPropsElem);
        }
        catch (XAdES4jException ex)
        {
            appendingStrategy.revert(xmlSignature.getElement(), referenceNode);
            throw ex;
        }

        return digest;
    }

    @Override
    public final XadesSignatureResult sign(
            SignedDataObjects signedDataObjects,
            Node parent) throws XAdES4jException
    {
        return sign(signedDataObjects, parent, SignatureAppendingStrategies.AsLastChild);
    }
    
    @Override
    public final XadesSignatureResult sign(
            SignedDataObjects signedDataObjects,
            Node referenceNode,
            SignatureAppendingStrategy appendingStrategy) throws XAdES4jException
    {
        if (null == referenceNode)
        {
            throw new NullPointerException("Reference node node cannot be null");
        }
        if (null == signedDataObjects)
        {
            throw new NullPointerException("References cannot be null");
        }
        if (signedDataObjects.isEmpty())
        {
            throw new IllegalArgumentException("Data objects list is empty");
        }

        Document signatureDocument = DOMHelper.getOwnerDocument(referenceNode);

        // Generate unique identifiers for the Signature and the SignedProperties.
        String signatureId = String.format("xmldsig-%s", UUID.randomUUID());
        String signedPropsId = String.format("%s-signedprops", signatureId);

        // Signing certificate chain (may contain only the signing certificate).
        List<X509Certificate> signingCertificateChain = this.keyingProvider.getSigningCertificateChain();
        if (null == signingCertificateChain || signingCertificateChain.isEmpty())
        {
            throw new SigningCertChainException("Signing certificate not provided");
        }
        X509Certificate signingCertificate = signingCertificateChain.get(0);
        String signatureMethodURI = this.algorithmsProvider.getSignatureAlgorithm ( 
        		signingCertificate.getPublicKey().getAlgorithm() ).getUri();

        // The XMLSignature (ds:Signature).
        XMLSignature xmlSignature = createXMLSignature(
                signatureDocument,
                signedDataObjects.getBaseUri(),
                signingCertificate.getPublicKey().getAlgorithm());
        ExtXMLSignature extSignature;

        xmlSignature.setId(signatureId);
//        xmlSignature.getElement().setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ds", QualifyingProperty.XADESV141_XMLNS);

        /* ds:KeyInfo */
        this.keyInfoBuilder.buildKeyInfo(signingCertificate, xmlSignature);

        /* References */
        // Process the data object descriptions to get the References and mappings.
        // After this call all the signed data objects References and XMLObjects
        // are added to the signature.
        Map<DataObjectDesc, Reference> referenceMappings = this.dataObjectDescsProcessor.process(
                signedDataObjects,
                xmlSignature);

        /* QualifyingProperties element */
        // Create the QualifyingProperties element
        Element qualifyingPropsElem = ElementProxy.createElementForFamily(
                xmlSignature.getDocument(),
                QualifyingProperty.XADES_XMLNS, QualifyingProperty.QUALIFYING_PROPS_TAG);
        qualifyingPropsElem.setAttributeNS(null, QualifyingProperty.TARGET_ATTR, '#' + signatureId);
//        qualifyingPropsElem.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xades141", QualifyingProperty.XADESV141_XMLNS);
        // ds:Object to contain QualifyingProperties
        ObjectContainer qPropsXmlObj = new ObjectContainer(xmlSignature.getDocument());
        qPropsXmlObj.appendChild(qualifyingPropsElem);
        try
        {
            xmlSignature.appendObject(qPropsXmlObj);
        } catch (XMLSignatureException ex)
        {
            // -> xmlSignature.appendObject(xmlObj): not thrown when signing.
            throw new IllegalStateException(ex);
        }

        /* Collect the properties */
        // Get the format specific signature properties.
        Collection<SignedSignatureProperty> fsssp = new ArrayList<SignedSignatureProperty>(2);
        Collection<UnsignedSignatureProperty> fsusp = new ArrayList<UnsignedSignatureProperty>(2);
//        getFormatSpecificSignatureProperties(fsssp, fsusp, signingCertificateChain);
        // Gather all the signature and data objects properties.
        QualifyingProperties qualifProps = qualifPropsProcessor.getQualifyingProperties(
                signedDataObjects, fsssp, fsusp);

        try
        {
            // The signature needs to be appended to the document from now on because
            // property data generation may need to dereference same-document data
            // object references.
            appendingStrategy.append(xmlSignature.getElement(), referenceNode);

            /* Signed properties */
            // Create the context for signed properties data objects generation.
            ExtPropertiesDataGenerationContext propsDataGenCtx = new ExtPropertiesDataGenerationContext(
                    signedDataObjects.getDataObjectsDescs(),
                    referenceMappings,
                    signatureDocument);
            // Generate the signed properties data objects. The data objects structure
            // is verifier in the process.
            SigAndDataObjsPropertiesData signedPropsData = this.propsDataObjectsGenerator.generateSignedPropertiesData(
                    qualifProps.getSignedProperties(),
                    propsDataGenCtx);
            // Marshal the signed properties data to the QualifyingProperties node.
            this.signedPropsMarshaller.marshal(signedPropsData, qualifyingPropsElem);
            Element signedPropsElem = DOMHelper.getFirstChildElement(qualifyingPropsElem);
            DOMHelper.setIdAsXmlId(signedPropsElem, signedPropsId);
            
            xmlSignature.addDocument(
                    '#' + signedPropsElem.getAttribute("Id"),
                    null,
                    this.algorithmsProvider.getDigestAlgorithmForDataObjsReferences());

            // SignedProperties reference
            // XAdES 6.3.1: "In order to protect the properties with the signature,
            // a ds:Reference element MUST be added to the XMLDSIG signature (...)
            // composed in such a way that it uses the SignedProperties element (...)
            // as the input for computing its corresponding digest. Additionally,
            // (...) use the Type attribute of this particular ds:Reference element,
            // with its value set to: http://uri.etsi.org/01903#SignedProperties."

            String digestAlgUri = algorithmsProvider.getDigestAlgorithmForDataObjsReferences();
            if (null == digestAlgUri)
            {
                throw new NullPointerException("Digest algorithm URI not provided");
            }
//
//            try
//            {
//                xmlSignature.addDocument('#' + signedPropsId, null, digestAlgUri, null, QualifyingProperty.SIGNED_PROPS_TYPE_URI);
//            } catch (XMLSignatureException ex)
//            {
//                // Seems to be thrown when the digest algorithm is not supported. In
//                // this case, if it wasn't thrown when processing the data objects it
//                // shouldn't be thrown now!
//                throw new UnsupportedAlgorithmException(
//                        "Digest algorithm not supported in the XML Signature provider",
//                        digestAlgUri, ex);
//            }

            // Inject the digitalSignature into an ExtXMLSignature instance 
            try
            {
            	// Obtain the xmlSignature's owner document
            	Element docElem = xmlSignature.getDocument().getDocumentElement();
            	
            	// Create the extSignature starting from the xmlSignature XML code
                extSignature = DOMUtils.xmlSignatureToExt ( xmlSignature, signatureMethodURI );
                
                // Inject the digital signature into the extSignature, this will evaluate the digestValues as side effect. The 
                // 		digestValueElement relative to the signature target is still empty and will need to be injected afterwards
                extSignature.sign ( keyingProvider.getSigningKey(signingCertificate), digitalSignature );
                
                // Replace the xmlSignature with the extSignature into the document
                //		from now any reference to xmlSignature inside this method code will be replaced with extSignature 
                DOMUtils.searchReplace(docElem, xmlSignature.getElement(), extSignature.getElement());

                String expression = "*[local-name() = 'Signature']/*[local-name() = 'SignedInfo']/*[local-name() = 'Reference' and @URI='#']/*[local-name() = 'DigestValue']"; 
                Element digestValueElement = DOMUtils.searchElement ( docElem, expression );
                
                digestValueElement.setTextContent(Base64.encode(digest));
            }
            catch (XMLSignatureException | XPathExpressionException ex)
            {
                throw new XAdES4jXMLSigException(ex.getMessage(), ex);
			}
            // Set the ds:SignatureValue id.
            Element sigValueElem = DOMHelper.getFirstDescendant(
                    extSignature.getElement(),
                    Constants.SignatureSpecNS, Constants._TAG_SIGNATUREVALUE);
            DOMHelper.setIdAsXmlId(sigValueElem, String.format("%s-sigvalue", signatureId));

            /* Marshal unsigned properties */
            // Generate the unsigned properties data objects. The data objects structure
            // is verifier in the process.
            propsDataGenCtx.setTargetExtSignature(extSignature, signatureMethodURI);
            SigAndDataObjsPropertiesData unsignedPropsData = this.propsDataObjectsGenerator.generateUnsignedPropertiesData(
                    qualifProps.getUnsignedProperties(),
                    propsDataGenCtx);
            // Marshal the unsigned properties to the final QualifyingProperties node.
            this.unsignedPropsMarshaller.marshal(unsignedPropsData, qualifyingPropsElem);
        }
        catch (XAdES4jException ex)
        {
            appendingStrategy.revert(xmlSignature.getElement(), referenceNode);
            throw ex;
        } catch (XMLSignatureException e) {
        	throw new XAdES4jXMLSigException(e.getMessage(), e);
		}

        return new XadesSignatureResult(DOMUtils.extSignatureToXML(extSignature, signatureMethodURI), qualifProps);
    }

    private XMLSignature createXMLSignature(Document signatureDocument, String baseUri, String signingKeyAlgorithm) throws XAdES4jXMLSigException, UnsupportedAlgorithmException
    {
        Algorithm signatureAlg = this.algorithmsProvider.getSignatureAlgorithm(signingKeyAlgorithm);
        if (null == signatureAlg)
        {
            throw new NullPointerException("Signature algorithm not provided");
        }
        Element signatureAlgElem = createElementForAlgorithm(signatureAlg, Constants._TAG_SIGNATUREMETHOD, signatureDocument);


        Algorithm canonAlg = this.algorithmsProvider.getCanonicalizationAlgorithmForSignature();
        if (null == canonAlg)
        {
            throw new NullPointerException("Canonicalization algorithm not provided");
        }
        Element canonAlgElem = createElementForAlgorithm(canonAlg, Constants._TAG_CANONICALIZATIONMETHOD, signatureDocument);

        try
        {
            return new XMLSignature(signatureDocument, baseUri, signatureAlgElem, canonAlgElem);
        } catch (XMLSecurityException ex)
        {
            // Following the code, doesn't seem to be thrown at all.
            throw new XAdES4jXMLSigException(ex.getMessage(), ex);
        }
    }
    
    private Element createElementForAlgorithm(Algorithm algorithm, String elementName, Document signatureDocument) throws UnsupportedAlgorithmException
    {
        Element algorithmElem = XMLUtils.createElementInSignatureSpace(signatureDocument, elementName);
        algorithmElem.setAttributeNS(null, Constants._ATT_ALGORITHM, algorithm.getUri());

        List<Node> algorithmParams = this.algorithmsParametersMarshaller.marshalParameters(algorithm, signatureDocument);
        if (algorithmParams != null)
        {
            for (Node p : algorithmParams)
            {
                algorithmElem.appendChild(p);
            }
        }
        return algorithmElem;
    }

    /**
     * Override in subclasses to collect the signature properties that are mandatory
     * in the corresponding format.
     */
    protected void getFormatSpecificSignatureProperties(
            Collection<SignedSignatureProperty> formatSpecificSignedSigProps,
            Collection<UnsignedSignatureProperty> formatSpecificUnsignedSigProps,
            List<X509Certificate> signingCertificateChain) throws XAdES4jException
    {
        SigningCertificateProperty scp = new SigningCertificateProperty(signingCertificateChain);
        formatSpecificSignedSigProps.add(scp);
    }
    
    
    
    
    private byte[] digest;
    private byte[] digitalSignature;
    
    @Override
    public void setDigitalSignature(byte[] digitalSignature) {
    	this.digitalSignature = digitalSignature;
    }
    
    @Override
    public void setDigest(byte[] digest) {
    	this.digest = digest;    	
    }
}
