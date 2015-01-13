/*
 * Copyright (C) 2010 - 2012 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.sinekartads.model.domain;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.util.TemplateUtils;

/**
 *
 * @author adeprato
 */
public class TsRequestInfo implements Serializable {
	
	private static final long serialVersionUID = -6854690147613859378L;

	public TsRequestInfo (
			SignDisposition.TimeStamp disposition,
			DigestAlgorithm messageImprintAlgOID,
			BigInteger nounce,
			String tsUrl,
			String tsUsername,
			String tsPassword ) {	
		
		Assert.assertNotNull ( disposition );
		Assert.assertNotNull ( messageImprintAlgOID );
		Assert.assertNotNull ( tsUrl );
		if ( StringUtils.isNotEmpty(tsUsername) || StringUtils.isNotEmpty(tsPassword) ) {
			Assert.assertNotNull ( tsUsername );
			Assert.assertNotNull ( tsPassword );
		}
		
		
		this.disposition = disposition;
		this.messageImprintAlgorithm = messageImprintAlgOID;
		this.tsUrl = tsUrl;
		this.tsUsername = tsUsername;
		this.tsPassword = tsPassword;
		
//		if ( nounce == null ) {
//			nounce = BigInteger.valueOf ( RandomUtils.nextLong() );
//		}
		this.nounce = nounce;
	}
	
	private final SignDisposition.TimeStamp disposition;
	private final DigestAlgorithm messageImprintAlgorithm;
	private final BigInteger nounce;
	private final String tsUrl;
	private final String tsUsername;
	private final String tsPassword;
	
    public SignDisposition.TimeStamp getDisposition() {
		return disposition;
	}
		
	public DigestAlgorithm getMessageImprintAlgorithm() {
		return messageImprintAlgorithm;
	}

	public BigInteger getNounce() {
		return nounce;
	}

	public String getTsUrl() {
		return tsUrl;
	}

	public String getTsUsername() {
		return tsUsername;
	}

	public String getTsPassword() {
		return tsPassword;
	}

    
    
    // -----
 	// --- Status-depending properties
 	// -

	private byte[] messageImprintDigest;
 	
	public byte[] getMessageImprintDigest ( ) {
		assertStatus ( TsRequestStatus.DIGEST );
		return messageImprintDigest;
	}
 	
 	
 	
 	// -----
 	// --- Status transition
 	// -
 	
 	public static enum TsRequestStatus {
 		EMPTY,
 		DIGEST
 	}
    
	public TsRequestStatus getStatus ( ) {
		
		TsRequestStatus status;
		if ( ArrayUtils.isEmpty(messageImprintDigest) )	{
			status = TsRequestStatus.EMPTY;
		}  else {
			status = TsRequestStatus.DIGEST;
		}
		return status;
	}
	
	protected void assertNotFinalized ( ) 
			throws IllegalStateException {

		TsRequestStatus status = getStatus(); 
		if ( status == TsRequestStatus.DIGEST ) {
			throw new IllegalStateException(String.format(
					"unable to modify a TsRequest which already received the digest"));
		}
	}
	
	protected void assertStatus ( 
			TsRequestStatus expectedStatus ) 
					throws IllegalStateException {
		
		TsRequestStatus status = getStatus(); 
		if ( status != expectedStatus ) {
			throw new IllegalStateException(String.format(
					"invalid signature status: %s - expected: %s", status, expectedStatus ));
		}
	}
	
	public TsRequestInfo evaluateMessageImprint ( byte[] message ) {
		
    	assertNotFinalized();
    	Assert.assertTrue ( ArrayUtils.isNotEmpty(message) );
    	
    	// Evaluate the message imprint digest
    	byte[] messageImprintDigest;
    	try {
			MessageDigest digester = MessageDigest.getInstance(messageImprintAlgorithm.getName());
			digester.update(message);
			messageImprintDigest = digester.digest();
    	} catch(NoSuchAlgorithmException e) {
    		// never thrown, algorithm validity granted by DigestAlgorithm
    		throw new RuntimeException(e);
    	}
    	TsRequestInfo digestInstance = TemplateUtils.Instantiation.clone ( this );
		digestInstance.messageImprintDigest = messageImprintDigest;
		return digestInstance;    	
    }
    
    public boolean isFinalized() {
    	return getStatus() == TsRequestStatus.DIGEST;
    }

}
