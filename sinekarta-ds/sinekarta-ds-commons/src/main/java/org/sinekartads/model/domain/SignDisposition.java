package org.sinekartads.model.domain;


public interface SignDisposition<SD extends SignDisposition<SD>> {
	
	public String name();
	
	public enum CMS implements SignDisposition<CMS> {
		DETACHED,
		EMBEDDED
	}

	public enum PDF implements SignDisposition<PDF> {
		DETACHED,
		DEFERRED

	}

	public enum XML implements SignDisposition<XML> {
		DETACHED,
		ENVELOPED,
		ENVELOPING
	}

	public enum TimeStamp implements SignDisposition<TimeStamp> {
		DETACHED,
		ATTRIBUTE,
		ENVELOPING
	}
}