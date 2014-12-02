package org.sinekartads.util.verification;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class BeanVerifier<T> {
	
	// -----
	// --- Singleton implementation
	// -
	
	static final BeanVerifier<Object> myself = new BeanVerifier<Object>();
	
	public static BeanVerifier<Object> getDefaultInstance() {
		return myself;
	}
	
	private BeanVerifier() { }
	
	
	
	// -----
	// --- Beans validation
	// -
	
	public void verifyBean(T target) 
			throws CompositeBeanException, 
					VerificationException{
		
		CompositeBeanException.ExceptionComposer composer = CompositeBeanException.getComposer();
		try {
			for ( Field field : target.getClass().getDeclaredFields() ) {
				try {
					verifyBeanProperty(target, field);
				} catch(InvalidPropertyException e) {
					composer.embedBeanException(e);
				}
			}
			doVerifyBean(target);
		} catch (InvalidBeanException e) {
			composer.embedBeanException(e);
		} catch (VerificationException e) {
			throw e;
		} catch (Exception e) {
			throw new VerificationException(e);
		} 
	}
	
	protected void doVerifyBean(Object target) throws InvalidBeanException {
		// override if necessary
	}
	
	
	
	// -----
	// --- Bean properties verification
	// -
	
	boolean verifyBeanProperty(Object target, Field field) 
			throws MissingPropertyException, 
					VerificationException {
		String property = field.getName();
		Object value;
		try {
			value = BeanUtils.getProperty(target, property);
		} catch(Exception e) {
			throw new VerificationException(e);
		}
		doVerifyMandatoryProperty(target, field, value);
		doVerifyProperty(target, field, value);
		return true;
	}
	
	protected void doVerifyMandatoryProperty(Object target, Field field, Object value) 
			throws MissingPropertyException {
		
		String property = field.getName();
		boolean mandatory = false;
		for ( Annotation annot : field.getAnnotations() ) {
			if ( annot.annotationType().equals(Mandatory.class) ) {
				mandatory = true;
			}
		}
		if(mandatory) {
			if(value == null) {
				throw new MissingPropertyException(target, property);
			} 
			if(value instanceof String && StringUtils.isBlank((String)value)) {
				throw new MissingPropertyException(target, property);
			}
			if(value instanceof Object[] && ArrayUtils.isEmpty((Object[])value)) {
				throw new MissingPropertyException(target, property);
			}
		}
	}
	
	protected boolean doVerifyProperty(Object target, Field field, Object value) 
			throws InvalidBeanException, VerificationException {
		// override if necessary
		return true;
	}
	
	
	
}
