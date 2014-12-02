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

import java.security.cert.X509Certificate;

import org.sinekartads.model.domain.SecurityLevel.TimeStampVerifyResult;
import org.sinekartads.model.domain.Transitions.BaseTimeStamp;
import org.sinekartads.model.domain.Transitions.DisposedTimeStamp;
import org.sinekartads.model.domain.Transitions.UntrustedTimeStamp;
import org.sinekartads.model.domain.Transitions.VerifiedTimeStamp;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.TemplateUtils;
import org.springframework.util.Assert;

/**
 *
 * @author adeprato
 */
public class TimeStampInfo

		extends SignatureInfo 	< 	SignatureType.TimeStamp, 
									SignDisposition.TimeStamp, 
									TimeStampVerifyResult, 
									TimeStampInfo >

				implements BaseTimeStamp,
						   UntrustedTimeStamp,
						   VerifiedTimeStamp,
						   DisposedTimeStamp {

	private static final long serialVersionUID = 1630221563278385721L;

	public TimeStampInfo (
			SignatureAlgorithm signatureAlgorithm,
			DigestAlgorithm digestAlgorithm,
			DigestInfo messageImprintInfo,
//			CertificateInfo certificate,
			X509Certificate[] certificateChain,
			byte[] digitalSignature,
			TimeStampVerifyResult verifyResult,
			byte[] encTimeStampToken ) {
		
		super ( SignatureType.TimeStamp.TIMESTAMP, 
				null,
				signatureAlgorithm, 
				digestAlgorithm );
		
		this.digitalSignature = digitalSignature;
		this.messageImprintInfo = messageImprintInfo;
//		this.certificate = certificate;
		this.rawX509Certificates = certificateChain;
		this.verifyResult = verifyResult;
		this.encTimeStampToken = encTimeStampToken;
	}	
	
	
	
	// -----
	// --- RawTimeStamp - Mandatory properties
	// -
	
	private final DigestInfo messageImprintInfo;
	private final byte[] encTimeStampToken;
	
	@Override
	public byte[] getEncTimeStampToken() {
		return encTimeStampToken;
	}
	
	@Override
	public DigestInfo getMessageInprintInfo() {
		return messageImprintInfo;
	}

	@Override
	public String getTsaName() {
		// TODO extract the string from "rawTimeStampToken.getTimeStampInfo().getTsa().getName()"
		throw new UnsupportedOperationException ( "extract the string from \"rawTimeStampToken.getTimeStampInfo().getTsa().getName()\"" );
	}

	@Override
	public SignatureStatus getStatus ( ) {
		
		if ( disposition != null )	return SignatureStatus.Stable.FINALIZED;
		if ( verifyResult != null )	return SignatureStatus.TimeStampVerifyProcess.VERIFIED;
		else						return SignatureStatus.TimeStampVerifyProcess.UNTRUSTED;
	}
	
	
	
	// -----
	// --- RawTimeStamp -> UntrustedTimeStamp
	// -
	
	@Override
	public UntrustedTimeStamp startValidation ( ) {
		
		TimeStampInfo untrustedInstance = TemplateUtils.Instantiation.clone ( this );
		return untrustedInstance;
	}
	
	
	
	// -----
	// --- UntrustedTimeStamp -> VerifiedTimeStamp
	// -
	
	@Override
	public VerifiedTimeStamp toVerifiedTimeStamp ( TimeStampVerifyResult verifyResult ) {
		
		Assert.notNull( verifyResult );
		
		TimeStampInfo verifiedInstance = TemplateUtils.Instantiation.clone ( this );
		verifiedInstance.verifyResult = verifyResult;
		return verifiedInstance;
	}
	
	
	
	// -----
	// --- VerifiedTimeStamp -> DisposedTimeStamp (FINALIZED)
	// -
	
	@Override
	public TimeStampVerifyResult getVerifyResult() {
		return verifyResult;
	}
	
	@Override
	public DisposedTimeStamp toDisposedTimeStamp ( SignDisposition.TimeStamp disposition ) {
		
		Assert.notNull ( disposition );
		
		TimeStampInfo verifiedInstance = TemplateUtils.Instantiation.clone ( this );
		verifiedInstance.disposition = disposition;
		return verifiedInstance;
	}
	
	
	
	// -----
	// --- Signature specific protocol disabilitation
	// -

	/**
	 * @deprecated use {@link #getMessageInprintInfo()} instead
	 */
	@Override
	public DigestInfo getDigest() {
		return messageImprintInfo;
	}
	
	/**
	 * @deprecated do not use - operation not applicable to a timestamp
	 */
	@Override
	public boolean isCounterSignature( ) {
		throw new UnsupportedOperationException ( "operation not applicable to a timestamp" );	
	}
	
	/**
	 * @deprecated do not use - operation not applicable to a timestamp
	 */
	@Override
	public TimeStampInfo[] getTimeStamps( ) {
		throw new UnsupportedOperationException ( "operation not applicable to a timestamp" );		
	}
}
