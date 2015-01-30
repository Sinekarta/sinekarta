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
import org.sinekartads.dto.request.SkdsKeyStoreRequest.SkdsKeyStoreReadRequest;
import org.sinekartads.dto.response.SkdsKeyStoreResponse.SkdsKeyStoreReadResponse;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.share.util.JavaWebscriptTools;

public class SkdsSignCallKeyStoreReadWS extends BaseSignController {
	
	@Override
	protected void processData (
			SignWizardDTO dto ) 
					throws AlfrescoException {
		
		String[] hexCertificateChain = null;
		String hexPrivateKey = "";
		if ( StringUtils.isNotBlank(dto.getKsUserAlias()) ) {
			try {
				SkdsKeyStoreReadRequest ksrreq = new SkdsKeyStoreReadRequest();
				ksrreq.setKsPin(dto.getKsPin());
				ksrreq.setKsRef(dto.getKsRef());
				ksrreq.setUserAlias(dto.getKsUserAlias());
				ksrreq.setUserPassword(dto.getKsUserPassword());
				SkdsKeyStoreReadResponse ksrresp = JavaWebscriptTools.postJsonRequest ( 
						ksrreq, SkdsKeyStoreReadResponse.class, connectorService );
				hexCertificateChain = ksrresp.getCertificateChain();
				hexPrivateKey = ksrresp.getPrivateKey();
			} catch(AlfrescoException e) {
				String errorMessage = e.getMessage();
				if ( StringUtils.equals(errorMessage, "skds.error.keyStorePinWrong") ) {
					addFieldError(dto, "ksPin", errorMessage);
				} else if ( StringUtils.equals(errorMessage, "skds.error.keyStoreUserNotFound") ) {
					addFieldError(dto, "ksUserAlias", errorMessage);
				} else {
					processError(dto, e);
				}
			}
		}
		dto.getSignature().setHexCertificateChain(hexCertificateChain);
		dto.setKsHexPrivateKey(hexPrivateKey);
	}

	@Override
	protected int currentStep() {
		return STEP_KSREAD;
	}
}
