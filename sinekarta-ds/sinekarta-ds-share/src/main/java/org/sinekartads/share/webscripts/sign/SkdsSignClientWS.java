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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.util.x509.X509Utils;
import org.springframework.util.Assert;

public class SkdsSignClientWS extends BaseSignController {

	@Override
	protected void processData(
			SignWizardDTO dto)
					throws AlfrescoException {
		
		try {
			Assert.notNull ( dto.getSignature() );
			Assert.isTrue ( ArrayUtils.isNotEmpty(dto.getSignature().getHexCertificateChain()) );
			for ( String hexCertificate : dto.getSignature().getHexCertificateChain() ) {
				if ( StringUtils.isBlank(hexCertificate) ) {
					throw new NullPointerException();
				}
				X509Utils.rawX509CertificateFromHex ( hexCertificate );
			}
		} catch(Exception e) {
			processError ( getMessage("error.invalidCertificateChain") );
		}
		
//		String sessionId = dto.getSessionId();
//		DocumentDTO[] documents     	= dto.getDocuments();
//		SignatureDTO chainSignature		= dto.getSignature();
//		TimeStampRequestDTO timeStampRequest = chainSignature.getTimeStampRequest();
//		TsSelection tsSelection = TsSelection.valueOf(dto.getTsSelection());
//				
//		switch ( tsSelection ) {
//			case NONE: {
//				timeStampRequest.setTsUrl ( "" );
//				timeStampRequest.setTsUsername ( "" );
//				timeStampRequest.setTsPassword ( "" );
//				break;
//			} 
//			case DEFAULT: {
//				timeStampRequest.setTsUrl ( conf.getTsaUrl() );
//				timeStampRequest.setTsUsername ( conf.getTsaUser() );
//				timeStampRequest.setTsPassword ( conf.getTsaPassword() );
//				break;
//			}
//			default: {	}
//		}
//		
//		for ( DocumentDTO document : documents ) {
//			document.setSignatures ( 
//					(SignatureDTO[]) ArrayUtils.add ( 
//							document.getSignatures(), chainSignature ) );
//		}
//		
//		// Execute the pre-sign to the documents.
//		try {
//			SkdsPreSignRequest prereq = new SkdsPreSignRequest();
//	    	prereq.documentsToBase64(documents);
//	    	SkdsPreSignResponse dsiresp = postJsonRequest ( prereq, SkdsPreSignResponse.class );
//	    	documents = dsiresp.documentsFromBase64();
//			dto.setDocuments(documents);
//		} catch(AlfrescoException e) {
//			throw new RuntimeException(e);
//		}
//		
//		try {
//			SignatureClientType clientType	= SignatureClientType.valueOf(dto.getClientType());
//			SignatureClientCtrl<?> client = clientFactory.getSignatureCtrl(sessionId, clientType);
//		
//			documents = client.sign(documents);
//		
//	    	// call the postSign service	
//	    	SkdsPostSignRequest postreq = new SkdsPostSignRequest();
//	    	postreq.documentsToBase64(documents);
//	    	SkdsPostSignResponse dsiresp = postJsonRequest ( postreq, SkdsPostSignResponse.class );
//	    	documents = dsiresp.documentsFromBase64();
//	    	
//	    	// take the updated documentDtos
//	    	dto.setDocuments(documents);
//    	
//		} catch ( Exception e ) {
//			throw new RuntimeException(e);
//		}
	}
	
	@Override
	protected String currentForm() {
		return "skdsSignClient";
	}
}
