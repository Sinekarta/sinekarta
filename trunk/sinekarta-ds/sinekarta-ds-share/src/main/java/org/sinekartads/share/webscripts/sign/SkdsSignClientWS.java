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

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.model.client.SignatureClient.SignatureClientType;
import org.sinekartads.share.util.AlfrescoException;
import org.springframework.util.Assert;

public class SkdsSignClientWS extends BaseSignController {

	@Override
	protected void processData(
			SignWizardDTO dto)
					throws AlfrescoException {
		
		try {
			Assert.notNull ( dto.getSignature() );
			if ( StringUtils.equals(dto.getClientType(), SignatureClientType.KEYSTORE.name()) ) {
				if ( StringUtils.isBlank(dto.getKsUserAlias()) ) {
					addFieldError(dto, "ksUserAlias", getMessage(MANDATORY));
				}
				if ( StringUtils.isBlank(dto.getScDriver()) ) {
					addFieldError(dto, "ksPin", getMessage(MANDATORY));
				}
			} else if ( StringUtils.equals(dto.getClientType(), SignatureClientType.SMARTCARD.name()) ) {
				if ( StringUtils.isBlank(dto.getScDriver()) ) {
					addFieldError(dto, "scDriver", getMessage(MANDATORY));
				}
				if ( StringUtils.isBlank(dto.getScDriver()) ) {
					addFieldError(dto, "scPin", getMessage(MANDATORY));
				}
				if ( StringUtils.isBlank(dto.getScUserAlias()) ) {
					addFieldError(dto, "scUserAlias", getMessage(MANDATORY));
				}
			}
		} catch(Exception e) {
			processError ( dto, e.getMessage() );
		}
	}
	
	@Override
	protected int currentStep() {
		return STEP_CLIENT;
	}
}
