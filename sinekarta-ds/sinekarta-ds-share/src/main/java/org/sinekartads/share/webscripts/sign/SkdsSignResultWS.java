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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPostSignRequest;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPreSignRequest;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPostSignResponse;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPreSignResponse;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.dto.share.SignWizardDTO.TsSelection;
import org.sinekartads.model.client.DigitalSignatureException;
import org.sinekartads.model.client.SignatureClient.SignatureClientCtrl;
import org.sinekartads.model.client.SignatureClient.SignatureClientType;
import org.sinekartads.share.util.AlfrescoException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public class SkdsSignResultWS extends SignController {

	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	private static Logger tracer = Logger.getLogger(SkdsSignResultWS.class);

	/**
	 * SignApply form preparation method.<br/>
	 * Loads from dto:
	 * <ul>
	 * <li> documents 
	 * <li> certificated signature (CS)
	 * <li> clientType
	 * </ul>
	 * The execution changes depending by the chosen clientType:
	 * <ul>
	 * <li>KEYSTORE - everything is already available from Alfresco: the method perform
	 * the preSign and then the form will simply display a waiting message for a while,  
	 * in conformity with the other cases
	 * <li>SMARTCARD - the method will perform the preSign with the previously loaded 
	 * certificate and finally display the signature control form; the applet will expect to 
	 * detect the smartCard and require to Share the digest and credentials to be used, it applies
	 * then the digitalSignature and connect with the SmartCardClient to send it the signature;
	 * during the form processing phase, the script will perform the postSign by using the
	 * digitalSignature received in this way.
	 * <li>SIGNATURE_WEBSERVICE - to be implemented 
	 * </ul>
	 * During the preSign signature phase through each document will receive a Digest 
	 * SignatureInfo instance (DS), founding the digestInfo field populated with the
	 * digest of data + chain + signOptions. The postSign phase will so ignore the signature
	 * template used till here. 
	 * <br/>
	 * Send to form:
	 * <ul>
	 * <li> the clientType
	 * </ul>
	 * @throws AlfrescoException 
	 */
	@Override
	protected void prepareForm(
			WebScriptRequest req, SignWizardDTO dto ) throws AlfrescoException {

		DocumentDTO[] documents     	= dto.getDocuments();
		SignatureDTO chainSignature		= dto.getSignature();
		TimeStampRequestDTO timeStampRequest = chainSignature.getTimeStampRequest();
		TsSelection tsSelection = TsSelection.valueOf(dto.getTsSelection());
				
		switch ( tsSelection ) {
			case NONE: {
				chainSignature.setTimeStampRequest ( null );
			} 
			case DEFAULT: {
				timeStampRequest.setTsUrl ( conf.getTsaUrl() );
				timeStampRequest.setTsUsername ( conf.getTsaUser() );
				timeStampRequest.setTsPassword ( conf.getTsaPassword() );
			}
			default: {	}
		}
		
		for ( DocumentDTO document : documents ) {
			document.setSignatures ( 
					(SignatureDTO[]) ArrayUtils.add ( 
							document.getSignatures(), chainSignature ) );
		}
		
		// Execute the pre-sign to the documents.
		SkdsPreSignRequest prereq = new SkdsPreSignRequest();
    	prereq.documentsToBase64(documents);
    	SkdsPreSignResponse dsiresp = postJsonRequest ( prereq, SkdsPreSignResponse.class );
    	documents = dsiresp.documentsFromBase64();
		dto.setDocuments(documents);
	}

	@Override
	protected void processForm(
			WebScriptRequest req, SignWizardDTO dto) throws AlfrescoException {
		
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
		return "skdsSignResult";
	}
}
