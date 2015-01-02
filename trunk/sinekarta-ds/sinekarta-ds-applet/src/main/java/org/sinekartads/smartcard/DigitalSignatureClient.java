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
package org.sinekartads.smartcard;

import java.security.cert.X509Certificate;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.exception.InvalidCertificateException;
import org.sinekartads.exception.InvalidDriverException;
import org.sinekartads.exception.SignFailedException;
import org.sinekartads.exception.SmartCardAccessException;
import org.sinekartads.utils.HexUtils;

public class DigitalSignatureClient {

	private static final Logger tracer = Logger.getLogger(DigitalSignatureClient.class);

	private String driver;
	private String pin;
	private String alias;
	private String[] aliases;
	private ISmartCardAccess sca;
	
	public DigitalSignatureClient() {
		super();
	}
	
	public DigitalSignatureClient(String driver) {
		super();
		this.driver = driver;
	}

	public DigitalSignatureClient(String driver, String pin) {
		super();
		this.driver = driver;
		this.pin = pin;
	}

	public DigitalSignatureClient(String driver, String pin, String alias) {
		super();
		this.driver = driver;
		this.pin = pin;
		this.alias = alias;
	}

	public void verifyDriver () throws SmartCardAccessException {
		tracer.info(String.format("verifying driver %s with %s", driver, sca));
		tracer.info(String.format("selectDriver - %s", driver));
		try {
			sca.selectDriver ( driver );
		} catch (SmartCardAccessException e) {
			driver = null;
			throw e;
		}
	}
	
	public String[] certificateList() throws SmartCardAccessException {
		try {
			tracer.info(String.format("loginWithPin"));
			tracer.info(String.format("pin:    %s", pin));
			aliases = sca.loginAndCertificateList( pin );
			return aliases;
		} catch (SmartCardAccessException e) {
			pin = null;
			throw e;
		}
	}
	
	public X509Certificate selectCertificate () throws SmartCardAccessException {
		try {
			if ( ArrayUtils.isEmpty(aliases) ) {
				certificateList();
			}
			if ( ArrayUtils.isNotEmpty(aliases) ) {
				if ( ArrayUtils.contains(aliases, alias) ) {
					return sca.selectCertificate ( alias );
				} else {
					tracer.error(String.format("impossibile reperire il certificato per l'identità %s", alias));
					throw new InvalidCertificateException(String.format("impossibile reperire il certificato per l'identità %s", alias));
				}
			} else {
				tracer.error(String.format("impossibile reperire il certificato per l'identità %s", alias));
				throw new InvalidCertificateException(String.format("impossibile reperire il certificato per l'identità %s", alias));
			}
		} catch (SmartCardAccessException e) {
			alias = null;
			throw e;
		}
	}
	
	public String sign(String hexDigest) throws SmartCardAccessException {
		try {
			if ( selectCertificate() != null ) {
				byte[] digest = HexUtils.decodeHex(hexDigest);
				byte[] digitalSignature = sca.signFingerPrint(digest);
				return HexUtils.encodeHex(digitalSignature);
			} else {
				tracer.error("invalid vertificate");
				throw new SignFailedException("invalid vertificate");
			}
		} catch (SmartCardAccessException e) {
			throw e;
		}
	}
	
	public void open() throws SmartCardAccessException {
		if (sca==null) {
			if (StringUtils.isEmpty(driver)) {
				throw new InvalidDriverException("driver must be set befor calling to open");
			}
			if ( StringUtils.equals(driver, FakeSmartCardAccess.FAKE_DRIVER) ) {
				sca = new FakeSmartCardAccess ( );
			} else {
				sca = new SmartCardAccess ( );
			}
		} else {
			if ( StringUtils.equals(driver, FakeSmartCardAccess.FAKE_DRIVER) && !(sca instanceof FakeSmartCardAccess)) {
				sca = new FakeSmartCardAccess ( );
			}
			if ( !StringUtils.equals(driver, FakeSmartCardAccess.FAKE_DRIVER) && (sca instanceof FakeSmartCardAccess)) {
				sca = new SmartCardAccess ( );
			}
		}
		sca.open();
	}

	public void close() throws SmartCardAccessException {
		sca.close();
	}

	public String getDriver() {
		return driver;
	}
	
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public String getPin() {
		return pin;
	}
	
	public void setPin(String pin) {
		this.pin = pin;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
}
