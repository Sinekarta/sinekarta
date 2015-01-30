package org.sinekartads.dto.request;

public class SkdsHelloWorldRequest extends BaseRequest {

	private static final long serialVersionUID = 8333476611041956260L;
	
	private String name;
	private String messageFormat;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getMessageFormat() {
		return messageFormat;
	}

	public void setMessageFormat(String messageFormat) {
		this.messageFormat = messageFormat;
	}
}