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
package org.sinekartads.dto.domain;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.formats.FlagDTOProperty;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;

public class VerifyDTO extends BaseDTO {
	
	private static final long serialVersionUID = -2375852914919974189L;
	
	public static VerifyDTO fromBase64 ( String base64 ) {
		return BaseDTO.fromHex ( base64, VerifyDTO.class );
	}
	
	
	
	// ----- 
	// --- Simple properties
	// -
	
	private DocumentDTO document;
	private SignatureDTO[] signatures;
	private String minSecurityLevel;
	@FlagDTOProperty
	private String extracted;
	
	public DocumentDTO getDocument() {
		return document;
	}

	public void setDocument(DocumentDTO document) {
		this.document = document;
	}

	public SignatureDTO[] getSignatures() {
		return signatures;
	}

	public void setSignatures(SignatureDTO[] signatures) {
		this.signatures = signatures;
	}

	public String getMinSecurityLevel() {
		return minSecurityLevel;
	}

	public void setMinSecurityLevel(String minSecurityLevel) {
		this.minSecurityLevel = minSecurityLevel;
	}
	
	public VerifyResult minSecurityLevelFromString() {
		VerifyResult minSecLevel;
		if ( StringUtils.isNotBlank(minSecurityLevel) ) {
			minSecLevel = VerifyResult.valueOf(minSecurityLevel);
		} else {
			minSecLevel = null;
		}
		return minSecLevel;
	}
	
	public void minSecurityLevelToString(VerifyResult minSecurityLevel) {
		if ( minSecurityLevel != null) { 
			this.minSecurityLevel = minSecurityLevel.name();
		} else {
			this.minSecurityLevel = null;
		}
	}

	public String getExtracted() {
		return extracted;
	}

	public void setExtracted(String extracted) {
		this.extracted = extracted;
	}
	
	public Boolean extractedFromString() {
		return Boolean.valueOf(extracted); 
	}
	
	public void extractedToString(Boolean extracted) {
		this.extracted = BooleanUtils.toString(extracted, "true", "false");
	}
}
