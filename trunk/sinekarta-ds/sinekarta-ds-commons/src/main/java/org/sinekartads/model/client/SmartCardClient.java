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

import java.security.cert.X509Certificate;

public abstract class SmartCardClient extends SignatureClient {
	
	public SmartCardClient ( String sessionId ) {
		
		super ( sessionId, SignatureClientType.SMARTCARD );
	}
	

	
	// -----
	// --- State-safe smartCard client controller
	// -
	
	public static enum SmartCardStatus {
		NOT_DETECTED,
		MISSING_DRIVER,
		LOGGED_OFF,
		READY
	}
	
//	private static final int DRIVERS_AVAILABLE = 2;
//	private static final int DRIVER_SELECTED = 3;
	
	public class SmartCardClientCtrl extends SignatureClientCtrl<SmartCardClient> {
		
		public void setDigitalSignature ( byte[] digitalSignature ) {
			SmartCardClient.this.digitalSignature = digitalSignature;
		}
	}
	
	SmartCardClientCtrl controller = new SmartCardClientCtrl();
	protected byte[] digitalSignature;
	
	@SuppressWarnings("unchecked")
	public SmartCardClientCtrl getController() {
		return controller;
	}
	
	
	
	// -----
	// --- Communication protocol
	// -

	protected String[] aliases;
	protected X509Certificate[] untrustedChain;
	protected SmartCardStatus smartCardStatus;

}
