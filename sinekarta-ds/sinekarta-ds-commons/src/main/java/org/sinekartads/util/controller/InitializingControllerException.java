package org.sinekartads.util.controller;

public class InitializingControllerException extends IllegalControllerStateException {

	private static final long serialVersionUID = -6184097719053614377L;

	public InitializingControllerException(String message) {
		super(message);
	}
}