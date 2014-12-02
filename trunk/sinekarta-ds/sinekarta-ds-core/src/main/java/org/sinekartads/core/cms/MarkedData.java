package org.sinekartads.core.cms;

import java.io.IOException;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
//import org.bouncycastle.asn1.ASN1EncodableVector;
//import org.bouncycastle.asn1.ASN1Integer;
//import org.bouncycastle.asn1.DEREncodable;
//import org.bouncycastle.asn1.DERNull;
//import org.bouncycastle.asn1.DERObject;
//import org.bouncycastle.asn1.DEROctetString;
//import org.bouncycastle.asn1.DERSequence;
//import org.bouncycastle.asn1.DERTaggedObject;
//import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
//import org.bouncycastle.cms.CMSSignedData;
//import org.bouncycastle.tsp.TSPException;
//import org.bouncycastle.tsp.TimeStampToken;
import org.sinekartads.asn1.ASN1Parser;
import org.sinekartads.asn1.ASN1Selector.ASN1OidLabelSelector;
import org.sinekartads.asn1.ASN1Selector.ASN1PathSelector;
import org.sinekartads.asn1.ASN1Utils;
import org.sinekartads.model.oid.SinekartaDsObjectIdentifiers;
import org.sinekartads.util.HexUtils;

public abstract class MarkedData extends ASN1Parser {

	// -----
	// --- Factory methods
	// -
	
	public static final Format DEFAULT_FORMAT = Format.DIKE;
	public static final int DEFAULT_VERSION = 1;
	
	/**
	 * available formats
	 */
	public enum Format {
		DIKE;
	}
	
	public static MarkedData getInstance (
			byte[] markedContent,
			byte[] encTimeStampToken ) 
					throws ASN1ParseException, 
						   TSPException, 
						   IOException, 
						   CMSException {
		
		return getInstance ( markedContent, encTimeStampToken, DEFAULT_FORMAT, DEFAULT_VERSION );
	}
	
	public static MarkedData getInstance (
			byte[] markedContent,
			byte[] encTimeStampToken,
			Format format,
			int version ) 
					throws ASN1ParseException, 
						   TSPException, 
						   IOException, 
						   CMSException {
		
		TimeStampToken tsToken = new TimeStampToken ( new CMSSignedData(encTimeStampToken) );
		
		MarkedData markedData;
		if(format ==  Format.DIKE) {
			markedData = new Dike(tsToken, markedContent, version);
		} else {
			throw new IllegalArgumentException("unsupported format: " + format);
		}
		return markedData;
	}
	
	public static MarkedData getInstance (
			Object object ) 
					throws ASN1ParseException {
		
		// Obtain the container object to look for the pattern in 
		DEREncodable container;
		if(object instanceof DEREncodable) {
			container = (DEREncodable)object;
		} else if(object instanceof byte[]) {
			container = ASN1Utils.readObject((byte[])object);
		} else {
			throw new IllegalArgumentException("expected a DEREncodable object or its encoding");
		}
		
		MarkedData markedData = null;
		// Retrieve the matching markedData among the supported ones
		if(markedData == null) {
			try {
				markedData = new Dike(container);
			} catch(ASN1ParseException e) {
				// do nothing, try with the next pattern
			}
		}
		
		// Return the generated markedData object
		if(markedData == null) {
			 throw new ASN1ParseException("unrecognized markedData pattern");
		}
		return markedData;
	}
	
	protected MarkedData(DEREncodable container) throws ASN1ParseException {
		// Locate the root element into the container and parse the values
		ASN1Parser.parseSingle(this, container);
	}
	
	protected MarkedData(TimeStampToken tsToken, byte[] markedContent, int version) {
		this.tsToken = tsToken;
		this.markedContent = markedContent;
		this.version = version;
		try {
			nestedSignedData = new CMSSignedData(markedContent);
		} catch(Exception e) {
			// do nothing, just no cmsSignedData are embedded
		}
	}
	
	private int version;
	private byte[] markedContent;
	private TimeStampToken tsToken;
	private CMSSignedData nestedSignedData;
	
	
	
	// -----
	// --- ASN1Parser protocol 
	// -
	
	@Override
	protected void parse(DEREncodable root) throws ASN1ParseException {
		ASN1PathSelector pathSelector = new ASN1PathSelector();
		ASN1OidLabelSelector oidSelector = new ASN1OidLabelSelector();
		
		// Extract version and content at the specified paths
		version = ((ASN1Integer)pathSelector.get(root, getVersionPath())).getValue().intValue();
		markedContent = ((DEROctetString)pathSelector.get(root, getMarkedContentPath())).getOctets();
		
		// Locate the signedData by the oidLabel 
		try {
			byte[] signedDataEnc = ((DERSequence)oidSelector.get(root, new ASN1ObjectIdentifier(SinekartaDsObjectIdentifiers.cnt_signedData))).getEncoded();
			tsToken = new TimeStampToken(new CMSSignedData(signedDataEnc));
		} catch(Exception e) {
			throw new ASN1ParseException("signedData not found", e);
		}
		
		// Try to parse the nestedSignedData (null if fail)
		try {
			nestedSignedData = new CMSSignedData(markedContent);
		} catch(Exception e) {
			// do nothing, just no cmsSignedData are embedded
		}
	}
	
	@Override
	protected String getHexTemplate() {
		String hexTemplate;
		ASN1EncodableVector asn1Vect;
		
		// Basic signedData structure
		asn1Vect = new ASN1EncodableVector();
		asn1Vect.add( new ASN1ObjectIdentifier(SinekartaDsObjectIdentifiers.cnt_signedData) );
		asn1Vect.add( new DERTaggedObject(0, new DERNull()) );        
		DEREncodable derSignedData = new DERSequence(asn1Vect);
		// dummy version
		DEREncodable derVersion = new ASN1Integer(0);
		// dummy markedContent
		DEREncodable derMarkedContent = new DEROctetString("dummy".getBytes());
		
		// Envelope the dummy data to generate the template
		DERObject template = envelope(derSignedData, derMarkedContent, derVersion);
		
		// Return the hexTemplate
		try {
			hexTemplate = HexUtils.encodeHex(template.getEncoded());
		} catch(IOException e) {
			// never thrown, the generated encoding has to be correct
			throw new RuntimeException(e);
		}
		return hexTemplate;
	}
	
	
	// -----
	// --- Conversion to byte array
	// -
	
	public byte[] getEncoded() {
		byte[] encoded;
		try {
			// basic signedData structure
			DEREncodable derSignedData = ASN1Utils.readObject(tsToken.getEncoded());
			// dummy version
			DEREncodable derVersion = new ASN1Integer(version);
			// dummy markedContent
			DEREncodable derMarkedContent = new DEROctetString(markedContent);
			
			// envelope the dummy data to generate the template
			DERObject root = envelope(derSignedData, derMarkedContent, derVersion);
		
			encoded = root.getEncoded();
		} catch(IOException e) {
			// never thrown, the generated encoding has to be correct
			throw new RuntimeException(e);
		}
		return encoded;
	}
	
	
	
	// -----
	// --- Abstract protocol: localization element and format methods 
	// -
	
	protected abstract DEREncodable[] getVersionPath();
	
	protected abstract DEREncodable[] getMarkedContentPath();
	
	protected abstract DERObject envelope(DEREncodable derSignedData, DEREncodable derMarkedContent, DEREncodable derVersion);
	
	
	
	// -----
	// --- Read only properties
	// -

	public int getVersion() {
		return version;
	}

	public byte[] getMarkedContent() {
		return markedContent;
	}

	public TimeStampToken getRawTimeStampToken() {
		return tsToken;
	}

	public CMSSignedData getNestedSignedData() {
		return nestedSignedData;
	}
	
	
	
	// -----
	// --- Formats implementation
	// -
	
	/**
	 * specialization for the *.tsd generated with Dike 
	 */
	private static class Dike extends MarkedData {

		Dike(TimeStampToken tsToken, byte[] markedContent, int version) {
			super(tsToken, markedContent, version);
		}
		Dike(DEREncodable container) throws ASN1ParseException {
			super(container);
		}
		
		@Override
		protected DERObject envelope(DEREncodable derSignedData, DEREncodable derMarkedContent, DEREncodable derVersion) {		
			ASN1EncodableVector asn1Vect = new ASN1EncodableVector();
        
			// envelope for the tsSignedData (it is already labeled)
	        ASN1EncodableVector vec01020 = new ASN1EncodableVector();
	        vec01020.add(derSignedData);
	        DERSequence seq0120 = new DERSequence(vec01020);         
	        // timestampedData body
	        asn1Vect = new ASN1EncodableVector();
	        asn1Vect.add( derVersion );
	        asn1Vect.add( derMarkedContent );
	        asn1Vect.add( new DERTaggedObject(0, seq0120) );
	        DERSequence seq010 = new DERSequence(asn1Vect);
	        // envelope for the timestampedData 
	        asn1Vect = new ASN1EncodableVector();
	        asn1Vect.add( new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.1.31") );
	        asn1Vect.add( new DERTaggedObject(0, seq010) );
	        DERSequence seq0 = new DERSequence(asn1Vect);
			
			return seq0;
		}

		@Override
		protected DEREncodable[] getVersionPath() {
			return new DEREncodable[] {new DERSequence(), new DERTaggedObject(0, null), new DERSequence(), new ASN1Integer(0)};
		}

		@Override
		protected DEREncodable[] getMarkedContentPath() {
			return new DEREncodable[] {new DERSequence(), new DERTaggedObject(0, null), new DERSequence(), new DEROctetString("dummy".getBytes())};
		}
	}
}
