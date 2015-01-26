/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package xades4j.xml.sign;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.signature.XMLSignatureException;

/**
 * This class is used by ExtOutputStream instead of the original SignerOutputStream
 * of apache. It involves the same operation but when it digests of the bytes that are 
 * being sent to the nested SignatureAlgorithm. The {@link #getDigest()} returns the 
 * digest of the all bytes that have been received.
 * @author adeprato
 */
public class DigesterOutputStream extends ByteArrayOutputStream {
	
	private static final Map<String, String> reverseUriMapping;
    static {
    	reverseUriMapping = new HashMap<String, String>();
    	reverseUriMapping.put("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "SHA256");
    }
    
    final SignatureAlgorithm sa;
    final MessageDigest digester;

    /**
     * @param sa
     */
    public DigesterOutputStream(SignatureAlgorithm sa) {
        this.sa = sa;       
        try {
        	String algorithm = reverseUriMapping.get ( sa.getAlgorithmURI() );
			this.digester = MessageDigest.getInstance ( algorithm );
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
    }

    /** @inheritDoc */
    public void write(byte[] arg0)  {
        try {
            sa.update(arg0);
        } catch (XMLSignatureException e) {
            throw new RuntimeException("" + e);
        }
        digester.update(arg0);
    }

    /** @inheritDoc */
    public void write(int arg0) {
        try {
            sa.update((byte)arg0);
        } catch (XMLSignatureException e) {
            throw new RuntimeException("" + e);
        }
        digester.update((byte)arg0);
    }

    /** @inheritDoc */
    public void write(byte[] arg0, int arg1, int arg2) {
        try {
            sa.update(arg0, arg1, arg2);
        } catch (XMLSignatureException e) {
            throw new RuntimeException("" + e);
        }
        digester.update(arg0, arg1, arg2);
    }
    
    public byte[] getDigest() {
    	return digester.digest();
    }
}
