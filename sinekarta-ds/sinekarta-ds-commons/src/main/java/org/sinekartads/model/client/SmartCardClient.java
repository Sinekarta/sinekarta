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

import org.sinekartads.util.controller.Controller;

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
		
		public SmartCardStatus getSmarCardStatus ( ) {
			return SmartCardClient.this.smartCardStatus;
		}
		
//		public String[] getDriverList() {
//			super.verifyInitializationState ( DRIVERS_AVAILABLE );
//			return SmartCardClient.this.drivers; 
//		}
//		
//		public void selectDriver ( String driver ) {
//			super.verifyInitializationState ( DRIVERS_AVAILABLE );
//			SmartCardClient.this.selectDriver ( driver ); 
//			super.goToInitStep ( DRIVER_SELECTED );
//		}
		
		public void login ( String smartCardPin ) {
//			super.verifyInitializationState ( DRIVER_SELECTED );
			SmartCardClient.this.login ( smartCardPin ); 
			super.goToInitStep ( ALIASES_AVAILABLE );
		}
		
		/**
		 * @deprecated do not use - this operation is intended to be performed through the applet
		 */
		@Override
		public String[] selectIdentity(String alias, String password) {
			throw new UnsupportedOperationException ( "this operation is intended to be performed through the applet" );
		}
		
		public X509Certificate[] getUntrustedChain() {
			return untrustedChain;
		}
	}
	
	SmartCardClientCtrl controller = new SmartCardClientCtrl();
	
	@SuppressWarnings("unchecked")
	public SmartCardClientCtrl getController() {
		return controller;
	}
	

	
	
	// -----
	// --- Communication protocol
	// -

//	private String smartCardPin;
//	private String[] allowedSignAlgorithms;
	protected String[] aliases;
//	private String userAlias;
	protected X509Certificate[] untrustedChain;
	protected SmartCardStatus smartCardStatus;
	
	protected abstract void receiveSmartCardStatus ( SmartCardStatus smartCardStatus ) ;
	
	protected abstract void receiveAliases ( String[] aliases ) ;

	protected abstract void receiveUntrustedChain(X509Certificate[] untrustedChain) ;
	
	protected abstract void login ( String smartCardPin );
	
	public class JSController extends Controller {
		
		public void receiveSmartCardStatus ( SmartCardStatus smartCardStatus ) {
			SmartCardClient.this.smartCardStatus = smartCardStatus;
		}
		
		public void receiveAliases ( String[] aliases ) {
			SmartCardClient.this.aliases = aliases;
		}
		
		public void receiveChain ( X509Certificate[] rawX509CertificateChain ) {
			
			SmartCardClient.this.untrustedChain = rawX509CertificateChain;
		}
	}


}
