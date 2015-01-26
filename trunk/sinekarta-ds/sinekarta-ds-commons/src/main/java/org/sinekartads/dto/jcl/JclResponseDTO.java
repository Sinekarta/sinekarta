package org.sinekartads.dto.jcl;

import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.ResultCode;

public class JclResponseDTO extends BaseDTO {

	private static final long serialVersionUID = -4022334556491609402L;
	
	// -----
	// --- Data transport protocol
	// -
	
	private String result;
	private String resultCode;
	private String errorMessage;
	
	public ResultCode resultCodeFromString() {
		return ResultCode.valueOf(resultCode);
	}
	
	public void resultCodeToString(ResultCode resultCode) {
		this.resultCode = resultCode.name();
	}
	
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