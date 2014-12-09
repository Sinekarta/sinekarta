package org.sinekartads.dto.domain;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.model.domain.SecurityLevel.KeyRingSupport;


public class KeyStoreDTO extends BaseDTO {

	private static final long serialVersionUID = -6472731635903990962L;
	
	private String name;
	private String support;
	private String reference;
	private String type;
	private String provider;
	private String password;
	private String[] aliases = new String[0];
		
	@Override
	public boolean isEmpty ( ) {
		return StringUtils.isBlank ( name );
	}
	

	
	// -----
	// --- Entity properties
	// -
	
	public String getName() {
		return name;
	} 
	
	public void setName(String name) {
		this.name = name;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}
	
	public KeyRingSupport supportFromString() {
		return KeyRingSupport.valueOf ( support );
	}
	
	public void supportToString(KeyRingSupport support) {
		this.support = support.name ( );
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public KeyStoreType typeFromString() {
		return KeyStoreType.valueOf ( type );
	}
	
	public void typeToString(KeyStoreType type) {
		this.type = type.name ( );
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getPin() {
		return password;
	}

	public void setPin(String password) {
		this.password = password;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

//	public NodeDTO getNode() {
//		return node;
//	}
//
//	public void setNode(NodeDTO node) {
//		this.node = node;
//	}

}
