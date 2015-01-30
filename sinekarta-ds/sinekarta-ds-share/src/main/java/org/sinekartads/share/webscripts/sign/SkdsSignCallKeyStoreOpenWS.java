/*
< * Copyright (C) 2010 - 2012 Jenia Software.
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

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.request.SkdsKeyStoreRequest.SkdsKeyStoreOpenRequest;
import org.sinekartads.dto.response.SkdsKeyStoreResponse.SkdsKeyStoreOpenResponse;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.share.util.JavaWebscriptTools;

public class SkdsSignCallKeyStoreOpenWS extends BaseSignController {
	
	@Override
	protected void processData (
			SignWizardDTO dto ) 
					throws AlfrescoException {

		String ksRef = null;
		String[] ksAliases = null;
		try {
			SkdsKeyStoreOpenRequest ksoreq = new SkdsKeyStoreOpenRequest();
			ksoreq.setKeyStorePin(dto.getKsPin());
			SkdsKeyStoreOpenResponse ksoresp = JavaWebscriptTools.postJsonRequest ( 
					ksoreq, SkdsKeyStoreOpenResponse.class, connectorService );
			ksAliases = ksoresp.getAliases();
			ksRef = ksoresp.getKsRef();
		} catch(AlfrescoException e) {
			String errorMessage = e.getMessage();
			if ( StringUtils.equals(errorMessage, "skds.error.keyStorePinWrong") ) {
				addFieldError(dto, "ksPin", getMessage(errorMessage));
			} else {
				processError(dto, e);
			}
		}
		dto.setKsRef(ksRef);
		dto.setKsAliases(ksAliases);
	}

	@Override
	protected int currentStep() {
		return STEP_KSOPEN;
	}
}
