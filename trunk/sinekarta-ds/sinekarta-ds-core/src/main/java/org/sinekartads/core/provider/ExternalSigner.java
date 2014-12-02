package org.sinekartads.core.provider;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

import org.sinekartads.core.CoreConfiguration;

import com.itextpdf.text.pdf.security.ExternalSignature;

public class ExternalSigner implements ExternalSignature {

	static final CoreConfiguration conf = CoreConfiguration.getInstance();
	
	final String digestAlgorithm;
	final String encryptionAlgorithm;
	final byte[] fakeSignature;
	
	
	public ExternalSigner(String digestAlgorithm, 	String encryptionAlgorithm) 
			throws NoSuchAlgorithmException, 
					InvalidKeyException {
		
		try {
			// generate a key from the cipher
			KeyPairFactory kpGen = new KeyPairFactory ( 
					conf.getSignatureAlgorithm().getEncryptionAlgorithm() );
			kpGen.updateKeyPairOptions ( encryptionAlgorithm, null, conf.getProviderName() );
			PrivateKey privateKey = kpGen.createKeyPair().getPrivate();
			Signature signature = Signature.getInstance ( digestAlgorithm + "with" + encryptionAlgorithm );
			signature.initSign(privateKey);
			this.fakeSignature = signature.sign();
			this.digestAlgorithm = digestAlgorithm;
			this.digitalSignature = new ThreadLocal<byte[]>();
			this.encryptionAlgorithm = encryptionAlgorithm;
		} catch (SignatureException e) {
			// hide the (improbable) implementation-dependent exception
			throw new RuntimeException(e);
		} catch(NoSuchProviderException e) {
			// never thrown, using the default provider
			throw new RuntimeException(e);
		} 
	}	
	
	
	
	// -----
	// --- Factory methods
	// - 
	
//	public static ExternalSigner getInstance(Object ... params) 
//				throws IllegalArgumentException, 
//						InvalidKeyException {
//		
//		String errorMessage =  "Invalid parameters. Accepted values: \n" +
//				" - [String sigAlgoName] \n" +
//				" - [SignatureAlgorithm signatureAlgorithm] \n" +
//				" - [String	digAlgoName, String ciphAlgoName] \n" +
//				" - [DigestAlgorithm digestAlgorithm, CipherAlgorithm cipherAlgorithm] \n" +
//				"If indicating just a signatureAlgorithm it must embed a digestAlgorithm, \n" +
//				"it can't be just a cipherAlgorithm embedded into a signatureAlgorithm";
//		DigestAlgorithm digestAlgorithm;
//		EncryptionAlgorithm cipherAlgorithm;
//		switch(params.length) {
//		case 1: {
//			SignatureAlgorithm signatureAlgorithm;
//			if(params[0] instanceof String) {
//				signatureAlgorithm = SignatureAlgorithm.fromName((String)params[0]);
//			} else if(params[0] instanceof SignatureAlgorithm) {
//				signatureAlgorithm = (SignatureAlgorithm)params[0];
//			} else {
//				throw new IllegalArgumentException(errorMessage);
//			}
//			digestAlgorithm = signatureAlgorithm.getDigestAlgorithm();
//			cipherAlgorithm = signatureAlgorithm.getEncryptionAlgorithm();
//			break;
//		} case 2: {
//			if(params[0] instanceof String && params[1] instanceof String) {
//				digestAlgorithm = DigestAlgorithm.fromName((String)params[0]);
//				cipherAlgorithm = EncryptionAlgorithm.fromName((String)params[1]);
//			} else if(params[0] instanceof DigestAlgorithm && params[1] instanceof EncryptionAlgorithm) {
//				digestAlgorithm = (DigestAlgorithm)params[0];
//				cipherAlgorithm = (EncryptionAlgorithm)params[1];
//			} else {
//				throw new IllegalArgumentException(errorMessage);
//			}
//			break;
//		} default: {
//			throw new IllegalArgumentException(errorMessage);
//		}	} 	// end switch
//		ExternalSigner remoteSignature;
//		try {
//			remoteSignature = new ExternalSigner(digestAlgorithm.getName(), cipherAlgorithm.getName());
//		} catch (NoSuchAlgorithmException e) {
//			// never thrown, the algorithms have been already verified 
//			throw new RuntimeException(e);
//		} 
//		return remoteSignature;
//	}
	
	
	
	// -----
	// --- External signature protocol
	// -
	
	@Override
	public String getHashAlgorithm() {
		return digestAlgorithm;
	}

	@Override
	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	@Override
	public byte[] sign(byte[] message) throws GeneralSecurityException {
		byte[] signatureValue = digitalSignature.get();
		if(signatureValue == null) {
			signatureValue = fakeSignature;
		}
		return signatureValue;
	}
	
	
	
	// -----
	// --- Digital signature management
	// -
	
	ThreadLocal<byte[]> digitalSignature;
	
	public void setDigitalSignature(byte[] signatureValue) {
		// TODO recognize the conformity with the given algorithm specifications
		this.digitalSignature.set(signatureValue);
	}
	
	public void reset() {
		digitalSignature.remove();
	}	
}
