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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.sinekartads.dto.domain.DocumentDTO;

public abstract class SkdsSignResponse extends BaseResponse {

	private static final long serialVersionUID = 1795756709990551060L;
		
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
	
	@XmlRootElement(name = "SkdsPreSignResponse")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class SkdsPreSignResponse extends SkdsSignResponse {

		private static final long serialVersionUID = -4262507094738224501L;
	}
	
	@XmlRootElement(name = "SkdsPostSignResponse")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class SkdsPostSignResponse extends SkdsSignResponse {

		private static final long serialVersionUID = 5505867464840306253L;
	}
	
}
