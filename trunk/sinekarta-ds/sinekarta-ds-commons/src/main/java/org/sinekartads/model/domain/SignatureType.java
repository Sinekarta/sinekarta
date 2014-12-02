package org.sinekartads.model.domain;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.Assert;

public interface SignatureType<ST extends SignatureType<ST>> {

	public String name ( );
	
	public boolean isTimeStamped ( ) ;
	
	public SignCategory getCategory ( ) ;
	
	
	public enum SignCategory implements SignatureType<SignCategory> {
		
		CMS (	SignDisposition.TimeStamp.ATTRIBUTE, 
				SignDisposition.TimeStamp.DETACHED, 
				SignDisposition.TimeStamp.ENVELOPING 	),
		PDF ( 	SignDisposition.TimeStamp.DETACHED,
				SignDisposition.TimeStamp.ATTRIBUTE 	),
		XML ( 	SignDisposition.TimeStamp.DETACHED,
				SignDisposition.TimeStamp.ATTRIBUTE  	),
		TIMESTAMP ( );
		
		SignCategory ( SignDisposition.TimeStamp ... supportedTimeStampDispositions ) {
			this.supportedTimeStampDispositions = supportedTimeStampDispositions;
		}
		
		final SignDisposition.TimeStamp[] supportedTimeStampDispositions;
		
		public SignDisposition.TimeStamp[] supportedTimeStampDispositions ( ) {
			return supportedTimeStampDispositions;
		}
		
		public void assertSupportedTimeStampDisposition ( SignDisposition.TimeStamp tsDisposition ) {
			Assert.isTrue( ArrayUtils.contains(supportedTimeStampDispositions, tsDisposition) );
		}
		
		
		public boolean isTimeStamped() {
			return true;
//			throw new UnsupportedOperationException ( "timestamping not supported for general signature categories" );
		}

		@Override
		public SignCategory getCategory() {
			return this;
		}
	}
	
	public static enum CMS implements SignatureType<CMS> {
		
		CMS			( false ),
		CMS_T		( true ),
		CAdES_BES		( false ),
		CAdES_T		( true );
		
		private final boolean timestamped;
		
		CMS ( boolean timestamped ) {
			this.timestamped = timestamped;
		}

		public boolean isTimeStamped() {
			return timestamped;
		}

		@Override
		public SignCategory getCategory() {
			return SignCategory.CMS;
		}
	}
	
	public static enum PDF implements SignatureType<PDF> {
		
		PDF			( false ),
		PDF_T		( true ),
		PAdES		( false ),
		PAdES_T	 	( true );
		
		private final boolean timestamped;
		
		PDF ( boolean timestamped ) {
			this.timestamped = timestamped;
		}

		public boolean isTimeStamped() {
			return timestamped;
		}
		
		@Override
		public SignCategory getCategory() {
			return SignCategory.PDF;
		}
	}
	
	public static enum XML implements SignatureType<XML> {
		
		XML			( false ),
		XML_T		( true ),
		XAdES		( false ),
		XAdES_T		( true );
		
		private final boolean timestamped;
		
		XML ( boolean timestamped ) {
			this.timestamped = timestamped;
		}

		public boolean isTimeStamped() {
			return timestamped;
		}
		
		@Override
		public SignCategory getCategory() {
			return SignCategory.XML;
		}
	}
	
	public static enum TimeStamp implements SignatureType<TimeStamp> {
		TIMESTAMP;

		@Override
		public boolean isTimeStamped() {
			return false;
		}

		@Override
		public SignCategory getCategory() {
			return SignCategory.TIMESTAMP;
		}
		
	}


}
