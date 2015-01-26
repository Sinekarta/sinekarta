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
package org.sinekartads.integration.cms;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.sinekartads.asn1.ASN1Utils;
import org.sinekartads.integration.BaseIntegrationTC;

import com.itextpdf.text.pdf.codec.Base64;

public class ExtractASN1TC extends BaseIntegrationTC {
	
	static Logger tracer = Logger.getLogger(ExtractASN1TC.class);
	
//	File folder = new File("/home/adeprato/Scaricati");
	File folder = null; 
	String[] fileNames = new String[] {
			"pippo.txt.p7m"
	};
	
//	@Test
//	public void testAsn1() throws Exception {
//		String asn1FileName;
//		File srcFile;
//		File asn1File;
//		for ( String fileName : fileNames ) {
//			asn1FileName = "asn1_" + fileName;
//			if ( folder != null ) {
//				srcFile = new File(folder, fileName);
//			} else {
//				srcFile = getTestResource(fileName);
//			}
//			asn1File = getTestResource(asn1FileName);
//			FileUtils.writeStringToFile ( asn1File, 
//					ASN1Utils.writeToString (
//							FileUtils.readFileToByteArray(srcFile) ) );
//		}
//	}
	
	@Test
	public void testEnc() throws Exception {
		String asn1FileName;
		File srcFile;
		File asn1File;
		for ( String fileName : fileNames ) {
			asn1FileName = "enc_" + fileName;
			if ( folder != null ) {
				srcFile = new File(folder, fileName);
			} else {
				srcFile = getTestResource(fileName);
			}
			asn1File = getTestResource(asn1FileName);
			FileUtils.writeStringToFile ( asn1File, 
					Base64.encodeBytes(
							FileUtils.readFileToByteArray(srcFile) ) );
		}
	}
	
//	@Test
//	public void testCert() throws Exception {
//		X509CertificateHolderTransformer transformer = new X509CertificateHolderTransformer();
//		String asn1FileName;
//		File srcFile;
//		File asn1File;
//		CMSSignedData signedData;
//		Store certHolderStore;
//		X509Certificate cert;
//		StringBuilder buf;
//		for ( String fileName : fileNames ) {
//			buf = new StringBuilder();
//			asn1FileName = "cert_" + fileName;
//			if ( folder != null ) {
//				srcFile = new File(folder, fileName);
//			} else {
//				srcFile = getTestResource(fileName);
//			}
//            signedData = new CMSSignedData ( FileUtils.readFileToByteArray(srcFile) );
//            certHolderStore = signedData.getCertificates();
//            for(Object certHolder : certHolderStore.getMatches(new NoFilterSelector())) {
//            	cert = transformer.transform((X509CertificateHolder)certHolder);
//            	buf.append(ASN1Utils.writeToString(cert.getEncoded())).append("\n");
//            }
//			asn1File = getTestResource(asn1FileName);
//			FileUtils.writeStringToFile ( asn1File, buf.toString() );
//		}
//	}
//	
//	@Test
//	public void testEnce() throws Exception {
//		X509CertificateHolderTransformer transformer = new X509CertificateHolderTransformer();
//		String asn1FileName;
//		File srcFile;
//		File asn1File;
//		CMSSignedData signedData;
//		Store certHolderStore;
//		X509Certificate cert;
//		StringBuilder buf;
//		for ( String fileName : fileNames ) {
//			buf = new StringBuilder();
//			asn1FileName = "ence_" + fileName;
//			if ( folder != null ) {
//				srcFile = new File(folder, fileName);
//			} else {
//				srcFile = getTestResource(fileName);
//			}
//            signedData = new CMSSignedData ( FileUtils.readFileToByteArray(srcFile) );
//            certHolderStore = signedData.getCertificates();
//            for(Object certHolder : certHolderStore.getMatches(new NoFilterSelector())) {
//            	cert = transformer.transform((X509CertificateHolder)certHolder);
//            	buf.append(Base64.encodeBytes(cert.getEncoded())).append("\n");
//            }
//			asn1File = getTestResource(asn1FileName);
//			FileUtils.writeStringToFile ( asn1File, buf.toString() );
//		}
//	}
}
