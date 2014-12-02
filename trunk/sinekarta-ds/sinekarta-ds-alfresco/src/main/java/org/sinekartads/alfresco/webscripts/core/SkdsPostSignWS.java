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

import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.domain.DocumentDTO;
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
						String hexResp = signatureService.postSign ( signedSignature.toHex(), contentHex );
						postSignResp = BaseDTO.deserializeHex ( PostSignResponseDTO.class, hexResp );
						finalizedSignature = extractResult ( SignatureDTO.class, hexResp );
					} catch(Exception e) {
						tracer.error("error during the digest evaluation", e);
						throw e;
					}
					
					// Store the results to the relative nodeRefs, if any
					storeIntoNode ( document.getDetachedSign(), postSignResp.getDetachedSign() );
					storeIntoNode ( document.getEmbeddedSign(), postSignResp.getEmbeddedSign() );
					storeIntoNode ( document.getTimeStamp(), 	postSignResp.getTsResponse() );
					storeIntoNode ( document.getMarkedSign(), 	postSignResp.getMarkedSign() );					
					
					// Replace the signedSignature with the evaluated finalizedSignature
					signatures [ lastIndex ] = finalizedSignature;
				} catch (Exception e) {
					processError ( resp, e );
				}
			}
		} catch (Exception e) {
			documents = null;
			processError ( resp, e );
		} finally {
			// Populate the response with the updated documents
			resp.documentsToBase64(documents);
		}

		return resp;
	}
}
