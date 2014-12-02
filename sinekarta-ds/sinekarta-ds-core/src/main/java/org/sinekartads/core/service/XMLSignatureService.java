package org.sinekartads.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.TimeStampInfo;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.VerifyInfo;
import org.sinekartads.model.domain.XMLSignatureInfo;

public class XMLSignatureService 
		extends AbstractSignatureService  <	SignCategory,
											SignDisposition.XML,
											XMLSignatureInfo > {

	// -----
	// --- Pre-Sign phase
	// -
	
	public DigestSignature < SignCategory, 
							 SignDisposition.XML, 
							 VerifyResult, 		
							 XMLSignatureInfo > 		doPreSign (	ChainSignature < SignCategory, 
													 	 						 	 SignDisposition.XML, 
													 								 VerifyResult, 		
													 								 XMLSignatureInfo 		 >	chainSignature,
																	InputStream 								contentIs 		)
													 							 
													 										 throws SignatureException, IOException {
		
		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

	// -----
	// --- Post-Sign phase
	// -
	
	@Override
	public FinalizedSignature < SignCategory, 
							 	SignDisposition.XML, 
							 	VerifyResult, 		
							 	XMLSignatureInfo > 		doPostSign (	SignedSignature	  <	SignCategory, 
													 	 						 		SignDisposition.XML, 
													 	 						 	 	VerifyResult, 		
													 	 						 	 	XMLSignatureInfo 		 >	signedSignature,
																	InputStream 									contentIs,
																	OutputStream 									detachedSignOs,
																	OutputStream 									embeddedSignOs,
																	OutputStream 									tsResultOs,
																	OutputStream 									markedSignOs 	)
																			
																			throws SignatureException, IOException 			{
		
		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

	@Override
	public TimeStampInfo doApplyTimeStamp (
			TsRequestInfo tsRequest,
			InputStream contentIs,
			InputStream detachedSignIs,
			InputStream embeddedSignIs,
			OutputStream timestampOs,
			OutputStream markedSignOs ) 
					throws SignatureException,
							CertificateException,
							IOException {

		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

	@Override
	public VerifyInfo doVerify ( 
			InputStream contentIs,
			InputStream tsResponseIs,
			InputStream envelopeIs,
			VerifyResult requiredSecurityLevel,
			OutputStream contentOs ) 
					throws 	CertificateException,
							SignatureException,
							IOException {

		// TODO body method not implemented yet
		throw new UnsupportedOperationException ( "body method not implemented yet" );
	}

}
