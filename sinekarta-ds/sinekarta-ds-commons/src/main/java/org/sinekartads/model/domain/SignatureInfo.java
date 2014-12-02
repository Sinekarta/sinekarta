package org.sinekartads.model.domain;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition.TimeStamp;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.DisposedTimeStamp;
import org.sinekartads.model.domain.Transitions.EmptySignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.InvalidSignature;
import org.sinekartads.model.domain.Transitions.MarkedSignature;
import org.sinekartads.model.domain.Transitions.RawSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.Transitions.VerifiedSignature;
import org.sinekartads.model.domain.Transitions.VerifiedTimeStamp;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.TextUtils;
import org.springframework.util.Assert;

public class SignatureInfo<ST extends SignatureType<ST>,
						   SD extends SignDisposition<SD>,
						   SV extends SecurityLevel<SV>,
						   SI extends SignatureInfo<ST, SD, SV, SI> >

		implements Serializable,
				   RawSignature				< ST, SD, SV, SI >,
				   EmptySignature			< ST, SD, SV, SI >, 
				   ChainSignature			< ST, SD, SV, SI >,
				   DigestSignature			< ST, SD, SV, SI >,
				   SignedSignature			< ST, SD, SV, SI >,
				   MarkedSignature			< ST, SD, SV, SI >,
				   VerifiedSignature		< ST, SD, SV, SI >, 
				   InvalidSignature			< ST, SD, SV, SI > {
	
	private static final long serialVersionUID = -6887834919909647669L;
	
	public SignatureInfo (
			ST type,
			SD disposition,
			SignatureAlgorithm signAlgorithm,
			DigestAlgorithm digestAlgorithm ) {
		
		this.type = type;
		this.disposition = disposition;
		this.signAlgorithm	= signAlgorithm;
		this.digestAlgorithm	= digestAlgorithm;
	}
	
	public String toString() {
		return String.format("[\n\talgorithm: %s (%s)\n\tdigitalSignature: %s\n]", 
				signAlgorithm.getId(), signAlgorithm.getName(), HexUtils.encodeHex(digitalSignature));
	}
	
	
	
	// -----
	// --- RawSignature - Mandatory properties
	// -
	
	final ST type;
	final SignatureAlgorithm signAlgorithm;
	final DigestAlgorithm digestAlgorithm;
	SD disposition;

	@Override
	public ST getSignType() {
		return type;
	}
	
	@Override
	public SignatureAlgorithm getSignAlgorithm() {
		return signAlgorithm;
	}
	
	@Override
	public DigestAlgorithm getDigestAlgorithm() {
		return digestAlgorithm;
	}
	
	@Override
	public SD getDisposition() {
		return disposition;
	}
	
	@Override
	public SignatureStatus getStatus ( ) {
		
		if ( verifyResult != null )							return SignatureStatus.Stable.VERIFIED;
		if ( finalized )									return SignatureStatus.Stable.FINALIZED;
		if ( ArrayUtils.isNotEmpty(timeStamps) )			return SignatureStatus.SignProcess.MARKED;
		if ( digitalSignature != null )						return SignatureStatus.SignProcess.SIGNED;
		if ( digest != null )								return SignatureStatus.SignProcess.DIGEST;
		if ( rawX509Certificates != null )					return SignatureStatus.SignProcess.CHAIN;
		return SignatureStatus.SignProcess.EMPTY;
	}
	
	protected void assertStatus ( SignatureStatus status ) {
		Assert.isTrue( status == getStatus() );
	}
	
	protected void assertStatusAmong ( SignatureStatus ... statuses ) {
		boolean validStatus = false;
		for ( SignatureStatus status : statuses ) {
			validStatus |= status == getStatus();
		}
		Assert.isTrue( validStatus );
	}
	
	protected void assertAllowedAccess ( SignatureStatus.SignProcess signProcessStatus ) {
		SignatureStatus status = getStatus();
		if ( status instanceof SignatureStatus.SignProcess ) {
			SignatureStatus[] statuses = SignatureStatus.SignProcess.values();
			assertStatusAmong ( ArrayUtils.subarray(statuses, signProcessStatus.ordinal(), statuses.length) );
		} else {
			assertStatusAmong ( SignatureStatus.Stable.FINALIZED, SignatureStatus.Stable.VERIFIED );
		}
	}
	
	
	@Override
	public boolean isFinalized() {
		return finalized;
	}
	
	@Override
	public boolean isVerified() {
		return getStatus() == SignatureStatus.Stable.VERIFIED;
	}
	
	
	
	// -----
	// --- BaseSignature - Optional properties
	// -
	
	private boolean counterSignature = false;
	private String reason = "Signed by SineKarta";
	private String location = null;
	private Date signingTime = null;
	private Map<String, Object> otherAttributes = new HashMap<String, Object>();
	
	@Override
	public boolean isCounterSignature() {
		return counterSignature;
	}
	
	@Override
	public String getReason() {
		return reason;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setSigningTime(Date signingTime) {
		this.signingTime = signingTime;
	}
	
	@Override
	public Map<String, Object> getOtherAttributes() {
		return Collections.unmodifiableMap(otherAttributes);
	}
	
	@Override
	public Object getOtherAttribute(String name) {
		return otherAttributes.get(name);
	}
	
	
	
	// -----
	// --- Status-depending properties
	// -
	
	/*
	 * These values could be not available in a transitory signature status because still unset.
	 * To avoid this, the following assertions prevent from any inconsistent access:
	 *  - SignProcess: write-only until a minimum signProcessStatus has been reached
	 *  - VerifyProcess: write-only since the verifyResult update, the signature switches then to a FINALIZED status
	 * A signature in a FINALIZED status allows the read-only access to every properties except tsRequest, which is
	 * binded to SignProcessTransition instances only.
	 */

	protected X509Certificate[] rawX509Certificates;
//	protected CertificateInfo certificate;
	protected DigestInfo digest;
	protected byte[] digitalSignature;
	protected TsRequestInfo tsRequest;
	protected DisposedTimeStamp[] timeStamps = new DisposedTimeStamp[0];
	protected boolean finalized;
	protected SV verifyResult;

	@Override
	public X509Certificate[] getRawX509Certificates() {
		return rawX509Certificates;
	}
	
//	@Override
//	public CertificateInfo getCertificate() {
////		assertAllowedAccess ( SignatureStatus.SignProcess.TRUSTED_CHAIN );
//		return certificate;
//	}
	
	@Override
	public DigestInfo getDigest() {
		assertAllowedAccess ( SignatureStatus.SignProcess.DIGEST );
		return digest;
	}
	
	@Override
	public byte[] getDigitalSignature() {
		assertAllowedAccess ( SignatureStatus.SignProcess.SIGNED );
		return digitalSignature;
	}
	
	@Override
	public TsRequestInfo getTsRequest() {
		return tsRequest;
	}
	
	@Override
	public TimeStampInfo[] getTimeStamps() {
		assertAllowedAccess ( SignatureStatus.SignProcess.MARKED );
		return TemplateUtils.Cast.cast(TimeStampInfo.class, timeStamps);
	}
	
	@Override
	public SV getVerifyResult() {
		assertStatusAmong ( SignatureStatus.Stable.values() );
		return verifyResult;
	}
	
	
	
	// -----
	// --- TransitorySignature
	// -
	
	@Override
	public void setCounterSignature(boolean counterSignature) {
		this.counterSignature = counterSignature;
	}
	
	@Override
	public Date getSigningTime() {
		return signingTime;
	}
	
	@Override
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	@Override
	public void setLocation(String location) {
		this.location = location;
	}
	
	@Override
	public void setOtherAttribute(String name, Object value) {
		otherAttributes.put(name, value);
	}

	@Override
	public void clearOtherAttributes() {
		otherAttributes.clear();
	}

	
	
	// -----
	// --- RawSignature -> EmptySignature | UntrustedSignature
	// -
	
	@Override
	public EmptySignature < ST, SD, SV, SI >			toEmptySignature() {
		
//		assertStatus ( SignatureStatus.Stable.RAW );
		
		SignatureInfo<ST, SD, SV, SI> emptyInstance = TemplateUtils.Serialization.clone ( this );
		return emptyInstance;
	}
	
	
	
	// -----
	// --- EmptySignature -> ChainSignature
	// -
	
	@Override
	public void setTsRequest ( TsRequestInfo tsRequest ) throws UnsupportedOperationException {

		assertStatus ( SignatureStatus.SignProcess.EMPTY );
		Assert.isTrue( type.isTimeStamped() );
		
		this.tsRequest = tsRequest;
	}
	
	
	public ChainSignature < ST, SD, SV, SI >	toChainSignature ( X509Certificate[] certificateChain ) {
		SignatureInfo<ST, SD, SV, SI> chainInstance = TemplateUtils.Serialization.clone ( this );
//		chainInstance.certificate = new CertificateInfo(untrustedChain);
		chainInstance.rawX509Certificates = certificateChain;
		return chainInstance;
	}
	
	
	// -----
	// --- ChainSignature -> DigestSignature
	// -

	@Override
	public DigestSignature<ST, SD, SV, SI> toDigestSignature ( DigestInfo digest ) {
		
		assertStatus ( SignatureStatus.SignProcess.CHAIN );
		Assert.notNull( digest );
		
		SignatureInfo<ST, SD, SV, SI> digestInstance = TemplateUtils.Serialization.clone ( this );
		digestInstance.digest = digest;
		return digestInstance;
	}

	
	
	// -----
	// --- DigestSignature -> SignedSignature
	// -
	
	@Override
	public SignedSignature<ST, SD, SV, SI> toSignedSignature ( byte[] digitalSignature ) {
		
		assertStatus ( SignatureStatus.SignProcess.DIGEST );
		Assert.isTrue( ArrayUtils.isNotEmpty(digitalSignature) );
		
		SignatureInfo<ST, SD, SV, SI> signedInstance = TemplateUtils.Serialization.clone ( this );
		signedInstance.digitalSignature = digitalSignature;
		return signedInstance;
	}
	
	
	
	// -----
	// --- SignedSignature -> MarkedSignature | FinalizedSignature
	// -
	
	@Override
	public MarkedSignature<ST, SD, SV, SI> toMarkedSignature ( ) {
		
		assertStatus ( SignatureStatus.SignProcess.SIGNED );
		Assert.isTrue ( type.isTimeStamped() );
		
		MarkedSignature<ST, SD, SV, SI> signedInstance = TemplateUtils.Serialization.clone ( this );
		return (MarkedSignature<ST, SD, SV, SI>)signedInstance;
	}

	
	
	// -----
	// --- MarkedSignature -> FinalizedSignature
	// -
	
	@Override
	public void appendTimeStamp ( DisposedTimeStamp disposedTimeStamp) {
		
		assertStatus ( SignatureStatus.SignProcess.MARKED );
		
		appendTimeStamp ( disposedTimeStamp, (SignDisposition.TimeStamp)disposedTimeStamp.getDisposition() );
	}
	
	@Override
	public void appendTimeStamp ( VerifiedTimeStamp verifiedTimeStamp,
								  SignDisposition.TimeStamp disposition ) {

		assertStatus ( SignatureStatus.SignProcess.MARKED );
		Assert.notNull( verifiedTimeStamp );
		Assert.notNull( disposition );
		
		SignatureType.SignCategory signatureCategory = type.getCategory();
		TimeStamp[] supportedTimeStampDispositions = signatureCategory.supportedTimeStampDispositions(); 
		if ( !ArrayUtils.contains(supportedTimeStampDispositions, disposition) ) {
			throw new UnsupportedOperationException(String.format (
					"timestamp disposition not supported by the %s signature type \n" +
							"  -    found: %s \n  - expected: %s", 
					type, disposition, TextUtils.fromArray(supportedTimeStampDispositions) ));
		}
		
		DisposedTimeStamp disposedTimeStamp;
		if ( verifiedTimeStamp instanceof DisposedTimeStamp ) {
			disposedTimeStamp = (DisposedTimeStamp) verifiedTimeStamp;
		} else {
			disposedTimeStamp = verifiedTimeStamp.toDisposedTimeStamp(disposition);
		}
		ArrayUtils.add(timeStamps, disposedTimeStamp);
	}


	
	// -----
	// --- Signature finalization - SignedSignature and MarkedSignature only
	// -
	
	@Override
	public FinalizedSignature<ST, SD, SV, SI> finalizeSignature ( ) {
		
		assertStatusAmong ( SignatureStatus.SignProcess.SIGNED, SignatureStatus.SignProcess.MARKED );
		if ( getStatus() == SignatureStatus.SignProcess.MARKED ) {
			Assert.notNull( timeStamps );
		}
		
		SignatureInfo<ST, SD, SV, SI> finalizedInstance = TemplateUtils.Serialization.clone ( this );
		finalizedInstance.finalized = true;
		return finalizedInstance;
	}
	
	
	
	// -----
	// --- UntrustedSignature -> VerifiedSignature (FINALIZED)
	// -
	
	@Override
	public VerifiedSignature<ST, SD, SV, SI> toVerifiedSignature ( SV verifyResult ) {
		
		Assert.notNull( verifyResult );
		
		SignatureInfo<ST, SD, SV, SI> verifiedInstance = TemplateUtils.Serialization.clone ( this );
		verifiedInstance.verifyResult = verifyResult;
		return verifiedInstance;
	}

	
	
	// -----
	// --- Any SignTransition instance -> InvalidSignature (FINALIZED) 
	// -

	@Override
	@SuppressWarnings("unchecked")
	public InvalidSignature<ST, SD, SV, SI> invalidateSignature () {
		
		SignatureInfo<ST, SD, SV, SI> invalidatedInstance = TemplateUtils.Serialization.clone ( this );
		invalidatedInstance.verifyResult = (SV)VerifyResult.INVALID;
		return invalidatedInstance;
	}
}