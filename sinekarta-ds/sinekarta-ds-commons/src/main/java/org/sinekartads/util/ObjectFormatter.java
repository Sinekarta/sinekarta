package org.sinekartads.util;

public interface ObjectFormatter<T extends Object> {
	
	public ObjectFormatter<Object> DEFAULT = new AbstractObjectFormatter<Object>() {
		@Override
		public String format(Object obj) {
			return obj.toString();
		}
	};
	
	public String format(T obj);
	
	public String formatObj(Object obj);
			
}
