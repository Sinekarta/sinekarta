/*
 * Copyright (C) 2010 - 2012 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.sinekartads.dto.response;

import org.sinekartads.dto.domain.DocumentDTO;

public class SkdsDocumentDetailsResponse extends BaseResponse {

	private static final long serialVersionUID = -3373366314803845064L;
	
	private String[] documentsBase64;
	
	
	
	public DocumentDTO[] documentsFromBase64() {
		return deserializeHex(DocumentDTO.class, documentsBase64);
	}
	
	public void documentsToBase64(DocumentDTO[] documents) {
		documentsBase64 = serializeHex(documents);
	}
	
	/**
	 * @deprecated use documentsToBase64(DocumentDTO[]) instead
	 */
	public void setDocumentsBase64(String[] documentsBase64) {
		this.documentsBase64 = documentsBase64;
	}
	public String[] getDocumentsBase64() {
		return documentsBase64;
	}
	
}
