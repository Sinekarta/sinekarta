package org.sinekartads.util.controller;

public class FinalizedControllerException extends IllegalControllerStateException {

	private static final long serialVersionUID = -6184097719053614377L;

	public FinalizedControllerException(String message) {
		super(message);
	}
}