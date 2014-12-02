package org.sinekartads.model.client;

public class DigitalSignatureException extends Exception {

	private static final long serialVersionUID = -6005908010582135028L;

	public DigitalSignatureException() { }
	
	public DigitalSignatureException(String message) {
		super(message);
	}
	
	public DigitalSignatureException(Throwable cause) {
		super(cause);
	}
	
	public DigitalSignatureException(String message, Throwable cause) {
		super(message, cause);
	}
}