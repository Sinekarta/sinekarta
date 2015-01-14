/*
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
package org.sinekartads.share.webscripts;

import java.io.Serializable;
import java.util.ResourceBundle;

import org.sinekartads.dto.request.BaseRequest;
import org.sinekartads.dto.response.BaseResponse;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.share.util.JavaWebscriptTools;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public abstract class BaseWS extends DeclarativeWebScript {
	
	protected ConnectorService connectorService;
    
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	// ----- 
	// --- Webscript protocol
	// -
	
	public static Serializable getRequestParameter(WebScriptRequest req, String key) {
        RequestContext rc = ThreadLocalRequestContext.getRequestContext();
        return rc.getParameter(key);
	}
	
	public <SkdsResponse extends BaseResponse> SkdsResponse postJsonRequest (
			BaseRequest request, 
			Class<SkdsResponse> responseClass) throws AlfrescoException {
		return JavaWebscriptTools.postJsonRequest(request, responseClass, connectorService); 
	}
	
	protected String getMessage(String messageId) {
		String message = null;
		try {
			if(getResources().containsKey(messageId)) {
				message = getResources().getString(messageId);
			} else {
				message = ResourceBundle.getBundle("alfresco/messages/skds-commons").getString(messageId);
			}
		} catch(Exception e) {
			message = null;
		}
		if(message == null) {
			message = messageId;
		}
		return message;
	}

	protected String getParameter ( WebScriptRequest req, String parameter ) {
		return (String) JavaWebscriptTools.getRequestParameter(req, parameter);
	}
	
	protected <T> T getParameter ( WebScriptRequest req, String parameter, Class<T> tClass ) {
		return tClass.cast ( JavaWebscriptTools.getRequestParameter(req, parameter) );
	}
}