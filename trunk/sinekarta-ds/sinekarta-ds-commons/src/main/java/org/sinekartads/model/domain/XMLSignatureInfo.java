package org.sinekartads.model.domain;

import java.security.cert.CertificateException;

import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;

public class XMLSignatureInfo
		extends SignatureInfo	<	SignatureType.SignCategory,
									SignDisposition.XML,
									VerifyResult,
									XMLSignatureInfo 			> {
	
	private static final long serialVersionUID = -2223647671247595158L;

	public XMLSignatureInfo (
			SignatureAlgorithm signAlgorithm,
			DigestAlgorithm digestAlgorithm ) 
					throws CertificateException {
		
		super ( SignatureType.SignCategory.XML, SignDisposition.XML.ENVELOPING, signAlgorithm, digestAlgorithm );
	}
	
	public XMLSignatureInfo (
			SignatureAlgorithm signAlgorithm,
			DigestAlgorithm digestAlgorithm,
			String signatureId ) 
					throws CertificateException {
		
		super ( SignatureType.SignCategory.XML, SignDisposition.XML.ENVELOPING, signAlgorithm, digestAlgorithm );
		this.signatureId = signatureId;
	}
	
	public String getSignatureId() {
		return signatureId;
	}

	public void setSignatureId(String signatureId) {
		this.signatureId = signatureId;
	}

	private String signatureId;
	
}
