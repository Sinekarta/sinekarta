package org.sinekartads.core.cms;

import java.security.cert.X509Certificate;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.sinekartads.core.CoreConfiguration;
import org.sinekartads.util.EntityTransformer;

public class X509CertificateHolderTransformer extends EntityTransformer<X509CertificateHolder, X509Certificate> {
	
	static final JcaX509CertificateConverter converter = 
			new JcaX509CertificateConverter().setProvider ( 
					CoreConfiguration.getInstance().getProviderName() );
	
	@Override
	protected X509Certificate doTransform(X509CertificateHolder certHolder) {
		try {
			return converter.getCertificate(certHolder);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
