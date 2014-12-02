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
package org.sinekartads.core.xml;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;

import xades4j.production.EnvelopedXmlObject;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSignatureResult;
import xades4j.production.XadesSigner;
import xades4j.production.XadesSigningProfile;
import xades4j.properties.DataObjectDesc;
import xades4j.properties.DataObjectFormatProperty;
import xades4j.providers.KeyingDataProvider;

/**
 *
 * @author Luís
 */
public class SignerBESTest01 extends SignerTestBase
{
	

    @Test
    public void testSignBES() throws Exception
    {
        System.out.println("signBES");

        Document doc = getDocument("document.xml");
//        Document doc2 = getDocument("employeesalary.xml");
//        Node objectContent = doc1.importNode(doc2.getDocumentElement(), true);
//        Element elemToSign = doc1.getDocumentElement();
        
        String alias = "Alessandro De Prato";
        KeyingDataProvider kp = kdpFactory.getKeyRingDataProvider(alias);
		XadesSigningProfile p = new XadesBesSigningProfile(kp);
        XadesSigner signer = p.newSigner();


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
//        Document doc = dbf.newDocumentBuilder().parse("xmlpath"); // here put all xml

        DataObjectDesc obj = new EnvelopedXmlObject(doc.getDocumentElement(), "text/plain", "http://www.w3.org/2000/09/xmldsig#base64");
        DataObjectFormatProperty dofp = new DataObjectFormatProperty("text/plain", "http://www.w3.org/2000/09/xmldsig#base64");
        dofp.withDescription("BINARY_FORMAT [ZIP]");
        obj.withDataObjectFormat(dofp);
        SignedDataObjects dataObjs = new SignedDataObjects(obj);

        XadesSignatureResult result = signer.sign(dataObjs, doc);
        result.getSignature().getDocument();
        
////        DataObjectDesc obj = new DataObjectReference("http://...").withDataObjectTimeStamp();
//        DataObjectDesc obj = new EnvelopedXmlObject(node).withDataObjectTimeStamp();
//        SignedDataObjects dataObjs = new SignedDataObjects(obj).withCommitmentType(AllDataObjsCommitmentTypeProperty.proofOfOrigin());
//        Element sigParentNode = ...; // The DOM node to which the signature will be appended (Element or Document)
//        signer.sign(dataObjs, sigParentNode);
//        
//        
//        Element elemToSign = doc.getDocumentElement();
//        XadesSigner signer = new XadesTSigningProfile(...).newSigner();
//        new Enveloped(signer).sign(elemToSign);
    }
}
