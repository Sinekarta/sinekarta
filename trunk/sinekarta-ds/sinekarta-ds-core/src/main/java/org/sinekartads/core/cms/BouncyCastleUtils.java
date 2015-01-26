package org.sinekartads.core.cms;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.sinekartads.core.CoreConfiguration;

/**
 * Convenience suite of BouncyCastle utility methods.
 * @author adeprato
 */
public class BouncyCastleUtils {
	
	// -----
	// --- X509CertificateHolder utility methods
	// -
	
	/**
	 * Return the X509Certificate stored into the given X509CertificateHolder
	 * @param certHolder
	 * @return
	 * @throws CertificateException
	 */
	public static X509Certificate certHolderToRawX509Certificate(X509CertificateHolder certHolder) 
			throws CertificateException {
		
		JcaX509CertificateConverter converter = 
				new JcaX509CertificateConverter().setProvider ( "BC" );
		return converter.getCertificate(certHolder);
	}
	
	/**
	 * Return a SignerInformationVerifier based on the given X509CertificateHolder
	 * @param certHolder
	 * @return
	 * @throws CertificateException
	 */
	public static SignerInformationVerifier buildVerifierFor ( 
			X509CertificateHolder certHolder ) 
					throws CertificateException {
		
		SignerInformationVerifier verifier; 
		try {
			JcaSimpleSignerInfoVerifierBuilder builder = 
					new JcaSimpleSignerInfoVerifierBuilder().setProvider ( "BC" ); 
			verifier = builder.build((X509CertificateHolder) certHolder);
		} catch(OperatorCreationException e) {
			throw new CertificateException(e);
		}
		return verifier;
	}
}
