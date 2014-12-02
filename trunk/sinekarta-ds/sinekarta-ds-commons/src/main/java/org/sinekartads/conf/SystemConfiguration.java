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
package org.sinekartads.conf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.controller.Controller;

public abstract class SystemConfiguration extends Controller {
	
	// -----
	// --- Singleton implementation
	// -
	
	protected static SystemConfiguration singleton = null;
	
	public static SystemConfiguration getInstance() {
		return getInstance(null);
	}
	
	public static SystemConfiguration getInstance ( Object resources ) 
			throws IllegalStateException, 
				   IllegalArgumentException {
		if(singleton == null) 							throw new NullPointerException("unitialized instance");
		if(resources != null) {
			if(resources instanceof String) {
				singleton.setResources((String)resources);
			} else {
				throw new IllegalArgumentException("invalid resources, expected String or File[]");
			}
		}
		return singleton;
	}
	
	private Properties properties;
	
	protected SystemConfiguration() {
		super(1);
		singleton = this;
	}
	
	// -----
	// --- Configuration
	// -
	
	public void setResources ( 
			String resources) 
					throws IllegalStateException, 
							ConfigurationException {
		
		if(properties == null) 			properties = new Properties();
		super.initController();
		addResources(resources);

	}
	
	public void addResources(String resources) throws IllegalStateException, ConfigurationException {
		URL urlConfiguration = null;
		try {
			verifyControllerState();
			
			// find all CONFIGURATION files (in all classpath)
			Enumeration<URL> enumerationURLconfiguration = null;
			enumerationURLconfiguration = this.getClass().getClassLoader().getResources(resources);
			if(enumerationURLconfiguration.hasMoreElements() == false) {
				throw new ConfigurationException(String.format("no configuration url has been found (%s)", resources));
			}
			// invert the list of url, so i can load files stored in WEB-INF/classes after contained in jar
			List<URL> all = new ArrayList<URL>();
			while ( enumerationURLconfiguration.hasMoreElements() ) {
				urlConfiguration = (URL) enumerationURLconfiguration.nextElement();
				all.add(0,urlConfiguration);
			}
	
			// load inverted list
			for (URL url : all) {
				properties.load(url.openStream());
			}
			
			loadProperties();
		} catch(RuntimeException e) {
			// errors cause the controller closure, a new initialization (setResouces()) will be necessary
			super.closeController();
			throw e;
		} catch (IOException e) {
			throw new ConfigurationException(String.format("unable to load configuration urls (%s)", urlConfiguration), e);
		}
	}
	
	protected abstract void loadProperties() ;
	
	protected String getProperty ( String property ) {
		String value = properties.getProperty ( property );
		if ( StringUtils.isBlank(value) ) {
			value = "";
		}
		return value;
	}
	
	protected String getMandatoryProperty ( String property ) {
		String value = properties.getProperty ( property ); 
		if ( StringUtils.isBlank(value) ) { 		
			throw new ConfigurationException(String.format ( "property %s not found", property));
		}
		return value;
	}
	

	
	
	// -----
	// --- Optional settings
	// -

	public Properties getProperties() {
		super.verifyControllerState();
		return new Properties(properties);
	}
	
	/**
	 * default security provider: BC - BouncyCastle 
	 */
	public String getProviderName() {
		super.verifyControllerState();
		return "BC";
	}
	
	/**
	 * default signature algorithm: SHA256 
	 */
	public DigestAlgorithm getDigestAlgorithm() {
		super.verifyControllerState();
		return DigestAlgorithm.SHA256;
	}
	
	/**
	 * default signature algorithm: SHA256withRSA 
	 */
	public SignatureAlgorithm getSignatureAlgorithm() {
		super.verifyControllerState();
		return SignatureAlgorithm.SHA256withRSA;
	}
	
}
