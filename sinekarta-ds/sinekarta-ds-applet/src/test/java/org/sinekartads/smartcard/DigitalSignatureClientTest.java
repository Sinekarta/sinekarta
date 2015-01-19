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

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.sinekartads.exception.SmartCardAccessException;

public class DigitalSignatureClientTest extends TestCase {

	Logger tracer = Logger.getLogger(getClass());
	
	@Test
	public void test ( ) throws Throwable {

		String driver = "fake";
		String pin = "123";
		String alias = "SineKarta"; 

		DigitalSignatureClient digitalSignatureClient = new DigitalSignatureClient(driver, pin, alias);
		try {
			digitalSignatureClient.open();
			System.out.println(digitalSignatureClient.sign("2f265c664c0aa544a5c07b95b2e2e7756b7fddc9f4cdbce23befe35f755fcbf0"));
		} catch (Throwable e) {
			tracer.error(e.getMessage(), e);
			throw e;
		} finally {
			try {
				digitalSignatureClient.close();
			} catch (SmartCardAccessException e) {
				// nothing to do..
			}
		}

	}
}
