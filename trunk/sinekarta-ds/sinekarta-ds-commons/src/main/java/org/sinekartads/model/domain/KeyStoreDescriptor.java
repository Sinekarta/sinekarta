package org.sinekartads.model.domain;



public class KeyStoreDescriptor {
	public KeyStoreDescriptor (				
			String keystoreName,
			String keystoreRef, 
			KeyStoreType keystoreType, 		
			String keystorePassword ) {
		this.keyStoreName 	= keystoreName;
		this.reference 	= keystoreRef;
		this.type 		= keystoreType;
		this.password 	= keystorePassword;
	}
	
	private final String keyStoreName;
	private final String reference;
	private final KeyStoreType type;
	private final String password;
	private String[] aliases;

	public String getName() {
		return keyStoreName;
	}

	public String getReference() {
		return reference;
	}

	public KeyStoreType getType() {
		return type;
	}

	public String getPin() {
		return password;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}
}