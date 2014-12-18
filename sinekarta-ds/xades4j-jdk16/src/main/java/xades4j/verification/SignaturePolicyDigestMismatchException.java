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
package xades4j.verification;

import xades4j.properties.ObjectIdentifier;

/**
 * Thrown during verification of the {@code SignaturePolicyIdentifier} property
 * if the actual digest of the policy document and the digest contained in the
 * property are different.
 * @author Luís
 */
public class SignaturePolicyDigestMismatchException extends SignaturePolicyVerificationException
{
    public SignaturePolicyDigestMismatchException(ObjectIdentifier sigPolicyId)
    {
        super(sigPolicyId);
    }

    @Override
    protected String getVerificationMessage()
    {
        return "Signature policy digests (calculated and expected) don't match";
    }
}
