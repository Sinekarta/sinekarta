/*
 * XAdES4j - A Java library for generation and verification of XAdES signatures.
 * Copyright (C) 2012 Luis Goncalves.
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
package xades4j.xml.marshalling.algorithms;

import java.util.Collections;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import xades4j.properties.DataObjectTransform;

/**
 * Marshaller for the deprecated DataObjectTransform class, needed for backwards
 * compatibility.
 * @author Luís
 */
class DeprecatedDataObjectTransformParamsMarshaller implements AlgorithmParametersMarshaller<DataObjectTransform>
{
    @Override
    public List<Node> marshalParameters(DataObjectTransform alg, Document doc)
    {
        Node param = alg.getTransformParams();
        return param == null ? null : Collections.singletonList(param);
    }
}
