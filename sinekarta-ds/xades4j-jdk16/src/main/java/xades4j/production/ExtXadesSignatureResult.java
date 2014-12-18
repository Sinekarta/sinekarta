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

import xades4j.properties.QualifyingProperties;
import xades4j.properties.QualifyingProperty;
import xades4j.utils.DataGetter;
import xades4j.utils.DataGetterImpl;
import xades4j.xml.sign.ExtXMLSignature;

/**
 * The result of signature production. It includes the signature and the final
 * set of qualifying properties.
 * @author Luís
 */
public class ExtXadesSignatureResult
{
    private final ExtXMLSignature signature;
    private final QualifyingProperties qualifyingProperties;
    private final DataGetter<QualifyingProperty> propertyFilter;

    ExtXadesSignatureResult(
            ExtXMLSignature signature,
            QualifyingProperties qualifyingProperties)
    {
        this.signature = signature;
        this.qualifyingProperties = qualifyingProperties;
        this.propertyFilter = new DataGetterImpl<QualifyingProperty>(qualifyingProperties.all());
    }

    /**
     * Gets a {@code DataGetter} that allows easy filtered access to the properties.
     * @return the filter
     */
    public DataGetter<QualifyingProperty> getPropertyFilter()
    {
        return propertyFilter;
    }

    /**
     * Gets the whole set of qualifying properties in the signature, organized
     * by type.
     * @return the properties
     */
    public QualifyingProperties getQualifyingProperties()
    {
        return qualifyingProperties;
    }

    /**
     * Gets the resulting {@code ExtXMLSignature}.
     * @return the signature
     */
    public ExtXMLSignature getSignature()
    {
        return signature;
    }
}
