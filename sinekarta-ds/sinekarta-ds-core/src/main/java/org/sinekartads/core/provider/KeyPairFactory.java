package org.sinekartads.core.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import org.sinekartads.core.CoreConfiguration;
import org.sinekartads.model.oid.EncryptionAlgorithm;
import org.sinekartads.util.controller.Controller;
	
public class KeyPairFactory extends Controller {
	
	static final CoreConfiguration conf = CoreConfiguration.getInstance(); 

	public KeyPairFactory(EncryptionAlgorithm encryptionAlgorithm) {
		super(1);
		try {
			if ( encryptionAlgorithm == null ) {
				encryptionAlgorithm = conf.getSignatureAlgorithm().getEncryptionAlgorithm();
			}
			kpGen = KeyPairGenerator.getInstance(encryptionAlgorithm.getName(), conf.getProviderName());
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// never thrown, using the default provider and algorithm
			throw new RuntimeException(e);
		}
	}
	
	public KeyPair createKeyPair() 
			throws IllegalStateException {
		
		super.verifyControllerState();
		KeyPair keyPair;
		try {
			keyPair = createKeyPair(null, null, null);
		} catch (InvalidAlgorithmParameterException e) {
			// never thrown, only possible if params is populated
			throw new RuntimeException(e);
		}
		return keyPair;
	}
	
	public KeyPair createKeyPair(int keysize, SecureRandom random) 
			throws IllegalStateException {
		
		super.verifyControllerState();
		KeyPair keyPair;
		try {
			keyPair = createKeyPair(keysize, null, random);
		} catch (InvalidAlgorithmParameterException e) {
			// never thrown, only possible if params is populated
			throw new RuntimeException(e);
		}
		return keyPair;
	}
	
	public KeyPair createKeyPair(AlgorithmParameterSpec params, SecureRandom random) 
			throws IllegalStateException,
					InvalidAlgorithmParameterException {
		
		super.verifyControllerState();
		return createKeyPair(null, params, random);
	}
	
	private KeyPair createKeyPair(Integer keysize, AlgorithmParameterSpec params, SecureRandom random) 
			throws 	InvalidAlgorithmParameterException {
		
		KeyPairGenerator kpgClone = cloneKeyPairGenerator();
		if(keysize != null) {
			if(random != null) {
				kpgClone.initialize(keysize, random);
			} else {
				kpgClone.initialize(keysize);
			}
		}
		if(params != null) {
			if(random != null) {
				kpgClone.initialize(params, random);
			} else {
				kpgClone.initialize(params);
			}
		} else {
		}
		return  kpgClone.generateKeyPair();
	}
	
	private KeyPairGenerator cloneKeyPairGenerator() {
		KeyPairGenerator kpgClone;
		try {
			kpgClone = KeyPairGenerator.getInstance(kpGen.getAlgorithm(), kpGen.getProvider());
		} catch (NoSuchAlgorithmException e) {
			// never thrown, algorithm must be valid
			throw new RuntimeException(e);
		}
		return kpgClone;
	}

	public void updateKeyPairOptions(
			String 	encryptionAlgorithm, 
			Integer keysize, 
			String 	providerName) 
					throws NoSuchAlgorithmException, 
							NoSuchProviderException {

		KeyPairGenerator kpGen;
		if(encryptionAlgorithm == null) {
			encryptionAlgorithm = conf.getSignatureAlgorithm().getName();
		}
		if(providerName == null) {
			kpGen = KeyPairGenerator.getInstance(encryptionAlgorithm);
		} else {
			kpGen = KeyPairGenerator.getInstance(encryptionAlgorithm, providerName);
		}
		if(keysize != null) {
			kpGen.initialize(keysize);
		}
		
		setKeyPairGenerator(kpGen);
	}
	
	private KeyPairGenerator kpGen;
	
	public KeyPairGenerator getKeyPairGenerator() {
		return cloneKeyPairGenerator();
	}
	
	public void setKeyPairGenerator(KeyPairGenerator kpGen) {
		if(kpGen == null) {
			throw new NullPointerException("kpGen can't be null");
		}
		this.kpGen = kpGen;
		super.initController();
	}
}
