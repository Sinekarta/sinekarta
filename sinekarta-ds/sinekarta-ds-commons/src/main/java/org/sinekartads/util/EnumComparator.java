package org.sinekartads.util;

public class EnumComparator extends EntityComparator<Enum<?>> {

	private static final long serialVersionUID = -7504734360147494867L;

	@Override
	public int doCompare(Enum<?> value0, Enum<?> value1) 
			throws IllegalArgumentException {

		if ( !value0.getClass().equals(value1.getClass()) ) {
			throw new IllegalArgumentException("the two enum values must belong to the same type");
		}
		return Integer.valueOf(value0.ordinal())
				.compareTo(Integer.valueOf(value1.ordinal()));
	}

}
