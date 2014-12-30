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

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.sinekartads.alfresco.util.NodeTools;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.NodeDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.jcl.PostSignResponseDTO;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPostSignRequest;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPostSignResponse;
import org.sinekartads.dto.tools.SignatureService;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

public class SkdsPostSignWS 
		extends SkdsBaseSignWS<SkdsPostSignRequest, SkdsPostSignResponse> {

	@Override
	protected SkdsPostSignResponse executeImpl ( SkdsPostSignRequest req, 
                     			   				 Status status, 
                       			   				 Cache cache ) {

		DocumentDTO[] documents = null;
		SkdsPostSignResponse resp = new SkdsPostSignResponse();
		try {
			documents = req.documentsFromBase64();
			
			int 			 	lastIndex;
			SignatureDTO[] 	signatures;
			SignatureDTO  	signedSignature;
			SignatureDTO 	finalizedSignature;
			PostSignResponseDTO	postSignResp;
			SignCategory 	 	signCategory;
			SignatureService 	signatureService;
			String			 	contentHex;
			
			for ( DocumentDTO document : documents ) {
				signatures 		= document.getSignatures ( );
				lastIndex 		= signatures.length - 1;
				signedSignature	= signatures [ lastIndex ];
				
				signCategory = signedSignature.signCategoryFromString ( );
				signatureService = getSignatureService ( signCategory );
				
				try {
					// Retrieve the content
					contentHex = loadFromNode ( document.getBaseDocument() );
							
					// Perform the post-sign phase
					try {
						String base64Resp = signatureService.postSign ( signedSignature.toBase64(), contentHex );
						postSignResp = BaseDTO.deserializeBase64 ( PostSignResponseDTO.class, base64Resp );
						finalizedSignature = extractResult ( SignatureDTO.class, base64Resp );
					} catch(Exception e) {
						tracer.error("error during the digest evaluation", e);
						throw e;
					}
					
					// Create the destination node
					NodeDTO node = new NodeDTO();
					node.setDescription(signedSignature.getReason());
					node.setFileName(document.getDestName());
					node.setParentRef(document.getBaseDocument().getParentRef());
					if ( BaseDTO.isNotEmpty(signedSignature.getTimeStampRequest()) ) {
						document.setMarkedSign(node);
					} else {
						document.setEmbeddedSign(node);
					}
					
					// Store the results to the relative nodeRefs, if any
					storeIntoNode ( document.getDetachedSign(), postSignResp.getDetachedSign() );
					storeIntoNode ( document.getEmbeddedSign(), postSignResp.getEmbeddedSign() );
//					storeIntoNode ( document.getTimeStamp(), 	postSignResp.getTsResponse() );
					storeIntoNode ( document.getMarkedSign(), 	postSignResp.getMarkedSign() );
					
					// Complete the document details
					Date now = new Date();
					String mimetype;
					switch(finalizedSignature.signCategoryFromString()) {
						case XML: {
							mimetype = "text/xml";
							break;
						}
						case PDF: {
							mimetype = "application/pdf";
							break;
						}
						default: {
							mimetype = "application/octet-stream";
						}
					}
					node.creationDateToString(now);
					node.lastUpdateToString(now);
					node.setFilePath(NodeTools.translatePath ( nodeService, new NodeRef(node.getNodeRef()) ));
					node.setMimetype(mimetype);
					
					// Replace the signedSignature with the evaluated finalizedSignature
					signatures [ lastIndex ] = finalizedSignature;
					
					// Populate the response with the updated documents
					resp.documentsToBase64(documents);
				} catch (Exception e) {
					processError ( resp, e );
				}
			}
		} catch (Exception e) {
			documents = null;
			processError ( resp, e );
		}
		return resp;
	}
}
