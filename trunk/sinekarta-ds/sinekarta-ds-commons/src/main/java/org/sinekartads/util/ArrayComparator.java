package org.sinekartads.util;

import java.util.Comparator;

public class ArrayComparator<T> extends EntityComparator<T[]> {

	private static final long serialVersionUID = -7964026899199462489L;

	public ArrayComparator() {
		this(null);
	}
	
	public ArrayComparator(Comparator<T> nestedComparator) {
		// generate a default comparator if missing
		if(nestedComparator == null) {
			nestedComparator = new ObjectComparator<T>();
		}
		// save the nestedComparator
		this.nestedComparator = nestedComparator;
	}
	
	private final Comparator<T> nestedComparator;
	
	@Override
	public int doCompare(T[] array0, T[] array1) {
		int comparison;
		
		// compare the array lengths
		Integer length0 = array0.length;
		Integer length1 = array1.length;
		comparison = length0.compareTo(length1);
		if(comparison != 0) {
			return comparison;
		}
		
		// compare the content from left to right
		for ( int i=0; i<length0; i++ ) {
			comparison = nestedComparator.compare(array0[i], array1[i]);
			if(comparison != 0) {
				return comparison;
			}
		}
		
		return 0;
	}

}

