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

import java.io.IOException;
import java.security.SignatureException;

import org.apache.commons.io.FileUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.request.SkdsSignRequest.SkdsPreSignRequest;
import org.sinekartads.dto.response.SkdsSignResponse;
import org.sinekartads.dto.response.SkdsSignResponse.SkdsPreSignResponse;
import org.sinekartads.dto.tools.DTOConverter;
import org.sinekartads.dto.tools.SignatureService;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.util.SerializationUtils;

public class SkdsPreSignWS 
		extends SkdsBaseSignWS<SkdsPreSignRequest, SkdsSignResponse> {

	static final DTOConverter converter = DTOConverter.getInstance();
	
	@Override
	protected SkdsPreSignResponse executeImpl ( SkdsPreSignRequest req, 
                     			   				Status status, 
                     			   				Cache cache ) {

		DocumentDTO[] documents = null;
		SkdsPreSignResponse resp = new SkdsPreSignResponse();
		try {
			documents = req.documentsFromBase64();
			
			int 			 	lastIndex;
			SignatureDTO[] 		signatures;
			SignatureDTO	 	chainSignature;
			SignatureDTO	 	digestSignature;
			SignCategory 	 	signCategory;
			SignatureService 	signatureService;
			String 			 	contentHex;
			
			for ( DocumentDTO document : documents ) {
				signatures 		= document.getSignatures ( );
				lastIndex 		= signatures.length - 1;
				chainSignature 	= signatures [ lastIndex ];
				
				signCategory = chainSignature.signCategoryFromString ( );
				signatureService = getSignatureService ( signCategory );
				
				try {
					// Retrieve the content
					contentHex = loadFromNode ( document.getBaseDocument() );
					FileUtils.writeByteArrayToFile(new java.io.File("/home/adeprato/hex.txt"), BaseDTO.serializeJSON(chainSignature).getBytes());
//					FileUtils.writeByteArrayToFile(new java.io.File("/home/adeprato/hex.txt"), SerializationUtils.serialize(chainSignature));
					// Perform the pre-sign phase
					try {
						String responseHex = signatureService.preSign(chainSignature.toHex(), contentHex);
						digestSignature = extractResult ( SignatureDTO.class, responseHex );
					} catch(Exception e) {
						tracer.error("error during the pre sign phase", e);
						throw e;
					}
					
					// Replace the chainSignature with the evaluated digestSignature
					signatures [ lastIndex ] = digestSignature;
				} catch (SignatureException | IOException e) {
					processError ( resp, e, "error during the fingerPrint evaluation: %s" );
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