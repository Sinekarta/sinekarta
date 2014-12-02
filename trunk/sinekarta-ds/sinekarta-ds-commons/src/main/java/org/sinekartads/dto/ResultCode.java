package org.sinekartads.dto;

public enum ResultCode {

	SUCCESS(200),
	BAD_REQUEST(400),
	UNAUTHORIZED(401),
	REQUEST_TIMEOUT(408),
	INTERNAL_SERVER_ERROR(500),
	NOT_IMPLEMENTED(501),
	INTERNAL_CLIENT_ERROR(550);
	
	private ResultCode ( int code ) {
		this.code = code;
	}
	
	private final int code;

	public String getCode() {
		return Integer.toString(code);
	}
}
