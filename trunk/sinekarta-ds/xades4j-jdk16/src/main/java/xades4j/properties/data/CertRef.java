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
package xades4j.properties.data;

import java.math.BigInteger;

/**
 *
 * @author Luís
 */
public class CertRef
{
    public String digestAlgUri;
    /**
     * The digest value for the certificate, already decoded from base-64.
     */
    public byte[] digestValue;
    public String issuerDN;
    public BigInteger serialNumber;

    public CertRef(
            String issuerDN, BigInteger serialNumber,
            String digestAlgUri, byte[] digestValue)
    {
        this.digestAlgUri = digestAlgUri;
        this.digestValue = digestValue;
        this.issuerDN = issuerDN;
        this.serialNumber = serialNumber;
    }
}
