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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.ArrayUtils;
import org.sinekartads.dto.domain.KeyStoreDTO;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.EncryptionAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.verification.UserPermissionsVerifier;

/**
 * sinekarta digital signature generic client
 * this client will use String area returned by sinekarta alfresco action 
 * 
 * @author andrea.tessaro
 *
 */
public abstract class KeyStoreClient extends SignatureClient {
	
	static final UserPermissionsVerifier permissionsVerifier = UserPermissionsVerifier.getInstance();
	
	public KeyStoreClient ( String sessionId ) {
		
		super ( sessionId, SignatureClientType.KEYSTORE );
	}

	protected KeyStoreDTO keyStore;
	protected PrivateKey privateKey;
	
	
	
	// -----
	// --- State-safe controller
	// -
	
	public class KeyStoreClientCtrl extends SignatureClientCtrl<KeyStoreClient> {
		
		public String[] openKeyStore ( String keyStorePin ) 
				throws IllegalStateException, 
						IllegalArgumentException {
			
			return KeyStoreClient.this.openKeyStore ( keyStorePin );
		}
	}
	
	KeyStoreClientCtrl controller = new KeyStoreClientCtrl();
	
	@Override
	@SuppressWarnings("unchecked")
	public KeyStoreClientCtrl getController() {
		return controller;
	}

	
	
	// -----
	// --- SignatureClient implementation
	// -
	
	protected String[] aliases;
	
	@Override
	protected void init ( ) {
		
	}
	
	protected abstract String[] openKeyStore ( String keyStorePin ) 
			throws IllegalArgumentException;
	
	@Override
	protected void loadAliases() {
		this.controller.setInitStep(ALIASES_AVAILABLE);
		aliases = keyStore.getAliases();
	}
	
	@Override
	protected byte[] doSign(SignatureAlgorithm sigAlgorithm, DigestInfo digestInfo) 
			throws DigitalSignatureException {
		// TODO rivedere questa roba
		byte[] digitalSignature;
		try {
			EncryptionAlgorithm encryptionAlgorithm = sigAlgorithm.getEncryptionAlgorithm();
			if(sigAlgorithm.getDigestAlgorithm() == null) {
				// use a wrapped cipher algorithm
				byte[] prefix;
				// load the prefix for the given digest algorithm
				if(digestInfo.getAlgorithm().equals(DigestAlgorithm.SHA1)) {
					prefix = encryptionAlgorithm.getPrefixSHA1();
				} else if(digestInfo.getAlgorithm().equals(DigestAlgorithm.SHA256)) {
					prefix = encryptionAlgorithm.getPrefixSHA256();
				} else {
					throw new DigitalSignatureException(String.format(
							"unable to find a padding for the digest algorithm %s, use SHA1 or SHA256 instead",
							digestInfo.getAlgorithm()));
				}
				byte[] fingerPrint;
				// append the digest to the prefix, if any
				if(ArrayUtils.isNotEmpty(prefix)) {
					fingerPrint = ArrayUtils.addAll(prefix, digestInfo.getFingerPrint());
				} else {
					fingerPrint = digestInfo.getFingerPrint();
				}
				// apply the digital signature
				Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getName());
				cipher.init(Cipher.ENCRYPT_MODE, privateKey);
				digitalSignature = cipher.doFinal(fingerPrint);
			} else {
				// apply directly the signature algorithm
				Signature signature = Signature.getInstance(sigAlgorithm.getName());
				signature.initSign(privateKey);
				signature.update(digestInfo.getFingerPrint());
				digitalSignature = signature.sign();
			}
		} catch (NoSuchAlgorithmException e) {
			// never thrown: fixed algorithms
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			// never thrown: fixed padding
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			// never thrown: fixed padding
			throw new RuntimeException(e);
		} catch (IllegalBlockSizeException e) {
			throw new DigitalSignatureException(e.getMessage(), e);			
		} catch (InvalidKeyException e) {
			throw new DigitalSignatureException(e.getMessage(), e);
		} catch (SignatureException e) {
			throw new DigitalSignatureException(e.getMessage(), e);			
		}
		return digitalSignature;
	}
}
