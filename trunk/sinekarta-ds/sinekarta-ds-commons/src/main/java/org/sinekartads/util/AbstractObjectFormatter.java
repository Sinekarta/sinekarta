package org.sinekartads.util;

public abstract class AbstractObjectFormatter<S> implements ObjectFormatter<S> {

	@SuppressWarnings("unchecked")
	public String formatObj(Object obj) {
		if(obj == null) {
			return "null";
		}
		
		S cast;
		try {
			Class<?> typeArg = TemplateClassUtils.getTemplateArgument(this, ObjectFormatter.class);
			cast = (S)typeArg.cast(obj);
		} catch(IllegalArgumentException iea) {
			cast = null;
		} catch(ClassCastException iea) {
			cast = null;
		}
		
		String result;
		if(cast != null) {
			result = format(cast);
		} else {
			result = obj.toString();
		}
		return result;
	}
	
}
