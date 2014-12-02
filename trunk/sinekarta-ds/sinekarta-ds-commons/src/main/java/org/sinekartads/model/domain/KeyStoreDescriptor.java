package org.sinekartads.model.domain;

import org.alfresco.service.cmr.repository.NodeRef;


public class KeyStoreDescriptor {
	public KeyStoreDescriptor (				
			String keystoreName,
			NodeRef keystoreRef, 
			KeyStoreType keystoreType, 		
			String keystorePassword ) {
		this.keyStoreName 	= keystoreName;
		this.reference 	= keystoreRef;
		this.type 		= keystoreType;
		this.password 	= keystorePassword;
	}
	
	private final String keyStoreName;
	private final NodeRef reference;
	private final KeyStoreType type;
	private final String password;
	private String[] aliases;

	public String getName() {
		return keyStoreName;
	}

	public NodeRef getReference() {
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