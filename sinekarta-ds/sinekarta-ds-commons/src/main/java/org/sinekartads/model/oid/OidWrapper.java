package org.sinekartads.model.oid;

public interface OidWrapper {
	
	public String getName();
	
	public String getId();
	
	public String[] getIds();
	
	/**
	 * Verify if the algorithm matches with the given algorithmDescriptor. <br>
	 * The match will happen if the algorithmId related to the given algorithmDescriptor 
	 * appears among the algorithm oids.
	 * Accepted values: \n" +
	 * <ul>
	 * <li> OidWrapper			  	oidWrapper
	 * <li> String 			  		algorithmId 
	 * <li> String 			  		algorithmName 
	 * </ul>
	 * @param algorithmDescriptor
	 * @return the matching SinekartaDsAlgorithm
	 * @throws IllegalArgumentException if algorithmDescriptor is invalid or doesn't match with
	 * any SinekartaDsAlgorithm
	 */
	public boolean matchesWith(Object algorithmDescriptor);
	
	/**
	 * Verify if the OidWrapper is equivalent to the given OidWrapper; the OidWrapper equivalence
	 * needs that the two instance refers to the same oids.
	 * Accepted values: \n" +
	 * <ul>
	 * <li> OidWrapper			  	oidWrapper
	 * <li> String 			  		algorithmId 
	 * <li> String 			  		algorithmName 
	 * </ul>
	 * @param algorithmDescriptor
	 * @return true if and only if the OidWrappers refer to the same oids.
	 */
	public boolean equals(OidWrapper oidWrapper);
}
