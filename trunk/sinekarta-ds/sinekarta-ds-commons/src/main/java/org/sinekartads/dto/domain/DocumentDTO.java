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

import org.sinekartads.dto.BaseDTO;


public class DocumentDTO extends BaseDTO {

	private static final long serialVersionUID = -8285214506874983311L;

	private NodeDTO baseDocument;
	private NodeDTO detachedSign;
	private NodeDTO embeddedSign;
	private NodeDTO[] timeStamps;
	private NodeDTO markedSign;
	private String signCategory;
	private SignatureDTO[] signatures;
	private String baseName;
	private String extension;
	private String destName;

	public boolean isEmpty ( ) {
		if ( BaseDTO.isNotEmpty(baseDocument) )				return false;
		if ( BaseDTO.isNotEmpty(detachedSign) )				return false;
		if ( BaseDTO.isNotEmpty(embeddedSign) )				return false;
		if ( BaseDTO.isNotEmpty(markedSign) )				return false;
		for ( NodeDTO timeStamp : timeStamps ) {
			if ( BaseDTO.isNotEmpty(timeStamp) )			return false;
		}
		for ( SignatureDTO signature : signatures ) {
			if ( BaseDTO.isNotEmpty(signature) )			return false;
		}
		return true;
	}
	
	
	// -----
	// --- Simple properties
	// -

	public NodeDTO getBaseDocument() {
		return baseDocument;
	}

	public void setBaseDocument(NodeDTO baseDocument) {
		this.baseDocument = baseDocument;
	}

	public NodeDTO getDetachedSign() {
		return detachedSign;
	}

	public void setDetachedSign(NodeDTO detachedSign) {
		this.detachedSign = detachedSign;
	}

	public NodeDTO getEmbeddedSign() {
		return embeddedSign;
	}

	public void setEmbeddedSign(NodeDTO embeddedSign) {
		this.embeddedSign = embeddedSign;
	}
	
	public NodeDTO[] getTimeStamp() {
		return timeStamps;
	}

	public void setTimeStamp(NodeDTO[] timeStamps) {
		this.timeStamps = timeStamps;
	}

	public NodeDTO getMarkedSign() {
		return markedSign;
	}

	public void setMarkedSign(NodeDTO markedSign) {
		this.markedSign = markedSign;
	}

	public String getSignCategory() {
		return signCategory;
	}


	public void setSignCategory(String signCategory) {
		this.signCategory = signCategory;
	}


	public SignatureDTO[] getSignatures() {
		return signatures;
	}

	public void setSignatures(SignatureDTO[] signatures) {
		this.signatures = signatures;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String destName) {
		this.destName = destName;
	}
}
