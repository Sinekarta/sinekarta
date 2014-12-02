package org.sinekartads.util;

import java.io.Serializable;


public class EntityCaster<Source extends Serializable, Target extends Serializable> extends EntityTransformer<Source, Target> {

	public EntityCaster ( Class<Target> targetClass ) {
		
		this.targetClass = targetClass;
	}
	
	private final Class<Target> targetClass;
	
	@Override
	protected Target doTransform ( Source item ) {
		return targetClass.cast ( item );
	}

}
