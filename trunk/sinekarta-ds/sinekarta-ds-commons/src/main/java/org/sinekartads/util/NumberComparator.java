package org.sinekartads.util;

public class NumberComparator extends EntityComparator<Number> {

	private static final long serialVersionUID = -8826804996965074806L;

	@Override
	public int doCompare(Number number0, Number number1) {
		if(number0 instanceof Double || number1 instanceof Double) {
			return Double.valueOf(number0.doubleValue())
							.compareTo(Double.valueOf(number1.doubleValue()));
		}
		if(number0 instanceof Float || number1 instanceof Float) {
			return Float.valueOf(number0.floatValue())
							.compareTo(Float.valueOf(number1.floatValue()));
		}
		return Long.valueOf(number0.longValue())
						.compareTo(Long.valueOf(number1.longValue()));
	}

}
