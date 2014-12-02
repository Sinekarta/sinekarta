package org.sinekartads.util;

import org.apache.commons.lang3.StringUtils;

public class StringComparator extends EntityComparator<String>{

	private static final long serialVersionUID = -4369369831055421934L;

	@Override
	public int doCompare(String value0, String value1) {
		return value0.compareTo(value1);
	}
	
	@Override
	protected int nullCompare(String value0, String value1) {
		if(value0 == null) {
			if ( value1 != null ) {
				return -1;
			} 
		} else {
			if ( value1 == null ) {
				return 1;
			}
		}
		if ( StringUtils.isBlank(value0) ) {
			if ( StringUtils.isBlank(value1) ) {
				return -1;
			} else {
				return Integer.valueOf(value0.length())
						.compareTo(Integer.valueOf(value1.length()));
			}
		} else {
			if ( StringUtils.isBlank(value1) ) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
