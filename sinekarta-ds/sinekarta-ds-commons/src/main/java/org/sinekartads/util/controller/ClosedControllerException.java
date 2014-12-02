package org.sinekartads.util.controller;

public class ClosedControllerException extends IllegalControllerStateException {

	private static final long serialVersionUID = -6184097719053614377L;

	public ClosedControllerException(String message) {
		super(message);
	}
}