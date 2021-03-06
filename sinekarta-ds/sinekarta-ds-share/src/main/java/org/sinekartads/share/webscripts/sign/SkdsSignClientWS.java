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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.share.SignWizardDTO;
import org.sinekartads.share.util.AlfrescoException;
import org.springframework.util.Assert;

public class SkdsSignClientWS extends BaseSignController {

	@Override
	protected void processData(
			SignWizardDTO dto)
					throws AlfrescoException {
		
		Assert.notNull ( dto.getSignature() );
		if ( StringUtils.equals(dto.getClientType(), "KEYSTORE") ) {
			if ( StringUtils.isBlank(dto.getKsPin()) ) {
				addFieldError(dto, "ksPin", getMessage(MANDATORY));
			}
			if ( StringUtils.isBlank(dto.getKsUserAlias()) || ArrayUtils.isEmpty(dto.getSignature().getHexCertificateChain()) ) {
				addFieldError(dto, "ksUserAlias", getMessage(MANDATORY));
			}
		} else if ( StringUtils.equals(dto.getClientType(), "SMARTCARD") ) {
			if ( StringUtils.isBlank(dto.getScDriver()) ) {
				addFieldError(dto, "scDriver", getMessage(MANDATORY));
			}
			if ( StringUtils.isBlank(dto.getScDriver()) ) {
				addFieldError(dto, "scPin", getMessage(MANDATORY));
			}
			if ( StringUtils.isBlank(dto.getScUserAlias()) || ArrayUtils.isEmpty(dto.getSignature().getHexCertificateChain()) ) {
				addFieldError(dto, "scUserAlias", getMessage(MANDATORY));
			}
		}
		if ( ArrayUtils.isEmpty(dto.getFieldErrors()) ) {
			for ( DocumentDTO document : dto.getDocuments() ) {
				document.setSignatures(ArrayUtils.add(document.getSignatures(), dto.getSignature()));
			}
		}
	}
	
	@Override
	protected int currentStep() {
		return STEP_CLIENT;
	}
}
