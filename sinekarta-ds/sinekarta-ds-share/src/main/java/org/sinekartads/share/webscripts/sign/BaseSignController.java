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
import org.sinekartads.model.client.SignatureClientFactory;
import org.sinekartads.share.webscripts.WSController;

public abstract class BaseSignController extends WSController<SignWizardDTO> {

	
	protected static final String[] WIZARD_FORMS = {
		"skdsSignOptions", "skdsSignClient", "skdsSignResult"
	};
	protected static final WizardStep STEP_INIT 	= new WizardStep("skdsSignInit",     "skdsSignOptions");
	protected static final WizardStep STEP_OPTIONS 	= new WizardStep("skdsSignOptions",  "skdsSignOptions");
	protected static final WizardStep STEP_CLIENT 	= new WizardStep("skdsSignClient",   "skdsSignClient");
	protected static final WizardStep STEP_PRESIGN 	= new WizardStep("skdsSignPreSign",  "skdsSignClient");
	protected static final WizardStep STEP_POSTSIGN = new WizardStep("skdsSignPostSign", "skdsSignClient");
	protected static final WizardStep STEP_RESULTS 	= new WizardStep("skdsSignResults",  "skdsSignResults");
	
	SignatureClientFactory clientFactory = SignatureClientFactory.getInstance();
	
	@Override
	protected String[] getWizardForms() {
		return WIZARD_FORMS;
	}
	
	@Override
	protected WizardStep[] getWizardSteps() {
		return new WizardStep[] { STEP_INIT, STEP_OPTIONS, STEP_CLIENT, STEP_PRESIGN, STEP_POSTSIGN, STEP_RESULTS };
	}
}
