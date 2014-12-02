package org.sinekartads.util.verification;


public class UserPermissionsVerifier {
	
	// -----
	// --- Singleton implementation
	// -
	
	private static UserPermissionsVerifier singleton = null;
	
	public static UserPermissionsVerifier getInstance() {
		if(singleton == null) {
			return new UserPermissionsVerifier();
		}
		return singleton;
	}
	
	protected UserPermissionsVerifier() {
		singleton = this;
	}

	
	
	// -----
	// --- User type identification
	// -
	
	public boolean isUserUnauthenticated() {
		return getUserRightsLevel().equals(UserPermissionsLevel.UNAUTHENTICATED);
	}
	
	public boolean isUserAuthenticated() {
		return getUserRightsLevel().equals(UserPermissionsLevel.AUTHENTICATED);
	}
	
	public boolean isUserRCS() {
		return getUserRightsLevel().equals(UserPermissionsLevel.RCS);
	}
	
	public boolean isUserSinekartaAdministrator() {
		return getUserRightsLevel().equals(UserPermissionsLevel.SYSTEM_ADMINISTRATOR);
	}
	
	public boolean isUserAlfrescoAdministrator() {
		return getUserRightsLevel().equals(UserPermissionsLevel.ALFRESCO_ADMINISTRATOR);
	}
	
	public boolean isUserSystemProcess() {
		return getUserRightsLevel().equals(UserPermissionsLevel.SYSTEM_PROCESS);
	}
	
	
	
	// -----
	// --- Permissions level  identification
	// -
	
	public UserPermissionsLevel getUserRightsLevel() {
		// TODO dummy level: can do everything - implement method body
		return UserPermissionsLevel.SYSTEM_PROCESS;
	}
	
	public boolean hasUserSpecialPermissions() {
		return getUserRightsLevel().compareTo(UserPermissionsLevel.RCS) >= 0;
	}
	
	public boolean hasUserAdministratorPermissions() {
		return getUserRightsLevel().compareTo(UserPermissionsLevel.SYSTEM_ADMINISTRATOR) >= 0;
	}
	
	
	
	// -----
	// --- User permissions verification
	// -
	
	/**
	 * Verify whether the user is allowed to perform UNAUTHENTICATED-level operations. <br/> 
	 *
	 * Currently no operations are specifically for unauthenticated users, then the 
	 * control will always succeed.
	 * @throws InvalidUserPermissionsException currently never
	 */
	public void verifyUnauthenticatedPermissions() 
				throws InvalidUserPermissionsException {
		
		// do nothing, no operation are exclusive for unauthenticated users
	}
	
	/**
	 * Verify whether the user is allowed to perform AUTHENTICATED-level operations. <br/>
	 * 
	 * Currently no operations are specifically for unauthenticated users, then the 
	 * control will succeed with every permission level higher than AUTHENTICATED.
	 * @throws InvalidUserPermissionsException if the user permissions level is UNAUTHENTICATED
	 */
	public void verifyAuthenticatedPermissions() 
			throws InvalidUserPermissionsException {
		
		verifyMinimumUserPermissions(UserPermissionsLevel.AUTHENTICATED);
	}

	/**
	 * Verify whether the user is allowed to perform special operations. <br/>
	 * 
	 * This includes every permission level higher than RCS.
	 * @throws InvalidUserPermissionsException if the user permissions level 
	 *          is UNAUTHENTICATED or AUTHENTICATED
	 */
	public void verifySpecialPermissions() 
			throws InvalidUserPermissionsException {
		
		verifyMinimumUserPermissions(UserPermissionsLevel.RCS);
	}
	
	/**
	 * Verify whether the user is allowed to perform RCS-specific operations. 
	 * @throws InvalidUserPermissionsException if the user permissions level 
	 *          is different from RCS (or SYSTEM_PROCESS)
	 */
	public void verifyRCSPermissions() 
			throws InvalidUserPermissionsException {
		
		verifyRequiredUserPermissions(UserPermissionsLevel.RCS);
	}

	/**
	 * Verify whether the user has any administrator permissions. <br/>
	 * 
	 * This includes every permission level higher than SINEKARTA_ADMINISTRATOR.
	 * @throws InvalidUserPermissionsException if the user permissions level 
	 *          is different from SINEKARTA_ADMINISTRATOR or ALFRESCO_ADMINISTRATOR 
	 *			(or SYSTEM_PROCESS)
	 */
	public void verifyAdministratorPermissions() 
			throws InvalidUserPermissionsException {
		
		verifyMinimumUserPermissions(UserPermissionsLevel.SYSTEM_ADMINISTRATOR);
	}
	
	/**
	 * Verify whether the user is allowed to perform SINEKARTA_ADMINISTRATOR-specific operations. 
	 * @throws InvalidUserPermissionsException if the user permissions level 
	 *          is different from SINEKARTA_ADMINISTRATOR or (or SYSTEM_PROCESS)
	 */
	public void verifySinekartaAdministratorPermissions() 
			throws InvalidUserPermissionsException {
		
		verifyRequiredUserPermissions(UserPermissionsLevel.SYSTEM_ADMINISTRATOR);
	}
	
	/**
	 * Verify whether the user is allowed to perform ALFRESCO_ADMINISTRATOR-specific operations. 
	 * @throws InvalidUserPermissionsException if the user permissions level 
	 *          is different from ALFRESCO_ADMINISTRATOR (or SYSTEM_PROCESS)
	 */
	public void verifyAlfrescoAdministratorPermissions() 
			throws InvalidUserPermissionsException {
		
		verifyRequiredUserPermissions(UserPermissionsLevel.ALFRESCO_ADMINISTRATOR);
	}
	
	/**
	 * Verify whether the operation has been required by a system process, such as startup
	 * and maintanance.
	 * @throws InvalidUserPermissionsException if the user permissions level 
	 *          is different from SYSTEM_PROCESS
	 */
	public void verifySystemProcessPermissions() 
			throws InvalidUserPermissionsException {
		
		verifyRequiredUserPermissions(UserPermissionsLevel.SYSTEM_PROCESS);
	}
	
	void verifyRequiredUserPermissions (
			UserPermissionsLevel requiredPermissionLevel ) 
					throws InvalidUserPermissionsException {
		
		UserPermissionsLevel userPermissionLevel = getUserRightsLevel();
		if( !requiredPermissionLevel.equals(userPermissionLevel) 
				&& !userPermissionLevel.equals(UserPermissionsLevel.SYSTEM_PROCESS) ) {
			throw new InvalidUserPermissionsException(
					requiredPermissionLevel, userPermissionLevel);
		}
	}
	
	void verifyMinimumUserPermissions (
			UserPermissionsLevel minimumPermissionLevel ) 
				throws InvalidUserPermissionsException {
		
		UserPermissionsLevel userPermissionLevel = getUserRightsLevel();
		if( userPermissionLevel.compareTo(minimumPermissionLevel) < 0 
				&& !userPermissionLevel.equals(UserPermissionsLevel.SYSTEM_PROCESS)  ) {
			throw new InvalidUserPermissionsException(
					"operation denied. \n" +
					"minimum permission level: %s \n" +
					"current level: %s",
					minimumPermissionLevel, userPermissionLevel);
		}
	}
}
