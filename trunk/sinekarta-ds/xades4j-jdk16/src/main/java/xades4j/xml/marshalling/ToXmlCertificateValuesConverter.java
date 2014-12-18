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
package xades4j.xml.marshalling;

import java.util.Collection;
import java.util.List;
import org.w3c.dom.Document;
import xades4j.properties.data.CertificateValuesData;
import xades4j.properties.data.PropertyDataObject;
import xades4j.xml.bind.xades.XmlCertificateValuesType;
import xades4j.xml.bind.xades.XmlEncapsulatedPKIDataType;
import xades4j.xml.bind.xades.XmlUnsignedPropertiesType;

/**
 *
 * @author Luís
 */
class ToXmlCertificateValuesConverter implements UnsignedPropertyDataToXmlConverter
{
    @Override
    public void convertIntoObjectTree(
            PropertyDataObject propData,
            XmlUnsignedPropertiesType xmlProps,
            Document doc)
    {
        Collection<byte[]> certValues = ((CertificateValuesData)propData).getData();

        XmlCertificateValuesType xmlCertValues = new XmlCertificateValuesType();
        List xmlCerts = xmlCertValues.getEncapsulatedX509CertificateOrOtherCertificate();

        for (byte[] encodCer : certValues)
        {
            XmlEncapsulatedPKIDataType xmlEncodCert = new XmlEncapsulatedPKIDataType();
            xmlEncodCert.setValue(encodCer);
            xmlCerts.add(xmlEncodCert);
        }

        xmlProps.getUnsignedSignatureProperties().setCertificateValues(xmlCertValues);
    }
}
