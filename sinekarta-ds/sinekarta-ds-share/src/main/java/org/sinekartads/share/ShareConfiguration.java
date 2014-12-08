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
package org.sinekartads.share;

import org.sinekartads.conf.ConfigurationException;
import org.sinekartads.conf.SystemConfiguration;

/**
 * this class is a singleton! use Configuration.getInstance() to get current instance
 * Configuration is read from alfresco/extension/sinekarta-repository.properties in classloader
 * The file found in classes folder is the last read
 * 
 * 
 * @author andrea.tessaro
 *
 */
public class ShareConfiguration extends SystemConfiguration {
	
	// -----
	// --- Singleton implementation
	// -
	
	public static ShareConfiguration getInstance() {
		return (ShareConfiguration)singleton;
	}
	
	
	
	// -----
	// --- Provided properties
	// -
	
	// General - optional properties
	public static final String SMARTCARD_DRIVERS	= "SMARTCARD_DRIVERS";
	public static final String TSA_URL 				= "TSA_URL";
	public static final String TSA_USER 			= "TSA_USER";
	public static final String TSA_PASSWORD 		= "TSA_PASSWORD";
	
	
	
	// -----
	// --- Configuration
	// -
	
	@Override
	protected void loadProperties() {

		try {
			String property = getMandatoryProperty ( SMARTCARD_DRIVERS );
			String[] drivers = property.split(",");
			String[] nameDescr;
			
			driverNames = new String [ drivers.length+1 ];
			driverDescriptions = new String [ drivers.length+1 ];
			for ( int i=0; i<drivers.length; i++ ) {
				nameDescr = drivers[i].split(":");
				driverNames[i+1] = nameDescr[0];
				driverDescriptions[i+1] = nameDescr[1];
			}
			driverNames[0] = "fake";
			driverDescriptions[0] = "fake smartcard - pin: \"fake\"";
			
			tsaUrl = getProperty ( TSA_URL );
			tsaUser = getProperty ( TSA_USER );
			tsaPassword = getProperty ( TSA_PASSWORD );
		} catch(ConfigurationException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigurationException(String.format (
					"unable to parse sinekarta-repository.properties (%s: %s)",
					e.getClass().getName(), e.getMessage() ), e);
		}
	}
	
	
	
	
	// -----
	// --- Mandatory settings
	// -

	private String[] driverNames;
	private String[] driverDescriptions;
	private String tsaUrl;
	private String tsaUser;
	private String tsaPassword;
	
	public String[] getDriverNames() {
		return driverNames;
	}

	public void setDriverNames(String[] driverNames) {
		this.driverNames = driverNames;
	}
	
	public String[] getDriverDescriptions() {
		return driverDescriptions;
	}

	public void setDriverDescriptions(String[] driverDescriptions) {
		this.driverDescriptions = driverDescriptions;
	}

	public String getTsaUser() throws IllegalStateException {
		super.verifyControllerState();
		return tsaUser;
	}

	public String getTsaPassword() throws IllegalStateException {
		super.verifyControllerState();
		return tsaPassword;
	}

	public String getTsaUrl() throws IllegalStateException {
		super.verifyControllerState();
		return tsaUrl;
	}
}
