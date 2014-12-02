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
//import iaik.pkcs.pkcs11.wrapper.PKCS11Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.sinekartads.utils.DNParser;
import org.sinekartads.utils.HexUtils;

public class SmartCardAccess {
		
	
	String pkcs11Driver;
	
	String pin; 
	
	Module iaikPKCS11Module;
	
	Token iaikSmartCard;
	
	TokenInfo iaikSmartCardInfo;
	
	Session iaikSession;
	
	RSAPrivateKey iaikPrivateKey;

	/**
	 * canonical constructor of smart card access class
	 * need the pkcs11 driver (dll or .so) to work
	 * the driver must be present in native library path or in system path 
	 */
	public void selectDriver(String pkcs11Driver) 
			throws SmartCardReaderNotFoundException, PKCS11DriverNotFoundException, 
					InvalidPKCS11DriverException, InvalidSmartCardException, SmartCardAccessException {
		this.pkcs11Driver = pkcs11Driver;
		try {
			iaikPKCS11Module = Module.getInstance(pkcs11Driver);
		} catch (IOException e) {
			throw new PKCS11DriverNotFoundException("Unable to find driver: " + pkcs11Driver, e);
		} catch (Exception e) {
			throw new InvalidPKCS11DriverException("Invalid pkcs11 driver: " + pkcs11Driver, e);
		}
		if (iaikPKCS11Module==null) {
			throw new PKCS11DriverNotFoundException("pkcs11 driver not found");
		}
		try {
			InitializeArgs initializeArgs = new DefaultInitializeArgs();
			iaikPKCS11Module.initialize(initializeArgs);
		} catch (TokenException e) {
			if (!e.getMessage().contains("CKR_CRYPTOKI_ALREADY_INITIALIZED")) {
				throw new SmartCardAccessException("Unable to initialize pkcs11 module", e);
			}
		}
		try {
			InitializeArgs initializeArgs = new DefaultInitializeArgs();
			iaikPKCS11Module.initialize(initializeArgs);
		} catch (TokenException e) {
			if (!e.getMessage().contains("CKR_CRYPTOKI_ALREADY_INITIALIZED")) {
				throw new SmartCardAccessException("Unable to initialize pkcs11 module", e);
			}
		}
		Slot[] iaikSmartCardReaders;
		try {
			iaikSmartCardReaders = iaikPKCS11Module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
		} catch (Exception e) {
			throw new SmartCardReaderNotFoundException("Unable to find any smart card reader with a smart card", e);
		}
		if (ArrayUtils.isEmpty(iaikSmartCardReaders)) {
			throw new SmartCardReaderNotFoundException("No smart card reader found");
		}
		try {
			iaikSmartCard = iaikSmartCardReaders[0].getToken();
		} catch (TokenException e) {
			throw new SmartCardAccessException("Unable to find any smart card", e);
		}

		try {
			iaikSmartCardInfo = iaikSmartCard.getTokenInfo();
		} catch (TokenException e) {
			throw new SmartCardAccessException("Unable to read smart card info", e);
		}

		List<Mechanism> iaikSupportedMechanisms;
		try {
			iaikSupportedMechanisms = Arrays.asList(iaikSmartCard.getMechanismList());
			if (!iaikSupportedMechanisms.contains(Mechanism.RSA_PKCS)) {
				throw new InvalidSmartCardException("No support for RSA found on smart card");
			} else {
				MechanismInfo iaikRSAMechanismInfo = iaikSmartCard.getMechanismInfo(Mechanism.RSA_PKCS);
				if (!iaikRSAMechanismInfo.isSign()) {
					throw new InvalidSmartCardException("This smart card does not support RSA signing");
				}
			}
		} catch (TokenException e) {
			throw new InvalidSmartCardException("Unable to determine smart card properties",e);
		}

		try {
			iaikSession = iaikSmartCard.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RO_SESSION, null, null);
		} catch (TokenException e) {
			throw new SmartCardAccessException("Unable to open smart card session",e);
		}
	}
	
	public String[] login ( ) 
			throws IllegalStateException, 
					SmartCardAccessException {
		return login ( getPin() );
	}
		
	public String[] login ( String pin ) 
			throws IllegalStateException, 
					SmartCardAccessException {
		
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
					throw new InvalidPinException("Login failed, invalid PIN", e);
				} else if (e.getMessage().contains("CKR_PIN_LOCKED")) {
					throw new PinLockedException("Login failed, PIN locked", e);
				} else if (!e.getMessage().contains("CKR_USER_ALREADY_LOGGED_IN")) {
					throw new SmartCardAccessException("Login failed", e);
				}
			} catch(Exception e) {
				throw new SmartCardAccessException("Login failed", e);
			}
		}
		this.pin = pin;
		
		// Parse the certificate aliases
		String alias;
		X500Principal principal;
		List<String> aliases = new ArrayList<String>();
		for ( X509PublicKeyCertificate iaikCert : iaikCertificateList() ) {
			principal = new X500Principal(iaikCert.getSubject().getByteArrayValue());
			alias = DNParser.parse(principal.getName(), "CN");
			aliases.add(alias);
		}
		
		// return the aliases as an array
		return aliases.toArray ( new String[aliases.size()] );
	}
	

	public X509Certificate selectCertificate(String alias) 
			throws IllegalStateException, 
					SmartCardAccessException {
		
		if (iaikSession==null) {
			throw new IllegalStateException("Session not initialized, login before");
		}
		iaikPrivateKey = null;
		
		// Look for the suitable signing certificate with the given alias
		X509Certificate cert = null;
		X509PublicKeyCertificate iaikCert;
		Iterator<X509PublicKeyCertificate> iaikCertificateIt = iaikCertificateList().iterator();
		while ( iaikCertificateIt.hasNext() && iaikPrivateKey == null ) {

			// Transform the iaik certificate to a X509 instance
			try {
				iaikCert = iaikCertificateIt.next();
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(iaikCert.getValue().getByteArrayValue()));
			} catch (CertificateException e) {
				throw new CertificateListException("unable parse the certificate",e);
			}

			if (cert.getKeyUsage()[0]) {
				// Accept the certificate only if has the digitalSignature usage available 
//				if (cert.getSerialNumber().equals(iaikCert.getSerialNumber())) {
					Object[] iaikCorrespondingKeys;
					try {
						// Init the privateKey seek
						RSAPrivateKey iaikPrivateSignatureKeyTemplate = new RSAPrivateKey();
						iaikPrivateSignatureKeyTemplate.getId().setByteArrayValue(iaikCert.getId().getByteArrayValue());
						iaikSession.findObjectsInit(iaikPrivateSignatureKeyTemplate);
						
						// Look for the privateKey
						iaikCorrespondingKeys = iaikSession.findObjects(1);
						
						// Extract the private key result and store it into the iaikPrivateKey property
						iaikPrivateKey = (RSAPrivateKey)iaikCorrespondingKeys[0];
					} catch (TokenException e) {
						throw new CertificateListException("Unable to read private key from smart card (findObjectsInit)",e);
					} finally {
						try {
							iaikSession.findObjectsFinal();
						} catch (TokenException e) {
							throw new CertificateListException("Unable to read private key from smart card (findObjectsFinal)",e);
						}
					}
//				}
			} else {
				// If it doesn't, try with the next one
				cert = null;
			}
		}
		return cert;
	}

	public byte[] signFingerPrint(byte[] fingerPrint) 
			throws IllegalStateException, 
					IllegalArgumentException, 
					SmartCardAccessException {
		
		byte[] digitalSignature = null;
		
		// verify that the SCA is ready to sign
		if (iaikSession == null) {
			throw new IllegalStateException("session not initialized, login before");
		}
		if(iaikPrivateKey == null) {
			throw new IllegalStateException("missing privateKey, call selectCertificate before");
		}
		
		// verify whether the digest algorithm is supported and get the relative hex descriptor
		String hexDigAlgorithm = "06"+"09"+"608648016503040201"; 		// algorithm SHA256 (2.16.840.1.101.3.4.2.1)
		
		// generate the signature command
		String hhStr = "3031300d" + 	// command header (???)
			 	hexDigAlgorithm + 		// digest algorithm descriptor 
			 	"0500" + "0420" +		// digest prefix (???)  
			 	HexUtils.encodeHex(fingerPrint);
		byte[] toEncrypt = HexUtils.decodeHex(hhStr);
		
		// prepare the smartcard to sign
		try {
			iaikSession.signInit(Mechanism.RSA_PKCS, iaikPrivateKey);
		} catch (TokenException e) {
			throw new SmartCardAccessException("Unable to initialize signature", e);
		}
		
		// perform the signature
		try {
			digitalSignature = iaikSession.sign(toEncrypt);
		} catch (TokenException e) {
			throw new SmartCardAccessException("Unable to perform the digital signature", e);
		}
		return digitalSignature;
	}

	public void logout() 
			throws IllegalStateException,
					SmartCardAccessException {
		if (iaikSession!=null) {
			try {
				iaikSession.logout();
			} catch (TokenException e) {
				if( !StringUtils.contains(e.getMessage(), "CKR_USER_NOT_LOGGED_IN") ) {
					throw new SmartCardAccessException("Unable to perform pkcs11 logout", e);
				}
			}
		}
	}
	
	public void finalize() 
			throws SmartCardAccessException {
		
		try {
			if(iaikSession != null) {
				iaikSession.closeSession();
			}
		} catch (TokenException e) {
			throw new SmartCardAccessException("Unable to close pkcs11 session", e);
		}
		try {
			if(iaikPKCS11Module != null) {
				iaikPKCS11Module.finalize(null);
			}
		} catch (TokenException e) {
			throw new SmartCardAccessException("Unable to finalize pkcs11 module");
		}
	}

	public String getPKCS11Driver() {
		return SmartCardAccess.this.pkcs11Driver;
	}

	public String getPin() {
		return SmartCardAccess.this.pin;
	}
		

	
	


//	public String[] getAliases(String pin) throws InvalidPinException, PinLockedException, SmartCardAccessException {
//		if (iaikSmartCardInfo.isLoginRequired()) {
//			try {
//				if (iaikSmartCardInfo.isProtectedAuthenticationPath()) {
//					iaikSession.login(Session.UserType.USER, null); 
//				} else {
//					iaikSession.login(Session.UserType.USER, pin.toCharArray());
//				}
//			} catch (TokenException e) {
//				if (e.getMessage().contains("CKR_PIN_INCORRECT") || e.getMessage().contains("CKR_PIN_INVALID")) {
//					throw new InvalidPinException("Login failed, invalid PIN", e);
//				} else if (e.getMessage().contains("CKR_PIN_LOCKED")) {
//					throw new PinLockedException("Login failed, PIN locked", e);
//				} else if (!e.getMessage().contains("CKR_USER_ALREADY_LOGGED_IN")) {
//					throw new SmartCardAccessException("Login failed", e);
//				}
//			} catch(Exception e) {
//				throw new SmartCardAccessException("Login failed", e);
//			}
//		}
//		this.pin = pin;
//		
//		
//	}
	
	private List<X509PublicKeyCertificate> iaikCertificateList() 
			throws CertificateListException, SmartCardAccessException {
		
		List<X509PublicKeyCertificate> certList = new ArrayList<X509PublicKeyCertificate>();
		try {
			X509PublicKeyCertificate iaikCertToFind = new X509PublicKeyCertificate();
			try {
				iaikSession.findObjectsInit(iaikCertToFind);
			} catch (TokenException e) {
				throw new CertificateListException("Unable to read certificates from smart card (findObjectsInit)",e);
			}
			
			Object[] iaikCertFound;
			try {
				iaikCertFound = iaikSession.findObjects(1);
			} catch (TokenException e) {
				throw new CertificateListException("Unable to read certificates from smart card (findObjects)",e);
			}
	
			while (iaikCertFound!=null && iaikCertFound.length > 0) {
				X509PublicKeyCertificate iaikCert = (X509PublicKeyCertificate)iaikCertFound[0];
				certList.add(iaikCert);
				try {
					iaikCertFound = iaikSession.findObjects(1);
				} catch (TokenException e) {
					throw new CertificateListException("Unable to read certificates from smart card (findObjects)",e);
				}
			}
			try {
				iaikSession.findObjectsFinal();
			} catch (TokenException e) {
				throw new CertificateListException("Unable to read certificates from smart card (findObjectsFinal)",e);
			}
		} catch(SmartCardAccessException e) {
			throw e;
		} catch(Exception e) {
			throw new SmartCardAccessException(e);
		}
		
		return certList;
	}

//	private List<X509Certificate> doCertificateList() throws SmartCardAccessException {
//		List<X509Certificate> ret = new ArrayList<X509Certificate>();	
//		
//		List<X509PublicKeyCertificate> iaikCerts = iaikCertificateList();
//		for (X509PublicKeyCertificate iaikCert : iaikCerts) {
//			CertificateFactory cf;
//			X509Certificate certificate;
//			try {
//				cf = CertificateFactory.getInstance("X.509");
//				certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(iaikCert.getValue().getByteArrayValue()));
//			} catch (CertificateException e) {
////				logger.error("Unable parse certificate",e);
//				throw new CertificateListException("Unable parse certificate",e);
//			}
//
//			if (certificate.getKeyUsage()[0]) { // solo se e' attiva la caratteristica di firma digitale
//				ret.add(certificate);
//			}
//		}
//		return ret;
//	}


	
	
	
	
//	
//	public String[] login(String pin) 
//			throws IllegalStateException, 
//					SmartCardAccessException {
//		
//		if (iaikSmartCardInfo.isLoginRequired()) {
//			try {
//				if (iaikSmartCardInfo.isProtectedAuthenticationPath()) {
//					iaikSession.login(Session.UserType.USER, null); 
//				} else {
//					iaikSession.login(Session.UserType.USER, pin.toCharArray());
//				}
//			} catch (TokenException e) {
//				if (e.getMessage().contains("CKR_PIN_INCORRECT") || e.getMessage().contains("CKR_PIN_INVALID")) {
//					throw new InvalidPinException("Login failed, invalid PIN", e);
//				} else if (e.getMessage().contains("CKR_PIN_LOCKED")) {
//					throw new PinLockedException("Login failed, PIN locked", e);
//				} else if (!e.getMessage().contains("CKR_USER_ALREADY_LOGGED_IN")) {
//					throw new SmartCardAccessException("Login failed", e);
//				}
//			} catch(Exception e) {
//				throw new SmartCardAccessException("Login failed", e);
//			}
//		}
//		this.pin = pin;
//		
//		List<X509PublicKeyCertificate> iaikCertificateList = iaikCertificateList(); 
//		
//		List<String> aliases = new ArrayList<String>();
//		try {
//			X509PublicKeyCertificate iaikCertToFind = new X509PublicKeyCertificate();
//			try {
//				iaikSession.findObjectsInit(iaikCertToFind);
//			} catch (TokenException e) {
//				throw new CertificateListException("Unable to read certificates from smart card (findObjectsInit)",e);
//			}
//			
//			Object[] iaikCertFound;
//			try {
//				iaikCertFound = iaikSession.findObjects(1);
//			} catch (TokenException e) {
//				throw new CertificateListException("Unable to read certificates from smart card (findObjects)",e);
//			}
//	
//			String alias;
//			while (iaikCertFound!=null && iaikCertFound.length > 0) {
//				X509PublicKeyCertificate iaikCert = (X509PublicKeyCertificate)iaikCertFound[0];
//				alias = new String(iaikCert.getSubject().getByteArrayValue());
//				aliases.add(alias);
//				try {
//					iaikCertFound = iaikSession.findObjects(1);
//				} catch (TokenException e) {
//					throw new CertificateListException("Unable to read certificates from smart card (findObjects)",e);
//				}
//			}
//			try {
//				iaikSession.findObjectsFinal();
//			} catch (TokenException e) {
//				throw new CertificateListException("Unable to read certificates from smart card (findObjectsFinal)",e);
//			}
//		} catch(SmartCardAccessException e) {
//			throw e;
//		} catch(Exception e) {
//			throw new SmartCardAccessException(e);
//		}
//		
//		return aliases.toArray ( new String[aliases.size()] );
//	}
}
