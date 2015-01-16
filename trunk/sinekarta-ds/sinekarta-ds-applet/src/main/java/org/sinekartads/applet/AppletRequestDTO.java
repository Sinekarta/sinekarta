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
package org.sinekartads.applet;

import java.io.Serializable;


public class AppletRequestDTO implements Serializable {

	private static final long serialVersionUID = -4022334556491609402L;
	
	private String driver;
	private String pin;
	private String alias;
	private String hexDigest;

	public AppletRequestDTO() {
		
	}
	
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getHexDigest() {
		return hexDigest;
	}

	public void setHexDigest(String hexDigest) {
		this.hexDigest = hexDigest;
	}
}