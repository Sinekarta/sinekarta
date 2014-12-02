package org.sinekartads.dto.tools;

import java.security.cert.CertificateException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.ArrayUtils;
import org.sinekartads.dto.BaseDTO;
import org.sinekartads.dto.domain.CertificateDTO;
import org.sinekartads.dto.domain.DigestDTO;
import org.sinekartads.dto.domain.KeyStoreDTO;
import org.sinekartads.dto.domain.SignatureDTO;
import org.sinekartads.dto.domain.TimeStampDTO;
import org.sinekartads.dto.domain.TimeStampRequestDTO;
import org.sinekartads.dto.domain.VerifyDTO;
import org.sinekartads.model.domain.CMSSignatureInfo;
import org.sinekartads.model.domain.CertificateInfo;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.domain.KeyStoreDescriptor;
import org.sinekartads.model.domain.PDFSignatureInfo;
import org.sinekartads.model.domain.SecurityLevel.TimeStampVerifyResult;
import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignDisposition;
import org.sinekartads.model.domain.SignatureInfo;
import org.sinekartads.model.domain.SignatureStatus;
import org.sinekartads.model.domain.SignatureType;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.TimeStampInfo;
import org.sinekartads.model.domain.Transitions.ChainSignature;
import org.sinekartads.model.domain.Transitions.DigestSignature;
import org.sinekartads.model.domain.Transitions.DisposedTimeStamp;
import org.sinekartads.model.domain.Transitions.EmptySignature;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.domain.Transitions.MarkedSignature;
import org.sinekartads.model.domain.Transitions.RawSignature;
import org.sinekartads.model.domain.Transitions.SignedSignature;
import org.sinekartads.model.domain.Transitions.UntrustedTimeStamp;
import org.sinekartads.model.domain.Transitions.VerifiedSignature;
import org.sinekartads.model.domain.Transitions.VerifiedTimeStamp;
import org.sinekartads.model.domain.TsRequestInfo;
import org.sinekartads.model.domain.TsRequestInfo.TsRequestStatus;
import org.sinekartads.model.domain.VerifyInfo;
import org.sinekartads.util.HexUtils;
import org.sinekartads.util.x509.X509Utils;

public class DTOConverter {

	// -----
	// --- Singleton protocol
	// -
	
	private static DTOConverter singleton;
	
	public static DTOConverter getInstance() {
		if ( singleton == null ) {
			singleton = new DTOConverter();
		}
		return singleton;
	}
	
	private DTOConverter() {
		
	}
	
	
	
	// -----
	// --- Multiple dto conversion
	// -
	
	
	@SuppressWarnings("unchecked")
	public BaseDTO[] fromEntities ( Object ... entities ) {
		int length;
		if ( ArrayUtils.isNotEmpty(entities) ) {
			length = entities.length;
		} else {
			length = 0;
		}
		
		BaseDTO[] dtos = new BaseDTO[entities.length];
		BaseDTO dto;
		Object entity;
		for ( int i=0; i<length; i++ ) {
			dto = null;
			entity = entities[i];
			if ( entity instanceof DigestInfo ) {
				dto = fromDigestInfo( (DigestInfo)entity );
			} else if ( entity instanceof KeyStoreDescriptor ) {
				dto = fromKeyStoreDescriptor( (KeyStoreDescriptor)entity );					
//			} else if ( entity instanceof CertificateInfo ) {
//				dtos[i] = fromCertificateInfo( (CertificateInfo)entity );
			} else if ( entity instanceof SignatureInfo ) {
				dto = fromSignatureInfo( (SignatureInfo<?,?,VerifyResult,?>)entity );
			} else {
				throw new UnsupportedOperationException(String.format (
						"unable to convert an instance of %s", entity.getClass() ));
			}
			dtos[i] = dto;
		}
		return dtos;
	}
	
	public Object[] toEntities ( BaseDTO ... dtos ) {
		Object[] entities = new Object[dtos.length];
		BaseDTO dto;
		for ( int i=0; i<dtos.length; i++ ) {
			dto = dtos[i];
			if ( dto != null ) {
				if ( dto instanceof DigestDTO ) {
					entities[i] = toDigestInfo( (DigestDTO)dto );
//				} else if ( dto instanceof CredentialDTO<?> ) {
//					CredentialDTO<?> credential = (CredentialDTO<?>)dto;
//					if ( credential.getEntityClass() == WebCredential.class ) {
//						entities[i] = (Entity) toCredential( (CredentialDTO<WebCredential>)dto );
//					} else if ( credential.getEntityClass() == KeyStoreCredential.class ) {
//						entities[i] = (Entity) toCredential( (CredentialDTO<KeyStoreCredential>)dto );
//					} else {
//						throw new UnsupportedOperationException(String.format ( 
//								"unsupported credential type - %s", credential.getClass() ));
//					}
				} else if ( dto instanceof CertificateDTO ) {
					entities[i] = toCertificateInfo( (CertificateDTO)dto );
				} else if ( dto instanceof SignatureDTO ) {
					entities[i] = toSignatureInfo( (SignatureDTO)dto );
				} else {
					throw new UnsupportedOperationException(String.format (
							"unable to convert an instance of %s", dto.getClass() ));
				}
			} else {
				entities[i] = null;
			}
		}
		return entities;
	}
	
	
	
	// -----
	// --- Single entity to DTO
	// -
	
	// DigestDTO
	
	public DigestDTO fromDigestInfo(DigestInfo digestInfo) {
		if ( digestInfo == null)								 					return null;
		DigestDTO dto = new DigestDTO();
		dto.fingerPrintToHex(digestInfo.getFingerPrint());
		dto.digestAlgorithmToName(digestInfo.getAlgorithm());
		return dto;
	}
	
	// KeyStoreDTO
	
	public KeyStoreDTO fromKeyStoreDescriptor ( 
			KeyStoreDescriptor ksDescriptor ) {
		
		if ( ksDescriptor == null )								 					return null;
		KeyStoreDTO ksDto = new KeyStoreDTO();
		ksDto.setName(ksDescriptor.getName());
		ksDto.setPin(ksDescriptor.getPin());
		ksDto.typeToString(ksDescriptor.getType());
		ksDto.setReference(ksDescriptor.getReference().toString());
		ksDto.setAliases(ksDescriptor.getAliases());
		return ksDto;
	}
	
	// CertificateDTO
	
//	public CertificateDTO fromCertificateInfo (
//			CertificateInfo certificate ) {
//		
//		return fromCertificateInfo ( certificate, false );
//	}
//		
//	public CertificateDTO fromCertificateInfo (
//			CertificateInfo certificate,
//			boolean buildChain ) {
//		
//		if ( certificate == null )								 					return null;
//		
//		CertificateDTO dto = new CertificateDTO ( );
//		dto.setSubjectAlias			( certificate.getSubjectAlias() );
//		dto.setIssuerAlias			( certificate.getIssuerAlias()) ;
//		dto.setEmailAddress			( certificate.getEmailAddress() );
//		dto.setOrganizationUnitName	( certificate.getOrganizationUnitName() );
//		dto.setOrganizationName		( certificate.getOrganizationName() );
//		dto.setLocalityName			( certificate.getLocalityName() );
//		dto.setStateOrProvinceName	( certificate.getStateOrProvinceName() );
//		dto.setCountryName			( certificate.getCountryName() );
////		dto.setSourceName			( certificate.getSource().getName() );
//		dto.notBeforeToString		( certificate.getNotBefore() );
//		dto.notAfterToString		( certificate.getNotAfter() );
//		dto.setQcStatements			( certificate.getQcStatements() );
//		dto.certificateToHex		( certificate );
//		
////			X509Certificate[] certificateChain = certificate.getChain();
////			CertificateDTO[] chain = new CertificateDTO[certificateChain.length];
////			for ( int i=0; i<chain.length; i++) {
////				chain[i] = fromCertificateInfo ( certificateChain[i], false );
////			}
////			dto.setChain ( chain );
////		}
//		return dto;
//	}
	
	// SignatureInfo
	
	public SignatureDTO fromSignatureInfo ( 
			SignatureInfo<?,?,VerifyResult,?> signature ) {
		
		if ( signature == null)									 					return null;
		
		SignatureType<?> signatureType = signature.getSignType();
		SignatureDTO dto = new SignatureDTO();
		switch ( signatureType.getCategory() ) {
			case CMS: {
				break;
			}
			case PDF: {
				SignatureDTO pdfDto = new SignatureDTO 	( );
				PDFSignatureInfo pdfSignature = 		( PDFSignatureInfo ) signature; 
				pdfDto.setPdfSignName					( pdfSignature.getName()				);
				pdfDto.setPdfRevision					( pdfSignature.getRevision()			);
				pdfDto.pdfCoversWholeDocumentToString  	( pdfSignature.getCoversWholeDocument()	);
				dto = pdfDto;
				break;
			}
			case XML: {
				break;
			} 
			default: {
				throw new UnsupportedOperationException(String.format ( 
						"unable to convert to a %s signature", signatureType ));
			} 
		}

		dto.signCategoryToString			( signature.getSignType().getCategory() );
		dto.signAlgorithmToString			( signature.getSignAlgorithm() );
		dto.digestAlgorithmToName			( signature.getDigestAlgorithm() );
		
		dto.counterSignatureToString		( signature.isCounterSignature() );
		dto.setReason						( signature.getReason() );
		dto.setLocation						( signature.getLocation() );
		dto.signingTimeToString				( signature.getSigningTime() );
		dto.finalizedToString               ( signature.isFinalized() );
		
		if ( signature.isFinalized() ) {
			dto.setHexCertificateChain	( X509Utils.rawX509CertificatesToHex(signature.getRawX509Certificates()) );
//			dto.setCertificate				( fromCertificateInfo(signature.getCertificate()) );
			dto.setDigest					( fromDigestInfo(signature.getDigest()) );
			dto.digitalSignatureToHex		( signature.getDigitalSignature() );
			dto.setTimeStamps				( (TimeStampDTO[])fromEntities((Object[])signature.getTimeStamps()) );
			if ( signature.isVerified() ) {
				dto.verifyResultToString	( signature.getVerifyResult() );
			} else {
				dto.setTimeStampRequest		( fromTsRequestInfo(signature.getTsRequest()) );
			}
		} else {
			dto.setTimeStampRequest			( fromTsRequestInfo(signature.getTsRequest()) );
			switch ( (SignatureStatus.SignProcess) signature.getStatus() ) {
				case MARKED: {
					dto.setTimeStamps			( (TimeStampDTO[])fromEntities((Object[])signature.getTimeStamps()) );
				}
				case SIGNED: {
					dto.digitalSignatureToHex	( signature.getDigitalSignature() );
				}
				case DIGEST: {
					dto.setDigest				( fromDigestInfo(signature.getDigest()) );
				}
				case CHAIN: {
					dto.setHexCertificateChain 	( X509Utils.rawX509CertificatesToHex(signature.getRawX509Certificates()) );
				}
				default:
			}
		}
		
		return dto;
	}
	
	// TimeStampInfo
	
	public TimeStampDTO fromTimeStampInfo ( TimeStampInfo timeStamp ) {
		
		if ( timeStamp == null)									 					return null;
		TimeStampDTO dto = new TimeStampDTO ( );
		dto.encTimeStampTokenToHex			( timeStamp.getEncTimeStampToken() );
		dto.signAlgorithmToString 			( timeStamp.getSignAlgorithm() );
		dto.digestAlgorithmToString 		( timeStamp.getDigestAlgorithm() );
		dto.digitalSignatureToHex 			( timeStamp.getDigitalSignature() );
//		dto.setCertificate 					( fromCertificateInfo(timeStamp.getCertificate()) );
		dto.setHexCertificateChain			( X509Utils.rawX509CertificatesToHex(timeStamp.getRawX509Certificates()) );
		dto.setMessageImprint				( fromDigestInfo(timeStamp.getMessageInprintInfo()) );
		dto.signingTimeToString 			( timeStamp.getSigningTime() );
		dto.setReason						( timeStamp.getReason() );
		dto.setLocation						( timeStamp.getLocation() );
		
		SignatureStatus status = timeStamp.getStatus();
		if ( status == SignatureStatus.Stable.FINALIZED) {
			dto.verifyResultToString			( timeStamp.getVerifyResult() );
			dto.dispositionToString				( (SignDisposition.TimeStamp)timeStamp.getDisposition() );
		} else if ( status == SignatureStatus.TimeStampVerifyProcess.VERIFIED ) {
			dto.verifyResultToString			( timeStamp.getVerifyResult() );
		}
		
		return dto;
	}
	
	// TsRequestInfo
	
	public TimeStampRequestDTO fromTsRequestInfo ( TsRequestInfo tsRequest ) {
		
		if ( tsRequest == null )										return null; 
		TimeStampRequestDTO dto = new TimeStampRequestDTO ( );
		if ( tsRequest.getStatus() == TsRequestStatus.DIGEST ) {
			dto.messageImprintDigestToHex 		( tsRequest.getMessageImprintDigest() );
		}
		dto.messageImprintAlgorithmToString ( tsRequest.getMessageImprintAlgorithm() );
		dto.nounceToString 					( tsRequest.getNounce() );
		dto.setTsUrl 						( tsRequest.getTsUrl() );
		dto.setTsUsername					( tsRequest.getTsUsername() );
		dto.setTsPassword					( tsRequest.getTsPassword() );
		dto.timestampDispositionToString	( tsRequest.getDisposition() );
		return dto;
	}
	
	// VerifyInfo
	
	public VerifyDTO fromVerifyInfo(VerifyInfo verifyInfo) {
		
		SignatureDTO[] signatures = new SignatureDTO[verifyInfo.getSignatures().size()];
		
		int i=0;
		for ( VerifiedSignature<?,?,VerifyResult,?> verifiedSignature : verifyInfo.getSignatures() ) {
			
			signatures[i] = fromSignatureInfo((SignatureInfo<?,?,VerifyResult,?>)verifiedSignature);
			i++;
		}
		
		VerifyDTO dto = new VerifyDTO();
		dto.setSignatures ( signatures );
		dto.extractedToString ( verifyInfo.isExtracted() );
		dto.minSecurityLevelToString ( verifyInfo.getMinSecurityLevel() );
		return dto;
	}
	
	public VerifyInfo toVerifyInfo ( VerifyDTO dto ) {
		
		VerifyInfo verifyResult = new VerifyInfo();
		for ( SignatureDTO verifiedSignatureDTO : dto.getSignatures() ) {
			verifyResult.addSignature ( toSignatureInfo(verifiedSignatureDTO) );
		}
		verifyResult.setExtracted ( dto.extractedFromString() );
		verifyResult.setMinSecurityLevel( dto.minSecurityLevelFromString() );
		return verifyResult;
	}
	
	
	
	// -----
	// --- Single DTO to entity
	// -
	
	// DigestInfo
	
	public DigestInfo toDigestInfo ( DigestDTO dto ) {
		
		if ( BaseDTO.isEmpty(dto) )									 				return null;
		return DigestInfo.getInstance (
				dto.getDigestAlgorithmName(), 
				HexUtils.decodeHex(dto.getHexFingerPrint()) );
	}
	
	// KeyStoreDescriptor
	
	public KeyStoreDescriptor toKeyStoreDescriptor ( 
			KeyStoreDTO dto ) {

		if ( BaseDTO.isEmpty(dto) )						 							return null;
		KeyStoreDescriptor ksDescriptor = new KeyStoreDescriptor (
				dto.getName(), 
				new NodeRef(dto.getReference()), 
				dto.typeFromString(), 
				dto.getPin() );
		ksDescriptor.setAliases(ksDescriptor.getAliases());
		return ksDescriptor;
	}
	
	// CertificateInfo
	
	public CertificateInfo toCertificateInfo ( CertificateDTO dto ) {
		
		if ( BaseDTO.isEmpty(dto) )						 							return null;
		CertificateInfo certificate = new CertificateInfo ( dto.rawX509CertificateFromHex() );
		return certificate;
	}
	

	
	// SignatureInfo
	
	public SignatureInfo<?,?,VerifyResult,?> toSignatureInfo ( SignatureDTO dto ) {
		
		if ( BaseDTO.isEmpty(dto) )						 							return null;
		RawSignature<?,?,VerifyResult,?> rawSignature;
		SignCategory signCategory = dto.signCategoryFromString();
		try {
			switch ( signCategory ) {
				case CMS: {
					rawSignature = new CMSSignatureInfo ( dto.signAlgorithmFromString(), dto.digestAlgorithmFromName() );
					break;
				}
				case PDF: {
					PDFSignatureInfo pdfSignature = new PDFSignatureInfo ( dto.getPdfSignName(),
																		   dto.signAlgorithmFromString(),
																		   dto.digestAlgorithmFromName() );
					pdfSignature.setCoversWholeDocument	( dto.pdfCoversWholeDocumentFromString() );
					pdfSignature.setRevision ( dto.getPdfRevision() );
					rawSignature = pdfSignature;
					break;
				}
				default: {
					throw new UnsupportedOperationException(String.format ( 
							"unable to convert to a %s signature", signCategory ));
				}
			}
		} catch(CertificateException e) {
			throw new RuntimeException(e);
		}
			
		EmptySignature<?,?,VerifyResult,?> emptySignature = rawSignature.toEmptySignature(); 
		emptySignature.setCounterSignature ( dto.counterSignatureFromString() );
		emptySignature.setReason ( dto.getReason() );
		emptySignature.setLocation ( dto.getLocation() );
		emptySignature.setSigningTime ( dto.signingTimeFromString() );
		
		ChainSignature<?,?,VerifyResult,?> chainSignature = null;
		if ( hasPropertyAccess(dto, SignatureStatus.SignProcess.CHAIN) ) {
			chainSignature = emptySignature.toChainSignature (
					X509Utils.rawX509CertificatesFromHex(dto.getHexCertificateChain()) );
		}
		
		DigestSignature<?,?,VerifyResult,?> digestSignature = null;
		if ( hasPropertyAccess(dto, SignatureStatus.SignProcess.DIGEST ) ) {
			DigestInfo digest = toDigestInfo(dto.getDigest());
			digestSignature = chainSignature.toDigestSignature(digest);
		}
		
		SignedSignature<?,?,VerifyResult,?> signedSignature = null;
		if ( hasPropertyAccess(dto, SignatureStatus.SignProcess.SIGNED ) ) {
			byte[] digitalSignature = dto.digitalSignatureFromHex();
			signedSignature = digestSignature.toSignedSignature(digitalSignature);
		}
		
		MarkedSignature<?,?,VerifyResult,?> markedSignature = null;
		FinalizedSignature<?,?,VerifyResult,?> finalizedSignature = null;
		DisposedTimeStamp disposedTimeStamp;
		if ( hasPropertyAccess(dto, SignatureStatus.SignProcess.MARKED ) ) {
			markedSignature = signedSignature.toMarkedSignature();
			if ( ArrayUtils.isNotEmpty(dto.getTimeStamps()) ) {
				for ( TimeStampDTO timeStamp : dto.getTimeStamps() ) {
					disposedTimeStamp = (DisposedTimeStamp)toTimeStampInfo(timeStamp);
					if ( disposedTimeStamp != null ) {
						markedSignature.appendTimeStamp( disposedTimeStamp );
					}
				}
			}
			if ( dto.finalizedFromString() ) {
				finalizedSignature = markedSignature.finalizeSignature();
			}
		} else if ( hasPropertyAccess(dto, SignatureStatus.SignProcess.SIGNED) ) {
			if ( dto.finalizedFromString() ) {
				finalizedSignature = signedSignature.finalizeSignature();
			}
		}
		
		VerifiedSignature<?,?,VerifyResult,?> verifiedSignature = null;
		if ( hasPropertyAccess(dto, SignatureStatus.Stable.VERIFIED ) ) {
			verifiedSignature = finalizedSignature.toVerifiedSignature ( dto.verifyResultFromString() );
		}
		
		if ( verifiedSignature != null ) 		return (SignatureInfo<?,?,VerifyResult,?>) verifiedSignature;
		if ( finalizedSignature != null ) 		return (SignatureInfo<?,?,VerifyResult,?>) finalizedSignature;
		if ( markedSignature != null ) 			return (SignatureInfo<?,?,VerifyResult,?>) markedSignature;
		if ( signedSignature != null ) 			return (SignatureInfo<?,?,VerifyResult,?>) signedSignature;
		if ( digestSignature != null ) 			return (SignatureInfo<?,?,VerifyResult,?>) digestSignature;
		if ( chainSignature != null ) 	return (SignatureInfo<?,?,VerifyResult,?>) chainSignature;
		return (SignatureInfo<?,?,VerifyResult,?>) emptySignature;
	}
	
	private boolean hasPropertyAccess ( SignatureDTO dto, SignatureStatus minSignStatus ) {

		SignatureStatus status = dto.getStatus();
		if ( minSignStatus == SignatureStatus.Stable.VERIFIED )	{
			return status == SignatureStatus.Stable.VERIFIED;
		}
		if ( dto.finalizedFromString() || status == SignatureStatus.Stable.VERIFIED )	return true;
		if ( status instanceof SignatureStatus.SignProcess ) {
			return status.ordinal() >= minSignStatus.ordinal();
		}
		return false;
	}
	
	// TimeStampInfo
	
	public TimeStampInfo toTimeStampInfo ( TimeStampDTO dto ) {
		
		if ( BaseDTO.isEmpty(dto) )						 							return null;
		UntrustedTimeStamp untrustedTimeStamp = null;
		try {
			untrustedTimeStamp = new TimeStampInfo ( 
					dto.signAlgorithmFromString(),
					dto.digestAlgorithmFromString(),
					toDigestInfo(dto.getMessageImprint()),
//					toCertificateInfo(dto.getCertificate()),
					X509Utils.rawX509CertificatesFromHex(dto.getHexCertificateChain()),
					dto.digitalSignatureFromHex(),
					dto.verifyResultFromString(),
					dto.encTimeStampTokenFromHex() );
		} catch (CertificateException e) {
			// never thrown, using the TsTokenDTO protocol the certificate has to be correct
			throw new RuntimeException(e);
		}
		
		untrustedTimeStamp.setSigningTime	( dto.signingTimeFromString() );
		untrustedTimeStamp.setReason		( dto.getReason() );
		untrustedTimeStamp.setLocation		( dto.getLocation() );
		
		TimeStampVerifyResult verifyResult = dto.verifyResultFromString();
		VerifiedTimeStamp verifiedTimeStamp = null;
		if ( verifyResult != null )  {
			verifiedTimeStamp = untrustedTimeStamp.toVerifiedTimeStamp(verifyResult);
		}
		
		SignDisposition.TimeStamp disposition = dto.dispositionFromString();
		DisposedTimeStamp disposedTimeStamp = null;
		if ( verifyResult != null )  {
			disposedTimeStamp = verifiedTimeStamp.toDisposedTimeStamp(disposition);
		}
		
		if ( disposedTimeStamp != null) 		return (TimeStampInfo)disposedTimeStamp;
		if ( verifiedTimeStamp != null) 		return (TimeStampInfo)verifiedTimeStamp;
		
		return (TimeStampInfo)untrustedTimeStamp;
	}
	
	// TsRequestInfo
	
	public TsRequestInfo toTsRequestInfo ( TimeStampRequestDTO dto ) {
		
		if ( BaseDTO.isEmpty(dto) )						 							return null;
		TsRequestInfo tsRequest = new TsRequestInfo (
				dto.timestampDispositionFromString(),
				dto.messageImprintAlgorithmFromString(),
				dto.nounceFromString(),
				dto.getTsUrl(),
				dto.getTsUsername(),
				dto.getTsPassword() );
		return tsRequest;
	}
}
