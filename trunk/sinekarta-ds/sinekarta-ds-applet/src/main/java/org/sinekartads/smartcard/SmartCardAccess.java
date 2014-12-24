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

import iaik.pkcs.pkcs11.DefaultInitializeArgs;
import iaik.pkcs.pkcs11.InitializeArgs;
import iaik.pkcs.pkcs11.Mechanism;
import iaik.pkcs.pkcs11.MechanismInfo;
import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.TokenInfo;
import iaik.pkcs.pkcs11.objects.Object;
import iaik.pkcs.pkcs11.objects.RSAPrivateKey;
import iaik.pkcs.pkcs11.objects.X509PublicKeyCertificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.exception.CertificateListException;
import org.sinekartads.exception.InvalidPKCS11DriverException;
import org.sinekartads.exception.InvalidPinException;
import org.sinekartads.exception.InvalidSmartCardException;
import org.sinekartads.exception.PKCS11DriverNotFoundException;
import org.sinekartads.exception.PinLockedException;
import org.sinekartads.exception.SmartCardAccessException;
import org.sinekartads.exception.SmartCardReaderNotFoundException;
import org.sinekartads.utils.DNParser;
import org.sinekartads.utils.HexUtils;

public class SmartCardAccess implements ISmartCardAccess {
	
	private static final Logger tracer = Logger.getLogger(SmartCardAccess.class);

	private Module iaikPKCS11Module;

	private Token iaikSmartCard;

	private TokenInfo iaikSmartCardInfo;

	private Session iaikSession;

	private RSAPrivateKey iaikPrivateKey;

	public void selectDriver(String pkcs11Driver) throws SmartCardAccessException {
		tracer.info(String.format("selectDriver - %s", pkcs11Driver));
		class MyPrivilegedAction implements PrivilegedAction<Exception> {
			private String pkcs11Driver;

			public MyPrivilegedAction(String pkcs11Driver) {
				super();
				this.pkcs11Driver = pkcs11Driver;
			}

			public Exception run() {
				try {
					iaikPKCS11Module = Module.getInstance(pkcs11Driver);
					return null;
				} catch (IOException e) {
					return new PKCS11DriverNotFoundException(
							"Unable to find driver: " + pkcs11Driver, e);
				} catch (Throwable e) {
					return new InvalidPKCS11DriverException(
							"Invalid pkcs11 driver: " + pkcs11Driver, e);
				}
			}
		}
		MyPrivilegedAction action = new MyPrivilegedAction(pkcs11Driver);
		tracer.info(String.format("running the action..."));
		Exception ex = AccessController.doPrivileged(action);
		tracer.info(String.format("action performed."));
		if (ex != null) {
			tracer.info(String.format("action ended with error: ",
					ex.getMessage()));
			if (ex instanceof PKCS11DriverNotFoundException) {
				tracer.error("driver not found", ex);
				throw (PKCS11DriverNotFoundException) ex;
			} else if (ex instanceof InvalidPKCS11DriverException) {
				tracer.error("driver error", ex);
				throw (InvalidPKCS11DriverException) ex;
			} else {
				tracer.error("generic error", ex);
				throw new SmartCardAccessException(ex);
			}
		}

		if (iaikPKCS11Module == null) {
			tracer.error("pkcs11 driver not found");
			throw new PKCS11DriverNotFoundException("pkcs11 driver not found");
		}
		try {
			InitializeArgs initializeArgs = new DefaultInitializeArgs();
			iaikPKCS11Module.initialize(initializeArgs);
		} catch (TokenException e) {
			if (!e.getMessage().contains("CKR_CRYPTOKI_ALREADY_INITIALIZED")) {
				tracer.error("Unable to initialize pkcs11 module", e);
				throw new SmartCardAccessException(	"Unable to initialize pkcs11 module", e);
			}
		}
		try {
			InitializeArgs initializeArgs = new DefaultInitializeArgs();
			iaikPKCS11Module.initialize(initializeArgs);
		} catch (TokenException e) {
			if (!e.getMessage().contains("CKR_CRYPTOKI_ALREADY_INITIALIZED")) {
				tracer.error("Unable to initialize pkcs11 module", e);
				throw new SmartCardAccessException(	"Unable to initialize pkcs11 module", e);
			}
		}
		Slot[] iaikSmartCardReaders;
		try {
			iaikSmartCardReaders = iaikPKCS11Module
					.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
		} catch (Exception e) {
			tracer.error("Unable to find any smart card reader with a smart card", e);
			throw new SmartCardReaderNotFoundException(	"Unable to find any smart card reader with a smart card", e);
		}
		if (ArrayUtils.isEmpty(iaikSmartCardReaders)) {
			tracer.error("No smart card reader found");
			throw new SmartCardReaderNotFoundException("No smart card reader found");
		}
		try {
			iaikSmartCard = iaikSmartCardReaders[0].getToken();
		} catch (TokenException e) {
			tracer.error("Unable to find any smart card", e);
			throw new SmartCardAccessException("Unable to find any smart card", e);
		}

		try {
			iaikSmartCardInfo = iaikSmartCard.getTokenInfo();
		} catch (TokenException e) {
			tracer.error("Unable to read smart card info", e);
			throw new SmartCardAccessException("Unable to read smart card info", e);
		}

		List<Mechanism> iaikSupportedMechanisms;
		try {
			iaikSupportedMechanisms = Arrays.asList(iaikSmartCard
					.getMechanismList());
			if (!iaikSupportedMechanisms.contains(Mechanism.RSA_PKCS)) {
				throw new InvalidSmartCardException(
						"No support for RSA found on smart card");
			} else {
				MechanismInfo iaikRSAMechanismInfo = iaikSmartCard
						.getMechanismInfo(Mechanism.RSA_PKCS);
				if (!iaikRSAMechanismInfo.isSign()) {
					throw new InvalidSmartCardException(
							"This smart card does not support RSA signing");
				}
			}
		} catch (TokenException e) {
			throw new InvalidSmartCardException(
					"Unable to determine smart card properties", e);
		}

		try {
			iaikSession = iaikSmartCard.openSession(
					Token.SessionType.SERIAL_SESSION,
					Token.SessionReadWriteBehavior.RO_SESSION, null, null);
		} catch (TokenException e) {
			throw new SmartCardAccessException(
					"Unable to open smart card session", e);
		}
	}

	public String[] login(String pin) throws SmartCardAccessException {

		// Execute the login
		if (iaikSmartCardInfo.isLoginRequired()) {
			try {
				if (iaikSmartCardInfo.isProtectedAuthenticationPath()) {
					iaikSession.login(Session.UserType.USER, null);
				} else {
					iaikSession.login(Session.UserType.USER, pin.toCharArray());
				}
			} catch (TokenException e) {
				if (e.getMessage().contains("CKR_PIN_INCORRECT") || e.getMessage().contains("CKR_PIN_INVALID")) {
					tracer.error("Login failed, invalid PIN", e);
					throw new InvalidPinException("Login failed, invalid PIN", e);
				} else if (e.getMessage().contains("CKR_PIN_LOCKED")) {
					tracer.error("Login failed, PIN locked", e);
					throw new PinLockedException("Login failed, PIN locked", e);
				} else if (!e.getMessage().contains("CKR_USER_ALREADY_LOGGED_IN")) {
					tracer.error("Login failed", e);
					throw new SmartCardAccessException("Login failed", e);
				}
			} catch (Exception e) {
				tracer.error("Generic error, Login failed", e);
				throw new SmartCardAccessException("Generic error, Login failed", e);
			}
		}

		// Parse the certificate aliases
		String alias;
		List<String> aliases = new ArrayList<String>();
		X509Certificate cert;
		for (X509PublicKeyCertificate iaikCert : iaikCertificateList()) {
			cert = toX509Certificate(iaikCert);
			if (cert.getKeyUsage()[0]) {
				alias = DNParser.parse(cert.getSubjectX500Principal().getName(), "CN");
				aliases.add(alias);
			}
		}

		// return the aliases as an array
		return aliases.toArray(new String[aliases.size()]);
	}

	public X509Certificate selectCertificate(String alias) throws SmartCardAccessException {
		if (iaikSession == null) {
			tracer.error("Session not initialized, login before");
			throw new IllegalStateException("Session not initialized, login before");
		}
		iaikPrivateKey = null;

		// Look for the suitable signing certificate with the given alias
		X509Certificate cert = null;
		X509PublicKeyCertificate iaikCert;
		Iterator<X509PublicKeyCertificate> iaikCertificateIt = iaikCertificateList().iterator();
		while (iaikCertificateIt.hasNext() && iaikPrivateKey == null) {

			// Transform the iaik certificate to a X509 instance
			iaikCert = iaikCertificateIt.next();
			cert = toX509Certificate(iaikCert);

			if (cert.getKeyUsage()[0]) {
				// Accept the certificate only if has the digitalSignature usage
				// available
				// if
				// (cert.getSerialNumber().equals(iaikCert.getSerialNumber())) {
				Object[] iaikCorrespondingKeys;
				try {
					// Init the privateKey seek
					RSAPrivateKey iaikPrivateSignatureKeyTemplate = new RSAPrivateKey();
					iaikPrivateSignatureKeyTemplate.getId().setByteArrayValue(iaikCert.getId().getByteArrayValue());
					iaikSession.findObjectsInit(iaikPrivateSignatureKeyTemplate);

					// Look for the privateKey
					iaikCorrespondingKeys = iaikSession.findObjects(1);

					// Extract the private key result and store it into the
					// iaikPrivateKey property
					iaikPrivateKey = (RSAPrivateKey) iaikCorrespondingKeys[0];
				} catch (TokenException e) {
					tracer.error("Unable to read private key from smart card (findObjectsInit)",e);
					throw new CertificateListException("Unable to read private key from smart card (findObjectsInit)",e);
				} finally {
					try {
						iaikSession.findObjectsFinal();
					} catch (TokenException e) {
						tracer.error("Unable to read private key from smart card (findObjectsFinal)",e);
						throw new CertificateListException("Unable to read private key from smart card (findObjectsFinal)",e);
					}
				}
				// }
			} else {
				// If it doesn't, try with the next one
				cert = null;
			}
		}

		return cert;
	}

	public byte[] signFingerPrint(byte[] fingerPrint) throws SmartCardAccessException {

		byte[] digitalSignature = null;

		// verify that the SCA is ready to sign
		if (iaikSession == null) {
			tracer.error("session not initialized, login before");
			throw new IllegalStateException("session not initialized, login before");
		}
		if (iaikPrivateKey == null) {
			tracer.error("missing privateKey, call selectCertificate before");
			throw new IllegalStateException("missing privateKey, call selectCertificate before");
		}

		// verify whether the digest algorithm is supported and get the relative
		// hex descriptor
		String hexDigAlgorithm = "06" + "09" + "608648016503040201"; // algorithm
																		// SHA256
																		// (2.16.840.1.101.3.4.2.1)

		// generate the signature command
		String hhStr = "3031300d" + // command header (???)
				hexDigAlgorithm + // digest algorithm descriptor
				"0500" + "0420" + // digest prefix (???)
				HexUtils.encodeHex(fingerPrint);
		byte[] toEncrypt = HexUtils.decodeHex(hhStr);

		// prepare the smartcard to sign
		try {
			iaikSession.signInit(Mechanism.RSA_PKCS, iaikPrivateKey);
		} catch (TokenException e) {
			tracer.error("Unable to initialize signature", e);
			throw new SmartCardAccessException("Unable to initialize signature", e);
		}

		// perform the signature
		try {
			digitalSignature = iaikSession.sign(toEncrypt);
		} catch (TokenException e) {
			tracer.error("Unable to perform the digital signature", e);
			throw new SmartCardAccessException("Unable to perform the digital signature", e);
		}
		return digitalSignature;
	}

	public void logout() throws SmartCardAccessException {
		if (iaikSession != null) {
			try {
				iaikSession.logout();
			} catch (TokenException e) {
				if (!StringUtils.contains(e.getMessage(),"CKR_USER_NOT_LOGGED_IN")) {
					tracer.error("Unable to perform pkcs11 logout", e);
					throw new SmartCardAccessException("Unable to perform pkcs11 logout", e);
				}
			}
		}
	}


	private List<X509PublicKeyCertificate> iaikCertificateList() throws SmartCardAccessException {

		List<X509PublicKeyCertificate> certList = new ArrayList<X509PublicKeyCertificate>();
		try {
			X509PublicKeyCertificate iaikCertToFind = new X509PublicKeyCertificate();
			try {
				iaikSession.findObjectsInit(iaikCertToFind);
			} catch (TokenException e) {
				tracer.error("Unable to read certificates from smart card (findObjectsInit)",e);
				throw new CertificateListException("Unable to read certificates from smart card (findObjectsInit)",e);
			}

			Object[] iaikCertFound;
			try {
				iaikCertFound = iaikSession.findObjects(1);
			} catch (TokenException e) {
				tracer.error("Unable to read certificates from smart card (findObjects)",e);
				throw new CertificateListException("Unable to read certificates from smart card (findObjects)",e);
			}

			while (iaikCertFound != null && iaikCertFound.length > 0) {
				X509PublicKeyCertificate iaikCert = (X509PublicKeyCertificate) iaikCertFound[0];
				certList.add(iaikCert);
				try {
					iaikCertFound = iaikSession.findObjects(1);
				} catch (TokenException e) {
					tracer.error("Unable to read certificates from smart card (findObjects)",e);
					throw new CertificateListException("Unable to read certificates from smart card (findObjects)",e);
				}
			}
			try {
				iaikSession.findObjectsFinal();
			} catch (TokenException e) {
				tracer.error("Unable to read certificates from smart card (findObjectsFinal)",e);
				throw new CertificateListException("Unable to read certificates from smart card (findObjectsFinal)",e);
			}
		} catch (SmartCardAccessException e) {
			throw e;
		} catch (Exception e) {
			tracer.error("Generic error on iaikCertificateList",e);
			throw new SmartCardAccessException(e);
		}

		return certList;
	}

	private X509Certificate toX509Certificate(X509PublicKeyCertificate iaikCert) throws SmartCardAccessException{
		CertificateFactory cf;
		try {
			cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf
					.generateCertificate(new ByteArrayInputStream(iaikCert
							.getValue().getByteArrayValue()));
		} catch (CertificateException e) {
			tracer.error("Generic error on toX509Certificate",e);
			throw new SmartCardAccessException("Generic error on toX509Certificate", e);
		}
	}

	public void open() throws SmartCardAccessException {
		// NOTHING TO DO BY NOW
	}

	public void close() throws SmartCardAccessException {
		try {
			if (iaikSession != null) {
				iaikSession.closeSession();
			}
		} catch (TokenException e) {
			// nothing to do..
		}
		try {
			if (iaikPKCS11Module != null) {
				iaikPKCS11Module.finalize(null);
			}
		} catch (TokenException e) {
			// nothing to do..
		}
	}
}
