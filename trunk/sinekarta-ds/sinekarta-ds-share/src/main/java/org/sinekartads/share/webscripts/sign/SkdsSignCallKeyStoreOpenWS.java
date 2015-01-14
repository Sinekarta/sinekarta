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

import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.model.client.KeyStoreClient.KeyStoreClientCtrl;
import org.sinekartads.share.util.AlfrescoException;

public class SkdsSignCallKeyStoreOpenWS extends BaseSignController {
	
	@Override
	protected void processData (
			SignWizardDTO dto ) 
					throws AlfrescoException {
		
		String sessionId = dto.getSessionId();
		String keyStorePin = dto.getKsPin();
		KeyStoreClientCtrl keyStoreClient = clientFactory.getKeyStoreCtrl ( sessionId );
		dto.setKsAliases ( keyStoreClient.openKeyStore(keyStorePin) );
	}

	@Override
	protected int currentStep() {
		return STEP_KSOPEN;
	}
}
