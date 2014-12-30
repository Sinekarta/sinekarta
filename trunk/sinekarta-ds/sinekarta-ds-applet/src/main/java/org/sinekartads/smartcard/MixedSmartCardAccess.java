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
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.sinekartads.exception.SignFailedException;
import org.sinekartads.exception.SignInitializationException;
import org.sinekartads.exception.SmartCardAccessException;
import org.sinekartads.exception.SmartCardReaderNotFoundException;
import org.sinekartads.utils.DNParser;
import org.sinekartads.utils.HexUtils;

/**
 * smart card access class
 * this class realize communication with smart card.
 * communication is done using iaik for pkcs11 interface
 * 
 * @author andrea.tessaro
 *
 */
public class MixedSmartCardAccess implements ISmartCardAccess {
	
	private static final Logger tracer = Logger.getLogger(MixedSmartCardAccess.class);
	
	private String pkcs11Driver;
	
	private String pin; 
	
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
					tracer.error("Login failed, invalid PIN",e);
					throw new InvalidPinException("Login failed, invalid PIN",e);
				} else if (e.getMessage().contains("CKR_PIN_LOCKED")) {
					tracer.error("Login failed, PIN locked",e);
					throw new PinLockedException("Login failed, PIN locked",e);
				} else if (!e.getMessage().contains("CKR_USER_ALREADY_LOGGED_IN")) {
					tracer.error("Login failed",e);
					throw new SmartCardAccessException("Login failed", e);
				}
			} catch (Exception e) {
				tracer.error("Generic error, Login failed", e);
				throw new SmartCardAccessException("Generic error, Login failed", e);
			}
		}
		this.pin = pin;
		
		// Parse the certificate aliases
		String alias;
		List<String> aliases = new ArrayList<String>();
		X509Certificate cert;
		for (X509PublicKeyCertificate iaikCert : iaikCertificateList()) {
			cert = toX509Certificate(iaikCert);
			if (cert.getKeyUsage()[1]) {
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
		
		X509Certificate cert = null;
		List<X509PublicKeyCertificate> iaikCerts = iaikCertificateList();
		
		for (X509PublicKeyCertificate iaikCert : iaikCerts) {
			try {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(iaikCert.getValue().getByteArrayValue()));
			} catch (CertificateException e) {
				tracer.error("Unable parse certificate",e);
				throw new CertificateListException("Unable parse certificate",e);
			}
			
			try {
				cert.checkValidity();
			} catch (CertificateExpiredException e) {
				tracer.error("Invalid certificate, expired!",e);
				throw new CertificateListException("Invalid certificate, expired!",e);
			} catch (CertificateNotYetValidException e) {
				tracer.error("Invalid certificate, not yet valid!",e);
				throw new CertificateListException("Invalid certificate, not yet valid!",e);
			}

			if (cert.getKeyUsage()[1]) { // solo se e' attiva la caratteristica di firma digitale
//				if (cert.getSerialNumber().equals(certificate.getSerialNumber())) {
				if (StringUtils.equals(DNParser.parse(cert.getSubjectX500Principal().getName(), "CN"), alias)) {
					
					RSAPrivateKey iaikPrivateSignatureKeyTemplate = new RSAPrivateKey();
					iaikPrivateSignatureKeyTemplate.getId().setByteArrayValue(iaikCert.getId().getByteArrayValue());
					
					try {
						iaikSession.findObjectsInit(iaikPrivateSignatureKeyTemplate);
					} catch (TokenException e) {
						tracer.error("Unable to read private key from smart card (findObjectsInit)",e);
						throw new CertificateListException("Unable to read private key from smart card (findObjectsInit)",e);
					}

					Object[] iaikCorrespondingKeys;
					try {
						iaikCorrespondingKeys = iaikSession.findObjects(1);
					} catch (TokenException e) {
						tracer.error("Unable to read private key from smart card (findObjects)",e);
						throw new CertificateListException("Unable to read private key from smart card (findObjects)",e);
					}

					RSAPrivateKey iaikKey = (RSAPrivateKey)iaikCorrespondingKeys[0];
					iaikPrivateKey = iaikKey;
					try {
						iaikCorrespondingKeys = iaikSession.findObjects(1);
					} catch (TokenException e) {
						tracer.error("Unable to read private key from smart card (findObjects)",e);
						throw new CertificateListException("Unable to read private key from smart card (findObjects)",e);
					}
					// FIXME aggiunto ora
//					iaikPrivateKey = (RSAPrivateKey) iaikCorrespondingKeys[0];

					try {
						iaikSession.findObjectsFinal();
					} catch (TokenException e) {
						tracer.error("Unable to read private key from smart card (findObjectsFinal)",e);
						throw new CertificateListException("Unable to read private key from smart card (findObjectsFinal)",e);
					}

					break;
				}
			}
		}
		return cert;
	}
	
	/* (non-Javadoc)
	 * @see org.sinekarta.smartcard.ISmartCardAccess#sign(byte[])
	 */
	public byte[] signFingerPrint(byte[] digestedContent) throws SmartCardAccessException {
		String hhStr = "3031300d0609" + // intestazione comadno
	 	"608648016503040201" + // algorithm SHA-256 (2.16.840.1.101.3.4.2.1)
	 	"0500" + "0420" + HexUtils.encodeHex(digestedContent);

		byte[] toEncrypt = HexUtils.decodeHex(hhStr);
		
		byte[] signature = null;
		try {
			iaikSession.signInit(Mechanism.RSA_PKCS, iaikPrivateKey);
		} catch (TokenException e2) {
			tracer.error("Unable to initialize signature",e2);
			throw new SignInitializationException("Unable to initialize signature",e2);
		}
		try {
			signature = iaikSession.sign(toEncrypt);
		} catch (TokenException e1) {
			tracer.error("Unable to execute digital signature",e1);
			throw new SignFailedException("Unable to execute digital signature",e1);
		}
		return signature;
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

		while (iaikCertFound!=null && iaikCertFound.length > 0) {
			X509PublicKeyCertificate iaikCert = (X509PublicKeyCertificate)iaikCertFound[0];
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

	public List<X509Certificate> certificateList() throws SmartCardAccessException {
		List<X509Certificate> ret = new ArrayList<X509Certificate>();	
		
		List<X509PublicKeyCertificate> iaikCerts = iaikCertificateList();
		for (X509PublicKeyCertificate iaikCert : iaikCerts) {
			CertificateFactory cf;
			X509Certificate certificate;
			try {
				cf = CertificateFactory.getInstance("X.509");
				certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(iaikCert.getValue().getByteArrayValue()));
			} catch (CertificateException e) {
				tracer.error("Unable parse certificate",e);
				throw new CertificateListException("Unable parse certificate",e);
			}

			if (certificate.getKeyUsage()[1]) { // solo se e' attiva la caratteristica di firma digitale
				ret.add(certificate);
			}
		}
		return ret;
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