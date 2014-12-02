package org.sinekartads.test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sinekartads.util.verification.UserPermissionsLevel;
import org.sinekartads.util.verification.UserPermissionsVerifier;

public abstract class SkdsTestCase extends TestCase {
	
	Logger tracer = Logger.getLogger(SkdsTestCase.class);
	
	// -----
	// --- Test configurations
	// -
	
	/**
	 * Dummy UserPermissionsVerifier implementation which allows test cases to use an user 
	 * verifier in which the permissionsLevel is a customizable externally-set value.
	 * Every JUnit test will be executed by default with SYSTEM_PROCESS permissions.
	 */
	static class FixedPermissionsVerifier extends UserPermissionsVerifier {
		
		// Breaking the singleton protocol
		
		FixedPermissionsVerifier(UserPermissionsLevel permissionsLevel) {
			this.permissionsLevel = permissionsLevel;
		}
		
		UserPermissionsLevel permissionsLevel; 
		
		public UserPermissionsLevel getUserRightsLevel() {
			return permissionsLevel;
		}
		
		public void setUserPermissionsLevel(UserPermissionsLevel permissionsLevel) {
			this.permissionsLevel = permissionsLevel;
		}
	};
	
	// test UserPermissionsVerifier - the user permission level is configurable runtime as needed 
	protected static UserPermissionsVerifier junitPermissionsVerifier = 
			new FixedPermissionsVerifier(UserPermissionsLevel.SYSTEM_PROCESS);
			
		
	/**
	 * Shared JUnit test setup - log4j configuration
	 * <p><b>
	 * Warning! if the configuration file is missing into the src/test/resources directory,  
	 * it will chosen the first one into the path
	 * </b></p> 
	 */
	@Override
	protected void setUp() {
		Security.addProvider(new BouncyCastleProvider());
		URL configUrl = null;
		String customLog4jConfiguration = System.getProperty("log4j.configuration");
		String resource = customLog4jConfiguration;
		if(StringUtils.isBlank(resource)) {
			resource = "log4j.properties";
		} 
		// Warning! if the configuration file is missing into the src/test/resources 
		// directory, it will chosen the first one into the path 
		if(StringUtils.isNotBlank(resource)) {
			configUrl = Loader.getResource(resource);
		} 
		if(configUrl != null) {
			PropertyConfigurator.configure(configUrl);
		} else {
			BasicConfigurator.resetConfiguration();
			BasicConfigurator.configure();
			if(StringUtils.isNotBlank(customLog4jConfiguration)) {
				tracer.error("unable to find the custom log4j.configuration " + customLog4jConfiguration);
			}
		} 
	}
	
	
	
	// -----
	// --- Test resource retrieval - default: src/test/resources/<PACKAGE>/.*
	// -
	
	protected static File getTestResource(Class<? extends SkdsTestCase> clazz, String name) {
		String className = clazz.getName();
		className = className.substring(0, className.lastIndexOf("."));
		String path = "src/test/resources/"+className.replace(".", "/")+"/";		
		File file = new File(path + name);
		return file;
	}
	
	protected File getTestResource(String name) {
		return getTestResource(this.getClass(), name);
	}
	
	
	
	// -----
	// --- Testing credentials retrieval (JENIA.p12 created by KeyStoreGenerationTest)
	// -
	
	protected enum Identity {
		ALESSANDRO 	( "Alessandro De Prato",	"alessandro01" ),
		ANDREA 		( "Andrea Tessaro Porta",	"andrea01"),
		ROBERTO		( "Roberto Colombari", 		"roberto01"),
		;		
		
		private Identity ( String alias, String password ) {
			this.alias = alias;
			this.password = password;
		}
		
		public final String alias;
		public final String password;
	};
	
	public static final char[] KEY_STORE_PASSWORD = "skdscip".toCharArray();
	
	public static final String[][] ALIASES_PASSWORDS = new String[][] {
		{"CN=Alessandro De Prato", 	"alessandro01"}, 
		{"CN=Andrea Tessaro Porta",	"andrea01"}, 
		{"CN=Roberto Colombari", 	"roberto01"}
	};	
	
	private KeyStore keyStore = null;
	private File keyStoreFile;
	
	protected KeyStore getKeyStore() {
		if(keyStore == null) {
			try {
				KeyStore store = KeyStore.getInstance("PKCS12-DEF", "BC");
		        store.load(new FileInputStream(getKeyStoreFile()), KEY_STORE_PASSWORD);
				keyStore = store;
			} catch (Exception e) {
				throw new RuntimeException(e); 
			}
		}
		return keyStore;
	}
	
	protected File getKeyStoreFile() {
		if(keyStoreFile == null) {
			keyStoreFile = new File("../test/resources/JENIA.p12");
			if(keyStoreFile.exists() == false) {
				throw new RuntimeException(String.format("unable to find the keystore: %s", keyStoreFile.getAbsolutePath()));
			}
		} 
		return keyStoreFile;
	}
	
	protected X509Certificate getCertificate(Identity owner) {
		X509Certificate certificate;
		KeyStore keyStore = getKeyStore();
		try {
			String alias = ALIASES_PASSWORDS[owner.ordinal()][0];
			certificate = (X509Certificate)keyStore.getCertificate(alias);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return certificate;
	}
	
	protected PrivateKey getPrivateKey(Identity owner) {
		PrivateKey privateKey;
		KeyStore keyStore = getKeyStore();
		try {
			String alias = ALIASES_PASSWORDS[owner.ordinal()][0];
			char[] password = StringUtils.isNotBlank(ALIASES_PASSWORDS[owner.ordinal()][1]) 
					? ALIASES_PASSWORDS[owner.ordinal()][0].toCharArray() : null;
			privateKey = (PrivateKey)keyStore.getKey(alias, password);
		} catch(Exception e) {
			throw new RuntimeException(e);
		} return privateKey;
	}
}
