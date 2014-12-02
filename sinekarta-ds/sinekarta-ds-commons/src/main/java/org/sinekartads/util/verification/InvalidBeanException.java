package org.sinekartads.util.verification;

public class InvalidBeanException extends VerificationException {

	private static final long serialVersionUID = -6088585888358044715L;

	public InvalidBeanException(Object target) {
		this(target, "verification failed");
	}
	
	public InvalidBeanException(Object target, String message) {
		super(message);
		this.target = target;
	}
		
	final Object target;
	
	public Object getTarget() {
		return target;
	}
	
}