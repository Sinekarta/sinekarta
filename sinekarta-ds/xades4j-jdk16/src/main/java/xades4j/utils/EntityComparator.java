package xades4j.utils;

import java.io.Serializable;
import java.util.Comparator;

public abstract class EntityComparator<T> 
		implements Comparator<T>, Cloneable, Serializable {

	private static final long serialVersionUID = 6875763467375901937L;
	
	boolean reverse;
	
	public EntityComparator() {
		this(false);
	}
	
	public EntityComparator(boolean reverse) {
		this.reverse = reverse;
	}
	
	@SuppressWarnings("unchecked")
	public EntityComparator<T> reverse() {
		EntityComparator<T> clone;
		try {
			clone = (EntityComparator<T>)this.clone();
		} catch (CloneNotSupportedException e) {
			// never thrown, Cloneable natively implemented
			throw new RuntimeException(e);
		}
		clone.reverse = !reverse;
		return clone;
	}

	@Override
	public int compare(T value0, T value1) {
		int comparison = nullCompare(value0, value1);
		if(comparison != 0) {
			return comparison;
		}
		return doCompare(value0, value1) * (reverse?-1:1);
	}
	
	public abstract int doCompare(T o1, T o2);
	
	
	
	public static enum NullPolicy {
		TO_FIRST_PLACE(-1),
		TO_LAST_PLACE(1);
		
		int multiplicator;
		
		NullPolicy(int multiplicator) {
			this.multiplicator = multiplicator;
		}
	}
	
	NullPolicy nullPolicy;
	
	@SuppressWarnings("unchecked")
	public EntityComparator<T> setNullPolicy(NullPolicy nullPolicy) {
		EntityComparator<T> clone;
		try {
			clone = (EntityComparator<T>)this.clone();
		} catch (CloneNotSupportedException e) {
			// never thrown, Cloneable natively implemented
			throw new RuntimeException(e);
		}
		clone.nullPolicy = nullPolicy;
		return clone;
	}
	
	protected int nullCompare(T value0, T value1) {
		int comparison;
		if(value0 == null) {
			if(value1 != null) {
				comparison = nullPolicy.multiplicator;
			} else {
				comparison = 0;
			}
		} else {
			if(value1 == null) {
				comparison = nullPolicy.multiplicator * -1;
			} else {
				comparison = 0;
			}
		}
		
		return comparison * (reverse?-1:1);
	}
	
	
}
