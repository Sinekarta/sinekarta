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
package org.sinekartads.alfresco.webscripts.core;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.VerifyDTO;
import org.sinekartads.dto.jcl.VerifyResponseDTO;
import org.sinekartads.dto.request.SkdsVerifyRequest;
import org.sinekartads.dto.response.SkdsVerifyResponse;
import org.sinekartads.dto.tools.SignatureService;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.util.Assert;

public class SkdsVerifyWS 
		extends SkdsBaseSignWS<SkdsVerifyRequest, SkdsVerifyResponse> {
	
	@Override
	protected SkdsVerifyResponse executeImpl ( SkdsVerifyRequest req, 
                 			   				   Status status, 
                 			   				   Cache cache ) {

		DocumentDTO document = null;
		VerifyDTO verifyResults = null;
		SkdsVerifyResponse resp = new SkdsVerifyResponse();
		
		try {
			String minSecurityLevel = req.getMinSecurityLevel();
			document = req.documentFromBase64();
			
			// Retrieve the content and the optional verification data
			String contentHex = loadFromNode ( document.getBaseDocument() );
			String tsResponse = loadFromNode ( document.getTimeStamp() );
			String envelope   = loadFromNode ( document.getMarkedSign() );
			VerifyResponseDTO	verifyResp = null;
			SignatureService signatureService;
			
			Assert.notNull(envelope);
		
			if ( verifyResults == null ) {
				try {
					signatureService = getSignatureService ( SignCategory.CMS );
					String hexResp = signatureService.verify ( contentHex, 
															   tsResponse, 
															   envelope, 
															   minSecurityLevel );
					verifyResp = BaseDTO.deserializeHex ( VerifyResponseDTO.class, hexResp );
					verifyResults = extractResult ( VerifyDTO.class, hexResp );
				} catch (Exception e) {
					// to nothing, try with the next signature type
				} 
			}
			
			if ( verifyResults == null ) {
				try {
					signatureService = getSignatureService ( SignCategory.PDF );
					String hexResp = signatureService.verify ( contentHex, 
															   tsResponse, 
															   envelope, 
															   minSecurityLevel );
					verifyResp = BaseDTO.deserializeHex ( VerifyResponseDTO.class, hexResp );
					verifyResults = extractResult ( VerifyDTO.class, hexResp );
				} catch (Exception e) {
					// to nothing, try with the next signature type
				}  
			}
			
			if ( verifyResults == null ) {
				try {
					signatureService = getSignatureService ( SignCategory.XML );
					String hexResp = signatureService.verify ( contentHex, 
															   tsResponse, 
															   envelope, 
															   minSecurityLevel );
					verifyResp = BaseDTO.deserializeHex ( VerifyResponseDTO.class, hexResp );
					verifyResults = extractResult ( VerifyDTO.class, hexResp );
				} catch (Exception e) {
					// to nothing, try with the next signature type
				}  
			}
			
			if ( verifyResults == null ) {
				throw new Exception("unable to verify the signature for the given document");
			}
			
			// Add a document reference to the verifyResults
			verifyResults.setDocument(document);
			
			// Store the content document if necessary
			if ( StringUtils.isEmpty(contentHex) ) {
				// TODO manage the extracted document node generation
				storeIntoNode(document.getBaseDocument(), verifyResp.getExtractedContent());
			}
		} catch (Exception e) {
			document = null;
			processError ( resp, e );
		} finally {
			// Populate the response with the updated documents
			resp.verifyInfoToBase64(verifyResults);
		}
		
		return resp;
	}
}




//String extractionParent = req.getExtractionRef();
//String destName = documentDto.getDestName();
//String description = documentDto.getDescription();		
//NodeRef nodeRef = new NodeRef(documentDto.getNodeRef());
//String destPath;
//
//boolean extractContent = StringUtils.isNotBlank(extractionParent) && StringUtils.isNotBlank(destName);
//
//NodeRef destRef;
//NodeRef parentRef;
//if(extractContent) { 
//	// extractionParent and destName set --> the file must not exists unless replaceFile==true 
//	parentRef = new NodeRef(extractionParent);
//	destPath = NodeTools.translatePath(nodeService, parentRef);
//	destRef = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, destName);
//	if(destRef != null && replaceFile == false) {
//		throw new SinekartaDsBadServerRequestException(ErrorCode.DUPLICATED_NAME, destPath+destName);
//	}
//	// destRef set (if replaceFile==true) or to be created (see below)
//} else {
//	// no extractionParent set --> temporaryFile int sinekarta tmp/ folder
//	parentRef = new NodeRef(configuration.getUserSpaceTemporaryFolder());
//	destPath = NodeTools.translatePath(nodeService, parentRef) + "/";
//	destRef = null;
//}
//
//Action sinekartaDsVerifyAction;
//// create and init the action
//if(StringUtils.equalsIgnoreCase(documentDto.getMimetype(), "application/xml")) {
//	throw new SinekartaDsBadServerRequestException(ErrorCode.UNSUPPORTED_SIGNATURE, documentDto.getMimetype());
//} else if(StringUtils.equalsIgnoreCase(documentDto.getMimetype(), "application/pdf")) {
//	throw new SinekartaDsBadServerRequestException(ErrorCode.UNSUPPORTED_SIGNATURE, documentDto.getMimetype());
//} else {
//	sinekartaDsVerifyAction = actionService.createAction(SinekartaDsCmsVerify.ACTION_NAME);
//}
//
//byte[] content = null;
//VerifyInfo verifyInfo = null;
//// execute the action and retrieve the results
//try {
//	actionService.executeAction(sinekartaDsVerifyAction, nodeRef, false, false);
//	verifyInfo = (VerifyInfo)sinekartaDsVerifyAction.getParameterValue(SinekartaDsCmsVerify.PARAM_RESULT);
//	content = (byte[])sinekartaDsVerifyAction.getParameterValue(SinekartaDsCmsVerify.PARAM_CONTENT);
//} catch(SinekartaDsAlfrescoException e) {
//	throw e;
//} catch(RuntimeException e) {
//	tracer.error(e.getMessage(), e);
//	throw new SinekartaDsServerError(e);
//}
//
//// create the file if it doesn't exists
//if(destRef == null) {
//	if(extractContent) {
//		// create the node at the user specified  location
//		destRef = NodeTools.createNode(nodeService, parentRef, destName);
//	} else {
//		// create a temporary file otherwise  
//		String tmpName;
//		// select a random tmpName which doen't exists into the 
//		do {
//			tmpName = HexUtils.randomHex(32);
//			destRef = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, destName);
//		} while(destRef != null);
//		// create the node
//		destName = tmpName;
//		destRef = NodeTools.createNode(nodeService, parentRef, destName);
//		// add the TEMPORARY_FILE aspect
//		Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
//		Calendar cal = Calendar.getInstance(); 
//		aspectProperties.put(SinekartaDsModel.PROP_QNAME_CREATION_DATE, cal.getTime());
//		cal.add(Calendar.DAY_OF_YEAR, configuration.getTemporaryFileTtl());
//		aspectProperties.put(SinekartaDsModel.PROP_QNAME_TIME_TO_LIVE, configuration.getTemporaryFileTtl());
//		aspectProperties.put(SinekartaDsModel.PROP_QNAME_EXPIRING_DATE, cal.getTime());
//		nodeService.addAspect(destRef, SinekartaDsModel.ASPECT_QNAME_TEMPORARY_FILE, aspectProperties);
//	}
//}
//
//// add or update the EXTRACTED_DOCUMENT aspect
//Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
//aspectProperties.put(SinekartaDsModel.PROP_QNAME_SOURCE_FILE, nodeRef.toString());
//aspectProperties.put(SinekartaDsModel.PROP_QNAME_EXTRACTION_DATE, new Date());
//nodeService.addAspect(destRef, SinekartaDsModel.ASPECT_QNAME_EXTRACTED_DOCUMENT, aspectProperties);
//
//// add or update the properties
//nodeService.setProperty(destRef, ContentModel.PROP_NAME, destName);
//nodeService.setProperty(destRef, ContentModel.PROP_DESCRIPTION, description);
//
//// put the content
//ContentWriter writer = contentService.getWriter(destRef, ContentModel.PROP_CONTENT, true);
//InputStream is = new ByteArrayInputStream(content);
//try {
//	writer.putContent(is);
//} finally {
//	IOUtils.closeQuietly(is);
//}		
//
//// create the verifyInfoDto
//List<SignerInfoDTO> validSigners = new ArrayList<SignerInfoDTO>();
//List<SignerInfoDTO> untrustedSigners = new ArrayList<SignerInfoDTO>();
//List<SignerInfoDTO> invalidSigners = new ArrayList<SignerInfoDTO>();
//SignerInfoDTO dto;
//VerifyResult verifyResult;
//for(SignerInfo signerInfo : verifyInfo.getSignerInfos()) {
//	dto = new SignerInfoDTO();
//	verifyResult = signerInfo.getVerifyResult();
//	dto.setVerifyResult(verifyResult.literal);
//	dto.setFlagCounterSignature(Boolean.toString(signerInfo.getCounterSignature()));
//	dto.setSubjectAlias(signerInfo.getSubjectAlias());
//	dto.setIssuer(signerInfo.getIssuer());
//	dto.setOrganizationName(signerInfo.getOrganizationName());
//	dto.setOrganizationUnitName(signerInfo.getOrganizationUnitName());
//	dto.signingTimeToString(signerInfo.getSigningTime());
//	dto.setCountryName(signerInfo.getCountryName());
//	dto.notBeforeToString(signerInfo.getNotBefore());
//	dto.notAfterToString(signerInfo.getNotAfter());
//	dto.setQcStatements(signerInfo.getQcStatements());
//	dto.certificateToHex(signerInfo.getX509Certificate());
//	if(verifyResult == VerifyResult.VALID) {
//		validSigners.add(dto);				
//	}  else if(verifyResult == VerifyResult.UNTRUSTED) {
//		untrustedSigners.add(dto);
//	} else {
//		invalidSigners.add(dto);
//	}
//}
//VerifyDTO verifyInfoDto = new VerifyDTO();
//verifyInfoDto.setSignatureType(verifyInfo.getSignatureType().literal);
//verifyInfoDto.setSignatureDisposition(verifyInfo.getSignatureDisposition().literal);
//verifyInfoDto.setDocument(documentDto);
//verifyInfoDto.setValidSigners(validSigners.toArray(new SignerInfoDTO[validSigners.size()]));
//verifyInfoDto.setUntrustedSigners(untrustedSigners.toArray(new SignerInfoDTO[untrustedSigners.size()]));
//verifyInfoDto.setInvalidSigners(invalidSigners.toArray(new SignerInfoDTO[invalidSigners.size()]));

// prepare and return the response
//SinekartaDsVerifyResponse resp = new SinekartaDsVerifyResponse();
//resp.setDestRef(destRef.toString());
//resp.verifyInfoToBase64(verifyInfoDto);