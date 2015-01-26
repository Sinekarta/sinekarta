package org.sinekartads.core.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.security.cert.CertificateException;

import org.apache.commons.codec.binary.Base64;
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
import org.sinekartads.dto.jcl.Base64OutputStream;
import org.sinekartads.dto.jcl.JclResponseDTO;
import org.sinekartads.dto.jcl.PostSignResponseDTO;
import org.sinekartads.dto.jcl.PreSignResponseDTO;
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

/**
 * This class receives the input of its methods as base64-encoded strings and performs the relative
 * conversion to the DTO objects. 
 * After that, the concrete signing and verifying operations are demanded to the specific subclasses by 
 * means of abstract methods. When the execution of the called method ends, the result is added to a
 * {@link JclResponseDTO} instance that will be serialized as a base64 string. If any error occurs 
 * during the process, it is trapped and its message added to the response object.
 * 
 * @author adeprato
 */
public abstract class AbstractSignatureService < ST extends SignatureType<ST>,
												 SD extends SignDisposition<SD>,
												 SI extends SignatureInfo<ST, SD, VerifyResult, SI> > 
		implements SignatureService {

	static Logger tracer = Logger.getLogger(AbstractSignatureService.class);
	static DTOConverter converter = DTOConverter.getInstance(); 
	static CoreConfiguration conf = CoreConfiguration.getInstance();
	static NoFilterSelector noFilterSelector =new NoFilterSelector();
	
	protected Class<SignatureDTO> dtoClass;
	protected TimeStampService timeStampService = new TimeStampService();
	
	protected VerifyResult minLevel ( VerifyResult result1, VerifyResult result2 ) {
		if ( result1.compareTo(result2) > 0) {
			return result2;
		}
		return result1;
	}
	
	
	
	// -----
	// --- Error management
	// -
	
	private void processError ( JclResponseDTO resp, Exception e ) {
		String errorMessage = e.getMessage();
		if ( StringUtils.isBlank(errorMessage) ) {
			errorMessage = e.getClass().getName();
		}
		tracer.error(String.format("error detected into the %s response - %s", resp.getClass(), errorMessage), e);
		resp.setErrorMessage ( errorMessage );
		resp.resultCodeToString ( ResultCode.INTERNAL_SERVER_ERROR );
	}
	
	
	
	// -----
	// --- Pre-Sign phase
	// -
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String preSign (
			String chainSignatureBase64,
			String contentBase64 ) {
		
		JclResponseDTO resp = new PreSignResponseDTO(); 
		try {
			
			ChainSignature	< ST, SD, VerifyResult, SI > chainSignature;
			DigestSignature	< ST, SD, VerifyResult, SI > digestSignature;
			InputStream contentIs = new ByteArrayInputStream ( Base64.decodeBase64( contentBase64 ));
			
			SignatureDTO dto = (SignatureDTO) SignatureDTO.fromBase64 ( chainSignatureBase64, dtoClass );
			chainSignature 	= (SignatureInfo<ST, SD, VerifyResult, SI>) converter.toSignatureInfo ( dto );
			digestSignature = doPreSign ( chainSignature, contentIs );
			SignatureDTO result = (SignatureDTO) converter.fromSignatureInfo ( 
					(SignatureInfo < ST, SD, VerifyResult, SI >) digestSignature );
			resp.setResult(result.toBase64());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toBase64();
	}
	
	/**
	 * Concrete implementation of the preSign phase, implemented by the subclasses relative to the
	 * various signature types.
	 * @param chainSignature the signature object containing the signature and digest algorithms to be
	 * used during the process and the signing certificate chain that will be added to the envelope
	 * @param contentIs an inputStream which allows to read the content to be signed
	 * @return the same signature object holding now even the envelope digest
	 * @throws IOException if any error occurs during an IO operation
	 * @throws SignatureException if any error occurs during the signature process
	 */
	public abstract DigestSignature<ST, SD, VerifyResult, SI> doPreSign (
			ChainSignature<ST, SD, VerifyResult, SI> chainSignature,
			InputStream contentIs )
					throws SignatureException,
							IOException ;
	
	
	
	// -----
	// --- Post-Sign phase - apply contestually the timeStamp if required by the SignatureDTO
	// -
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String postSign ( 
			String signedSignatureBase64,
			String contentBase64 ){
		
		Base64OutputStream detachedSignOs = new Base64OutputStream();
		Base64OutputStream embeddedSignOs = new Base64OutputStream();
		Base64OutputStream tsResponseOs = new Base64OutputStream();
		Base64OutputStream markedSignOs = new Base64OutputStream();
		
		PostSignResponseDTO resp = new PostSignResponseDTO(); 
		try {
			SignatureDTO dto = (SignatureDTO) SignatureDTO.fromBase64 ( signedSignatureBase64, dtoClass );
			SignedSignature	   < ST, SD, VerifyResult, SI > signedSignature;
			FinalizedSignature < ST, SD, VerifyResult, SI > finalizedSignature;
			InputStream contentIs = new ByteArrayInputStream ( Base64.decodeBase64( contentBase64 ));
			
			signedSignature    = (SignatureInfo<ST, SD, VerifyResult, SI>) converter.toSignatureInfo(dto);
			finalizedSignature = doPostSign ( signedSignature, 
											  contentIs, 
											  detachedSignOs, 
											  embeddedSignOs, 
											  tsResponseOs, 
											  markedSignOs);
			
			dto = (SignatureDTO) converter.fromSignatureInfo ( 
						(SignatureInfo < ST, SD, VerifyResult, SI >) finalizedSignature );
			resp.setResult(dto.toBase64());
			resp.setDetachedSign(detachedSignOs.getBase64());
			resp.setEmbeddedSign(embeddedSignOs.getBase64());
			resp.setTsResponse(tsResponseOs.getBase64());
			resp.setMarkedSign(markedSignOs.getBase64());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toBase64();
	}
	

	/**
	 * Concrete implementation of the postSign phase, implemented by the subclasses relative to the
	 * various signature types.
	 * @param signedSignature the signature object that has been produced at the previous preSign step,
	 * after having been 
	 * @param contentIs an inputStream which allows to read the content to be signed	 
	 * @param detachedSignOs an outputStream which allows to send the envelope for a detached signature
	 * @param embeddedSignOs an outputStream which allows to send the envelope for a embedded signature
	 * @param tsResponseOs an outputStream which allows to send the timeStamp response as a detached timeStamp
	 * @param markedSignOs an outputStream which allows to send the envelope for a marked signature
	 * @return the same signature object holding now even the envelope digest
	 * @throws IOException if any error occurs during an IO operation
	 * @throws SignatureException if any error occurs during the signature process
	 */
	public abstract FinalizedSignature<ST, SD, VerifyResult, SI> doPostSign ( 
			SignedSignature<ST, SD, VerifyResult, SI> signedSignature,
			InputStream contentIs,
			OutputStream detachedSignOs, 
			OutputStream embeddedSignOs, 
			OutputStream tsResponseOs, 
			OutputStream markedSignOs ) 
					throws SignatureException,
						   IOException ;
	
	
	
	// -----
	// --- Mark phase
	// -

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String applyTimeStamp (
			String tsRequestBase64,
			String contentHex,
			String detachedSignHex,
			String embeddedSignHex ) {
		
		ApplyMarkResponseDTO resp = new ApplyMarkResponseDTO(); 
		Base64OutputStream tsResponseOs = new Base64OutputStream();
		Base64OutputStream markedSignOs = new Base64OutputStream();

		try {
			TimeStampRequestDTO requestDTO = BaseDTO.fromBase64(tsRequestBase64, TimeStampRequestDTO.class);
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
			resp.setResult(timeStampDTO.toBase64());
			resp.setTsResponse(tsResponseOs.getBase64());
			resp.setMarkedSign(markedSignOs.getBase64());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toBase64();
	}
	
	/**
	 * Concrete implementation of the mark phase, implemented by the subclasses relative to the
	 * various signature types.
	 * @param signedSignature the signature object containing the signature and digest algorithms to be
	 * used during the process and the signing certificate chain that will be added to the envelope
	 * @param contentIs an inputStream which allows to read the content to be signed	 
	 * @param detachedSignOs an outputStream which allows to send the envelope for a detached signature
	 * @param embeddedSignOs an outputStream which allows to send the envelope for a embedded signature
	 * @param tsResponseOs an outputStream which allows to send the timeStamp response as a detached timeStamp
	 * @param markedSignOs an outputStream which allows to send the envelope for a marked signature
	 * @return the same signature object holding now even the envelope digest
	 * @throws IOException if any error occurs during an IO operation
	 * @throws SignatureException if any error occurs during the signature process
	 */
	/**
	 * 
	 * @param tsRequest
	 * @param contentIs
	 * @param detachedSignIs
	 * @param embeddedSignIs
	 * @param tsResponseOs
	 * @param markedSignOs
	 * @return
	 * @throws IOException if any error occurs during an IO operation
	 * @throws SignatureException if any error occurs during the timeStamp process
	 */
	public abstract TimeStampInfo doApplyTimeStamp (
			TsRequestInfo tsRequest,
			InputStream contentIs,
			InputStream detachedSignIs,
			InputStream embeddedSignIs,
			OutputStream tsResponseOs,
			OutputStream markedSignOs ) 
					throws SignatureException,
						   IOException ;
	
	
	
	// -----
	// --- Verify phase
	// -

	@Override
	public String verify ( 
			String envelopeBase64,
			String contentBase64,
			String tsResponseBase64 ) {
		
		VerifyResponseDTO resp = new VerifyResponseDTO ( ); 
		Base64OutputStream extractedContentOs = new Base64OutputStream();
		try {
			InputStream contentIs = null;
			InputStream tsResponseIs = null;
			InputStream envelopeIs = null;
			if (StringUtils.isNotBlank(contentBase64)) {
				contentIs = new ByteArrayInputStream(Base64.decodeBase64( contentBase64 ));
			}
			if (StringUtils.isNotBlank(tsResponseBase64)) {
				tsResponseIs = new ByteArrayInputStream(Base64.decodeBase64( tsResponseBase64 ));
			}
			if (StringUtils.isNotBlank(envelopeBase64)) {
				envelopeIs = new ByteArrayInputStream(Base64.decodeBase64( envelopeBase64 ));
			}
			VerifyInfo verifyResult = doVerify ( contentIs, 
												 tsResponseIs, 
												 envelopeIs,
												 extractedContentOs ); 
			
			VerifyDTO verifyDTO = converter.fromVerifyInfo ( verifyResult );
			resp.setExtractedContent(extractedContentOs.getBase64());
			resp.setResult(verifyDTO.toBase64());
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch ( Exception e ) {
			processError ( resp, e );
		}
		
		return resp.toBase64();
	}

	public abstract VerifyInfo doVerify ( 
			InputStream contentIs,
			InputStream tsResponseIs,
			InputStream envelopeIs,
			OutputStream contentOs ) 
					throws SignatureException,
							CertificateException,
							IOException ;
	
}
