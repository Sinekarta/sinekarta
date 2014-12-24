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

import java.io.IOException;

public class SmartCardAccessException extends IOException {

	private static final long serialVersionUID = -5194531013887291991L;
	
	private Throwable cause;
			
	public SmartCardAccessException() {
		super();
	}

	public SmartCardAccessException(String message) {
		super(message);
	}

	public SmartCardAccessException(Throwable cause) {
		this.cause = cause;
	}
	
	public SmartCardAccessException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}
}
