/*
 * eID Applet Project.
 * Copyright (C) 2009-2010 FedICT.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package org.sinekartads.core.cms;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;

/**
 * A dummy private key implementation.
 * 
 * @author Frank Cornelis
 * 
 */
public class DummyPrivateKey implements RSAPrivateKey {

	private static final long serialVersionUID = 1L;

	public String getAlgorithm() {
		return "RSA";
	}

	public byte[] getEncoded() {
		return null;
	}

	public String getFormat() {
		return null;
	}

	public BigInteger getModulus() {
		return null;
	}

	public BigInteger getPrivateExponent() {
		return null;
	}
}
