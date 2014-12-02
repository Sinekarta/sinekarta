package org.sinekartads.util.controller;

public class DisabledControllerException extends IllegalControllerStateException {

	private static final long serialVersionUID = -6184097719053614377L;

	public DisabledControllerException(String message) {
		super(message);
	}
}