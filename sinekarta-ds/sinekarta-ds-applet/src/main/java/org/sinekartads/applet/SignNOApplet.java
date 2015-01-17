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

public class SignNOApplet {

	private static final String FUNCTION_SIGN_DIGEST = "signDigest";
	private static final String FUNCTION_SELECT_CERTIFICATE = "selectCertificate";
	private static final String FUNCTION_LOGIN = "login";
	private static final String FUNCTION_SELECT_DRIVER = "selectDriver";
	
	private static final Logger tracer = Logger.getLogger(SignNOApplet.class);
	
	private transient DigitalSignatureClient digitalSignatureClient;
	
	public AppletResponseDTO selectDriver ( AppletRequestDTO request ) {
		
		String driver = StringUtils.trim(request.getDriver());
		if (digitalSignatureClient==null) {
			digitalSignatureClient = new DigitalSignatureClient();
		} else {
			try {
				digitalSignatureClient.close();
			} catch (SmartCardAccessException e) {
				// nothing to do..
			}
		}

		tracer.info(FUNCTION_SELECT_DRIVER);
		tracer.info(String.format("driver: %s", driver));
		
		AppletResponseDTO resp = new AppletResponseDTO ( FUNCTION_SELECT_DRIVER );
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
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON ( resp )));
		return resp;
	}
	
	public AppletResponseDTO login ( AppletRequestDTO request ) {

		String pin = StringUtils.trim(request.getPin());
		tracer.info(FUNCTION_LOGIN);
		tracer.info(String.format("pin:    %s", pin));

		if (digitalSignatureClient.isEmptyDriver()) {
			AppletResponseDTO respSelectDriver = selectDriver(request);
			if (respSelectDriver.checkError()) {
				return respSelectDriver;
			}
		}

		AppletResponseDTO resp = new AppletResponseDTO ( FUNCTION_LOGIN );
		try {
			if ( digitalSignatureClient!=null && StringUtils.isNotBlank(request.getDriver()) ) {
				digitalSignatureClient.setPin(pin);
				String[] aliases = digitalSignatureClient.certificateList ();
				if ( ArrayUtils.isNotEmpty(aliases) ) {
					String aliasesJSON = JSONUtils.serializeJSON ( aliases );
					resp.setResult ( aliasesJSON );
				}
			} else {
				tracer.error("Specificare un driver smartcard");
				resp.addFieldError("scDriver", "campo obbligatorio");
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
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON(resp)));
		return resp;
	}
	
	public AppletResponseDTO selectCertificate ( AppletRequestDTO request ) {

		String alias = StringUtils.trim(request.getAlias());
		tracer.info(FUNCTION_SELECT_CERTIFICATE);
		tracer.info(String.format("alias:  %s", alias));
		
		if (digitalSignatureClient.isEmptyDriver()) {
			AppletResponseDTO respSelectDriver = selectDriver(request);
			if (respSelectDriver.checkError()) {
				return respSelectDriver;
			}
		}

		if (digitalSignatureClient.isEmptyPin()) {
			AppletResponseDTO respLogin = login(request);
			if (respLogin.checkError()) {
				return respLogin;
			}
		}

		AppletResponseDTO resp = new AppletResponseDTO ( FUNCTION_SELECT_CERTIFICATE );
		try {
			digitalSignatureClient.setAlias(alias);
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
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON( resp )));
		return resp;
	}
	
	public AppletResponseDTO signDigest ( AppletRequestDTO request ) {

		String hexDigest = StringUtils.trim(request.getHexDigest());
		tracer.info(FUNCTION_SIGN_DIGEST);
		
		if (digitalSignatureClient.isEmptyDriver()) {
			AppletResponseDTO respSelectDriver = selectDriver(request);
			if (respSelectDriver.checkError()) {
				return respSelectDriver;
			}
		}

		if (digitalSignatureClient.isEmptyPin()) {
			AppletResponseDTO respLogin = login(request);
			if (respLogin.checkError()) {
				return respLogin;
			}
		}

		if (digitalSignatureClient.isEmptyAlias()) {
			AppletResponseDTO respSelectCertificate = selectCertificate(request);
			if (respSelectCertificate.checkError()) {
				return respSelectCertificate;
			}
		}

		AppletResponseDTO resp = new AppletResponseDTO ( FUNCTION_SIGN_DIGEST );
		try {
			resp.setResult (digitalSignatureClient.sign(hexDigest));
		} catch (Exception e) {
			tracer.error("errore rilevato nell'applicazione della firma digitale da SmartCard", e);
			processError ( resp, "errore rilevato nell'applicazione della firma digitale da SmartCard", e);
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON ( resp )));
		return resp;
	}
	
	public void close() {
		if (digitalSignatureClient!=null) {
			try {
				digitalSignatureClient.close();
			} catch (SmartCardAccessException e) {
				// nothing to do..
			}
		}
	}
	
	private void processError ( AppletResponseDTO resp, String errorMessage, Throwable errorCause ) {
		if ( StringUtils.isBlank(errorMessage) ) {
			errorMessage = errorCause.getClass().getName();
		}
		resp.addActionError(errorMessage, errorCause);
		tracer.error(errorMessage, errorCause);
	}

}