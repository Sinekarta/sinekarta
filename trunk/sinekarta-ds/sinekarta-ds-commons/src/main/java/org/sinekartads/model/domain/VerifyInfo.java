/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sinekartads.model.domain;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.Transitions.VerifiedSignature;

/**
 *
 * @author adeprato
 */
public class VerifyInfo implements Serializable {
	
	private static final long serialVersionUID = 5758578762901434576L;

	private final List<VerifiedSignature<?,?,VerifyResult,?>> signatures = new ArrayList<VerifiedSignature<?,?,VerifyResult,?>>();
	private VerifyResult minSecurityLevel;
	private boolean extracted;
	
	public void addSignature (
			VerifiedSignature<?,?,VerifyResult,?> signature) 
					throws IllegalStateException {
		if ( !signature.isVerified() ) {
			throw new IllegalStateException(String.format ( 
					"signature not verifier: %s", signature ));
		}
		signatures.add(signature);
	}

	public List<VerifiedSignature<?,?,VerifyResult,?>> getSignatures() {
		return Collections.unmodifiableList ( signatures );
	}

	public VerifyResult getMinSecurityLevel() {
		return minSecurityLevel;
	}

	public void setMinSecurityLevel(VerifyResult minSecurityLevel) {
		this.minSecurityLevel = minSecurityLevel;
	}

	public boolean isExtracted() {
		return extracted;
	}

	public void setExtracted(boolean extracted) {
		this.extracted = extracted;
	}
	
}

