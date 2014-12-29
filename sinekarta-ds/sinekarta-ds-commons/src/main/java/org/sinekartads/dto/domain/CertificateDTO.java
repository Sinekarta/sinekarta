/*
 * Copyright (C) 2010 - 2012 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.sinekartads.dto.domain;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.formats.DateDTOProperty;
import org.sinekartads.model.domain.CertificateId;
import org.sinekartads.model.domain.CertificateIdFactory;
import org.sinekartads.util.HexUtils;

public class CertificateDTO extends BaseDTO {

	private static final long serialVersionUID = 6127683611141959159L;


	
	
	private String subjectAlias;
	
	private String issuerAlias;
	
	private String emailAddress;
	
    private String organizationUnitName;
    
    private String organizationName;
    
    private String localityName;
    
    private String stateOrProvinceName;
    
    private String countryName;
    @DateDTOProperty
    private String notBefore;
    @DateDTOProperty
    private String notAfter;
    
    private String qcStatements;
        
    private String hexCertificate;
    
    private String sourceName;
    
    private String status;
    
    private CertificateDTO[] chain;
    
    
    
    
    // -----
    // --- EntityDTO transmission protocol
    // -
	
	@Override
    public boolean isEmpty ( ) {
    	return StringUtils.isBlank ( hexCertificate );
    }
	
    
	
    // -----
	// --- Entity properties
	// -
	
	public String getSubjectAlias() {
		return subjectAlias;
	}
	
	public void setSubjectAlias(String subjectAlias) {
		this.subjectAlias = subjectAlias;
	}

	public String getIssuerAlias() {
		return issuerAlias;
	}

	public void setIssuerAlias(String issuer) {
		this.issuerAlias = issuer;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getOrganizationUnitName() {
		return organizationUnitName;
	}

	public void setOrganizationUnitName(String unit) {
		this.organizationUnitName = unit;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organization) {
		this.organizationName = organization;
	}

	public String getLocalityName() {
		return localityName;
	}

	public void setLocalityName(String localityName) {
		this.localityName = localityName;
	}

	public String getStateOrProvinceName() {
		return stateOrProvinceName;
	}

	public void setStateOrProvinceName(String stateOrProvinceName) {
		this.stateOrProvinceName = stateOrProvinceName;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String signingCountry) {
		this.countryName = signingCountry;
	}
	
	public String getQcStatements() {
		return qcStatements;
	}
	
	public void setQcStatements(String qcStatements) {
		this.qcStatements = qcStatements;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	
	public CertificateDTO[] getChain() {
		return chain;
	}
	
	public void setChain(CertificateDTO[] chain) {
		this.chain = chain;
	}
	
    
	
    // -----
    // --- Formatted properties
    // -
	
	public CertificateId identityFromHex ( ) {
		return CertificateIdFactory.getCertificateId ( rawX509CertificateFromHex() );
	}
    
    public void certificateToHex(X509Certificate certificate) {
    	try {
			if(certificate != null) {
				hexCertificate = HexUtils.encodeHex(certificate.getEncoded());
			} else {
				hexCertificate = null;
			}
    	} catch(CertificateEncodingException e) {
    		// it should be never thrown
    		throw new RuntimeException(e);
    	}
	}
	
	public X509Certificate rawX509CertificateFromHex() {
		
		if ( hexCertificate != null )												return null;
		
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate rawX509Certificate = (X509Certificate) cf.generateCertificate (
					new ByteArrayInputStream(HexUtils.decodeHex(hexCertificate)) );
			return rawX509Certificate;
		} catch (CertificateException e) {
			// never thrown, using the TsTokenDTO protocol the certificate hex has to be correct
			throw new RuntimeException(e);
		}
	}
	
	public Date notBeforeFromString() {
		Date notBefore = null;
		if ( StringUtils.isNotBlank(this.notBefore) ) {
			try {
				notBefore = timeFormat.parse ( this.notBefore );
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return notBefore;
	}
	
	public void notBeforeToString(Date notBefore) {
		if ( notBefore == null) {
			this.notBefore = null;
		} else {
			this.notBefore = timeFormat.format(notBefore);
		}
	}
	    
	public Date notAfterFromString() {
		Date notAfter = null;
		if ( StringUtils.isNotBlank(this.notAfter) ) {
			try {
				notAfter = timeFormat.parse ( this.notAfter );
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
		return notAfter;
	}
	
	public void notAfterToString(Date notAfter) {
		if ( notAfter == null) {
			this.notAfter = null;
		} else {
			this.notAfter = timeFormat.format(notAfter);
		}
	}
	
//	public CertificateInfo[] chainFromDTO() {
//		
//		if ( this.chain == null ) 												return null;
//		
//		CertificateInfo[] chain = new CertificateInfo[this.chain.length];
//		for ( int i=0; i<chain.length; i++ ) {
//			chain[i] = DTOConverter.getInstance().toCertificateInfo ( this.chain[i] ); 
//		}
//		return chain;
//	}
//	
//	public void chainFromDTO(CertificateInfo[] chain) {
//		
//		if ( chain == null ) {
//			this.chain = null;
//		} else {
//			this.chain = new CertificateDTO[chain.length];
//			for ( int i=0; i<chain.length; i++ ) {
//				this.chain[i] = DTOConverter.getInstance().fromCertificateInfo ( chain[i], false ) ; 
//			}
//		}
//	}
	
	
	

	// -----
	// --- Direct access to formatted properties
	// -
	
	/**
	 * @deprecated use notBeforeToString(Date) instead
	 */ 
	public void setNotBefore(String notBefore) {
		this.notBefore = notBefore;
	}
	public String getNotBefore() {
		return notBefore;
	}

	/**
	 * @deprecated use notAfterToString(Date) instead
	 */ 
	public void setNotAfter(String notAfter) {
		this.notAfter = notAfter;
	}
	public String getNotAfter() {
		return notAfter;
	}

	/**
	 * @deprecated use certificateToHex(X509Certificate) instead
	 */ 
	public void setHexCertificate(String hexCertificate) {
		this.hexCertificate = hexCertificate;
	}
	public String getHexCertificate() {
		return hexCertificate;
	}
	
	/**
	 * 
	 * @deprecated use statusToString(CertificateStatus) instead
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
}
