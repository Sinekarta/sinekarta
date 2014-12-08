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

import org.apache.commons.lang.StringUtils;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.dto.share.SignWizardDTO.TsSelection;
import org.sinekartads.share.util.AlfrescoException;

public class SkdsSignOptionsWS extends BaseSignController {
	
	@Override
	protected void processData (
			SignWizardDTO wizardData ) 
					throws AlfrescoException {
		
		DocumentDTO[] 		documents 		 = wizardData.getDocuments();
		SignatureDTO 		signature 		 = wizardData.getSignature();
		TimeStampRequestDTO timeStampRequest = signature.getTimeStampRequest();
		TsSelection 		tsSelection 	 = TsSelection.valueOf ( wizardData.getTsSelection() );

		String tsUrl 				= timeStampRequest.getTsUrl();
		String tsUsername 			= timeStampRequest.getTsUsername();
		String tsPassword 			= timeStampRequest.getTsPassword();
		
		if ( tsSelection == TsSelection.CUSTOM ) {
			if ( StringUtils.isBlank(tsUrl) ) {
				wizardData.addFieldError("tsUrl", getMessage("error.mandatory") );
			}
			if ( StringUtils.isBlank(tsUsername) && StringUtils.isNotBlank(tsPassword) ) {
				wizardData.addFieldError("tsUrl", getMessage("error.wrongAnonymousUser") );
			}
		}
		
		// Load the parameters from the form POST 
		for(DocumentDTO document : documents) {
			if ( StringUtils.isBlank(document.getDestName()) ) {
				wizardData.addFieldError("destName", getMessage("error.mandatory") );
			}
		}
	}
	
	@Override
	protected WizardStep currentStep() {
		return STEP_OPTIONS;
	}
}
