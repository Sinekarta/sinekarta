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

import java.math.BigInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPreSignRequest;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPreSignResponse;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.dto.share.SignWizardDTO.TsSelection;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.util.TemplateUtils;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.util.Assert;

public class SkdsSignCallPreSignWS extends BaseSignController {

	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	@Override
	protected void processData (
			SignWizardDTO dto ) 
					throws AlfrescoException {

		DocumentDTO[] documents     = dto.getDocuments();
		int last = dto.getDocuments()[0].getSignatures().length - 1;
		SignatureDTO signature = dto.getDocuments()[0].getSignatures()[last];
		Assert.isTrue ( ArrayUtils.isNotEmpty(signature.getHexCertificateChain()) );
		
		// Clone the chainSignature in order to update it without impacting on the DTO
		TsSelection tsSelection = TsSelection.valueOf(dto.getTsSelection());
		SignatureDTO chainSignature	= TemplateUtils.Instantiation.clone ( signature );
		TimeStampRequestDTO timeStampRequest = chainSignature.getTimeStampRequest();
		timeStampRequest.timestampDispositionToString(SignDisposition.TimeStamp.ENVELOPING);
		timeStampRequest.messageImprintAlgorithmToString(DigestAlgorithm.SHA256);
		timeStampRequest.nounceToString(BigInteger.ONE);
				
		// Update the nested timeStampRequest depending on the tsSelection
		switch ( tsSelection ) {
			case NONE: {
				timeStampRequest.setTsUrl ( "" );
				timeStampRequest.setTsUsername ( "" );
				timeStampRequest.setTsPassword ( "" );
				break;
			} 
			case DEFAULT: {
				timeStampRequest.setTsUrl ( conf.getTsaUrl() );
				timeStampRequest.setTsUsername ( conf.getTsaUser() );
				timeStampRequest.setTsPassword ( conf.getTsaPassword() );
				break;
			}
			default: {	}
		}
		
		// Append the updated chainSignature to the documents' signatures, at the last position:
		//		the server-tier webScripts and the signature client implementations will consider 
		//		this one as the signature to be applied 
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
	protected int currentStep() {
		return STEP_PRESIGN;
	}
}
