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
package org.sinekartads.core;

import java.io.File;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sinekartads.conf.SystemConfiguration;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;

public class CoreConfiguration extends SystemConfiguration {
	
	// -----
	// --- Singleton implementation
	// -
	
	public static CoreConfiguration getInstance() {
		if ( singleton == null ) {
			return new CoreConfiguration();
		}
		return (CoreConfiguration)singleton;
	}
	
	
	// -----
	// --- Provided properties
	// -
	
	public static final String PROVIDER_NAME 		= "PROVIDER_NAME";
	public static final String SIGNATURE_ALGORITHM	= "SIGNATURE_ALGORITHM";
	public static final String DIGEST_ALGORITHM		= "DIGEST_ALGORITHM";
	public static final String TEMPORARY_FOLDER		= "TEMPORARY_FOLDER";
	
	
	
	// -----
	// --- Configuration
	// -
	
	CoreConfiguration() {
		if ( Security.getProvider("BC") == null ) {
			Security.addProvider(new BouncyCastleProvider());
		}
		temporaryFolder = new File ( 
				getClass().getClassLoader().getResource ( "." ).getFile() );
		if ( !temporaryFolder.exists() ) {
			temporaryFolder.mkdirs();
		}
	}
	
	File temporaryFolder;
	
	@Override
	protected void loadProperties() {
	}
	

	
	// -----
	// --- Alfresco-tier settings
	// -

		
	public String getProviderName() {
		return "BC";
	}
	
	public DigestAlgorithm getDigestAlgorithm() {
		return DigestAlgorithm.SHA256;
	}

	public SignatureAlgorithm getSignatureAlgorithm() {
		return SignatureAlgorithm.SHA256withRSA;
	}

	public File getTemporaryFolder() {
		return temporaryFolder;
	}

}
