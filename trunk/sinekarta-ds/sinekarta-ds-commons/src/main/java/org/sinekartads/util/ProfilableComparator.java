package org.sinekartads.util;

import org.apache.commons.lang.ArrayUtils;

public abstract class ProfilableComparator<T> extends EntityComparator<T> {

	private static final long serialVersionUID = 3504477423655332140L;

	public interface ComparisonProfile { }

	public ProfilableComparator(ComparisonProfile defaultComparisonProfile) {
		comparisonProfile = defaultComparisonProfile;
	}
	
	private ComparisonProfile comparisonProfile;
	
	public ProfilableComparator<T> switchToProfile(ComparisonProfile comparisonProfile) 
			throws IllegalArgumentException {
		ProfilableComparator<T> clone;
		try {
			@SuppressWarnings("unchecked")
			ProfilableComparator<T> clone2 = (ProfilableComparator<T>)this.clone();
			clone = clone2;
		} catch (CloneNotSupportedException e) {
			// never thrown, Cloneable natively implemented
			throw new RuntimeException(e);
		}
		if ( !ArrayUtils.contains(supportedComparisonProfiles(), comparisonProfile) ) {
			throw new IllegalArgumentException(String.format(
					"unsupported comparison profile %s - choose among %s", 
					comparisonProfile,
					TextUtils.fromArray(supportedComparisonProfiles()) ));
		}
		clone.comparisonProfile = comparisonProfile;
		return clone;
	}
	
	@Override
	public int doCompare(T obj1, T obj2) {
		return doCompare(comparisonProfile, obj1, obj2);
	}
	
	protected abstract ComparisonProfile[] supportedComparisonProfiles();
		
	public abstract int doCompare(ComparisonProfile comparisonProfile, T obj1, T obj2);
	
	
}
