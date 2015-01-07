package org.sinekartads.model.domain;

import java.security.cert.CertificateException;

import org.sinekartads.model.domain.SecurityLevel.VerifyResult;
import org.sinekartads.model.domain.SignatureType.SignCategory;
import org.sinekartads.model.domain.Transitions.FinalizedSignature;
import org.sinekartads.model.oid.DigestAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.util.TemplateUtils;

public class PDFSignatureInfo 
		extends SignatureInfo	<	SignCategory,
									SignDisposition.PDF,
									VerifyResult,
									PDFSignatureInfo 			> {
	
	private static final long serialVersionUID = 4457611880685240489L;

	public static enum Subfilter {
		CADES,
		CMS
	}
	
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
	private byte[] documentId;
	private byte[] fileId;
	private String unicodModDate;
	private byte[] authenticatedAttributeBytes;
	
	public String getName() {
		return name;
	}

	public String getSubfilter() {
		Subfilter subfilter = Subfilter.CADES;
//		CryptoStandard subfilter
//		switch ( (SignatureType.PDF)type ) {
//			case PDF: 
//			case PDF_T: {
//				subfilter = Subfilter.CMS;
//				break;
//			}
//			case PAdES: 
//			case PAdES_T: {
//				subfilter = Subfilter.CADES;
//				break;
//			}
//			default: {
//				throw new UnsupportedOperationException(String.format ( 
//						"unable to recognize a subfilter for a %s signature", type  ));
//			} 
//		} 
		return subfilter.name();
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
	
	public byte[] getDocumentId() {
		return documentId;
	}

	public void setDocumentId(byte[] documentId) {
		this.documentId = documentId;
	}
	
	public byte[] getFileId() {
		return fileId;
	}

	public void setFileId(byte[] fileId) {
		this.fileId = fileId;
	}
	
	public String getUnicodeModDate() {
		return unicodModDate;
	}

	public void setUnicodeModDate(String unicodModDate) {
		this.unicodModDate = unicodModDate;
	}
	
	
	
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
		
		finalizedInstance = TemplateUtils.Instantiation.clone ( this );
		finalizedInstance.finalized = true;
		return finalizedInstance;
	}

	public byte[] getAuthenticatedAttributeBytes() {
		return authenticatedAttributeBytes;
	}

	public void setAuthenticatedAttributeBytes(
			byte[] authenticatedAttributeBytes) {
		this.authenticatedAttributeBytes = authenticatedAttributeBytes;
	}

}
