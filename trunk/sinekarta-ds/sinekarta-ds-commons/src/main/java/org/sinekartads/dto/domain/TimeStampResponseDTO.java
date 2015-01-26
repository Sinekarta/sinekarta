package org.sinekartads.dto.domain;

import java.security.cert.CertificateException;

import org.sinekartads.dto.BaseDTO;
import org.sinekartads.util.HexUtils;

public class TimeStampResponseDTO extends BaseDTO {

	private static final long serialVersionUID = 5211381154218052951L;
	
	private TimeStampDTO timeStamp;
	private String hexTimeStampResponse;
	
	public TimeStampDTO getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(TimeStampDTO timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getHexTimeStampResponse() {
		return hexTimeStampResponse;
	}

	public void setHexTimeStampResponse(String hexTimeStampResponse) {
		this.hexTimeStampResponse = hexTimeStampResponse;
	}

	public void encTimeStampResponseToHex(byte[] encTimeStampToken) {
    	try {
			if(encTimeStampToken != null) {
				hexTimeStampResponse = HexUtils.encodeHex(encTimeStampToken);
			} else {
				hexTimeStampResponse = null;
			}
    	} catch(Exception e) {
    		// it should be never thrown
    		throw new RuntimeException(e);
    	}
	}
	
	public byte[] encTimeStampResponseFromHex() throws CertificateException {
		byte[] encTimeStampToken = null;
		try {
			if(hexTimeStampResponse != null) {
				encTimeStampToken = HexUtils.decodeHex(hexTimeStampResponse);
			}
		} catch ( Exception e ) {
			throw new RuntimeException(e);
		}
		
		return encTimeStampToken;
	}
	
}

