package org.sinekartads.model.domain;



public class TsResponseInfo {
	
	public TsResponseInfo ( 
			byte[] encTimeStampResponse, 
			TimeStampInfo timeStampInfo ) {
		
		this.encTimeStampResponse = encTimeStampResponse;
		this.timeStamp = timeStampInfo;
	}
	
	private final byte[] encTimeStampResponse;
	private final TimeStampInfo timeStamp;
	
	public byte[] getEncTimeStampResponse() {
		return encTimeStampResponse;
	}

	public TimeStampInfo getTimeStamp() {
		return timeStamp;
	}
}
