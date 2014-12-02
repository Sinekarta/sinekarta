package org.sinekartads.util;

public class BooleanComparator extends EntityComparator<Boolean> {

	private static final long serialVersionUID = 5887907953787588822L;

	@Override
	public int doCompare(Boolean boolean0, Boolean boolean1) {		
		if(boolean0) {
			if(boolean1) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if(boolean1) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
