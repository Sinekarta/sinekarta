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
package org.sinekartads.smartcard;

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
