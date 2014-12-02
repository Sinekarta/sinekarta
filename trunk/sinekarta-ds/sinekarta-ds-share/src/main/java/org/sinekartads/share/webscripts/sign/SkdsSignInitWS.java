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

import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.request.SkdsDocumentDetailsRequest;
import org.sinekartads.dto.response.SkdsDocumentDetailsResponse;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.dto.share.SignWizardDTO.TsSelection;
import org.sinekartads.model.client.SignatureClient.SignatureClientType;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.util.HexUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public class SkdsSignInitWS extends SignController {
	
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	@Override
	protected void prepareForm (
			WebScriptRequest req, 
			SignWizardDTO dto ) throws AlfrescoException {
		// TODO assert signature status at least EMPTY into a SignatureStatus utility
		DocumentDTO[] documents = dto.getDocuments();
		SignatureDTO signature = dto.getSignature();
		
		// Default initialization at the first form visualization
		if ( ArrayUtils.isEmpty(documents) ) {
			String[] nodeRefs = new String[] {getParameter(req, "nodeRef")};
			boolean applyMark = BooleanUtils.toBoolean(getParameter(req, "applyMark"));
			
			dto.setNodeRefs(nodeRefs);	
			
			// Generate a random hex sessionId and use it to instantiate the signatureClients
			String sessionId = HexUtils.randomHex(16);
			clientFactory.createSignatureClients ( sessionId );
			dto.setSessionId(sessionId);
			
			// Retrieve from the repository-tier the document details
			SkdsDocumentDetailsRequest ddreq = new SkdsDocumentDetailsRequest();
			ddreq.setNodeRefs(nodeRefs);
			SkdsDocumentDetailsResponse ddresp = postJsonRequest ( ddreq, SkdsDocumentDetailsResponse.class );
			documents = ddresp.documentsFromBase64();
			dto.setDocuments(documents);
			
			// Evaluate the general mimeType and extract the document baseName and extension 
			DocumentDTO document;
			String fileName;
			String baseName;
			String extension;
			String mimetype = null;
			boolean sameType = true;
			for ( int i=0; i<documents.length; i++ ) {
				document = documents[i];
				if ( i == 0 ) {
					mimetype = document.getBaseDocument().getMimetype();
				} else {
					sameType &= StringUtils.equalsIgnoreCase ( 
							mimetype, document.getBaseDocument().getMimetype() );
				}
				
				fileName = document.getBaseDocument().getFileName(); 
				baseName = FilenameUtils.getBaseName ( fileName );
				extension =  FilenameUtils.getExtension ( fileName );
				
				document.setBaseName(baseName);
				document.setExtension(extension);
			}
			
			// Choose the "octet-stream" mimeType if there is more than one type 
			if ( sameType ) {
				mimetype = "application/octet-stream";
			}
			dto.setMimetype(mimetype);
			
			// Evaluate the best signatureType for the mimetypes to be signed
			SignCategory signCategory;
			if ( mimetype.equals("application/pdf") ) {
				signCategory = SignCategory.PDF;
			} else if ( mimetype.equals("text/xml") ) {
				signCategory = SignCategory.XML;
			} else {
				signCategory = SignCategory.CMS;
			}
			
			// Set the default tsSelection to NO_TIMESTAMP or DEFAULT_TIMESTAMP 
			if ( applyMark ) {
				dto.setTsSelection(TsSelection.DEFAULT.name());
			} else {
				dto.setTsSelection(TsSelection.NONE.name());
			}
			
			// Init the default tsCredential
			TimeStampRequestDTO timeStampRequest = signature.getTimeStampRequest(); 
			timeStampRequest.setTsUrl ( conf.getTsaUrl() );
			timeStampRequest.setTsUsername ( conf.getTsaUser() );
			timeStampRequest.setTsPassword ( conf.getTsaPassword() );
			
			// Create a default signature
			dto.getSignature().signCategoryToString(signCategory);
			signature.digestAlgorithmToString ( conf.getDigestAlgorithm() );
			signature.signAlgorithmToString ( conf.getSignatureAlgorithm() );
			signature.signingTimeToString(new Date());
			signature.setLocation(getMessage("signature.location"));
			signature.setReason(getMessage("signature.reason"));
			
			// set KEYSTORE as default clientType
			dto.setClientType(SignatureClientType.KEYSTORE.name());
		}
	}

	@Override
	protected void processForm(
			WebScriptRequest req, SignWizardDTO wizard) {
		
		DocumentDTO[] 		documents 		 = wizard.getDocuments();
		SignatureDTO 		signature 		 = wizard.getSignature();
		TimeStampRequestDTO timeStampRequest = signature.getTimeStampRequest();
		TsSelection 		tsSelection 	 = TsSelection.valueOf ( wizard.getTsSelection() );

		String tsUrl 				= timeStampRequest.getTsUrl();
		String tsUsername 			= timeStampRequest.getTsUsername();
		String tsPassword 			= timeStampRequest.getTsPassword();
		
		if ( tsSelection == TsSelection.CUSTOM ) {
			if ( StringUtils.isBlank(tsUrl) ) {
				addFieldError("tsUrl", getMessage("error.mandatory") );
			}
			if ( StringUtils.isBlank(tsUsername) && StringUtils.isNotBlank(tsPassword) ) {
				addFieldError("tsUrl", getMessage("error.wrongAnonymousUser") );
			}
		}
		
		// Load the parameters from the form POST 
		for(DocumentDTO document : documents) {
			if ( StringUtils.isBlank(document.getDestName()) ) {
				addFieldError("destName", getMessage("error.mandatory") );
			}
		}
	}
	
	@Override
	protected String currentForm() {
		return "skdsSignOptions";
	}
}
