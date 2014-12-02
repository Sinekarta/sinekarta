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


public interface SignatureService {

	// -----
	// --- Pre-Sign phase
	// -
	
	public String preSign (
			String chainSignatureBase64,
			String contentHex ) ;
	
	
	
	// -----
	// --- Post-Sign phase - apply contestually the timeStamp if required by the SignatureDTO
	// -
	
	public String postSign ( 
			String signedSignatureBase64,
			String contentHex ) ;
	
	
	
	// -----
	// --- Mark phase
	// -

	public String applyTimeStamp (
			String tsRequest,
			String contentHex,
			String detachedSignHex,
			String embeddedSignHex ) ;
	
	
	
	// -----
	// --- Verify phase
	// -
	
	public String verify (
			String envelopeHex,
			String contentHex,
			String tsResponseHex,
			String requiredSecurityLevel ) ;

}
