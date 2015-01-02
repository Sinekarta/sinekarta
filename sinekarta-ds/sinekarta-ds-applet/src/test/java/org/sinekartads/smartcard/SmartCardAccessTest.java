/*
 * Copyright (C) 2014 - 2015 Jenia Software.
 *
 * This file is part of Sinekarta-ds
 *
 * Sinekarta-ds is Open SOurce Software: you can redistribute it and/or modify
 * it under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sinekartads.smartcard;

import java.security.cert.X509Certificate;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.sinekartads.exception.InvalidPKCS11DriverException;
import org.sinekartads.exception.SmartCardAccessException;
import org.sinekartads.exception.SmartCardReaderNotFoundException;
import org.sinekartads.utils.HexUtils;
import org.sinekartads.utils.X509Utils;

public class SmartCardAccessTest extends TestCase {

	Logger tracer = Logger.getLogger(getClass());
	
	@Test
	public void test ( ) {

		// The allowedDrivers are received from the html page
//		String[] knownDrivers = new String[] { "libbit4ipki.so", "libASEP11.so" };
		String pin = "18071971";
		// Input: fingerPrint - arbitrary 64 byte sequence (SHA256)
		byte[] fingerPrint = HexUtils.decodeHex("2f265c664c0aa544a5c07b95b2e2e7756b7fddc9f4cdbce23befe35f755fcbf0");
		// Output: digitalSignature evaluated by the smartCard
		byte[] digitalSignature;
		
		// SmartCard wrapper
		SmartCardAccess sca = null;
		// Alias available into the smartCard
		String[] aliases;
		// Chosen signing alias
		String alias;
		// Chosen signing certificate
		X509Certificate signingCertificate;
		
		try {
			// Applet init - receives from share the known driver name list and returns the 
			// 		available on the array with those which have been found into the library path
			//		Generate then the smartCard wrapper assigning to it the first matching driver 
			try {
				sca = new SmartCardAccess();
				
//				String[] matchingDrivers = SmartCardUtils.detectDrivers(knownDrivers);
//				StringBuilder buf = new StringBuilder();
//				for ( String driver : matchingDrivers ) {
//					buf.append(driver).append(" ");
//				}
//				tracer.info(String.format ( "matching drivers:    %s", buf.toString() ));
//				sca.selectDriver ( matchingDrivers[0] );
				sca.selectDriver("libbit4ipki.so");
			} catch(Throwable e) {
//				throw new RuntimeException(e);
				return;
			}
			
			// SmartCard login - received the pin, return the signing alias list
			try {
				aliases = sca.loginAndCertificateList( pin );
				sca.logout();
				
				StringBuilder buf = new StringBuilder();
				for ( String a : aliases ) {
					buf.append(a).append(" ");
				}
				alias = aliases[0];
				tracer.info(String.format ( "available aliases:   %s", buf ));
				tracer.info(String.format ( "signing alias:       %s", alias ));
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			
			// Certificate selection - received the alias, choose the privateKey and return the certificate
			try {
				sca.login(pin);
				signingCertificate = sca.selectCertificate ( alias );
				sca.logout();
				tracer.info(String.format ( "signing certificate: %s", signingCertificate ));
				tracer.info(String.format ( "certificate hex:     %s", X509Utils.rawX509CertificateToHex(signingCertificate) ));
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			
			// Digital Signature evaluation - sign the received digest with the smartCard
			try {
				sca.login ( pin );
				tracer.info(String.format ( "fingerPrint:         %s", HexUtils.encodeHex(fingerPrint) ));
				digitalSignature = sca.signFingerPrint(fingerPrint);
				tracer.info(String.format ( "digitalSignature:    %s", HexUtils.encodeHex(digitalSignature) ));
				sca.logout();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		} catch(Exception e) {
			Throwable cause = e.getCause();
			if ( cause != null && (cause instanceof SmartCardReaderNotFoundException || cause instanceof InvalidPKCS11DriverException) ) {
				tracer.info("unable to run the test because the smart card reader has not been found");
			}
		} finally {
			// SmartCard finalization - called when the wizard steps to the results form
			try {
				sca.close();
			} catch (SmartCardAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
