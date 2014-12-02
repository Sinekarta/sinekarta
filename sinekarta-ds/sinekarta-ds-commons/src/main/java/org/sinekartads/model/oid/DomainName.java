package org.sinekartads.model.oid;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.util.TextUtils;

// ----------------------------------------------------------------------------
// --- TODO OidWrapperUtils
// ----------------------------------------------------------------------------
//
// Features:
//  - match(OidWrapper, OidWrapper) 
//  - equals(OidWrapper, OidWrapper)
//  - convert(OidWrapper, Class<OidWrapper>) -> equivalent instance of new class
//  - getInstance(Object, Class<OidWrapper>) 
//
// ----------------------------------------------------------------------------
public enum DomainName implements OidWrapper {
	
    commonName				("commonName", 				SinekartaDsObjectIdentifiers.dn_commonName),    
	surname					("surname", 				SinekartaDsObjectIdentifiers.dn_surname),
	serialNumber			("serialNumber", 			SinekartaDsObjectIdentifiers.dn_serialNumber),
	countryName			("countryName", 			SinekartaDsObjectIdentifiers.dn_countryName),
	localityName			("localityName", 			SinekartaDsObjectIdentifiers.dn_localityName),
	stateOrProvinceName	("stateOrProvinceName", 	SinekartaDsObjectIdentifiers.dn_stateOrProvinceName),
	streetAddress			("streetAddress", 			SinekartaDsObjectIdentifiers.dn_streetAddress),
	organizationName		("organizationName", 		SinekartaDsObjectIdentifiers.dn_organizationName),
	organizationUnitName	("organizationUnitName", 	SinekartaDsObjectIdentifiers.dn_organizationUnitName),
	title					("title", 					SinekartaDsObjectIdentifiers.dn_title),
	givenName				("givenName", 				SinekartaDsObjectIdentifiers.dn_givenName),
    initials				("initials", 				SinekartaDsObjectIdentifiers.dn_initials),
    generation				("generation", 				SinekartaDsObjectIdentifiers.dn_generation),
    uniqueId				("uniqueId", 				SinekartaDsObjectIdentifiers.dn_uniqueID),
    emailAddress			("emailAddress", 			SinekartaDsObjectIdentifiers.dn_emailAddress),
    qcStatements			("qcStatements", 			SinekartaDsObjectIdentifiers.dn_qcStatements),
	;
	
	DomainName(String name, String oid) {
		this.name = name;
		this.oid = oid;
	}

	private final String name;
	private final String oid;
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return oid;
	}
	
	@Override
	public String[] getIds() {
		return new String[] {oid};
	}
	
	@Override
	public boolean matchesWith(Object oidDescriptor) {
		if ( oidDescriptor instanceof String ) {
			String strDescriptor = (String)oidDescriptor;
			for ( DomainName dn : values()) {
				if ( StringUtils.equals(strDescriptor, dn.getName()) ) 			return true;
				if ( StringUtils.equals(strDescriptor, dn.getId()) ) 			return true;
			}
		} else if ( oidDescriptor instanceof OidWrapper ) {
			String[] oids = ((OidWrapper)oidDescriptor).getIds();
			for ( DomainName dn : values()) {
				for ( String id : oids ) {
					if ( StringUtils.equals(id, dn.getId()) ) 					return true;
				}
			}
		} else {
			throw new IllegalArgumentException(String.format ( 
					"invalid oidDescriptor %s \nsupported descriptor types: %s", 
					oidDescriptor, 
					TextUtils.formatClasses ( new Class[] {
							String.class, 
							OidWrapper.class})) );
		}
		return false;
	}

	@Override
	public boolean equals(OidWrapper oidWrapper) {
		if ( this.getIds().length != oidWrapper.getIds().length )				return false;
		for ( String id : this.getIds() ) {
			if ( !oidWrapper.matchesWith(id) )									return false; 
		}
		for ( String id : oidWrapper.getIds() ) {
			if ( !this.matchesWith(id) )										return false; 
		}
		return true;
	}
	
	
	
	// -----
	// --- Instance retrieving
	// -
	
	public DomainName getInstance ( Object oidDescriptor ) {
		if ( oidDescriptor instanceof OidWrapper ) {
			OidWrapper oidWrapper = (OidWrapper)oidDescriptor;
			for ( DomainName dn : values()) {
				if ( dn.matchesWith(oidWrapper) ) {
					return dn;
				}
			}
		} else {
			String id;
			if ( oidDescriptor instanceof String ) {
				try {
					return fromName ((String)oidDescriptor);
				} catch(IllegalArgumentException iae) {
					// unable to recognize the oidWrapper by name, try by id
					id = (String)oidDescriptor;
				}
			} else {
				throw new IllegalArgumentException(String.format ( 
						"invalid oidDescriptor %s \nsupported descriptor types: %s", 
						oidDescriptor, 
						TextUtils.formatClasses ( new Class[] {
								String.class, 
								OidWrapper.class})) );
			}
			return fromId(id);
		}
		
		throw new IllegalArgumentException(String.format ("invalid oidDescriptor - %s", oidDescriptor) );
	}
	
	public DomainName fromId(String id) {
		for ( DomainName dn : values()) {
			if ( StringUtils.equals(id, dn.getId()) ) {
				return dn;
			}
		}
		throw new IllegalArgumentException(String.format ("invalid id - %s", id) );
	}
	
	public DomainName fromName(String name) {
		for ( DomainName dn : values()) {
			if ( StringUtils.equals(name, dn.getName()) ) {
				return dn;
			}
		}
		throw new IllegalArgumentException(String.format ("invalid name - %s", name) );
	}

}
