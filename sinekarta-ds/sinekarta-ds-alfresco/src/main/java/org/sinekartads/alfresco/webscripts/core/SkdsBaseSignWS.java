package org.sinekartads.alfresco.webscripts.core;

import org.sinekartads.alfresco.webscripts.BaseAlfrescoWS;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.jcl.JclResponseDTO;
import org.sinekartads.dto.request.BaseRequest;
import org.sinekartads.dto.response.BaseResponse;
import org.sinekartads.dto.tools.SignatureService;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.JclUtils;


public abstract class SkdsBaseSignWS
				< Request  extends BaseRequest, 
				  Response extends BaseResponse > extends BaseAlfrescoWS<Request, Response> {

	protected <DTO extends BaseDTO> DTO extractResult(Class<DTO> dtoClass, String respHex) throws Exception {
		JclResponseDTO resp = BaseDTO.deserializeHex(JclResponseDTO.class, respHex);
		ResultCode resultCode = resp.resultCodeFromString();
		DTO dto;
		if ( resultCode == ResultCode.SUCCESS ) {
			dto = BaseDTO.deserializeHex(dtoClass, resp.getResult());
		} else {
			throw new Exception ( resp.getErrorMessage() );
		}
		return dto;
	}

	
	protected SignatureService getSignatureService ( SignCategory signCategory ) {
		
		SignatureService signatureService;
		
		JarClassLoader jcl = new JarClassLoader();
		jcl.add(getClass().getClassLoader().getResource("alfresco/extension/sinekarta-ds-alfresco/core"));
		JclObjectFactory factory = JclObjectFactory.getInstance();
		Object obj;
		
		switch ( signCategory ) {
			case CMS: {
				obj = factory.create ( jcl, "org.sinekartads.core.service.CMSSignatureService" );
				signatureService = JclUtils.cast ( obj, SignatureService.class );
				break;
			}
			case PDF: {
				obj = factory.create ( jcl, "org.sinekartads.core.service.PDFSignatureService" );
				signatureService = JclUtils.cast ( obj, SignatureService.class );
				break;
			} 
			case XML: {
				obj = factory.create ( jcl, "org.sinekartads.core.service.XMLSignatureService" );
				signatureService = JclUtils.cast ( obj, SignatureService.class );
				break;
			}
			default: {
				throw new IllegalArgumentException(String.format ( "unsupported signature type - %s", signCategory ));
			}
		}
		
		return signatureService;
	}
}
