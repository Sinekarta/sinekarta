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

package org.sinekartads.exception;

public class PKCS11FinalizationException extends SmartCardAccessException {

	private static final long serialVersionUID = 1L;

	public PKCS11FinalizationException() {
	}

	public PKCS11FinalizationException(String message, Throwable cause) {
		super(message, cause);
	}

	public PKCS11FinalizationException(String message) {
		super(message);
	}

	public PKCS11FinalizationException(Throwable cause) {
		super(cause);
	}

}
