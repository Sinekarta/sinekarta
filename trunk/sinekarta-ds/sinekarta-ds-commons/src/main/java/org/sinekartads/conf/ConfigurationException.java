package org.sinekartads.conf;

public class ConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -2869013267622384852L;

	public ConfigurationException() {

	}

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}
}