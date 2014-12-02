package org.sinekartads.util.verification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CompositeBeanException extends InvalidBeanException {

	private static final long serialVersionUID = -1026731807806315164L;

	protected CompositeBeanException(Object target) {
		this(target, null, null, null);
	}
	
	protected CompositeBeanException(Object target, List<String> missingProperties) {
		this(target, missingProperties, null, null);
	}
	
	protected CompositeBeanException( 
			Object target, 
			List<String> beanErrors, 
			List<String> missingProperties, 
			Map<String, String> invalidProperties ) {
		
		super(target);
		this.target = target;
		this.beanErrors = beanErrors.toArray(new String[beanErrors.size()]);
		this.missingProperties = missingProperties.toArray(new String[missingProperties.size()]);
		this.invalidProperties = new HashMap<String, String>(invalidProperties);
	}
	
	final Object target;
	final String[] missingProperties;
	final HashMap<String, String> invalidProperties;
	final String[] beanErrors;
	
	public Object getTarget() {
		return target;
	}
	
	public String[] getMissingProperties() {
		return missingProperties;
	}
	
	public Map<String, String> getInvalidProperties() {
		return invalidProperties;
	}
	
	public String[] getBeanErrors() {
		return beanErrors;
	}
	
	
	// -----
	// --- Exception factory
	// -
	
	public static class ExceptionComposer {
		Object target = null;
		List<String> beanErrors = new ArrayList<String>();
		List<String> missingProperties = new ArrayList<String>();
		Map<String, String> invalidProperties = new HashMap<String, String>();
		
		public void embedBeanException(InvalidBeanException e) throws VerificationException {
			if ( target == null ) {
				target = e.getTarget();
			} else if ( !target.equals(e.getTarget()) ) {
				throw new VerificationException("all the exceptions must be related to the same target");
			}			

			if(e instanceof CompositeBeanException) { 
				CompositeBeanException cbe = (CompositeBeanException)e;
				beanErrors.addAll(Arrays.asList(cbe.beanErrors));
				missingProperties.addAll(Arrays.asList(cbe.missingProperties));
				invalidProperties.putAll(cbe.invalidProperties);
			} else if(e instanceof InvalidPropertyException) {
				InvalidPropertyException ipe = (InvalidPropertyException)e;
				if(e instanceof MissingPropertyException) {
					missingProperties.add(ipe.getProperty());
				} else {
					embedCustomPropertyException(ipe);
				}
			} else {
				beanErrors.add(e.getMessage());
			}
		}
		
		protected void embedCustomPropertyException(InvalidPropertyException e) throws VerificationException {
			invalidProperties.put(e.getProperty(), e.getMessage());
		}
				
		public CompositeBeanException compose() {
			CompositeBeanException e = new CompositeBeanException(target, beanErrors, missingProperties, invalidProperties);
			target = null;
			beanErrors.clear();
			missingProperties.clear();
			invalidProperties.clear();
			return e;
		}
	}
	
	public static ExceptionComposer getComposer() {
		return new ExceptionComposer();
	}
}