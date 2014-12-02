package org.sinekartads.dto.response;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.domain.VerifyDTO;

@XmlRootElement(name = "SinekartaDsVerifyResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkdsVerifyResponse extends BaseResponse {
	
	private static final long serialVersionUID = 5999146721492507923L;

	public static SkdsVerifyResponse fromXML(InputStream is) throws JAXBException {
		return (SkdsVerifyResponse)BaseDTO.fromXML(is, SkdsVerifyResponse.class);
	}

	public static SkdsVerifyResponse fromJSON(InputStream is) throws IOException {
		return (SkdsVerifyResponse)BaseDTO.fromJSON(is, SkdsVerifyResponse.class);
	}
	
	
	
	private String destRef;
	
	private String verifyInfoBase64;
	
	
	
	public VerifyDTO verifyInfoFromBase64() {
		return deserializeHex(VerifyDTO.class, verifyInfoBase64);
	}
	
	public void verifyInfoToBase64(VerifyDTO verifyInfoDto) {
		verifyInfoBase64 = serializeHex(verifyInfoDto);
	}

	
	
	public String getVerifyInfoBase64() {
		return verifyInfoBase64;
	}

	public void setVerifyInfoBase64(String verifyInfoBase64) {
		this.verifyInfoBase64 = verifyInfoBase64;
	}
	

	public String getDestRef() {
		return destRef;
	}

	public void setDestRef(String destRef) {
		this.destRef = destRef;
	}
	
}