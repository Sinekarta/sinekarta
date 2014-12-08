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
package org.sinekartads.share.webscripts.sign;

import org.apache.log4j.Logger;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPostSignRequest;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPostSignResponse;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.model.client.DigitalSignatureException;
import org.sinekartads.model.client.SignatureClient.SignatureClientCtrl;
import org.sinekartads.model.client.SignatureClient.SignatureClientType;
import org.sinekartads.share.util.AlfrescoException;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public class SkdsSignCallPostSignWS extends BaseSignController {

	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	private static Logger tracer = Logger.getLogger(SkdsSignCallPostSignWS.class);

	@Override
	protected void processData (
			SignWizardDTO dto ) 
					throws AlfrescoException {
		
		try {
			DocumentDTO[] documents = dto.getDocuments();
			String sessionId = dto.getSessionId();
			
			SignatureClientType clientType	= SignatureClientType.valueOf(dto.getClientType());
			SignatureClientCtrl<?> client = clientFactory.getSignatureCtrl(sessionId, clientType);
		
			documents = client.sign(documents);
		
	    	// call the postSign service	
	    	SkdsPostSignRequest postreq = new SkdsPostSignRequest();
	    	postreq.documentsToBase64(documents);
	    	SkdsPostSignResponse dsiresp = postJsonRequest ( postreq, SkdsPostSignResponse.class );
	    	documents = dsiresp.documentsFromBase64();
	    	
	    	// take the updated documentDtos
	    	dto.setDocuments(documents);
    	
		} catch ( DigitalSignatureException e ) {
			tracer.error(e.getMessage(), e);
		}
	}

	@Override
	protected String currentForm() {
		return "skdsSignCallPostSign";
	}
}
