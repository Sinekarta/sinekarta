package org.sinekartads.core.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.Store;
import org.sinekartads.asn1.ASN1Utils;
import org.sinekartads.core.CoreConfiguration;
import org.sinekartads.core.cms.BouncyCastleUtils;
import org.sinekartads.core.cms.NoFilterSelector;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.SecurityLevel.TimeStampVerifyResult;
import org.sinekartads.model.domain.TimeStampInfo;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.TsResponseInfo;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.EncryptionAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;


public class TimeStampService {

	static final CoreConfiguration conf = CoreConfiguration.getInstance();
	static final NoFilterSelector noFilterSelector =new NoFilterSelector();
	
	private static final JcaX509CertificateConverter certHolderConverter = 
			new JcaX509CertificateConverter().setProvider ( 
					CoreConfiguration.getInstance().getProviderName() );
	
	
	
	public TsResponseInfo processTsTequest ( 
			TsRequestInfo tsRequest ) 
					throws IOException, SignatureException {
		
		// Create the rawTimeStampRequest 
		TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
		reqGen.setCertReq(true);
    	TimeStampRequest rawTimeStampRequest = reqGen.generate (
    			tsRequest.getMessageImprintAlgorithm().getId(), 
    			tsRequest.getMessageImprintDigest(), 
    			tsRequest.getNounce() );
		
		String tsaUrl 							= tsRequest.getTsUrl();
		String tsaUsername 						= tsRequest.getTsUsername();
		String tsaPassword 						= tsRequest.getTsPassword();
		 
		// Send the tsRequest to tsaUrl as a POST  
		PostMethod method = new PostMethod (tsaUrl);
		method.setRequestEntity ( new ByteArrayRequestEntity(rawTimeStampRequest.getEncoded()) );
		method.setRequestHeader ( "Content-type", "application/timestamp-query" );
		if ( StringUtils.isNotBlank(tsaUsername) &&  StringUtils.isNotBlank(tsaPassword) ) {
			 String userPassword = tsaUsername + ":" + tsaPassword;
			 String basicAuth = "Basic " + new String(Base64.encodeBase64(userPassword.getBytes()));
			 method.setRequestHeader("Authorization", basicAuth);            
		}
		HttpClient httpClient = new HttpClient();
		httpClient.executeMethod ( method );	
		
		// Create the tsResponse, TSPException if the request failed (status!=0)
		InputStream in = method.getResponseBodyAsStream ( );
		byte[] encResponse = IOUtils.toByteArray(in);
		TimeStampResponse rawTimeStampResponse;
		try {
			rawTimeStampResponse = new TimeStampResponse ( new ByteArrayInputStream(encResponse) );
			rawTimeStampResponse.validate(rawTimeStampRequest);
		} catch(TSPException e) {
			throw new SignatureException(String.format("error during the timeStamp validation", e.getMessage()), e);
		}
		
		TimeStampToken rawTimeStampToken = rawTimeStampResponse.getTimeStampToken();
		if ( rawTimeStampResponse.getStatus() != 0 ) {            
			throw new SignatureException(String.format (
					"response error status %d - %s", rawTimeStampResponse.getStatus(), rawTimeStampResponse.getStatusString() ));
		}
		
		TimeStampInfo timeStamp = verify ( rawTimeStampToken );
		TsResponseInfo tsResponse = new TsResponseInfo ( rawTimeStampResponse.getEncoded(), timeStamp );
		
		// Return the tsResponse
		return tsResponse;
	}
	
	
	public TimeStampInfo verify ( 
			TimeStampToken rawTimeStampToken ) 
					throws IOException, SignatureException {
		
		if ( rawTimeStampToken == null )										return null;
		
		// Verify the TimeStampToken and evaluate the result
		TimeStampVerifyResult verifyResult = evalTimeStampSecurityLevel ( rawTimeStampToken, null );
		
		// Return the verified TsTokenInfo
		return toTimeStampInfo ( rawTimeStampToken, verifyResult );
	}
	
	public TimeStampInfo verify ( 
			TimeStampToken rawTimeStampToken, 
			byte[] tsContent ) 
					throws IOException,
					SignatureException {
		
		if ( rawTimeStampToken == null )										return null;
		
		// Verify the TimeStampToken and evaluate the result
		TimeStampVerifyResult verifyResult = evalTimeStampSecurityLevel ( rawTimeStampToken, tsContent );
		
		// Return the verified TsTokenInfo
		return toTimeStampInfo ( rawTimeStampToken, verifyResult );
	}
	
	public TimeStampInfo verify ( 
			TimeStampToken rawTimeStampToken, 
			InputStream dataIs ) 
					throws IOException, SignatureException {
		
		if ( rawTimeStampToken == null )										return null;
		
		return verify ( rawTimeStampToken, IOUtils.toByteArray(dataIs) );		
	}
	
	private TimeStampInfo toTimeStampInfo ( 
			TimeStampToken rawTimeStampToken, 
			TimeStampVerifyResult verifyResult ) 
					throws IOException, SignatureException {
		
		byte[] encTimeStampToken = rawTimeStampToken.getEncoded();
		
//		CertificateInfo signingCertificate = extractSigningCertificate ( rawTimeStampToken );
		X509Certificate signingCertificate = extractSigningCertificate ( rawTimeStampToken );
		
		SignerId signerId = rawTimeStampToken.getSID();
		SignerInformation signer = rawTimeStampToken.toCMSSignedData().getSignerInfos().get(signerId);
		byte[] digitalSignature = signer.getSignature();
		
		DigestAlgorithm digestAlgorithm = DigestAlgorithm.getInstance(signer.getDigestAlgOID());
		EncryptionAlgorithm cipherAlgorithm = EncryptionAlgorithm.getInstance(signer.getEncryptionAlgOID());
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.getInstance(digestAlgorithm, cipherAlgorithm);
	
		TimeStampTokenInfo rawTimeStampTokenInfo = rawTimeStampToken.getTimeStampInfo();
		DigestInfo messageImprintInfo = DigestInfo.getInstance (
				rawTimeStampTokenInfo.getMessageImprintAlgOID(), 
				rawTimeStampTokenInfo.getMessageImprintDigest() );
		
		return new TimeStampInfo ( 
				signatureAlgorithm,
				digestAlgorithm,
				messageImprintInfo,
				new X509Certificate[] {signingCertificate},
				digitalSignature,
				verifyResult,
				encTimeStampToken );
	}
	
	private X509Certificate extractSigningCertificate (
			TimeStampToken rawTimeStampToken ) 
					throws SignatureException {
		
		X509Certificate signingCertificate;
		Store certificateStore = rawTimeStampToken.getCertificates();

//		Collection<?> rawChain = (Collection<?>)certificateStore.getMatches(noFilterSelector);
//		
//		X509Certificate[] untrustedChain = 
//				TemplateUtils.Conversion.collectionToArray (
//						TemplateUtils.Cast.cast ( X509Certificate.class, rawChain ) );
		
		X509CertificateHolder certHolder = (X509CertificateHolder)
				certificateStore.getMatches( rawTimeStampToken.getSID() ).iterator().next();
		try {
			signingCertificate = certHolderConverter.getCertificate(certHolder);
		} catch ( CertificateException e ) {
			throw new SignatureException(e);
		}
		
		return signingCertificate;
	}
	
	private TimeStampVerifyResult evalTimeStampSecurityLevel (
			TimeStampToken rawTimeStampToken,
			byte[] tsContent ) 
					throws IOException, SignatureException {
		
		TimeStampVerifyResult verifyResult = null;
		try {
			X509CertificateHolder certHolder = (X509CertificateHolder) 
					rawTimeStampToken.getCertificates().getMatches(rawTimeStampToken.getSID()).iterator().next();
			
			SignerInformationVerifier verifier = 
					BouncyCastleUtils.buildVerifierFor ( certHolder );
			rawTimeStampToken.validate ( verifier );
			
			if ( tsContent != null ) {
				TimeStampTokenInfo timeStampInfo = rawTimeStampToken.getTimeStampInfo();
				DigestAlgorithm digestAlgorithm = DigestAlgorithm.getInstance(
						timeStampInfo.getMessageImprintAlgOID() );
				try {
					digestAlgorithm.validate(tsContent, timeStampInfo.getMessageImprintDigest());
				} catch(GeneralSecurityException e) {
					verifyResult = TimeStampVerifyResult.INVALID;
				} 
			}

			verifyResult = TimeStampVerifyResult.VALID;
		} catch (TSPException e) {
			verifyResult = TimeStampVerifyResult.INVALID;
		} catch (CertificateException e) {
			throw new SignatureException(e.getMessage(), e);
		}
		
		return verifyResult;
	}
	
}