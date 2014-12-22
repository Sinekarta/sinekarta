package org.sinekartads.model.domain;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

import org.sinekartads.model.domain.SecurityLevel.TimeStampVerifyResult;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;

public class Transitions <	ST extends SignatureType<ST>,
											SD extends SignDisposition<SD>,
											SV extends SecurityLevel<SV>,
										 	SI extends SignatureInfo<ST, SD, SV, SI> > {
	
	// -----
	// --- Base signature protocol
	// -
	
	public interface BaseSignature <	ST extends SignatureType<ST>,
										SD extends SignDisposition<SD>,
										SV extends SecurityLevel<SV>,
									 	SI extends SignatureInfo<ST, SD, SV, SI> > {
		
		public String toString();
		
		
		// Mandatory properties
		
		public ST getSignType() ;
		
		public SignatureAlgorithm getSignAlgorithm() ;
		
		public DigestAlgorithm getDigestAlgorithm() ;
		
		public SD getDisposition() ;

		public SignatureStatus getStatus();
		
		public boolean isFinalized();
		
		public boolean isVerified();
		
		
		// Optional properties
		
		public boolean isCounterSignature() ;
		
		public String getReason() ;
		
		public Date getSigningTime() ;
		
		public String getLocation() ;
		
		public Map<String, Object> getOtherAttributes() ;
		
		public Object getOtherAttribute(String name) ;
		
		
		// Status-depending properties
		
		public X509Certificate[] getRawX509Certificates() throws UnsupportedOperationException ;
		
//		public CertificateInfo getCertificate() throws UnsupportedOperationException ;
		
		public DigestInfo getDigest() throws UnsupportedOperationException ;
		
		public byte[] getDigitalSignature() throws UnsupportedOperationException ;
		
		public TimeStampInfo[] getTimeStamps() throws UnsupportedOperationException ;
		
		public SV getVerifyResult() throws UnsupportedOperationException ;
		
	}
	
	
	
	// -----
	// --- Stable signature protocol - read-only
	// -
	
	// RawSignature -> EmptySignature | UntrustedSignature - before to be applied | verified
	
	public interface RawSignature 	<	ST extends SignatureType<ST>,
										SD extends SignDisposition<SD>,
										SV extends SecurityLevel<SV>,
									 	SI extends SignatureInfo<ST, SD, SV, SI> > 
	
																extends BaseSignature < ST, SD, SV, SI > {
		
		public EmptySignature < ST, SD, SV, SI >			toEmptySignature();
		
	}
	
	
	
	// -----
	// --- Transitory signature protocol - editable
	// -
	
	public interface TransitorySignature	<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
												SI extends SignatureInfo<ST, SD, SV, SI> >
	
																	extends BaseSignature < ST, SD, SV, SI > {
		
		public void setCounterSignature(boolean counterSignature) ;
		
		public void setReason(String reason) ;
		
		public void setLocation(String location) ;
		
		public void setSigningTime(Date signingTime) ;
		
		public void setOtherAttribute(String name, Object value) ;
		
		public void clearOtherAttributes() ;
	}
	
	
	
	// -----
	// --- Sign Process Transitions
	// -
	
	// SignTransition - marker for signatures being applied to a document 
	
	public interface SignTransition			<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
												SI extends SignatureInfo<ST, SD, SV, SI> >

																		extends TransitorySignature < ST, SD, SV, SI > {
		public InvalidSignature < ST, SD, SV, SI > 			invalidateSignature ( ) ;
	}
	
	
	// EmptySignature -> UntrustedChainSignature | TrustedChainSignature
	
	public interface EmptySignature			<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
												SI extends SignatureInfo<ST, SD, SV, SI> >
	
																		extends SignTransition < ST, SD, SV, SI > {
		
		public void setTsRequest ( TsRequestInfo tsRequest ) throws UnsupportedOperationException ;
		
		public ChainSignature < ST, SD, SV, SI >	toChainSignature ( X509Certificate[] untrustedChain ) ;
	}
	
	
	// ChainSignature - marker for the signatures that received a certificate chain (trusted or not)
	
	public interface ChainSignature			<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
												SI extends SignatureInfo<ST, SD, SV, SI> >
									
																		extends SignTransition < ST, SD, SV, SI > {
		
		public TsRequestInfo getTsRequest ( ) throws UnsupportedOperationException ;
		
		public DigestSignature < ST, SD, SV, SI > 			toDigestSignature ( DigestInfo digest ) ;
	}
	
	
	
	// DigestSignature -> SignedSignature
	
	public interface DigestSignature		<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
												SI extends SignatureInfo<ST, SD, SV, SI> >
											
																		extends SignTransition < ST, SD, SV, SI > { 
		
		public TsRequestInfo getTsRequest ( ) throws UnsupportedOperationException ;

		public SignedSignature < ST, SD, SV, SI > 			toSignedSignature ( byte[] digitalSignature ) ;
	}
	
	
	// SignedSignature -> MarkedSignature | FinalizedSignature
	
	public interface SignedSignature		<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
												SI extends SignatureInfo<ST, SD, SV, SI> >
											
																		extends SignTransition < ST, SD, SV, SI > { 

		public TsRequestInfo getTsRequest ( ) throws UnsupportedOperationException ;
		
		public MarkedSignature < ST, SD, SV, SI > 			toMarkedSignature ( ) ;
		
		public FinalizedSignature < ST, SD, SV, SI > 		finalizeSignature ( ) ;
	}

	
	// MarkedSignature -> FinalizedSignature
	
	public interface MarkedSignature		<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
											 	SI extends SignatureInfo<ST, SD, SV, SI> >
		
																		extends SignTransition <ST, SD, SV, SI> {

		public void appendTimeStamp ( DisposedTimeStamp tsToken ) throws UnsupportedOperationException;
		
		public void appendTimeStamp ( VerifiedTimeStamp tsToken, 
									  SignDisposition.TimeStamp tsDisposition )
											  throws UnsupportedOperationException;
		
		public FinalizedSignature < ST, SD, SV, SI > 		finalizeSignature ( ) ;
	}
	
	
	// FinalizedSignature - any property is locked, can be verified
	
	public interface FinalizedSignature		<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
											 	SI extends SignatureInfo<ST, SD, SV, SI> > 
	
																		extends BaseSignature <ST, SD, SV, SI> {

		public VerifiedSignature < ST, SD, SV, SI >			toVerifiedSignature ( SV verifyResult );
	}
	
	
	// VerifiedSignature - applied | verified signature - locked
	
	public interface VerifiedSignature		<	ST extends SignatureType<ST>,
												SD extends SignDisposition<SD>,
												SV extends SecurityLevel<SV>,
											 	SI extends SignatureInfo<ST, SD, SV, SI> >
	
																		extends FinalizedSignature <ST, SD, SV, SI> {
		
	}
	
	public interface InvalidSignature		<	ST extends SignatureType<ST>,
		SD extends SignDisposition<SD>,
		SV extends SecurityLevel<SV>,
	 	SI extends SignatureInfo<ST, SD, SV, SI> >
	
								extends VerifiedSignature <ST, SD, SV, SI> {

	}
	
	
	
	// -----
	// --- Stable TimeStamp protocol
	// -
	
	public interface BaseTimeStamp 
			extends BaseSignature < SignatureType.TimeStamp,
									SignDisposition.TimeStamp,
									TimeStampVerifyResult,
									TimeStampInfo					> {
		
		public byte[] getEncTimeStampToken ( ) ;
		
		public DigestInfo getMessageInprintInfo ( )  ;
		
		public String getTsaName ( ) ;
		
		public UntrustedTimeStamp startValidation ( );
	}
	

	
	// -----
	// --- TimeStamp Verify Process Transitions
	// -
	
	public interface TimeStampVerifyTransition 
			extends BaseTimeStamp {
		
	}
	
	public interface RawTimeStamp
			extends TimeStampVerifyTransition {
	
		public UntrustedTimeStamp toUntrustedTimeStamp ( TimeStampVerifyResult verifyResult );
	}
	
	public interface UntrustedTimeStamp 
			extends TimeStampVerifyTransition {
		
		public void setSigningTime(Date signingTime) ;
		
		public void setReason(String reason) ;
		
		public void setLocation(String location) ;
		
		public VerifiedTimeStamp toVerifiedTimeStamp ( TimeStampVerifyResult verifyResult );
	}
	
	public interface VerifiedTimeStamp 
			extends TimeStampVerifyTransition, 
					VerifiedSignature	<	SignatureType.TimeStamp,
											SignDisposition.TimeStamp,
											TimeStampVerifyResult,
											TimeStampInfo 							> {
		
		public DisposedTimeStamp toDisposedTimeStamp ( SignDisposition.TimeStamp disposition );
	}
	
	
	public interface DisposedTimeStamp 
			extends VerifiedTimeStamp, 
					FinalizedSignature	<	SignatureType.TimeStamp,
											SignDisposition.TimeStamp,
											TimeStampVerifyResult,
											TimeStampInfo 							> {
		
	}
}
