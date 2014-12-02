package org.sinekartads.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.sinekartads.asn1.ASN1Writer.StringASN1Writer;
import org.sinekartads.model.oid.SinekartaDsObjectIdentifiers;
import org.sinekartads.util.HexUtils;


public abstract class ASN1Utils {
	
	public static DERObject readObject(InputStream is) throws IOException {
		ASN1InputStream asn1Is = null;
		DERObject object;
		try {
			asn1Is = new ASN1InputStream(is);
			object = asn1Is.readObject();
		} finally {
			IOUtils.closeQuietly(asn1Is);
		}
		return object;
	}
	
	public static DERObject readObject(byte[] encoded) {
		ASN1InputStream asn1Is = null;
		DERObject object;
		try {
			asn1Is = new ASN1InputStream(encoded);
			object = asn1Is.readObject();
		} catch(IOException e) {
			// never thrown: internal byte array I/O
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(asn1Is);
		}
		return object;
	}
	
	public static DERObject readObject(String hex) {
		return readObject(HexUtils.decodeHex(hex));
	}
	
	
	
	public static String writeToString(byte[] encoded) {
		return writeToString(readObject(encoded));
	}
	
	public static String writeToString(DEREncodable root) {
		StringASN1Writer stringWriter = new StringASN1Writer();
		try {
			stringWriter.write(root);
		} catch(IOException e) {
			// never thrown: StringBuilder operations
		} finally {
			IOUtils.closeQuietly(stringWriter);
		}
		return stringWriter.readContent();
	}
	
	
	
static Map<String, String> dictionary;
	
	static {
		dictionary = new HashMap<String, String>();
		
		DERObjectIdentifier objId;
		try {
			Class<?>[] classes = new Class<?>[] {
					 org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.class, 
					 org.bouncycastle.asn1.nist.NISTObjectIdentifiers.class, 
					 SinekartaDsObjectIdentifiers.class, 
					 org.bouncycastle.asn1.eac.EACObjectIdentifiers.class,
					 com.itextpdf.text.pdf.security.CertificateInfo.class};
			for(Class<?> clazz : classes) {
				for(Field field : clazz.getDeclaredFields()) {
					if(DERObjectIdentifier.class.isAssignableFrom(field.getType())) {
						objId = (DERObjectIdentifier)field.get(null);
						dictionary.put(objId.getId(), field.getName());
					}
				}
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String friendlyASN1ObjectIdentifier(ASN1ObjectIdentifier id) {
		return friendlyASN1ObjectIdentifier(id.getId());
	}
	
	public static String friendlyASN1ObjectIdentifier(String id) {
		String value;
		String name = dictionary.get(id);
		if(name != null) {
			value = id + " - " +  name;
		} else {
			value = id;
		}
		return value;
	}
}
