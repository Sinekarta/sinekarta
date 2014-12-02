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
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPostSignRequest;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPreSignRequest;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPostSignResponse;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPreSignResponse;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.dto.share.SignWizardDTO.TsSelection;
import org.sinekartads.model.client.KeyStoreClient.KeyStoreClientCtrl;
import org.sinekartads.model.client.SignatureClient.SignatureClientCtrl;
import org.sinekartads.model.client.SignatureClient.SignatureClientType;
import org.sinekartads.share.util.AlfrescoException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public class SkdsSignClientWS extends SignController {
	
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	/**
	 * SignOptions form preparation method.<br>
	 * Loads from dto and send to form:
	 * <ul>
	 * <li> the sessionId
	 * <li> the current clientType
	 * <li> the current keyStore if any, or the personal one if any, the first available otherwise
	 * <li> the smartCard detection status
	 * <li> smartCardPin
	 * <li> keyStorePin 
	 * </ul>
	 * Evaluates, store into dto and send to form:
	 * <ul>
	 * <li> the availableKeyStores, visible to the current user authorities 
	 * </ul>
	 * 
	 */
	@Override
	protected void prepareForm (
			WebScriptRequest req, 
			SignWizardDTO dto ) {

//		String sessionId = dto.getSessionId();
//		SignatureClientType clientType = SignatureClientType.valueOf ( dto.getClientType() );
//		String keyStorePin = dto.getKeyStorePin();
//
//		KeyStoreClientCtrl keyStoreClient = clientFactory.getKeyStoreCtrl ( sessionId );
//		SmartCardClientCtrl smartCardClient = clientFactory.getSmartCardCtrl ( sessionId );

//		model.put("clientType", clientType);
//		model.put("keyStorePin", keyStorePin);
//		model.put("availableKeyStores", keyStoreClient.getAvailableKeyStores());
//		model.put("smartCardDetected", smartCardClient.getSmarCardStatus());
	}
	
	@Override
	protected void performExtraOperation( 
			WebScriptRequest req, SignWizardDTO dto, String formOperation ) 
					throws AlfrescoException  {
		
		String sessionId = dto.getSessionId();
		String keyStorePin = dto.getKsPin();
		KeyStoreClientCtrl keyStoreClient = clientFactory.getKeyStoreCtrl ( sessionId );
		dto.setKsAliases ( keyStoreClient.openKeyStore(keyStorePin) );
	}

	@Override
	protected void processForm(
			WebScriptRequest req, SignWizardDTO dto) {
		
		SignatureDTO signature = dto.getSignature();
		String sessionId = dto.getSessionId();
		String ksUserAlias = dto.getKsUserAlias();
		String ksUserPassword = dto.getKsUserPassword();
		KeyStoreClientCtrl keyStoreClient = clientFactory.getKeyStoreCtrl ( sessionId );
		signature.setHexCertificateChain( 
				keyStoreClient.selectIdentity(ksUserAlias, ksUserPassword) );
		
		DocumentDTO[] documents     	= dto.getDocuments();
		SignatureDTO chainSignature		= dto.getSignature();
		TimeStampRequestDTO timeStampRequest = chainSignature.getTimeStampRequest();
		TsSelection tsSelection = TsSelection.valueOf(dto.getTsSelection());
				
		switch ( tsSelection ) {
			case NONE: {
				timeStampRequest.setTsUrl ( "" );
				timeStampRequest.setTsUsername ( "" );
				timeStampRequest.setTsPassword ( "" );
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
		try {
			SkdsPreSignRequest prereq = new SkdsPreSignRequest();
	    	prereq.documentsToBase64(documents);
	    	SkdsPreSignResponse dsiresp = postJsonRequest ( prereq, SkdsPreSignResponse.class );
	    	documents = dsiresp.documentsFromBase64();
			dto.setDocuments(documents);
		} catch(AlfrescoException e) {
			throw new RuntimeException(e);
		}
		
		try {
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
    	
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected String currentForm() {
		return "skdsSignClient";
	}
}
