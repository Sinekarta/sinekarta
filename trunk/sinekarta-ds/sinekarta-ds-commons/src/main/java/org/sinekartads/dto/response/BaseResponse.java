/*
 * Copyright (C) 2010 - 2012 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.sinekartads.dto.response;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.util.TemplateUtils;

public class BaseResponse extends BaseDTO {
	
	private static final long serialVersionUID = 7478526792110730390L;

	public static BaseResponse fromJSON(InputStream is) throws IOException {
		
		return (BaseResponse)TemplateUtils.Encoding.deserializeJSON(BaseResponse.class, is);
	}
	
	private String resultCode;
	private String message;
	
	@Override
	public String toXML() throws UnsupportedOperationException {
		
		throw new UnsupportedOperationException ( "only JSON and Base64 are currently supported" );
	}

	public ResultCode resultCodeFromString() {
		if ( StringUtils.isBlank(resultCode) ) 				return null;
		return ResultCode.valueOf(resultCode);
	}

	public void resultCodeToString(ResultCode resultCode) {
		this.resultCode = resultCode.toString();
	}

	
	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
