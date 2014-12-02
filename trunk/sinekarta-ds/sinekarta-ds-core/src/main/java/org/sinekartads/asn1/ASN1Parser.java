package org.sinekartads.asn1;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
//import org.bouncycastle.asn1.ASN1ObjectIdentifier;
//import org.bouncycastle.asn1.DEREncodable;
//import org.bouncycastle.asn1.DEROctetString;
//import org.bouncycastle.asn1.DERSequence;
import org.sinekartads.asn1.ASN1Selector.ASN1StructureSelector;
import org.sinekartads.util.HexUtils;

/**
 * Usage
 <pre>
			ASN1FingerPrint[] fingerPrints = ASN1Parser.locateFingerPrints(new ASN1InputStream(toBeMarked).readObject());
			ASN1FingerPrint asn1FingerPrint = null;
			for(ASN1FingerPrint fp : fingerPrints) {
				if(StringUtils.equals(fp.getAlgorithmId().getId(), "2.16.840.1.101.3.4.2.1")
						&& ArrayUtils.isEquals(fp.getFingerPrint().getOctets(), fingerPrint)) {
					asn1FingerPrint = fp;
				}
			}
			assertNotNull(asn1FingerPrint);
			tracer.info("digest found and matching");
			tracer.info("digest algorithm:");
			asn1Writer.write(asn1FingerPrint.getAlgorithmId());
			tracer.info("fingerprint:");
			asn1Writer.write(asn1FingerPrint.getFingerPrint());
 </pre>
 */

public abstract class ASN1Parser implements Cloneable {

	public static class ASN1ParseException extends Exception {
		private static final long serialVersionUID = 8631722489885989914L;
		public ASN1ParseException(String message) {
			super(message);
		}
		public ASN1ParseException(Throwable cause) {
			super(cause);
		}
		public ASN1ParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	public static class ASN1FingerPrint extends ASN1Parser {
		@Override
		protected void parse(DEREncodable enc) throws ASN1ParseException {
			DERSequence seq0 = (DERSequence) enc;
			DERSequence seq1 = (DERSequence) seq0.getObjectAt(0);
			algorithmId  = (ASN1ObjectIdentifier) seq1.getObjectAt(0);
			fingerPrint = (DEROctetString)seq0.getObjectAt(1);
		}

		@Override
		protected String getHexTemplate() {
			return "3031300D06096086480165030402010500042010C20119EB65645B1AE5B2973FF8A89848506629BB618FAB5A3B1C9CDE869C73";
		}

		private ASN1ObjectIdentifier algorithmId;
		private DEROctetString fingerPrint;
		
		public DEROctetString getFingerPrint() {
			return fingerPrint;
		}

		public ASN1ObjectIdentifier getAlgorithmId() {
			return algorithmId;
		}
	}
	
	static ASN1StructureSelector selector = new ASN1StructureSelector(); 
	
	public static ASN1FingerPrint[] locateFingerPrints(DEREncodable enc) throws ASN1ParseException {
		ASN1Parser[] parsers  = parse(new ASN1FingerPrint(), enc);
		ASN1FingerPrint[] result = new ASN1FingerPrint[parsers.length];
		for(int i=0; i<parsers.length; i++) {
			result[i] = (ASN1FingerPrint)parsers[i]; 
		}
		return result;
	}
	
	public static ASN1Parser parseSingle(ASN1Parser parser, DEREncodable enc) throws ASN1ParseException {
		return parse(parser, enc, true)[0];
	}
	
	public static ASN1Parser[] parse(ASN1Parser parser, DEREncodable enc) throws ASN1ParseException {
		return parse(parser, enc, false);
	}
	
	private static ASN1Parser[] parse(ASN1Parser parser, DEREncodable enc, boolean single) throws ASN1ParseException {
		DEREncodable template;
		template = ASN1Utils.readObject(HexUtils.decodeHex(parser.getHexTemplate()));		
		DEREncodable[] matches = selector.find(enc, template);
		if(ArrayUtils.isEmpty(matches)) {
			throw new ASN1ParseException("template structure not found.");  
		}
		ASN1Parser[] parsers;
		try {
			if(single) {
				if(matches.length == 1) {
					parsers = new ASN1Parser[] {parser};
					parser.parse(matches[0]);
				} else {
					throw new ASN1ParseException("expected exactly one match with the give template.");
				}
			} else {
				parsers = new ASN1Parser[matches.length];
				for(int i=0; i<matches.length; i++) {
					parsers[i] = parser.getClass().newInstance();
					parsers[i].parse(matches[i]);
				}
			}
		} catch(Exception e) {
			throw new ASN1ParseException(e);
		}
		
		return parsers;
	}

	protected void parse(byte[] encoded) throws ASN1ParseException {
		parse(ASN1Utils.readObject(encoded));
	}
	
	protected abstract void parse(DEREncodable enc) throws ASN1ParseException;
	
	protected abstract String getHexTemplate();
	
}
