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
package org.sinekartads.share.webscripts.verify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.request.SkdsDocumentDetailsRequest;
import org.sinekartads.dto.response.SkdsDocumentDetailsResponse;
import org.sinekartads.dto.share.VerifyWizardDTO;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.share.util.JavaWebscriptTools;
import org.springframework.extensions.webscripts.WebScriptRequest;


public class SkdsVerifyInitWS extends BaseVerifyWS {

	@Override
	protected void prepareForm(
			WebScriptRequest req, VerifyWizardDTO wizardDto ) throws AlfrescoException {
		
		String verifyOperation = wizardDto.getVerifyOperation();
		String destName = wizardDto.getDestName();
		String pathChoice = wizardDto.getPathChoice();
		String extractionParent = wizardDto.getExtractionParent();	
		String flagReplaceFile = wizardDto.getReplaceFiles();
		
		DocumentDTO document = wizardDto.getDocument();
		try {
			if(document == null) {
				String[] nodeRefs = new String[] {wizardDto.getNodeRef()};			
				SkdsDocumentDetailsRequest ddreq = new SkdsDocumentDetailsRequest();
				ddreq.setNodeRefs(nodeRefs);
				SkdsDocumentDetailsResponse ddresp = postJsonRequest ( ddreq, SkdsDocumentDetailsResponse.class );
				document = ddresp.documentsFromBase64()[0];
			}
			
			if(StringUtils.isBlank(verifyOperation)) {
				verifyOperation = "justVerify";
			} 
			if(StringUtils.isBlank(pathChoice)) {
				pathChoice = "sameFolder";
			}
			if(StringUtils.isBlank(flagReplaceFile)) {
				flagReplaceFile = "false";
			} else if(flagReplaceFile.equals("on")) {
				flagReplaceFile = "true";
			}
			if(extractionParent == null) {
				extractionParent = "";
			}
			if(StringUtils.isBlank(destName)) {
				String fileName = document.getBaseDocument().getFileName();
				Matcher mtc = Pattern.compile("(\\w+\\.\\w+)\\.p7m").matcher(fileName);
				if(mtc.find()) {
					fileName = mtc.group(1);
				} 
//				destName = FilenameUtils.getBaseName(fileName) + conf.getExtractedSuffix() + "." + FilenameUtils.getExtension(fileName);
				// TODO populate the destName 
//				document.setDestName(destName);
			}
		} finally {
			wizardDto.setDocument(document);
			wizardDto.setReplaceFiles(flagReplaceFile);
			
//			model.put("destName", destName);
//			model.put("pathChoice", pathChoice);
//			model.put("verifyOperation", verifyOperation);
//			model.put("extractionParent", extractionParent);
//			model.put("flagReplaceFile", flagReplaceFile);
		}
	}

	@Override
	protected void processForm(WebScriptRequest req, VerifyWizardDTO wizardDto) {

		String verifyOperation = (String)JavaWebscriptTools.getRequestParameter(req, "verifyOperation");
		String pathChoice = (String)JavaWebscriptTools.getRequestParameter(req, "pathChoice");
//		String destName = (String)JavaWebscriptTools.getRequestParameter(req, "destName"); 
//		String description = (String)JavaWebscriptTools.getRequestParameter(req, "description");
		String flagReplaceFile = (String)JavaWebscriptTools.getRequestParameter(req, "flagReplaceFile");
		
		DocumentDTO document = wizardDto.getDocument();
		try {
			if(StringUtils.equals(verifyOperation, "verifyAndExtract")) {
				// TODO copy file details
//				document.setDestName(destName);
//				document.setDescription(description);
			}
		} finally {
			wizardDto.setDocument(document);
			wizardDto.setVerifyOperation(verifyOperation);
			wizardDto.setPathChoice(pathChoice);
			wizardDto.setReplaceFiles(flagReplaceFile);
		}
	}
 
	@Override
	protected String currentForm() {
		return "skdsVerifyInit";
	}
}