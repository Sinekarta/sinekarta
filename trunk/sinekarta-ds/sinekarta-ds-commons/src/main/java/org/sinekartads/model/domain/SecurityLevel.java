package org.sinekartads.model.domain;

public interface SecurityLevel<SL extends SecurityLevel<SL>> {
	
	public String name();
	
	public int ordinal();
	
	public static enum KeyRingSupport implements SecurityLevel<KeyRingSupport> {
		JAVA_KEYTOOL,
		PHYSICAL_DEVICE,
		SIGN_WS,
		FILE_SYSTEM,
		SYSTEM,
		ALFRESCO,
		OTHER_SUPPORT;	
	}
	
	public static enum TimeStampVerifyResult implements SecurityLevel<TimeStampVerifyResult> {
		VALID,
		INVALID;
	}
	
	public static enum VerifyResult implements SecurityLevel<VerifyResult> {
		VALID,
		INVALID;
	}
}
