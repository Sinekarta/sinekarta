package org.sinekartads.util.verification;


public class InvalidPropertyException extends InvalidBeanException {

	private static final long serialVersionUID = -7577742935045194760L;

	public InvalidPropertyException(Object target, String property) {
		this("invalid property value: %s", target, property);
	}
	
	public InvalidPropertyException(String messageFormat, Object target, String property) {
		super(target, String.format(messageFormat, property));
		this.property = property;
	}
	
	final String property;
	
	public String getProperty() {
		return property;
	}
}