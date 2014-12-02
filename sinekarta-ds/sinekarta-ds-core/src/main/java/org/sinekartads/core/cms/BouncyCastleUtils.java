package org.sinekartads.core.cms;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.sinekartads.core.CoreConfiguration;

public class BouncyCastleUtils {
	
	static final CoreConfiguration conf = CoreConfiguration.getInstance(); 
	
	
	
	// -----
	// --- X509CertificateHolder utility methods
	// -
	
	public static X509Certificate certHolderToRawX509Certificate(X509CertificateHolder certHolder) 
			throws CertificateException {
		
		// Return the X509Certificate stored into the given X509CertificateHolder
		JcaX509CertificateConverter converter = 
				new JcaX509CertificateConverter().setProvider ( conf.getProviderName() );
		return converter.getCertificate(certHolder);
	}
	
	public static SignerInformationVerifier buildVerifierFor ( 
			X509CertificateHolder certHolder ) 
					throws CertificateException {
		
		// Return a SignerInformationVerifier based on the given X509CertificateHolder
		SignerInformationVerifier verifier; 
		try {
			JcaSimpleSignerInfoVerifierBuilder builder = 
					new JcaSimpleSignerInfoVerifierBuilder().setProvider ( conf.getProviderName() ); 
			verifier = builder.build((X509CertificateHolder) certHolder);
		} catch(OperatorCreationException e) {
			throw new CertificateException(e);
		}
		return verifier;
	}
}
