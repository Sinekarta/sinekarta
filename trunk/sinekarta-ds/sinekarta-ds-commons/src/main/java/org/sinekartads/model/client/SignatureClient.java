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
package org.sinekartads.model.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.tools.DTOConverter;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.controller.Controller;
import org.sinekartads.util.controller.ControllerState;
import org.springframework.util.Assert;


/**
 * sinekarta digital signature generic client
 * this client will use String area returned by sinekarta alfresco action 
 * 
 * @author andrea.tessaro
 *
 */
public abstract class SignatureClient {
	
	public enum SignatureClientType {
		SMARTCARD,
		USB_TOKEN,
		SIGN_WS,
		KEYSTORE
	}

	public SignatureClient ( String sessionId, SignatureClientType type ) {
		this.sessionId = sessionId;
		this.type = type;
		listeners = new ArrayList<SignatureListener>();
		init ( );
	}

	protected final DTOConverter converter = DTOConverter.getInstance();
	protected final SignatureClientType type;
	protected final String sessionId;
	protected String[] certificateChain;
	private List<SignatureListener> listeners;

	public SignatureClientType getType() {
		return type;
	}
	
	
	
	
	
	// -----
	// --- State-safe controller
	// -
	
	// A subclass can introduce other init steps between 1 and 7
	public static final int ALIASES_AVAILABLE = 8;
	public static final int IDENTITY_SELECTED = 9;
	
	public class SignatureClientCtrl<Client extends SignatureClient> extends Controller {
		
		SignatureClientCtrl() {
			super(SignatureClient.this.getInitSteps());
		}
		
		public String[] getAliases() 
				throws IllegalStateException {
			
			super.verifyInitializationState(ALIASES_AVAILABLE);
			return TemplateUtils.Cast.cast(String.class, SignatureClient.this.aliases.toArray());
		}
		
		public String[] selectIdentity(String alias, String password) 
				throws IllegalStateException, 
						IllegalArgumentException {
			
//			super.verifyInitializationState(ALIASES_AVAILABLE);
			return SignatureClient.this.selectIdentity(alias, password);
		}
		
		public DocumentDTO[] sign(DocumentDTO[] documents) 
				throws IllegalStateException, 
						IllegalArgumentException,
						DigitalSignatureException {
			
//			super.verifyInitializationState(IDENTITY_SELECTED);
			if( ArrayUtils.isEmpty(documents) ) {
				throw new IllegalArgumentException("no documents to sign");
			}
			for(DocumentDTO document : documents) {
				SignatureClient.this.doSign(document);
			}
			return documents;
		}
		
		public DocumentDTO sign(DocumentDTO document)  
				throws IllegalStateException, 
						IllegalArgumentException,
						DigitalSignatureException {
	
//			super.verifyControllerState();
			if ( document == null ) {
				throw new IllegalArgumentException ( "no documents to sign" );
			}
			return SignatureClient.this.doSign(document);
		}
		
		public void close() 
				throws IllegalStateException, 
						DigitalSignatureException {
			
			super.verifyFinalizedState();
			if ( canRun() ) {
				SignatureClient.this.close();
			}
			super.closeController();
		}
		
		public void finalize() 
				throws IllegalStateException, 
						DigitalSignatureException {
			
			super.verifyFinalizedState();
			if( !isFinalized() ) {
				SignatureClient.this.finalize();
			}
			super.finalizeController();
		}
		
		public Enumeration<SignatureListener> getListeners() {
			return Collections.enumeration ( listeners );
		}
		
		public void addListener(SignatureListener listener) {
			listeners.add(listener);
		}
		
		public void removeListener(SignatureListener listener) {
			listeners.remove(listener);
		}
		
		ControllerState state() {
			return state;
		}
		
		protected synchronized void setInitStep(int step) {
			super.goToInitStep(step);
		}
	}
	
	public abstract <Type extends SignatureClientCtrl<?>> Type getController();
	
	protected int getInitSteps() {
		return 2;
	}
	
	
	
	// -----
	// --- Protocol implementation
	// -
	
	private Collection<String> aliases;
	
	protected void init ( ) {
		
	}

	protected abstract void loadAliases();
	
	protected abstract String[] selectIdentity(String alias, String password);
	
	private DocumentDTO doSign(DocumentDTO document) throws DigitalSignatureException {
//		for(SignatureListener listener : listeners) listener.startSigning(document);

		SignatureDTO[] signatures = document.getSignatures();
		Assert.isTrue( ArrayUtils.isNotEmpty(signatures) );
		
		SignatureAlgorithm signatureAlgorithm;
		DigestInfo digestInfo;
		byte[] digitalSignature;
		final int LAST = signatures.length - 1;
		
		SignatureDTO signature = signatures[LAST];
		signatureAlgorithm = signature.signAlgorithmFromString();
		digestInfo = converter.toDigestInfo(signature.getDigest());
		digitalSignature = doSign ( signatureAlgorithm, digestInfo );
		signature.digitalSignatureToHex(digitalSignature);
		
//		for(SignatureListener listener : listeners) listener.endSigning(document);
		return document;
	}
	
	protected abstract byte[] doSign(SignatureAlgorithm sigAlgorithm, DigestInfo digestInfo) throws DigitalSignatureException;
	
	protected void close() throws DigitalSignatureException {
		
	}
	
	protected void finalize() throws DigitalSignatureException {
		
	}
}
