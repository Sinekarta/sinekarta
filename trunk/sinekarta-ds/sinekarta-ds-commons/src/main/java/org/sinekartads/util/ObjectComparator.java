package org.sinekartads.util;

public class ObjectComparator<T> extends EntityComparator<T> {

	private static final long serialVersionUID = -8623358866364745235L;

	@SuppressWarnings("unchecked")
	@Override
	public int doCompare(T obj0, T obj1) {
		// try to use the Comparable interface
		if(obj0 instanceof Comparable && obj1 instanceof Comparable) {
			return ((Comparable<T>)obj0).compareTo(obj1); 
		}

		// compare by using the hashes 
		Integer hash0 = obj0.hashCode();
		Integer hash1 = obj1.hashCode();
		return hash0.compareTo(hash1);
	}
}
