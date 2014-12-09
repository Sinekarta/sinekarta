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

import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.model.client.SmartCardClient.SmartCardClientCtrl;
import org.sinekartads.share.util.AlfrescoException;

public class SkdsSignSetDigitalSignatureWS extends BaseSignController {
	
	@Override
	protected void processData (
			SignWizardDTO dto ) 
					throws AlfrescoException {
		
		int LAST = dto.getDocuments()[0].getSignatures().length - 1;
		SignatureDTO signature = dto.getDocuments()[0].getSignatures()[LAST];
		String sessionId = dto.getSessionId();
		SmartCardClientCtrl keyStoreClient = clientFactory.getSmartCardCtrl ( sessionId );
		keyStoreClient.setDigitalSignature ( signature.digitalSignatureFromHex() );
	}

	@Override
	protected WizardStep currentStep() {
		return STEP_DIGSIG;
	}
}