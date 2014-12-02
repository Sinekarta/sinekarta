package org.sinekartads.dto.request;

import org.sinekartads.dto.domain.DocumentDTO;

public abstract class SkdsSignRequest extends BaseRequest {

	private static final long serialVersionUID = 5770544609426553783L;
	
	private String[] documentsBase64;
	
	
	public DocumentDTO[] documentsFromBase64() {
		return deserializeHex(DocumentDTO.class, documentsBase64); 
	}
	
	public void documentsToBase64(DocumentDTO[] documents) {
		documentsBase64 = serializeHex(documents);
	}
	
	public void setDocumentsBase64(String[] documentsBase64) {
		this.documentsBase64 = documentsBase64;
	}
	public String[] getDocumentsBase64() {
		return documentsBase64;
	}
	

	
	public static class SkdsPreSignRequest extends SkdsSignRequest {

		private static final long serialVersionUID = 4752473207246432061L;
	}
	
	public static class SkdsPostSignRequest extends SkdsSignRequest {
		
		private static final long serialVersionUID = 3439446591957941705L;
	}

}
