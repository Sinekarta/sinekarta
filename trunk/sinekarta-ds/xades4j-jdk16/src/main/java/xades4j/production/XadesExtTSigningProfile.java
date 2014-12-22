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

import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SignaturePolicyInfoProvider;
import xades4j.providers.impl.ExtKeyringDataProvider;

/**
 * A profile for producing XAdES-T signatures. A {@link KeyingDataProvider} has
 * to be supplied. The library has a default {@link xades4j.providers.TimeStampTokenProvider}
 * that will be used to configure the {@code XadesSigner}. As all teh other components
 * it can be exchanged.
 * <p>
 * A {@link SignaturePolicyInfoProvider} should be added to produce a XAdES-T based
 * on XAdES-EPES.
 * @see XadesSigningProfile
 * @author Luís
 */
public class XadesExtTSigningProfile extends XadesSigningProfile
{
    public XadesExtTSigningProfile(ExtKeyringDataProvider extKeyingProvider) {
        super(extKeyingProvider);
    }

    @Override
    protected Class<? extends XadesSigner> getSignerClass() {
        return SignerExtT.class;
    }
}
