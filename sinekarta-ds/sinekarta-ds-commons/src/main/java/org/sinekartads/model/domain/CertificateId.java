package org.sinekartads.model.domain;

import java.security.cert.X509Certificate;

public interface CertificateId {
	
	public boolean equals ( Object onj );
	
	public boolean match ( X509Certificate certificate ) ;
	
	@Override
	public String toString ( ) ;
	
}

