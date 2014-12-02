package org.sinekartads.model.domain;

import java.io.Serializable;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.sinekartads.util.DNParser;

public class CertificateIdFactory {
	
	static class CertificateIdImpl implements CertificateId, Serializable {
		
		private static final long serialVersionUID = -7112291352606951153L;
		
		Principal subjectPrincipal;
		boolean[] subjectUniqueID;
		byte[] signature;
		

		public CertificateIdImpl(boolean[] subjectUniqueID) {
			this.subjectUniqueID = subjectUniqueID;
		}
		
		public CertificateIdImpl(byte[] signature) {
			this.signature = signature;
		}
		
		public CertificateIdImpl(Principal subjectPrincipal) {
			this.subjectPrincipal = subjectPrincipal;
		}
		
		public CertificateIdImpl(String subjectName) {
			subjectPrincipal = DNParser.evalPrincipal(subjectName) ;
		}
		
		public CertificateIdImpl(X509Certificate certificate) {
			if ( certificate.getSubjectUniqueID() != null ) {
				subjectUniqueID = certificate.getSubjectUniqueID();
			} else if ( certificate.getSignature() != null ) {
				signature = certificate.getSignature();
			} else if ( certificate.getSubjectX500Principal() != null ) {
				subjectPrincipal = certificate.getSubjectX500Principal();
			} else {
				throw new IllegalArgumentException(String.format ( 
						"unable to extract an identity for certificate \n%s", certificate ) );
			}
		}
		
		@Override
		public CertificateId clone() {
			CertificateId clone;
			try {
				clone = (CertificateId)super.clone();
			} catch (CloneNotSupportedException e) {
				// never thrown, Selector extends Cloneable
				throw new RuntimeException(e);
			}
			return clone;
		}

		@Override
		public boolean match(X509Certificate certificate) {
			if ( Arrays.equals(signature, certificate.getSignature()) ) 				return true;
			if ( Arrays.equals(subjectUniqueID, certificate.getSubjectUniqueID()) ) 	return true;
			if ( subjectPrincipal.equals(certificate.getSubjectX500Principal()) ) 		return true;
			return false;
		}

		@Override
		public boolean equals(Object identity) {
			CertificateIdImpl certIdImpl = (CertificateIdImpl)identity;
			if ( !Arrays.equals(signature, certIdImpl.signature) ) 						return true;
			if ( !Arrays.equals(subjectUniqueID, certIdImpl.subjectUniqueID) ) 			return true;
			if ( !subjectPrincipal.equals(certIdImpl.subjectPrincipal) ) 				return true;
			return false;
		}
		
	}
	
	public static CertificateId getCertificateId ( 
			Object identityDescriptor ) 
					throws IllegalArgumentException {
		
		// Generate a proper selectorInstance from the given identityDescriptor
		if ( identityDescriptor instanceof X509Certificate ) { 
			return new CertificateIdImpl ( (X509Certificate)identityDescriptor );
		}
		if ( identityDescriptor instanceof boolean[] ) { 
			return new CertificateIdImpl ( (boolean[])identityDescriptor );
		}
		if ( identityDescriptor instanceof byte[] ) { 
			return new CertificateIdImpl ( (byte[])identityDescriptor );
		}
		if ( identityDescriptor instanceof Principal ) { 
			return new CertificateIdImpl ( (Principal)identityDescriptor );
		}		
		if ( identityDescriptor instanceof String ) { 
			return new CertificateIdImpl ( (String)identityDescriptor );
		}
		// Throws an IllegalArgumentException if the identityDescriptor is not recognized
		throw new IllegalArgumentException(String.format ( 
				"invalid identity descriptor: %s \n" +
				"expected descriptors: \n" +
				" - X509Certificate certificate \n" + 
				" - X509CertificateHolder certHolder \n" +
				" - boolean[] uniqueSubjectId \n" +
				" - byte[] signature \n" +
				" - Principal subjectPrincipal \n" +
				" - String subjectName \n" +
				" - String subjectCN",
				identityDescriptor ));
	}
}
