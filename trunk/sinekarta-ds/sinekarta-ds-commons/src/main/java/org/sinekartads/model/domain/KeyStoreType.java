package org.sinekartads.model.domain;

import org.apache.commons.lang3.StringUtils;

public enum KeyStoreType {

	JKS			( "jks", "jks",			"SUN"),
	PKCS12_DEF	( "p12", "PKCS12-DEF", 	"BC"), 
	PKCS12 		( "p12", "PKCS12", 		"SunJSSE");
	
	private final String extension;
	private final String type;
	private final String provider;
	
	KeyStoreType(String extension, String type, String provider) {
		this.extension = extension;
		this.type = type;
		this.provider = provider;
	}
	
	public static KeyStoreType getInstance(String type) {
		if(StringUtils.isBlank(type)) {
			return null;
		}
		for(KeyStoreType item : KeyStoreType.values()) {
			if(StringUtils.equalsIgnoreCase(item.type, type)) {
				return item;
			}
		}
		for(KeyStoreType item : KeyStoreType.values()) {
			if(StringUtils.equalsIgnoreCase(item.name(), type)) {
				return item;
			}
		}
		throw new IllegalArgumentException("invalid keystore type: " + type);
	}
	
	public String getExtension() {
		return extension;
	}
	
	public String getType() {
		return type;
	}
	
	public String getProvider() {
		return provider;
	}
	
	@Override
	public String toString() {
		return String.format("%s: %s -> [%s,%s]", name(), extension, type, provider);
	}
	
}