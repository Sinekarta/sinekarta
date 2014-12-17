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
package org.sinekartads.alfresco;

import org.alfresco.service.namespace.QName;


public class SinekartaDsModel {

	// commons
	public static final String NAMESPACE_SINEKARTADS_CONTENT_MODEL = "http://www.sinekarta.org/alfresco/model/content/1.0";
	public static final String SINEKARTADS_PREFIX = "sinekartads:";
	public static final String ALFRESCO_PREFIX = "cm:";

	public static final String PERMISSION_GROUP_SINEKARTA_RCS = "SinekartaRCS";
	
	// sinekarta archive folder
	public static final String TYPE_ARCHIVE = SINEKARTADS_PREFIX+"archive";
	
	// generic content
	public static final String TYPE_CONTENT = ALFRESCO_PREFIX+"content";

	static QName sinekartaQName(String localName) {
		try {
			return QName.createQName("{"+NAMESPACE_SINEKARTADS_CONTENT_MODEL+"}"+localName);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	static String sinekartaName(String localName) {
		return SINEKARTADS_PREFIX+localName;
	}

	// Aspects
	// each aspect has the String value and the relative QName
	// each aspect (both string and QNames) starts with ASPECT_
	
	public static final String DOCUMENT_ACQUIRING = "documentAcquiring";
	public static final String ASPECT_DOCUMENT_ACQUIRING = sinekartaName(DOCUMENT_ACQUIRING);
	public static final QName ASPECT_QNAME_DOCUMENT_ACQUIRING = sinekartaQName(DOCUMENT_ACQUIRING);		
	
	public static final String SIGNED_DOCUMENT = "signedDocument";
	public static final String ASPECT_SIGNED_DOCUMENT = sinekartaName(SIGNED_DOCUMENT);
	public static final QName ASPECT_QNAME_SIGNED_DOCUMENT = sinekartaQName(SIGNED_DOCUMENT);	
	
	public static final String SIGNED_DOCUMENT_CMS = "signedDocumentCms";
	public static final String ASPECT_SIGNED_DOCUMENT_CMS = sinekartaName(SIGNED_DOCUMENT_CMS);
	public static final QName ASPECT_QNAME_SIGNED_DOCUMENT_CMS = sinekartaQName(SIGNED_DOCUMENT_CMS);
	
	public static final String SIGNED_DOCUMENT_XML = "signedDocumentXml";
	public static final String ASPECT_SIGNED_DOCUMENT_XML = sinekartaName(SIGNED_DOCUMENT_XML);
	public static final QName ASPECT_QNAME_SIGNED_DOCUMENT_XML = sinekartaQName(SIGNED_DOCUMENT_XML);
	
	public static final String SIGNED_DOCUMENT_PDF = "signedDocumentPdf";
	public static final String ASPECT_SIGNED_DOCUMENT_PDF = sinekartaName(SIGNED_DOCUMENT_PDF);
	public static final QName ASPECT_QNAME_SIGNED_DOCUMENT_PDF = sinekartaQName(SIGNED_DOCUMENT_PDF);
	
	public static final String TIMESTAMP_MARK = "timestampMark";
	public static final String ASPECT_TIMESTAMP_MARK = sinekartaName(TIMESTAMP_MARK);
	public static final QName ASPECT_QNAME_TIMESTAMP_MARK = sinekartaQName(TIMESTAMP_MARK);
	
	public static final String EXTRACTED_DOCUMENT = "extractedDocument";
	public static final String ASPECT_EXTRACTED_DOCUMENT = sinekartaName(EXTRACTED_DOCUMENT);
	public static final QName ASPECT_QNAME_EXTRACTED_DOCUMENT = sinekartaQName(EXTRACTED_DOCUMENT);
	
	public static final String TEMPORARY_FILE = "temporaryFile";
	public static final String ASPECT_TEMPORARY_FILE = sinekartaName(TEMPORARY_FILE);
	public static final QName ASPECT_QNAME_TEMPORARY_FILE = sinekartaQName(TEMPORARY_FILE);

	
	
	// aspect SIGNED_DOCUMENT
	
	public static final QName PROP_QNAME_SIGNATURE_TYPE = sinekartaQName("signatureType");
	
	public static final QName PROP_QNAME_SIGNATURE_DISPOSITION = sinekartaQName("signatureDisposition");
	
	public static final QName PROP_QNAME_DATA_FILE = sinekartaQName("dataFile");
	
	
	
	// aspect EXTRACTED_DOCUMENT
	
	public static final QName PROP_QNAME_SOURCE_FILE = sinekartaQName("sourceFile");
	
	public static final QName PROP_QNAME_EXTRACTION_DATE = sinekartaQName("extractionDate");
	
	
	
	// aspect TEMPORARY_FILE
	
	public static final QName PROP_QNAME_CREATION_DATE = sinekartaQName("creationDate");
	
	public static final QName PROP_QNAME_TIME_TO_LIVE = sinekartaQName("timeToLive");
	
	public static final QName PROP_QNAME_EXPIRING_DATE = sinekartaQName("expiringDate");
	
	

	// aspect timestampMark

	public static final QName PROP_MARK_DOCUMENT_DESCRIPTION = sinekartaQName("markDocumentDescription");

	public static final QName PROP_MARK_DOCUMENT_REFERENCE_ID = sinekartaQName("markDocumentReferenceId");
	
	public static final QName PROP_MARK_TIMESTAMP_RCS_SIGNATURE = sinekartaQName("markTimestampRCSSignature");
	
	public static final QName ASSOCIATION_MARKED_DOCUMENT_LIST = sinekartaQName("markedDocumentList");
	
	public static final QName PROP_MARK_FINGER_PRINT = sinekartaQName("markFingerPrint");

	public static final QName PROP_MARK_TIMESTAMP_TOKEN = sinekartaQName("markTimestampToken");
	
	public static final QName PROP_MARK_DOCUMENT_TYPE = sinekartaQName("markDocumentType");
	
/*		
	// Aspects
	// each aspect has the String value and the relative QName
	// each aspect (both string and QNames) starts with ASPECT_
	
	public static final String ASPECT_SIGNED_DOCUMENT = SINEKARTA_PREFIX+"signedDocument";
	public static final String ASPECT_QNAME_SIGNED_DOCUMENT = "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}signedDocument";

	public static final String ASPECT_DOCUMENT_ACQUIRING = SINEKARTA_PREFIX+"documentAcquiring";
	public static final String ASPECT_QNAME_DOCUMENT_ACQUIRING = "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}documentAcquiring";
			
	public static final String ASPECT_RCS_SIGNATURE = SINEKARTA_PREFIX+"RCSSignature";
	public static final String ASPECT_QNAME_RCS_SIGNATURE = "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}RCSSignature";
			
	public static final String ASPECT_TIMESTAMP_MARK = SINEKARTA_PREFIX+"timestampMark";
	public static final String ASPECT_QNAME_TIMESTAMP_MARK = "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}timestampMark";

	public static final String ASPECT_SUBSTITUTIVE_PRESERVATION = SINEKARTA_PREFIX+"substitutivePreservation";
	public static final String ASPECT_QNAME_SUBSTITUTIVE_PRESERVATION =  "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}substitutivePreservation";
	
	public static final String ASPECT_OCR = SINEKARTA_PREFIX+"OCR";
	public static final String ASPECT_QNAME_OCR =  "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}OCR";

	public static final String ASPECT_PU_SIGNATURE = SINEKARTA_PREFIX+"PUSignature";
	public static final String ASPECT_QNAME_PU_SIGNATURE = "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}PUSignature";
	
	public static final String ASPECT_TIMESTAMP_AEMARK = SINEKARTA_PREFIX+"timestampAEMark";
	public static final String ASPECT_QNAME_TIMESTAMP_AEMARK = "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}timestampAEMark";

	public static final String ASPECT_AEMARK_CREATED = SINEKARTA_PREFIX+"aemarkCreated";
	public static final String ASPECT_QNAME_AEMARK_CREATED = "{"+NAMESPACE_SINEKARTA_CONTENT_MODEL+"}aemarkCreated";

	// PropertiesPath
	// each property has the String value and the relative QName
	// each property (both string and QNames) starts with PROP_
	
	// type archive

	public static final String PROP_RCS_USER_ID = SINEKARTA_PREFIX+"rcsUserId";
	
	public static final String PROP_SINEKARTA_ADMIN_USER_ID = SINEKARTA_PREFIX+"sinekartaAdminUserId";
	
	// aspect signedDocument
	
	public static final String PROP_SIGNATURE_FORMAT = SINEKARTA_PREFIX+"signatureFormat";
	
	// aspect documentAcquiring
	
	public static final String PROP_DOCUMENT_TYPE = SINEKARTA_PREFIX+"documentType";
	
	public static final String PROP_LANGUAGE = SINEKARTA_PREFIX+"language";

	public static final String PROP_DOCUMENT_DATE = SINEKARTA_PREFIX+"documentDate";

	public static final String PROP_DOCUMENT_MOVED = SINEKARTA_PREFIX+"documentMoved";

	public static final String PROP_TIMESTAMP_PROCESS_START = SINEKARTA_PREFIX+"timestampProcessStart";

	public static final String PROP_REFERENCE_ID = SINEKARTA_PREFIX+"referenceId";
	
	public static final String PROP_PU_SIGN_REQUIRED = SINEKARTA_PREFIX+"PUSignRequired";
	
	// aspect RCSSignature

	public static final String PROP_RCS_SIGNED_DOCUMENT_FINGERPRINT = SINEKARTA_PREFIX+"RCSSignedDocumentFingerprint";
	
	public static final String PROP_TIMESTAMP_RCS_SIGNATURE = SINEKARTA_PREFIX+"timestampRCSSignature";
	
	// aspect timestampMark

	public static final String PROP_MARK_DOCUMENT_DESCRIPTION = SINEKARTA_PREFIX+"markDocumentDescription";

	public static final String PROP_MARK_DOCUMENT_REFERENCE_

	public static final String PROP_MARK_DOCUMENT_DESCRIPTION = SINEKARTA_PREFIX+"markDocumentDescription";ID = SINEKARTA_PREFIX+"markDocumentReferenceId";
	
	public static final String PROP_MARK_TIMESTAMP_RCS_SIGNATURE = SINEKARTA_PREFIX+"markTimestampRCSSignature";
	
	public static final String ASSOCIATION_MARKED_DOCUMENT_LIST = SINEKARTA_PREFIX+"markedDocumentList";
	
	public static final String PROP_MARK_FINGER_PRINT = SINEKARTA_PREFIX+"markFingerPrint";

	public static final String PROP_MARK_TIMESTAMP_TOKEN = SINEKARTA_PREFIX+"markTimestampToken";
	
	public static final String PROP_MARK_DOCUMENT_TYPE = SINEKARTA_PREFIX+"markDocumentType";
	
	// aspect substitutivePreservation
	
	public static final String ASSOCIATION_MARKS_DOCUMENT = SINEKARTA_PREFIX+"marksDocument";

	// aspect OCR
	
	public static final String PROP_OCR_RESULT = SINEKARTA_PREFIX+"OCRResult";

	// aspect PUSignature

	public static final String PROP_PU_SIGNED_DOCUMENT_FINGERPRINT = SINEKARTA_PREFIX+"PUSignedDocumentFingerprint";
	
	public static final String PROP_TIMESTAMP_PU_SIGNATURE = SINEKARTA_PREFIX+"timestampPUSignature";
	
	// aspect aemarkCreated
	
	public static final String ASSOCIATION_AEXMLFILE_DOCUMENT = SINEKARTA_PREFIX+"aeXMLFile";

	public static final String ASSOCIATION_AEPDFFILE_DOCUMENT = SINEKARTA_PREFIX+"aePDFFile";

	// generic attributes
	
	// used for saving (jn user noderef) the last smart card dll used
	public static final String PROP_SINEKARTA_SMARTCARD_DLL = SINEKARTA_PREFIX+"sinekartaSmartcardDLL";
	
	// used for saving (jn archive noderef) last datiTitolareContabilita used
	public static final String PROP_SINEKARTA_AE_DATITITOLARECONTABILITA = SINEKARTA_PREFIX+"datiTitolareContabilita";
	
	// used for saving (jn user noderef) last DatiResponsabileConservazione used
	public static final String PROP_SINEKARTA_AE_DATIRESPONSABILECONSERVAZIONE = SINEKARTA_PREFIX+"datiResponsabileConservazione";
	
	// used for saving (jn user noderef) last DatiDelegatiConservazione used
	public static final String PROP_SINEKARTA_AE_DATIDELEGATICONSERVAZIONE = SINEKARTA_PREFIX+"datiDelegatiConservazione";

	// used for saving (jn archive noderef) last DatiTrasmissione used
	public static final String PROP_SINEKARTA_AE_DATITRASMISSIONE = SINEKARTA_PREFIX+"datiTrasmissione";
	
	// used for saving (jn user noderef) last LuogoConservazione used
	public static final String PROP_SINEKARTA_AE_LUOGOCONSERVAZIONE = SINEKARTA_PREFIX+"luogoConservazione";
	
*/	
}
