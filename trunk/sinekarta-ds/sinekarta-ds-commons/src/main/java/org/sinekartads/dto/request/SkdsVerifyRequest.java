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
package org.sinekartads.dto.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.formats.FlagDTOProperty;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;

@XmlRootElement(name = "SinekartaDsDocumentVerifyRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkdsVerifyRequest extends BaseRequest {

	private static final long serialVersionUID = -7664506240352914238L;

	private String documentBase64;
	@FlagDTOProperty	
	private String minSecurityLevel;
	
	
	
	public DocumentDTO documentFromBase64() {
		return deserializeHex ( DocumentDTO.class, documentBase64 );
	}
	
	public void documentToBase64(DocumentDTO document) {
		documentBase64 = serializeHex(document);
	}

	public void setDocumentBase64(String documentBase64) {
		this.documentBase64 = documentBase64;
	}
	public String getDocumentBase64() {
		return documentBase64;
	}

	public String getMinSecurityLevel() {
		return minSecurityLevel;
	}

	public void setMinSecurityLevel(String minSecurityLevel) {
		this.minSecurityLevel = minSecurityLevel;
	}
	
	public VerifyResult minSecurityLevelFromString () {
		return VerifyResult.valueOf(minSecurityLevel);
	}
	
	public void minSecurityLevelToString (VerifyResult minSecurityLevel) {
		this.minSecurityLevel = minSecurityLevel.name(); 
	}

}
