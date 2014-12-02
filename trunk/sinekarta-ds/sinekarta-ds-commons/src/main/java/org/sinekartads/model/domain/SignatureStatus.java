package org.sinekartads.model.domain;

public interface SignatureStatus {
	
	public String name();
	
	public int ordinal();

	public static enum Stable implements SignatureStatus {
		RAW,
		FINALIZED,
		VERIFIED;
	}
	
	public static enum SignProcess implements SignatureStatus {
		EMPTY,
		CHAIN,
		DIGEST,
		SIGNED,
		MARKED;
	}
	
	public static enum TimeStampVerifyProcess implements SignatureStatus {
		UNTRUSTED,
		VERIFIED,
	}
}
