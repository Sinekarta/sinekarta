package org.sinekartads.util.verification;


public class MissingPropertyException extends InvalidPropertyException {

	private static final long serialVersionUID = -7577742935045194760L;

	public MissingPropertyException(Object target, String missingProperty) {
		super("the bean is missing the mandatory property: %s", target, missingProperty);
	}
		
}