package org.sinekartads.model.domain;

import java.security.cert.CertificateException;

import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.TemplateUtils;

import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

public class PDFSignatureInfo 
		extends SignatureInfo	<	SignCategory,
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo 			> {
	
	private static final long serialVersionUID = 4457611880685240489L;

	public PDFSignatureInfo (
			String name, 
			SignatureAlgorithm signAlgorithm,
			DigestAlgorithm digestAlgorithm) 
					throws CertificateException {
		
		super ( SignCategory.PDF, SignDisposition.PDF.DETACHED, signAlgorithm, digestAlgorithm );
		this.name = name;
	}
	
	
	
	// -----
	// --- PDF signature extra properties
	// -

	private final String name;
	private Boolean coversWholeDocument;
	private String revision;
	
	public String getName() {
		return name;
	}

	public CryptoStandard getSubfilter() {
		CryptoStandard subfilter = CryptoStandard.CADES;;
//		CryptoStandard subfilter
//		switch ( (SignatureType.PDF)type ) {
//			case PDF: 
//			case PDF_T: {
//				subfilter = CryptoStandard.CMS;
//				break;
//			}
//			case PAdES: 
//			case PAdES_T: {
//				subfilter = CryptoStandard.CADES;
//				break;
//			}
//			default: {
//				throw new UnsupportedOperationException(String.format ( 
//						"unable to recognize a subfilter for a %s signature", type  ));
//			} 
//		} 
		return subfilter;
	}

	public Boolean getCoversWholeDocument() {
		return coversWholeDocument;
	}

	public void setCoversWholeDocument(Boolean coversWholeDocument) {
		this.coversWholeDocument = coversWholeDocument;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
//
//	public PdfSignatureAppearance getAppearance(PdfStamper stamper) {
//		return new PdfSignatureAppearance(stamper) {
//			@Override
//			public String getReason() {
//				return PDFSignatureInfo.this.getReason();
//			}
//			
//			/**
//			 * @deprecated call this method on the enclosing PDFSignatureInfo instead
//			 */
//			@Override
//			public void setReason(String reason) {
//				PDFSignatureInfo.this.setReason(reason);
//			}
//
//			@Override
//			public String getLocation() {
//				return PDFSignatureInfo.super.getLocation();
//			}
//			
//			/**
//			 * @deprecated call this method on the enclosing PDFSignatureInfo instead
//			 */
//			@Override
//			public void setLocation(String location) {
//				PDFSignatureInfo.this.setLocation(location);
//			}
//			
//			@Override
//			public X509Certificate getCertificate() {
//				return PDFSignatureInfo.this.getRawX509Certificates()[0];
//			}
//			
//			public void setCertificate(Certificate certificate) {
//				//throw new UnsupportedOperationException ( "read-only property" );
//				PDFSignatureInfo.this.getRawX509Certificates()[0] = (X509Certificate)certificate;
//			}
//		};
//	}
	
	
	
	// -----
	// --- TimeStamp control disabilitation - we haven't been able to extract the tsToken from the PDF signature yet
	// -
	
	@Override
	public FinalizedSignature < SignCategory, 
								SignDisposition.PDF,
								VerifyResult,
								PDFSignatureInfo 		> finalizeSignature ( ) {
		
		SignatureInfo 	  	  < SignCategory, 
								SignDisposition.PDF,
								VerifyResult,
								PDFSignatureInfo >			finalizedInstance;
		
		finalizedInstance = TemplateUtils.Serialization.clone ( this );
		finalizedInstance.finalized = true;
		return finalizedInstance;
	}

}
