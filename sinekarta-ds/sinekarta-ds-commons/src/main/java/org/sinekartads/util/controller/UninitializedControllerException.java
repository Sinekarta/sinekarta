package org.sinekartads.util.controller;

public class UninitializedControllerException extends IllegalControllerStateException {

	private static final long serialVersionUID = -6184097719053614377L;

	public UninitializedControllerException(String message) {
		super(message);
	}
}