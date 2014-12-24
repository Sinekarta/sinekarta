package org.sinekartads.applet;

import java.io.Serializable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.utils.JSONUtils;


public class AppletResponseDTO implements Serializable {

	private static final long serialVersionUID = -4022334556491609402L;
	private static Logger tracer = Logger.getLogger(AppletResponseDTO.class); 
	
	public static final String SUCCESS = "SUCCESS";
	public static final String ERROR = "ERROR";
	
	
	public AppletResponseDTO() {
		
	}
	
	public AppletResponseDTO(String appletMethod) {
		this.appletMethod = appletMethod;
	}
	
	private String appletMethod;
	private String result;
	/**
	 * @deprecated ignore this field - fake field for serialization only proposes
	 */
	@SuppressWarnings("unused")
	private String resultCode;
	private ActionErrorDTO[] actionErrors = new ActionErrorDTO[0];
	private FieldErrorDTO[] fieldErrors = new FieldErrorDTO[0];
	
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
		tracer.info(String.format("added fieldError: %s - %s", field, error));
		tracer.info(JSONUtils.serializeJSON(fieldErrors,true));
	}
	
	public void addActionError ( String errorMessage, Exception errorCause ) {
		ActionErrorDTO actionError = new ActionErrorDTO ( );
		actionError.errorMessage = errorMessage;
		// TODO serialize the errorCause to JSON and add it to the actionError
		actionErrors = (ActionErrorDTO[]) ArrayUtils.add ( actionErrors, actionError );
		tracer.info(String.format("added actionError: %s - %s", errorMessage, errorCause));
		tracer.info(JSONUtils.serializeJSON(actionErrors,true));
	}
	
	

	// -----
	// --- Data transport protocol
	// -
	
	public String getAppletMethod() {
		return appletMethod;
	}

	public void setAppletMethod(String appletMethod) {
		this.appletMethod = appletMethod;
	}

	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	
	
	// -----
	// --- Elaboration status transmission
	// -

	public String getResultCode() {
		if ( ArrayUtils.isEmpty(actionErrors) && ArrayUtils.isEmpty(fieldErrors) ) {
			return "SUCCESS";
		} else {
			return "ERROR";
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
}