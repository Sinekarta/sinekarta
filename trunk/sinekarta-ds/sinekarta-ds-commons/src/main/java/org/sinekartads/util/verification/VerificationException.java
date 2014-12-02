package org.sinekartads.util.verification;

public class VerificationException extends /*IllegalArgument*/Exception {

	private static final long serialVersionUID = 344858925672483899L;

	public VerificationException(String message) {
		super(message);
	}

	public VerificationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public VerificationException(Throwable cause) {
		super(cause);
	}
}