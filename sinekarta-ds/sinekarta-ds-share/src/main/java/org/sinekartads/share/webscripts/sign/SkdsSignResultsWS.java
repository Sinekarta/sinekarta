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
import org.sinekartads.share.util.AlfrescoException;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public class SkdsSignResultsWS extends BaseSignController {

	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	@Override
	protected void processData (
			SignWizardDTO dto ) 
					throws AlfrescoException {
	}

	@Override
	protected int currentStep() {
		return STEP_RESULTS;
	}
}
