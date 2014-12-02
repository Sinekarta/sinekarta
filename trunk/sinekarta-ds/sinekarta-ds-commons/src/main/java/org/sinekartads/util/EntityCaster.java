package org.sinekartads.util;

public class EntityCaster<Source, Target> extends EntityTransformer<Source, Target> {

	public EntityCaster ( Class<Target> targetClass ) {
		
		this.targetClass = targetClass;
	}
	
	private final Class<Target> targetClass;
	
	@Override
	protected Target doTransform ( Source item ) {
		return targetClass.cast ( item );
	}

}
