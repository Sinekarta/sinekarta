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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xades4j.properties.DataObjectDesc;
import xades4j.utils.DOMHelper;
import xades4j.xml.sign.DOMUtils;
import xades4j.xml.sign.ExtXMLSignature;

/**
 * Context used during the generation of the properties low-level data (property
 * data objects). Contains informations about the algorithms in use and the resources
 * being signed.
 * 
 * @see PropertiesDataObjectsGenerator
 * @author Luís
 */
public class ExtPropertiesDataGenerationContext extends PropertiesDataGenerationContext
{

    private final List<Reference> references;
    private final Map<DataObjectDesc, Reference> referencesMappings;
    private final Document sigDocument;
    private String signatureMethodURI;
    private ExtXMLSignature targetExtSignature;

    /**
     * @param orderedDataObjs
     * @param referencesMappings should be unmodifiable
     * @param elemInSigDoc
     * @param algorithmsProvider
     */
    ExtPropertiesDataGenerationContext(
            Collection<DataObjectDesc> orderedDataObjs,
            Map<DataObjectDesc, Reference> referencesMappings,
            Document sigDocument)
    {
    	super(orderedDataObjs, referencesMappings, sigDocument);
        this.referencesMappings = referencesMappings;
        this.sigDocument = sigDocument;

        List<Reference> orderedRefs = new ArrayList<Reference>(orderedDataObjs.size());
        for (DataObjectDesc dataObjDesc : orderedDataObjs)
        {
            orderedRefs.add(referencesMappings.get(dataObjDesc));
        }

        this.references = Collections.unmodifiableList(orderedRefs);
    }

    /**
     * Gets all the {@code Reference}s present in the signature that is being
     * created, except the signed properties reference, in order of appearence
     * within {@code SignedInfo}.
     * @return the unmodifiable list of {@code Reference}s
     */
    public List<Reference> getReferences()
    {
        return references;
    }

    /**
     * Gets the mappings from high-level {@code DataObjectDesc}s to {@code Reference}s.
     * This should be used when a data object property needs any information from
     * the {@code Reference} that corresponds to the data object.
     * @return the unmodifiable mapping
     */
    public Map<DataObjectDesc, Reference> getReferencesMappings()
    {
        return referencesMappings;
    }

    /**
     * Gets the XML Signature that is being created. This is only available when
     * generating unisgned properties data objects.
     * @return the target signature or {@code null} if not yet available
     */
    public XMLSignature getTargetXmlSignature()
    {
        return DOMUtils.extSignatureToXML(targetExtSignature, signatureMethodURI);
    }
    
    /**
     * @deprecated do not use - use setTargetExtSignature(ExtXMLSignature, String) instead
     */
    @Override
    void setTargetXmlSignature(XMLSignature targetXmlSignature) {
    	throw new UnsupportedOperationException ( "use setTargetExtSignature(ExtXMLSignature, String) instead" );
    }

    void setTargetExtSignature(ExtXMLSignature targetXmlSignature, String signatureMethodURI)
    {
        if (this.targetExtSignature != null)
        {
            throw new IllegalStateException("TargetXMLSignature already set");
        }
        this.targetExtSignature = targetXmlSignature;
        this.signatureMethodURI = signatureMethodURI;
    }

    Document getSignatureDocument()
    {
        return this.sigDocument;
    }

    /**
     * Creates a DOM {@code Element} in the signature's document. This can be useful
     * when generating {@link xades4j.properties.data.GenericDOMData} data objects.
     * @param name the local name of the element
     * @param namespace the namespace where the element will be created
     * @return the created element
     */
    public Element createElementInSignatureDoc(String name, String prefix, String namespace)
    {
        return DOMHelper.createElement(this.sigDocument, name, prefix, namespace);
    }
}
