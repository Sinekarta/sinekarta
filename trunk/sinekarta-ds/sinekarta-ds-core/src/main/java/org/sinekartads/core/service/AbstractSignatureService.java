package org.sinekartads.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.core.CoreConfiguration;
import org.sinekartads.core.cms.NoFilterSelector;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.domain.VerifyDTO;
import org.sinekartads.dto.jcl.ApplyMarkResponseDTO;
import org.sinekartads.dto.jcl.HexOutputStream;
import org.sinekartads.dto.jcl.JclResponseDTO;
import org.sinekartads.dto.jcl.PostSignResponseDTO;
import org.sinekartads.dto.jcl.VerifyResponseDTO;
import org.sinekartads.dto.tools.DTOConverter;
import org.sinekartads.dto.tools.SignatureService;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureInfo;
import org.sinekartads.model.domain.SignatureType;
import org.sinekartads.model.domain.TimeStampInfo;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.VerifyInfo;
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.TemplateUtils;

public abstract class AbstractSignatureService < ST extends SignatureType<ST>,
												 SD extends SignDisposition<SD>,
												 SI extends SignatureInfo<ST, SD, VerifyResult, SI> > 
		implements SignatureService {

	static Logger tracer = Logger.getLogger(AbstractSignatureService.class);
	static DTOConverter converter = DTOConverter.getInstance(); 
	static CoreConfiguration conf = CoreConfiguration.getInstance();
	static NoFilterSelector noFilterSelector =new NoFilterSelector();
	
	protected Class<SignatureDTO> dtoClass;
	protected TimeStampService timeStampService;
	
	public void setTimeStampService(TimeStampService timeStampService) {
		this.timeStampService = timeStampService;
	}
	
	protected VerifyResult minLevel ( VerifyResult result1, VerifyResult result2 ) {
		if ( result1.compareTo(result2) > 0) {
			return result2;
		}
		return result1;
	}
	
	
	
	// -----
	// --- Error management
	// -
	
	public void processError ( JclResponseDTO resp, Exception e ) {
		String errorMessage = e.getMessage();
		if ( StringUtils.isBlank(errorMessage) ) {
			errorMessage = e.getClass().getName();
		}
		resp.setError ( TemplateUtils.Encoding.serializeHex(e) );
		resp.setErrorType ( e.getClass().getName() );
		resp.setErrorMessage ( errorMessage );
		resp.resultCodeToString ( ResultCode.INTERNAL_SERVER_ERROR );
		tracer.error(String.format("error detected into the %s response - %s", resp.getClass(), errorMessage), e);
	}
	
	
	
	// -----
	// --- Pre-Sign phase
	// -
	
	@SuppressWarnings("unchecked")
	@Override
	public String preSign (
			String chainSignatureBase64,
			String contentHex ) {
		
		JclResponseDTO resp = new JclResponseDTO(); 
		try {
			
			ChainSignature	< ST, SD, VerifyResult, SI > chainSignature;
			DigestSignature	< ST, SD, VerifyResult, SI > digestSignature;
			InputStream contentIs = HexUtils.decodeHexToInputStream ( contentHex );
			
			SignatureDTO dto = (SignatureDTO) SignatureDTO.fromHex ( chainSignatureBase64, dtoClass );
			chainSignature 	= (SignatureInfo<ST, SD, VerifyResult, SI>) converter.toSignatureInfo ( dto );
			digestSignature = doPreSign ( chainSignature, contentIs );
			SignatureDTO result = (SignatureDTO) converter.fromSignatureInfo ( 
					(SignatureInfo < ST, SD, VerifyResult, SI >) digestSignature );
			resp.setResult(result.toHex());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toHex();
	}
	
	public abstract DigestSignature<ST, SD, VerifyResult, SI> doPreSign (
			ChainSignature<ST, SD, VerifyResult, SI> emptySignature,
			InputStream contentIs )
					throws SignatureException,
							IOException ;
	
	
	
	// -----
	// --- Post-Sign phase - apply contestually the timeStamp if required by the SignatureDTO
	// -
	
	@SuppressWarnings("unchecked")
	@Override
	public String postSign ( 
			String signedSignatureBase64,
			String contentHex ){
		
		HexOutputStream detachedSignOs = new HexOutputStream();
		HexOutputStream embeddedSignOs = new HexOutputStream();
		HexOutputStream tsResponseOs = new HexOutputStream();
		HexOutputStream markedSignOs = new HexOutputStream();
		
		PostSignResponseDTO resp = new PostSignResponseDTO(); 
		try {
			SignatureDTO dto = (SignatureDTO) SignatureDTO.fromHex ( signedSignatureBase64, dtoClass );
			SignedSignature	   < ST, SD, VerifyResult, SI > signedSignature;
			FinalizedSignature < ST, SD, VerifyResult, SI > finalizedSignature;
			InputStream contentIs = HexUtils.decodeHexToInputStream ( contentHex );
			
			signedSignature    = (SignatureInfo<ST, SD, VerifyResult, SI>) converter.toSignatureInfo(dto);
			finalizedSignature = doPostSign ( signedSignature, 
											  contentIs, 
											  detachedSignOs, 
											  embeddedSignOs, 
											  tsResponseOs, 
											  markedSignOs);
			
			dto = (SignatureDTO) converter.fromSignatureInfo ( 
						(SignatureInfo < ST, SD, VerifyResult, SI >) finalizedSignature );
			resp.setResult(dto.toHex());
			resp.setDetachedSign(detachedSignOs.getHex());
			resp.setEmbeddedSign(embeddedSignOs.getHex());
			resp.setTsResponse(tsResponseOs.getHex());
			resp.setMarkedSign(markedSignOs.getHex());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toHex();
	}
	
	public abstract FinalizedSignature<ST, SD, VerifyResult, SI> doPostSign ( 
			SignedSignature<ST, SD, VerifyResult, SI> digestSignature,
			InputStream contentIs,
			OutputStream detachedSignOs, 
			OutputStream embeddedSignOs, 
			OutputStream tsResponseOs, 
			OutputStream markedSignOs ) 
					throws SignatureException,
							CertificateException,
							IOException ;
	
	
	
	// -----
	// --- Mark phase
	// -

	@Override
	public String applyTimeStamp (
			String tsRequestBase64,
			String contentHex,
			String detachedSignHex,
			String embeddedSignHex ) {
		
		ApplyMarkResponseDTO resp = new ApplyMarkResponseDTO(); 
		HexOutputStream tsResponseOs = new HexOutputStream();
		HexOutputStream markedSignOs = new HexOutputStream();

		try {
			TimeStampRequestDTO requestDTO = BaseDTO.fromHex(tsRequestBase64, TimeStampRequestDTO.class);
			TsRequestInfo tsRequest = converter.toTsRequestInfo(requestDTO);
			InputStream contentIs = HexUtils.decodeHexToInputStream ( contentHex );
			InputStream detachedSignIs = HexUtils.decodeHexToInputStream ( detachedSignHex );
			InputStream embeddedSignIs = HexUtils.decodeHexToInputStream ( embeddedSignHex );
			
			TimeStampInfo timeStamp = doApplyTimeStamp ( tsRequest, 
														 contentIs, 
														 detachedSignIs, 
														 embeddedSignIs, 
														 tsResponseOs, 
														 markedSignOs );
			
			TimeStampDTO timeStampDTO = converter.fromTimeStampInfo ( timeStamp );
			resp.setResult(timeStampDTO.toHex());
			resp.setTsResponse(tsResponseOs.getHex());
			resp.setMarkedSign(markedSignOs.getHex());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toHex();
	}
	
	public abstract TimeStampInfo doApplyTimeStamp (
			TsRequestInfo tsRequest,
			InputStream contentIs,
			InputStream detachedSignIs,
			InputStream embeddedSignIs,
			OutputStream tsResponseOs,
			OutputStream markedSignOs ) 
					throws SignatureException,
							CertificateException,
							IOException ;
	
	
	
	// -----
	// --- Verify phase
	// -

	@Override
	public String verify ( 
			String envelopeHex,
			String contentHex,
			String tsResponseHex,
			String requiredSecurityLevel ) {
		
		VerifyResponseDTO resp = new VerifyResponseDTO ( ); 
		HexOutputStream extractedContentOs = new HexOutputStream();
		try {
			InputStream contentIs = HexUtils.decodeHexToInputStream ( contentHex );
			InputStream tsResponseIs = HexUtils.decodeHexToInputStream ( tsResponseHex );
			InputStream envelopeIs = HexUtils.decodeHexToInputStream ( envelopeHex );
			
			VerifyInfo verifyResult = doVerify ( contentIs, 
												 tsResponseIs, 
												 envelopeIs, 
												 VerifyResult.valueOf ( requiredSecurityLevel ), 
												 extractedContentOs ); 
			
			VerifyDTO verifyDTO = converter.fromVerifyInfo ( verifyResult );
			resp.setExtractedContent(extractedContentOs.getHex());
			resp.setResult(verifyDTO.toHex());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toHex();
	}

	public abstract VerifyInfo doVerify ( 
			InputStream contentIs,
			InputStream tsResponseIs,
			InputStream envelopeIs,
			VerifyResult requiredSecurityLevel,
			OutputStream contentOs ) 
					throws SignatureException,
							CertificateException,
							IOException ;
	
}
