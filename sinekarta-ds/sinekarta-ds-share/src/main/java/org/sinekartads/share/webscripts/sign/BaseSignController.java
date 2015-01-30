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
import org.sinekartads.share.webscripts.WSController;

public abstract class BaseSignController extends WSController<SignWizardDTO> {

	
	protected static final String[] WIZARD_FORMS = {
		"skdsSignOptions", "skdsSignClient", "skdsSignResults"
	};
	protected static final int STEP_INIT 	 = 0;
	protected static final int STEP_OPTIONS  = 1;
	protected static final int STEP_CLIENT 	 = 2;
	protected static final int STEP_KSOPEN 	 = 3;
	protected static final int STEP_KSREAD 	 = 4;
	protected static final int STEP_DIGSIG 	 = 5;
	protected static final int STEP_PRESIGN  = 6;
	protected static final int STEP_POSTSIGN = 7;
	protected static final int STEP_RESULTS	 = 8;
	
	@Override
	protected String[] getWizardForms() {
		return WIZARD_FORMS;
	}
	
	@Override
	protected WizardStep[] getWizardSteps() {
		return new WizardStep[] { 
				new WizardStep("skdsSignInit",     		 	"skdsSignOptions"), 
				new WizardStep("skdsSignOptions",  		 	"skdsSignOptions"), 
				new WizardStep("skdsSignClient",   		 	"skdsSignClient"),
				new WizardStep("skdsSignCallKeyStoreOpen", 	"skdsSignClient"), 
				new WizardStep("skdsSignCallKeyStoreRead",	"skdsSignClient"), 
				new WizardStep("skdsSignSetDigitalSignature", "skdsSignClient"),
				new WizardStep("skdsSignPreSign",  		 	"skdsSignClient"), 
				new WizardStep("skdsSignPostSign", 		 	"skdsSignClient"), 
				new WizardStep("skdsSignResults",  		 	"skdsSignResults") };
	}
}
