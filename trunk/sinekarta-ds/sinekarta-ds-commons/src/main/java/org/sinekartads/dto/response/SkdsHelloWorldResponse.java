package org.sinekartads.dto.response;

public class SkdsHelloWorldResponse extends BaseResponse {

	private static final long serialVersionUID = 8333476611041956260L;
	
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
