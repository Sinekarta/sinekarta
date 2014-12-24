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
package org.sinekartads.applet;

import java.applet.Applet;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.exception.InvalidCertificateException;
import org.sinekartads.exception.InvalidPKCS11DriverException;
import org.sinekartads.exception.InvalidPinException;
import org.sinekartads.exception.InvalidSmartCardException;
import org.sinekartads.exception.PKCS11DriverNotFoundException;
import org.sinekartads.exception.PinLockedException;
import org.sinekartads.exception.SmartCardAccessException;
import org.sinekartads.exception.SmartCardReaderNotFoundException;
import org.sinekartads.smartcard.DigitalSignatureClient;
import org.sinekartads.utils.JSONUtils;
import org.sinekartads.utils.X509Utils;

public class SignApplet extends Applet {

	private static final long serialVersionUID = -2886113966359858032L;
	private static final Logger tracer = Logger.getLogger(SignApplet.class);
	
	private transient DigitalSignatureClient digitalSignatureClient;
	
	@Override
	public void init ( ) {
		tracer.info("Initializing the signing applet.");
		digitalSignatureClient = new DigitalSignatureClient();
	}
	
	public String selectDriver ( String driver ) {
		driver = StringUtils.trim(driver);
		tracer.info("selectDriver");
		tracer.info(String.format("driver: %s", driver));
		
		AppletResponseDTO resp = new AppletResponseDTO ( "selectDriver" );
		try {
			digitalSignatureClient.setDriver(driver);
			digitalSignatureClient.open();
			digitalSignatureClient.verifyDriver();
			resp.setResult(digitalSignatureClient.getDriver());
		} catch (SmartCardReaderNotFoundException e) {
			tracer.error("Impossibile trovare il lettore di SmartCard", e);
			resp.addActionError("Impossibile trovare il lettore di SmartCard - "+e.getMessage(), e);
		} catch (InvalidSmartCardException e) {
			tracer.error(e.getMessage(), e);
			resp.addActionError("SmartCard non riconosciuta - "+e.getMessage(), e);
		} catch (PKCS11DriverNotFoundException e) {
			tracer.error(e.getMessage(), e);
			resp.addFieldError("scDriver", "Driver SmartCard non trovato - "+e.getMessage());
		} catch (InvalidPKCS11DriverException e) {
			tracer.error(e.getMessage(), e);
			resp.addFieldError("scDriver", "Driver SmartCard non riconosciuto - "+e.getMessage());
		} catch (SmartCardAccessException e) {
			tracer.error(e.getMessage(), e);
			resp.addActionError("Error SmartCard - "+e.getMessage(), e);
		} catch (Throwable e) {
			tracer.error(e.getMessage(), e);
			resp.addActionError("Exception SmartCard - "+e.getMessage(), e);
		} finally {
			try {
				digitalSignatureClient.close();
			} catch (SmartCardAccessException e) {
				// nothing to do..
			}
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON ( resp )));
		return JSONUtils.serializeJSON ( resp );
	}
	
	public String login ( String pin ) {
		pin = StringUtils.trim(pin);
		tracer.info("login");
		tracer.info(String.format("pin:    %s", pin));
		
		AppletResponseDTO resp = new AppletResponseDTO ( "login" );
		try {
			digitalSignatureClient.setPin(pin);
			digitalSignatureClient.open();
			String[] aliases = digitalSignatureClient.certificateList ();
			if ( ArrayUtils.isNotEmpty(aliases) ) {
				String aliasesJSON = JSONUtils.serializeJSON ( aliases );
				resp.setResult ( aliasesJSON );
			}
		} catch (InvalidPinException e) {
			tracer.error("pin non riconosciuto", e);
			resp.addFieldError("scPin", "pin non riconosciuto");
		} catch (PinLockedException e) {
			tracer.error("pin bloccato", e);
			resp.addFieldError("scPin", "pin bloccato");
		} catch (SmartCardReaderNotFoundException e) {
			tracer.error("lettore SmartCard non trovato", e);
			resp.addFieldError("scPin", "lettore SmartCard non trovato");
		} catch (Throwable e) {
			tracer.error("login fallito a causa di un errore interno", e);
			resp.addFieldError("scPin", "login fallito a causa di un errore interno");
		} finally {
			try {
				digitalSignatureClient.close();
			} catch (SmartCardAccessException e) {
				// nothing to do..
			}
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON(resp)));
		return JSONUtils.serializeJSON ( resp );
	}
	
	public String selectCertificate ( String alias ) {
		alias = StringUtils.trim(alias);
		tracer.info("selectCertificate");
		tracer.info(String.format("alias:  %s", alias));
		
		AppletResponseDTO resp = new AppletResponseDTO ( "selectCertificate" );
		try {
			digitalSignatureClient.setAlias(alias);
			digitalSignatureClient.open();
			X509Certificate signingCertificate = digitalSignatureClient.selectCertificate();
			if ( signingCertificate != null ) {
				resp.setResult ( X509Utils.rawX509CertificateToHex(signingCertificate) );
			}
		} catch(CertificateException e) {
			tracer.error("impossibile reperire il certificato per l'identità %s", e);
			processError ( resp, "impossibile reperire il certificato per l'identità %s", e );
		} catch(InvalidCertificateException e) {
			tracer.error("impossibile reperire il certificato per l'identità %s", e);
			processError ( resp, "impossibile reperire il certificato per l'identità %s", e );
		} catch (Throwable e) {
			tracer.error("scelta certificato fallita a causa di un errore interno", e);
			processError(resp, "scelta certificato fallita a causa di un errore interno", e);
		} finally {
			try {
				digitalSignatureClient.close();
			} catch (SmartCardAccessException e) {
				// nothing to do..
			}
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON( resp )));
		return JSONUtils.serializeJSON(resp);
	}
	
	public String signDigest ( String hexDigest ) {
		hexDigest = StringUtils.trim(hexDigest);
		tracer.info("signDigest");
		
		AppletResponseDTO resp = new AppletResponseDTO ( "signDigest" );
		try {
			digitalSignatureClient.open();
			resp.setResult (digitalSignatureClient.sign(hexDigest));
		} catch (Exception e) {
			tracer.error("errore rilevato nell'applicazione della firma digitale da SmartCard", e);
			processError ( resp, "errore rilevato nell'applicazione della firma digitale da SmartCard", e);
		} finally {
			try {
				digitalSignatureClient.close();
			} catch (SmartCardAccessException e) {
				// nothing to do..
			}
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON ( resp )));
		return JSONUtils.serializeJSON(resp);
	}
	
	public void destroy() {
		tracer.info("signing applet destroyed");
	}
		
	private void processError ( AppletResponseDTO resp, String errorMessage, Throwable errorCause ) {
		if ( StringUtils.isBlank(errorMessage) ) {
			errorMessage = errorCause.getClass().getName();
		}
		resp.addActionError(errorMessage, errorCause);
		tracer.error(errorMessage, errorCause);
	}
}