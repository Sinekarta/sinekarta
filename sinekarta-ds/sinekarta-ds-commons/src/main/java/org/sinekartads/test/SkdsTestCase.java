package org.sinekartads.test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

public abstract class SkdsTestCase extends TestCase {
	
	Logger tracer = Logger.getLogger(SkdsTestCase.class);
	
	// -----
	// --- Test configurations
	// -
		
	/**
	 * Shared JUnit test setup - log4j configuration
	 * <p><b>
	 * Warning! if the configuration file is missing into the src/test/resources directory,  
	 * it will chosen the first one into the path
	 * </b></p> 
	 */
	@Override
	protected void setUp() {
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
	
}
