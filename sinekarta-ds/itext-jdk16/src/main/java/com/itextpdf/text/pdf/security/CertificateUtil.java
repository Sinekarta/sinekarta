/*
 * $Id: CertificateUtil.java 6134 2013-12-23 13:15:14Z blowagie $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2014 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extension;


/**
 * Patched by JENIA Software to adapt itext to bcprov-jdk16.
 * This class contains a series of static methods that
 * allow you to retrieve information from a Certificate.
 * @author Alessandro De Prato
 */
public class CertificateUtil {

	// Certificate Revocation Lists
	
	/**
	 * Gets a CRL from a certificate
	 * @param certificate
	 * @return	the CRL or null if there's no CRL available
	 * @throws CertificateException
	 * @throws CRLException
	 * @throws IOException
	 */
	public static CRL getCRL(X509Certificate certificate) throws CertificateException, CRLException, IOException {
		return CertificateUtil.getCRL(CertificateUtil.getCRLURL(certificate));
	}

	/**
	 * Gets the URL of the Certificate Revocation List for a Certificate
	 * @param certificate	the Certificate
	 * @return	the String where you can check if the certificate was revoked
	 * @throws CertificateParsingException
	 * @throws IOException 
	 */
	public static String getCRLURL(X509Certificate certificate) throws CertificateParsingException {
	    DEREncodable obj;
		try {
			// FIXME verificare, da jdk15 -> public static final ASN1ObjectIdentifier cRLDistributionPoints = new ASN1ObjectIdentifier("2.5.29.31"); 
			obj = getExtensionValue(certificate, "2.5.29.31");			
//			obj = getExtensionValue(certificate, Extension.cRLDistributionPoints.getId());
		} catch (IOException e) {
			obj = null;
		}
	    if (obj == null) {
	        return null;
	    }
	    CRLDistPoint dist = CRLDistPoint.getInstance(obj);
	    DistributionPoint[] dists = dist.getDistributionPoints();
	    for (DistributionPoint p : dists) {
	        DistributionPointName distributionPointName = p.getDistributionPoint();
	        if (DistributionPointName.FULL_NAME != distributionPointName.getType()) {
	            continue;
	        }
	        GeneralNames generalNames = (GeneralNames)distributionPointName.getName();
	        GeneralName[] names = generalNames.getNames();
	        for (GeneralName name : names) {
	            if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
	                continue;
	            }
	            // FIXME verificare che sia corretto
	            DERIA5String derStr = DERIA5String.getInstance(new DERTaggedObject(false, name.getTagNo(), name.getName()));
	            return derStr.getString();
	        }
	    }
	    return null;
	}

	/**
	 * Gets the CRL object using a CRL URL.
	 * @param url	the URL where to get the CRL
	 * @return	a CRL object
	 * @throws IOException
	 * @throws CertificateException
	 * @throws CRLException
	 */
	public static CRL getCRL(String url) throws IOException, CertificateException, CRLException {
		if (url == null)
			return null;
		InputStream is = new URL(url).openStream();
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return (CRL)cf.generateCRL(is); 
	}

	// Online Certificate Status Protocol

	/**
	 * Retrieves the OCSP URL from the given certificate.
	 * @param certificate the certificate
	 * @return the URL or null
	 * @throws IOException
	 */
	public static String getOCSPURL(X509Certificate certificate) {
		DEREncodable obj;
		try {
			obj = getExtensionValue(certificate, X509Extension.authorityInfoAccess.getId());
//			obj = getExtensionValue(certificate, Extension.authorityInfoAccess.getId());
	        if (obj == null) {
	            return null;
	        }
	        ASN1Sequence AccessDescriptions = (ASN1Sequence) obj;
	        for (int i = 0; i < AccessDescriptions.size(); i++) {
	        	ASN1Sequence AccessDescription = (ASN1Sequence) AccessDescriptions.getObjectAt(i);
	        	if ( AccessDescription.size() != 2 ) {
	        		continue;
	        	}
	        	else if (AccessDescription.getObjectAt(0) instanceof ASN1ObjectIdentifier) {
	        		ASN1ObjectIdentifier id = (ASN1ObjectIdentifier)AccessDescription.getObjectAt(0);
	        		if (SecurityIDs.ID_OCSP.equals(id.getId())) {
	            		DEREncodable description = AccessDescription.getObjectAt(1);
	                    String AccessLocation =  getStringFromGeneralName(description);
	                    if (AccessLocation == null) {
	                        return "" ;
	                    }
	                    else {
	                        return AccessLocation ;
	                    }
	                }
	            }
	        }
		} catch (IOException e) {
			return null;
		}
	    return null;
	}

    // Time Stamp Authority

	/**
	 * Gets the URL of the TSA if it's available on the certificate
	 * @param certificate	a certificate
	 * @return	a TSA URL
	 * @throws IOException
	 */
	public static String getTSAURL(X509Certificate certificate) {
	    byte der[] = certificate.getExtensionValue(SecurityIDs.ID_TSA);
	    if(der == null)
	        return null;
	    DEREncodable asn1obj;
		try {
			ASN1InputStream asn1inputstream = new ASN1InputStream(der);
			asn1obj = asn1inputstream.readObject();
			asn1inputstream.close();
	        DEROctetString octets = (DEROctetString)asn1obj;
	        asn1inputstream = new ASN1InputStream(octets.getOctets());
	        asn1obj = asn1inputstream.readObject();
	        asn1inputstream.close();
	        ASN1Sequence asn1seq = ASN1Sequence.getInstance(asn1obj);
	        return getStringFromGeneralName(asn1seq.getObjectAt(1));
		} catch (IOException e) {
			return null;
		}
	}
	
	// helper methods

    /**
     * @param certificate	the certificate from which we need the ExtensionValue
     * @param oid the Object Identifier value for the extension.
     * @return	the extension value as an DEREncodable object
     * @throws IOException
     */
    private static DEREncodable getExtensionValue(X509Certificate certificate, String oid) throws IOException {
        byte[] bytes = certificate.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }

    /**
     * Gets a String from an DEREncodable
     * @param names	the DEREncodable
     * @return	a human-readable String
     * @throws IOException
     */
    private static String getStringFromGeneralName(DEREncodable names) throws IOException {
        ASN1TaggedObject taggedObject = (ASN1TaggedObject) names ;
        return new String(ASN1OctetString.getInstance(taggedObject, false).getOctets(), "ISO-8859-1");
    }

}
