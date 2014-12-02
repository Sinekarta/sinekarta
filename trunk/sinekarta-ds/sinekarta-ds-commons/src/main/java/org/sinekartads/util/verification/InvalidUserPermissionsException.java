package org.sinekartads.util.verification;


public class InvalidUserPermissionsException extends IllegalStateException {

	private static final long serialVersionUID = -7577742935045194760L;

	public InvalidUserPermissionsException (
			UserPermissionsLevel requiredPermissionsLevel,
			UserPermissionsLevel userPermissionsLevel ) {
		
		this(	"operation denied. \n" +
				"required permission level: %s \n" +
				"current level: %s", 
			  requiredPermissionsLevel, userPermissionsLevel);
	}
	
	public InvalidUserPermissionsException (
			String messageFormat, 
			UserPermissionsLevel requiredPermissionsLevel,
			UserPermissionsLevel userPermissionsLevel ) {
		
		super(String.format(messageFormat, requiredPermissionsLevel.name()));
		this.requiredPermissionsLevel = requiredPermissionsLevel;
		this.userPermissionsLevel = userPermissionsLevel;
	}
	
	final UserPermissionsLevel userPermissionsLevel;
	final UserPermissionsLevel requiredPermissionsLevel;
	
	public UserPermissionsLevel getRequiredPermissionsLevel() {
		return requiredPermissionsLevel;
	}
}