/*
 * Copyright (C) 2014 - 2015 Jenia Software.
 *
 * This file is part of Sinekarta-ds
 *
 * Sinekarta-ds is Open SOurce Software: you can redistribute it and/or modify
 * it under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sinekartads.utils;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class DNParser {

	// -----
	// --- Utility methods
	// -
	
	public static Set<String> getDns(String dn) {
		DNParser parser = new DNParser(dn);
		return parser.values.keySet();
	}
	
	public static String parse(String dn, String id) {
		return new DNParser(dn).get(id);
	}
	
	public static boolean equals(Principal principal0, Principal principal1) {
		return equals(principal0.getName(), principal1.getName());
	}
	
	public static boolean equals(String dn0, String dn1) {
		DNParser parser0 = new DNParser(dn0);
		DNParser parser1 = new DNParser(dn1);
		return parser0.equals(parser1);
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