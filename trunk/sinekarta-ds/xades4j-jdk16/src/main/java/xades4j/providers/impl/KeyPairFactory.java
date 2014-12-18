package xades4j.providers.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.Assert;
	
public class KeyPairFactory {
	
	static final Provider bcProvider;
	
	static {
		bcProvider = new BouncyCastleProvider();
		if ( Security.getProvider(bcProvider.getName()) == null ) {
			Security.addProvider ( bcProvider );
		}
	}
	
	public KeyPairFactory(String encryptionAlgorithm) {
		Assert.isTrue ( StringUtils.isNotBlank(encryptionAlgorithm) );
		try {
			kpGen = KeyPairGenerator.getInstance(encryptionAlgorithm, bcProvider);
		} catch (NoSuchAlgorithmException e) {
			// never thrown, using the default provider and algorithm
			throw new RuntimeException(e);
		}
	}
	
	public KeyPair createKeyPair() 
			throws IllegalStateException {
		
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
	
	private KeyPairGenerator kpGen;
}
