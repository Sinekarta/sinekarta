package org.sinekartads.dto.share;

import java.util.List;
import java.util.Map;

import org.sinekartads.dto.BaseDTO;

public abstract class WizardDTO extends BaseDTO {

	private static final long serialVersionUID = 253027307921564851L;
	
	private String backUrl;
	private String[] wizardForms;
	private String currentForm;
	private String resultCode;
	private List<String> actionErrors;
	private Map<String, List<String>> fieldErrors;

	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public String getCurrentForm() {
		return currentForm;
	}

	public String[] getWizardForms() {
		return wizardForms;
	}

	public void setWizardForms(String[] wizardForms) {
		this.wizardForms = wizardForms;
	}

	public void setCurrentForm(String currentForm) {
		this.currentForm = currentForm;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public List<String> getActionErrors() {
		return actionErrors;
	}

	public void setActionErrors(List<String> actionErrors) {
		this.actionErrors = actionErrors;
	}

	public Map<String, List<String>> getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(Map<String, List<String>> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}
}
