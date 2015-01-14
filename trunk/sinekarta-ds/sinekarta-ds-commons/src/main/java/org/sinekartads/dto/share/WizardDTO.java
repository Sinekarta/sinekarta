package org.sinekartads.dto.share;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.sinekartads.dto.BaseDTO;

public abstract class WizardDTO extends BaseDTO {

	public static class WizardStepDTO extends BaseDTO {
		
		private static final long serialVersionUID = -3967625378702167910L;
		private String name;
		private String form;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}

		public String getForm() {
			return form;
		}

		public void setForm(String form) {
			this.form = form;
		}
	}
	

	
	private static final long serialVersionUID = 253027307921564851L;
	public static final String SUCCESS = "SUCCESS";
	public static final String ERROR = "ERROR";
	
	private String backUrl;
	private String[] wizardForms;
	private String currentStep;
	private WizardStepDTO[] wizardSteps;
	private ActionErrorDTO[] actionErrors = new ActionErrorDTO[0];
	private FieldErrorDTO[] fieldErrors = new FieldErrorDTO[0];

	/**
	 * @deprecated ignore this field - fake field for serialization only proposes
	 */
	@SuppressWarnings("unused")
	private String resultCode;
	
	/**
	 * @deprecated ignore this setter - fake field for serialization only proposes
	 */
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	
	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public String[] getWizardForms() {
		return wizardForms;
	}

	public void setWizardForms(String[] wizardForms) {
		this.wizardForms = wizardForms;
	}

	public WizardStepDTO[] getWizardSteps() {
		return wizardSteps;
	}

	public void setWizardSteps(WizardStepDTO[] wizardSteps) {
		this.wizardSteps = wizardSteps;
	}

	public String getCurrentStep() {
		return currentStep;
	}

	public void setCurrentStep(String currentStep) {
		this.currentStep = currentStep;
	}



	// -----
	// --- Elaboration status transmission
	// -

	public void addFieldError ( String field, String error ) {
		FieldErrorDTO fieldError = null;
		for ( FieldErrorDTO fe : fieldErrors ) {
			if ( StringUtils.equals(fe.field, field) ) {
				fieldError = fe;
			}
		}
		if ( fieldError == null ) {
			fieldError = new FieldErrorDTO();
			fieldError.setField(field);
			fieldErrors = (FieldErrorDTO[]) ArrayUtils.add ( fieldErrors, fieldError );
		}
		fieldError.errors = (String[]) ArrayUtils.add ( fieldError.errors, error );
	}
	
	public void addActionError ( String errorMessage, Exception errorCause ) {
		ActionErrorDTO actionError = new ActionErrorDTO ( );
		actionError.errorMessage = errorMessage;
		actionErrors = (ActionErrorDTO[]) ArrayUtils.add ( actionErrors, actionError );
		// TODO serialize the errorCause to JSON and add it to the actionError
	}
	
	public String getResultCode() {
		if ( ArrayUtils.isEmpty(actionErrors) && ArrayUtils.isEmpty(fieldErrors) ) {
			return SUCCESS;
		} else {
			return ERROR;
		}
	}

	public ActionErrorDTO[] getActionErrors() {
		return actionErrors;
	}

	public void setActionErrors(ActionErrorDTO[] actionErrors) {
		this.actionErrors = actionErrors;
	}

	public FieldErrorDTO[] getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(FieldErrorDTO[] fieldErrors) {
		this.fieldErrors = fieldErrors;
	}


	
	// -----
	// --- ActionError and FieldError implementation
	// -

	public static class ActionErrorDTO {
		
		private String errorMessage;
		private String errorCause;
		
		public String getErrorMessage() {
			return errorMessage;
		}
		
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public String getErrorCause() {
			return errorCause;
		}

		public void setErrorCause(String errorCause) {
			this.errorCause = errorCause;
		}
		
	}
	
	public static class FieldErrorDTO {
		
		private String field;
		private String[] errors;
		
		public String getField() {
			return field;
		}
		
		public void setField(String field) {
			this.field = field;
		}
		
		public String[] getErrors() {
			return errors;
		}
		
		public void setErrors(String[] errors) {
			this.errors = errors;
		}
	}
}
