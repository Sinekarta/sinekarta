package org.sinekartads.util;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.model.oid.DomainName;

public class DNParser {

	// -----
	// --- Utility methods
	// -
	
	public static Map<String, String> parse(Principal principal) {
		DNParser parser = new DNParser(principal);
		return parser.values;
	}
	
	public static String parse(Principal principal, DomainName domainName) {
		DNParser parser = new DNParser(principal);
		return parser.get(domainName.getId());
	}
	
	public static String parse(String dn, String id) {
		DNParser parser = new DNParser(dn);
		return parser.get(id);
	}
	
	public static String parseAlias(Principal principal) {
		return parse ( principal, DomainName.commonName );
	}
	
	public static String parseName(Principal principal) {
		return parseName ( principal.getName() );
	}
	
	public static String parseName(String dn) {
		DNParser parser = new DNParser(dn);
		
		// commonName
		String commonName = parser.get(DomainName.commonName);
		if ( StringUtils.isNotBlank(commonName) ) { 								
			return commonName;
		}
		
		// no commonName is available, attempt to build an identity anyways
		String givenName 	= parser.get(DomainName.givenName);
		String surname 		= parser.get(DomainName.surname);
		String initials 	= parser.get(DomainName.initials);
		String organization	= parser.get(DomainName.organizationName);
		String unit			= parser.get(DomainName.organizationUnitName);
		String country		= parser.get(DomainName.countryName);

		// subject name 	= fullName 
		if ( StringUtils.isNotBlank(givenName) && StringUtils.isNotBlank(surname) ) {
			return givenName + " " + surname;			
		} 
		
		// subject name = [ surname | initials ] , organization
		if ( StringUtils.isNotBlank(organization) ) {
			String name = "";
			if ( StringUtils.isNotBlank(initials) ) {
				name = initials; 
			} else if ( StringUtils.isNotBlank(surname) ) {
				name = surname; 
			}
			if( StringUtils.isNotBlank(name) ) {
				return name + ", " + organization;
			}
		}
		
		// organization		= organizationName - organizationUnitName
		//             		= organizationName, country
		//					= organizationName
		if ( StringUtils.isNotBlank(organization) ) {
			if ( StringUtils.isNoneBlank(unit) ) {
				return organization + " - " + unit;
			}
			if ( StringUtils.isNoneBlank(country) ) {
				return organization + ", " + country;
			}
			return organization;
		}
		
		// unable to parse a name, return the full dn
		return dn;
	}

	public static Set<String> getDns(String dn) {
		DNParser parser = new DNParser(dn);
		return parser.values.keySet();
	}
	
	public static boolean equals(Principal principal0, Principal principal1) {
		return equals(principal0.getName(), principal1.getName());
	}
	
	public static boolean equals(String dn0, String dn1) {
		DNParser parser0 = new DNParser(dn0);
		DNParser parser1 = new DNParser(dn1);
		return parser0.equals(parser1);
	}
	
	public static boolean sameName(Principal principal0, Principal principal1) {
		return equals(principal0.getName(), principal1.getName());
	}
	
	public static boolean sameName(String dn0, String dn1) {
		String name0 = parseName(dn0);
		String name1 = parseName(dn1);
		return name0.equalsIgnoreCase(name1);
	}
	
	public static Principal evalPrincipal (String name) {
		DNParser dnParser = new DNParser( name );
		if ( dnParser.isEmpty() ) {
			name = "CN=" + name;
		} 
		return new javax.security.auth.x500.X500Principal(name);
	}
	
	
	
	// -----
	// --- DN parsing implementation
	// -

	private static final String REGEX_DN = "([A-Za-z ]*)=([A-Za-z0-9: /]*)[,]?";
	private HashMap<String, String> values = new HashMap<String, String>();

	public DNParser(String dn) {
		Matcher m = Pattern.compile(REGEX_DN).matcher(dn);
		while (m.find()) {
			values.put(m.group(1).trim(), m.group(2));
		}
	}
	
	public DNParser(Principal principal) {
		Matcher m = Pattern.compile(REGEX_DN).matcher(principal.getName());
		while (m.find()) {
			values.put(m.group(1).trim(), m.group(2));
		}
	}
	
	
	
	// -----
	// --- Inner DNs querying
	// -
	
	public Collection<String> keys() {
		return values.keySet();
	}

	public String get(DomainName dn) {
		return values.get(dn.getName());
	}
	
	public String get(String key) {
		return values.get(key);
	}
	
	public boolean isEmpty() {
		return values.isEmpty();
	}
	
	public int size() {
		return values.size();
	}

	
	
	// -----
	// --- DN equivalence
	// -
	
	public boolean equals(DNParser parser) {
		if ( parser == null )				return false; 
		if ( size() != parser.size() )		return false;
		
		for(String key : keys()) {
			if ( StringUtils.equalsIgnoreCase(get(key), parser.get(key)) ) {
				return false;
			}
		}
		
		return true;
	}
	
}