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
package org.sinekartads.share.webscripts.verify;

import org.sinekartads.dto.share.VerifyWizardDTO;
import org.sinekartads.share.util.AlfrescoException;
import org.springframework.extensions.webscripts.WebScriptRequest;


public class SkdsVerifyResultWS extends BaseVerifyWS {

	@Override
	protected void prepareForm(
			WebScriptRequest req, VerifyWizardDTO wizardDto ) throws AlfrescoException {
		
//		String verifyOperation = wizardDto.getVerifyOperation();
////		String pathChoice = wizardDto.getPathChoice();
////		String extractionParent = wizardDto.getExtractionParent();	
//		
//		VerifyDTO verifyInfoDto = null;
//		String destRef = "";
//		String destName = "";
//		try {	
//			DocumentDTO document = wizardDto.getDocument();
//			
//			// TODO Evaluate the desired extractionParent 
////			if(StringUtils.equals(verifyOperation, "verifyAndExtract")) {
////				if(StringUtils.equals(pathChoice, "sameFolder")) {
////					extractionParent = document.getParentRef();
////				}
////			} else {
////				extractionParent = null;
////			}
//			
//			// Process the verify and return the verifyInfo and the downloadable destRef
//			SkdsVerifyRequest sdvreq = new SkdsVerifyRequest();
//			sdvreq.documentToBase64(document);
//			SkdsVerifyResponse ddresp = postJsonRequest ( sdvreq, SkdsVerifyResponse.class );
//			verifyInfoDto = BaseDTO.deserializeBase64 ( VerifyDTO.class, ddresp.getVerifyInfoBase64() );
//			Arrays.sort(verifyInfoDto.getSignatures(), new BeanComparator("signingDate"));
//			destRef = ddresp.getDestRef();
//			
//			// Apply the custom formats to verifyInfoDto
//			DTOFormatter dtoFormatter = getDtoFormatter();
//			dtoFormatter.format(verifyInfoDto);
//		} finally {
////			model.put("destRef", destRef);
////			model.put("destName", destName);
////			model.put("verifyInfoDto", verifyInfoDto);
////			model.put("verifyOperation", verifyOperation);
//		}
	}
 
	@Override
	protected void processForm(
			WebScriptRequest req, VerifyWizardDTO dto) {
	}

	@Override
	protected String currentForm() {
		return "skdsVerifyResult";
	}
}