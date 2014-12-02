package org.sinekartads.model.domain;

import java.security.cert.CertificateException;

import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;

public class CMSSignatureInfo extends SignatureInfo	<	SignCategory,
														SignDisposition.CMS,
														VerifyResult,
														CMSSignatureInfo 			> {
	
	private static final long serialVersionUID = 3603950906060085786L;

	public CMSSignatureInfo (
			SignatureAlgorithm signAlgorithm,
			DigestAlgorithm digestAlgorithm ) 
					throws CertificateException {
		
		super ( SignCategory.CMS, SignDisposition.CMS.EMBEDDED, signAlgorithm, digestAlgorithm );
	}
}