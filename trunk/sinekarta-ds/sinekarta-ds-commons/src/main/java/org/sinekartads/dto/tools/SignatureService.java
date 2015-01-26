/*
 * eID Applet Project.
 * Copyright (C) 2008-2009 FedICT.
 * Copyright (C) 2014 e-Contract.be BVBA.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */

package org.sinekartads.dto.tools;

import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.jcl.JclResponseDTO;


/**
 * Common signature service protocol for any signature type. <br>
 * 
 * The signature process involves three different steps:
 * <ul>
 * <li>preSign: the service simulates the envelope generation in order to gain the relative digest
 * <li>sign: the digital signature is then evaluated externally at the client side on the digest
 * obtained during the previous step
 * <li>postSign: the service receives this digitalSignature, create an envelope equivalent to 
 * the one generated at the preSign phase and fills it with the signature bytes
 * </ul>
 * 
 * The input for both the preSign and postSign methods is the base64 encoding of the {@link SignatureDTO}
 * to be applied and the hex content that will receive the signature.
 * Moreover, both of them will return as a response a base64-encoded {@link JclResponseDTO} which contains 
 * the resulting SignatureDTO, if succeeded, or an error message if anything fails. This response object
 * may include additional information depending on the called method, see specific documentation. 
 * 
 * 		cenni sulla marca
 * 
 * @author adeprato
 */
public interface SignatureService {

	// -----
	// --- Pre-Sign phase
	// -
	
	/**
	 * The propose of this method  is to perform a first signature envelope generation, without
	 * the real digital signature application. This will allow the service to obtain the digest
	 * that will be signed on the client side. <br/>
	 * Since every signature format include a claimed signing time, it is needed to store the value
	 * that will generated during the envelope generation in order to allow the following postSign
	 * phase to create an identical structure. Moreover, depending on the signature type there could
	 * be other specific time-based or random information which will receive a different value during
	 * the next step. For this reason, even those elements will be stored into the returned SignatureDTO
	 * and loaded afterwards.
	 * @param chainSignatureBase64 the base64-encoded SignatureDTO
	 * It is expected to contain the signature and digest algorithms and the certificate chain.
	 * @param contentBase64 the base64-encoded bytes that will be signed
	 * @return a base64-encoded {@link PreSignResponseDTO} enveloping a SignatureDTO which presents 
	 * the generated envelope digest and some signature type specific information
	 */
	public String preSign (
			String chainSignatureBase64,
			String contentBase64 ) ;
	
	
	
	// -----
	// --- Post-Sign phase - apply contestually the timeStamp if required by the SignatureDTO
	// -
	
	/**
	 * This method produces the signed, and eventually marked, document and send its content back
	 * as an base64-encoded string into the resulting {@link PostSignResponseDTO} object.<br>
	 * The method will rebuild an envelope equivalent to the first one injecting the signing time
	 * and the various signature type specific data. This mechanism will grant that the digest of the
	 * new envelope will match with the one which has been signed.
	 * The digital signature evaluated on the client side is then injected into the envelope.
	 * After this operation, if any timestamp is required, the service will connect with the TimeStamp
	 * Authority, gain the timestamp token and add it to signed document.
	 * After the signed envelope has been built, it is encoded as a base64 string and added to the 
	 * response object that will be returned to the client.
	 * @param signedSignatureBase64  the base64-encoded SignatureDTO, it equals to the result of the
	 * preSign phase, with the addition of the digital signature evaluated on the client side 
	 * @param contentBase64 contentBase64 the base64-encoded bytes that will be signed
	 * @return a base64-encoded {@link PostSignResponseDTO} enveloping a SignatureDTO which presents
	 * the addition of the optional timeStamps 
	 */
	public String postSign ( 
			String signedSignatureBase64,
			String contentBase64 ) ;
	
	
	
	// -----
	// --- Mark phase
	// -

	/**
	 * This operation applies the timestamp to an existing signed document.
	 * The resulting {@link ApplyMarkResponseDTO} will contain a TimeStampResponseDTO which allows to
	 * store the TimeStampResponse received from the timeStampAutority as a detached timestamp
	 * and the nested TimeStampToken that can be added to a marked signature.
	 * @param tsRequest the base64-encoded timeStamping request, holds the message imprint of the
	 * signed document, the TimeStamp Authority URL and the user authentication credentials 
	 * @param contentBase64 the base64-encoded original file content which the detached signature
	 * refers to
	 * @param detachedSignBase64 the base64-encoded detached signature 
	 * @param embeddedSignBase64 the base64-encoded embedded signature
	 * @return a response object containing the TimeStampResponseDTO and the base64-encoded marked
	 * document 
	 */
	public String applyTimeStamp (
			String tsRequest,
			String contentBase64,
			String detachedSignBase64,
			String embeddedSignBase64 ) ;
	
	
	
	// -----
	// --- Verify phase
	// -
	
	/**
	 * Digital signature verification of a signed, and eventually marked, document.
	 * The signed envelope is verified against its contents, if embedded inside the envelope itself, 
	 * or the external content, otherwise. 
	 * The {@link VerifyResponseDTO} that will returned as output contains a SignatureDTO which describes 
	 * the details of the signature contained inside the document. Moreover, if the signature is embedding
	 * the content, its bytes encoded as a base64 string will be added to the response. 
	 * @param envelopeBase64
	 * @param contentBase64
	 * @param tsResponseBase64
	 * @return
	 */
	public String verify (
			String envelopeBase64,
			String contentBase64,
			String tsResponseBase64) ;

}
