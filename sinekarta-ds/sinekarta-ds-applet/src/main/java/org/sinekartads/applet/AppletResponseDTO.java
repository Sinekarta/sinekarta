package org.sinekartads.applet;

import java.io.Serializable;


public class AppletResponseDTO implements Serializable {

	private static final long serialVersionUID = -4022334556491609402L;

	public static final String SUCCESS = "SUCCESS";
	public static final String ERROR = "ERROR";
	
	
	
	// -----
	// --- Data transport protocol
	// -
	
	private String result;
	private String resultCode;
	private String errorMessage;
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}