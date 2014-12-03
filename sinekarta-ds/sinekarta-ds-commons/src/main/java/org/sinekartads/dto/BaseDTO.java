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
package org.sinekartads.dto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.sinekartads.dto.tools.DTODeserializer.Base64Deserializer;
import org.sinekartads.dto.tools.DTODeserializer.HexDeserializer;
import org.sinekartads.dto.tools.DTODeserializer.JSONDeserializer;
import org.sinekartads.dto.tools.DTODeserializer.XMLDeserializer;
import org.sinekartads.dto.tools.DTOPropertyType;
import org.sinekartads.dto.tools.DTOSerializer.Base64Serializer;
import org.sinekartads.dto.tools.DTOSerializer.HexSerializer;
import org.sinekartads.dto.tools.DTOSerializer.JSONSerializer;
import org.sinekartads.dto.tools.DTOSerializer.XMLSerializer;
import org.sinekartads.util.TemplateUtils;

public abstract class BaseDTO implements Serializable {

	private static final long serialVersionUID = -4276897678144607001L;

	public static boolean isEmpty ( BaseDTO dto )  {
		if ( dto == null )							return true;
		return dto.isEmpty();
	}

	public static boolean isNotEmpty ( BaseDTO dto )  {
		if ( dto == null )							return false;
		return !dto.isEmpty();
	}
	
	public boolean isEmpty ( ) {
		return false;
	}
	
	/**
	 * @deprecated ignore this field - fake field for serialization only proposes
	 */
	protected transient boolean empty;
	
	
	
	public BaseDTO() {
		Class<? extends BaseDTO> dtoClass = getClass();
		
		try {
			String property;
			Class<?> fieldType;
			for ( Field field : dtoClass.getDeclaredFields() ) {
				// Ignore the static field 
				if((field.getModifiers() & java.lang.reflect.Modifier.STATIC) > 0) continue;
				
				// Set the property value to "" if it represents a String
				property = field.getName();
				fieldType = field.getType();
				if ( String.class.isAssignableFrom(fieldType) ) {
						BeanUtils.setProperty(this, property, "");
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String toString() {
		return TemplateUtils.Encoding.serializeJSON ( this );
	}
	
	public boolean equals ( Object obj ) {
		boolean equals = false;
		if ( obj != null && obj instanceof BaseDTO ) {
			equals = StringUtils.equals ( 
					TemplateUtils.Encoding.serializeJSON ( this ), 
					TemplateUtils.Encoding.serializeJSON ( (BaseDTO)obj ) );
		}
		return equals; 
	}
	
	
	
	// -----
	// --- Hex serialization
	// -
	
	public static <DTO extends BaseDTO> DTO fromHex(InputStream is, Class<DTO> clazz) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(is, baos);
		String hex = new String(baos.toByteArray());
		return fromHex(hex, clazz);
	}
	
	public static<DTO extends BaseDTO> DTO fromHex(String hex, Class<DTO> clazz) {
		return TemplateUtils.Encoding.deserializeHex(clazz, hex);
	}
	
	public String toHex() {
		return TemplateUtils.Encoding.serializeHex(this);
	}
	
	public static <DTO extends BaseDTO> String serializeHex ( DTO dto ) {
		return new HexSerializer<DTO>().transform ( dto );
	}
	
	@SafeVarargs
	public static <DTO extends BaseDTO> String[] serializeHex ( DTO ... dtos ) {
		return TemplateUtils.Transformation.transform ( new HexSerializer<DTO>(), dtos );
	}
	
	public static <DTO extends BaseDTO> Collection<String> serializeHex ( Collection<DTO> dtoCol ) {
		return TemplateUtils.Transformation.transform(new HexSerializer<DTO>(), dtoCol);
	}
	
	public static <DTO extends BaseDTO> DTO deserializeHex ( Class<DTO> dtoClass, String hex ) {
		return (DTO)TemplateUtils.Transformation.transform ( new HexDeserializer<DTO>(dtoClass), hex );
	}
	
	public static <DTO extends BaseDTO> DTO[] deserializeHex ( Class<DTO> dtoClass, String ... hexArray ) {
		return (DTO[])TemplateUtils.Transformation.transform ( new HexDeserializer<DTO>(dtoClass), hexArray );
	}
	
	public static <DTO extends BaseDTO, 
					DTOCol extends Collection<DTO>> 
							DTOCol deserializeHex ( Class<DTO> dtoClass, Collection<String> hexCol ) {
		return TemplateUtils.Transformation.transform(new HexDeserializer<DTO>(dtoClass), hexCol);
	}
	

	
	// -----
	// --- Base64 serialization
	// -
	
	public static <DTO extends BaseDTO> DTO fromBase64(InputStream is, Class<DTO> clazz) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(is, baos);
		String base64 = new String(baos.toByteArray());
		return fromBase64(base64, clazz);
	}
	
	public static<DTO extends BaseDTO> DTO fromBase64(String base64, Class<DTO> clazz) {
		return TemplateUtils.Encoding.deserializeBase64(clazz, base64);
	}
	
	public String toBase64() {
		return TemplateUtils.Encoding.serializeBase64(this);
	}
	
	public static <DTO extends BaseDTO> String serializeBase64 ( DTO dto ) {
		return new Base64Serializer<DTO>().transform ( dto );
	}
	
	@SafeVarargs
	public static <DTO extends BaseDTO> String[] serializeBase64 ( DTO ... dtos ) {
		return TemplateUtils.Transformation.transform ( new Base64Serializer<DTO>(), dtos );
	}
	
	public static <DTO extends BaseDTO> Collection<String> serializeBase64 ( Collection<DTO> dtoCol ) {
		return TemplateUtils.Transformation.transform(new Base64Serializer<DTO>(), dtoCol);
	}
	
	public static <DTO extends BaseDTO> DTO deserializeBase64 ( Class<DTO> dtoClass, String base64 ) {
		return (DTO)TemplateUtils.Transformation.transform ( new Base64Deserializer<DTO>(dtoClass), base64 );
	}
	
	public static <DTO extends BaseDTO> DTO[] deserializeBase64 ( Class<DTO> dtoClass, String ... base64Array ) {
		return (DTO[])TemplateUtils.Transformation.transform ( new Base64Deserializer<DTO>(dtoClass), base64Array );
	}
	
	public static <DTO extends BaseDTO, 
					DTOCol extends Collection<DTO>> 
							DTOCol deserializeBase64 ( Class<DTO> dtoClass, Collection<String> base64Col ) {
		return TemplateUtils.Transformation.transform(new Base64Deserializer<DTO>(dtoClass), base64Col);
	}
	
	
	
	// -----
	// --- JSON serialization
	// -
	
	public static <DTO extends BaseDTO> DTO fromJSON(Class<? extends DTO> dtoClass, String json) {
		return TemplateUtils.Encoding.deserializeJSON(dtoClass, json);
	}

	public static <DTO extends BaseDTO> DTO fromJSON(InputStream is, Class<? extends DTO> dtoClass) throws IOException {
		String json = new String ( IOUtils.toByteArray(is) );
		return TemplateUtils.Encoding.deserializeJSON ( dtoClass, json );
	}
	
	public void toJSON(OutputStream os) throws IOException {
		String json = toJSON(); 
		os.write ( json.getBytes() );
		os.flush();
	}

	public String toJSON() {
		return TemplateUtils.Encoding.serializeJSON ( this );
	}
	
	public static <DTO extends BaseDTO> String serializeJSON ( DTO dto ) {
		return new JSONSerializer<DTO>().transform ( dto );
	}
	
	@SafeVarargs
	public static <DTO extends BaseDTO> String[] serializeJSON ( DTO ... dtos ) {
		return TemplateUtils.Transformation.transform ( new JSONSerializer<DTO>(), dtos );
	}
	
	public static <DTO extends BaseDTO> Collection<String> serializeJSON (Collection<DTO> dtoCol ) {
		return TemplateUtils.Transformation.transform(new JSONSerializer<DTO>(), dtoCol);
	}
	
	public static <DTO extends BaseDTO> DTO[] deserializeJSON ( Class<DTO> dtoClass, String ... jsonArray ) {
		return TemplateUtils.Transformation.transform ( new JSONDeserializer<DTO>(dtoClass), jsonArray );
	}
	
	public static <DTO extends BaseDTO> DTO deserializeJSON ( Class<DTO> dtoClass, String json ) {
		return new JSONDeserializer<DTO>(dtoClass).transform ( json );
	}
	
	public static <DTO extends BaseDTO, 
					DTOCol extends Collection<DTO>> 
							DTOCol deserializeJSON ( Class<DTO> dtoClass, Collection<String> jsonCol ) {
		return TemplateUtils.Transformation.transform(new JSONDeserializer<DTO>(dtoClass), jsonCol);
	}
	
	
	
	// -----
	// --- XML serialization
	// -
	
	public static <DTO extends BaseDTO> DTO fromXML ( String xml, Class<DTO> clazz ) throws JAXBException {
		return fromXML ( new ByteArrayInputStream(xml.getBytes()), clazz );
	}
	
	@SuppressWarnings("unchecked")
	public static <DTO extends BaseDTO> DTO fromXML ( InputStream is, Class<DTO> clazz ) throws JAXBException {
		Unmarshaller u = JAXBContext.newInstance(clazz).createUnmarshaller();
		return (DTO) u.unmarshal(is);
	}
	
	public void toXML(OutputStream os) throws JAXBException {
		JAXBContext jcupr = JAXBContext.newInstance(this.getClass());
		Marshaller m = jcupr.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(this, os);
	}
	
	public String toXML() throws JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		toXML(baos);
		IOUtils.closeQuietly(baos);
		return new String(baos.toByteArray());
	}
	
	public static <DTO extends BaseDTO> String serializeXML ( DTO dto ) {
		return new XMLSerializer<DTO>().transform ( dto );
	}
	
	@SafeVarargs
	public static <DTO extends BaseDTO> String[] serializeXML ( DTO ... dtos ) {
		return TemplateUtils.Transformation.transform ( new XMLSerializer<DTO>(), dtos );
	}
	
	public static <DTO extends BaseDTO> Collection<String> serializeXML (Collection<DTO> dtoCol ) {
		return TemplateUtils.Transformation.transform(new XMLSerializer<DTO>(), dtoCol);
	}
	
	public static <DTO extends BaseDTO> DTO[] deserializeXML ( Class<DTO> dtoClass, String ... xmlArray ) {
		return TemplateUtils.Transformation.transform (new XMLDeserializer<DTO>(dtoClass), xmlArray );
	}
	
	public static <DTO extends BaseDTO> DTO deserializeXML ( Class<DTO> dtoClass, String xml ) {
		return new XMLDeserializer<DTO>(dtoClass).transform ( xml );
	}
	
	public static <DTO extends BaseDTO, 
					DTOCol extends Collection<DTO>> 
							DTOCol deserializeXML ( Class<DTO> dtoClass, Collection<String> hexCol ) {
		return TemplateUtils.Transformation.transform ( new XMLDeserializer<DTO>(dtoClass), hexCol );
	}
	
	
	
	// -----
	// --- Field internationalization 
	// -
	
//	private final static Map<Class<? extends Annotation>, DTOPropertyType> ANNOTATION_MAPPING = annotationMapping(Arrays.asList(DTOPropertyType.values()));
	
//	private static Map<Class<? extends Annotation>, DTOPropertyType> annotationMapping(Collection<DTOPropertyType> dtoPropertyTypes) {
//		Map<Class<? extends Annotation>, DTOPropertyType> mapping = 
//				new HashMap<Class<? extends Annotation>, DTOPropertyType>();
//		for(DTOPropertyType dtoPropertyType : dtoPropertyTypes) {
//			mapping.put(dtoPropertyType.getAnnot(), dtoPropertyType);
//		}
//		return mapping;
//	}
	
	protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	protected DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	protected DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	protected NumberFormat integerFormat = new DecimalFormat("0");
	protected NumberFormat decimalFormat = new DecimalFormat("0.00");
	
	protected void formatValues(Map<DTOPropertyType, String> dtoPropertyFormats) throws IllegalArgumentException {
		// Mapping annotation -> dtoPropertyType restricted on the managed annotations
		Map<Class<? extends Annotation>, DTOPropertyType> annotationTypes 
				= new HashMap<Class<? extends Annotation>, DTOPropertyType>();
		for(Map.Entry<DTOPropertyType, String> entry : dtoPropertyFormats.entrySet()) {
			annotationTypes.put(entry.getKey().getAnnot(), entry.getKey());
		}
		
		try {
			String newFormat;
			DateFormat newDateFormat = null;
			DateFormat newTimeFormat = null;
			DateFormat newDateTimeFormat = null;
			NumberFormat newIntegerFormat = null;
			NumberFormat newDecimalFormat = null;
			// create the the custom formatters 
			newFormat = dtoPropertyFormats.get(DTOPropertyType.Date); 
			if(newFormat != null) {
				newDateFormat = new SimpleDateFormat(newFormat);
			}
			newFormat = dtoPropertyFormats.get(DTOPropertyType.Time);
			if(newFormat != null) {
				newTimeFormat = new SimpleDateFormat(newFormat);
			}
			newFormat = dtoPropertyFormats.get(DTOPropertyType.DateTime);
			if(newFormat != null) {
				newDateTimeFormat = new SimpleDateFormat(newFormat);
			}
			newFormat = dtoPropertyFormats.get(DTOPropertyType.Integer);
			if(newFormat != null) {
				newIntegerFormat = new DecimalFormat(newFormat);
			}
			newFormat = dtoPropertyFormats.get(DTOPropertyType.Decimal);
			if(newFormat != null) {
				newDecimalFormat = new DecimalFormat(newFormat);
			}
			
			// change formats into the dto and all its children
			Field[] fields = getClass().getDeclaredFields();
			String property;
			Class<?> fieldType;
			DTOPropertyType dtoPropertyType;
			for(Field field : fields) {
				// ignore the static field 
				if((field.getModifiers() & java.lang.reflect.Modifier.STATIC) > 0) continue;
				
				property = field.getName();
				fieldType = field.getType();
				if(BaseDTO.class.isAssignableFrom(fieldType)) {
					// recursion on the children (as single member)
					BaseDTO dto = (BaseDTO)PropertyUtils.getProperty(this, property);
					dto.formatValues(dtoPropertyFormats);
				} else if(BaseDTO[].class.isAssignableFrom(fieldType)) {
					// recursion on the children (as an array)
					for(BaseDTO dto : (BaseDTO[])PropertyUtils.getProperty(this, property)) {
						dto.formatValues(dtoPropertyFormats);
					}
				} else {
					// format the other (String) values
					String strValue = (String)PropertyUtils.getProperty(this, property);
					if(StringUtils.isNotBlank(strValue)) {
						Object value = null;
						for(Annotation annot : field.getAnnotations()) {
							// dtoPropertyType of the current field (or null)
							dtoPropertyType = annotationTypes.get(annot.annotationType());
							// newFormat to be applied to the current field's dtoPropertyType 
							newFormat = dtoPropertyFormats.get(dtoPropertyType);
							// if not null (the annotation is owned by a managed DtoPropertyType)
							if(newFormat != null) {
								// in every managed case, parse the value and apply to it the new format
								switch(dtoPropertyType) {
								case Flag: {	
									value = BooleanUtils.toBoolean(strValue);
									PropertyUtils.setProperty(this, property, value);
									break;
								}
								case Date: {	
									value = dateFormat.parse(strValue);
									PropertyUtils.setProperty(this, property, newDateFormat.format(value));
									break;
								}
								case Time: {	
									value = timeFormat.parse(strValue);
									PropertyUtils.setProperty(this, property, newTimeFormat.format(value));
									break;
								}
								case DateTime: {	
									value = dateTimeFormat.parse(strValue);
									PropertyUtils.setProperty(this, property, newDateTimeFormat.format(value));
									break;
								}
								case Integer: {	
									value = integerFormat.parse(strValue).intValue();
									PropertyUtils.setProperty(this, property, newIntegerFormat.format(value));
									break;
								}
								case Decimal: { 
									value = decimalFormat.parse(strValue);
									PropertyUtils.setProperty(this, property, newDecimalFormat.format(value));
								}
								default: throw new IllegalArgumentException("unimplemented format: " + dtoPropertyType.name());
								} // end switch
							}
						}
						// non-annotated fields are not modified
					}
				}
			}
			
			// update the internal formatters
			if(newDateFormat != null) 		dateFormat = newDateFormat;
			if(newTimeFormat != null) 		timeFormat = newTimeFormat;
			if(newDateTimeFormat != null) 	dateTimeFormat = newDateTimeFormat;
			if(newIntegerFormat != null) 	integerFormat = newIntegerFormat;
			if(newDecimalFormat != null) 	decimalFormat = newDecimalFormat;
		} catch(RuntimeException e) {
			throw e;			
		} catch(Exception e) {
			// errors that should happen only during the test phase
			throw new RuntimeException(e);
		} 
	}
	
//	protected Object getAnnotatedProperty(String property) throws IllegalArgumentException {
//		Field field;
//		// verify the presence of the required property
//		try {
//			field = getClass().getDeclaredField(property);
//		} catch(NoSuchFieldException e) {
//			throw new IllegalArgumentException("Property not found: " + property, e);
//		}
//		Object value = null;
//		// apply the suitable formatter to parse the property value
//		try {
//			String strValue = (String)PropertyUtils.getProperty(this, property);
//			if(StringUtils.isBlank(strValue)) {
//				DTOPropertyType dtoPropertyType;
//				Annotation[] annotations = field.getAnnotations();
//				for(int i=0; i<annotations.length && value == null; i++) {
//					dtoPropertyType = ANNOTATION_MAPPING.get(annotations[i].annotationType());
//					switch(dtoPropertyType) {
//					case Flag: 		value = BooleanUtils.toBoolean(strValue);	break;
//					case Date: 		value = dateFormat.parse(strValue);			break;
//					case Time: 		value = timeFormat.parse(strValue);			break;
//					case DateTime: 	value = dateTimeFormat.parse(strValue);		break;
//					case Integer:  	value = integerFormat.parse(strValue);		break;
//					case Decimal:  	value = decimalFormat.parse(strValue);		break;
//					default: throw new IllegalArgumentException("unimplemented format: " + dtoPropertyType.name());
//					}
//				}
//				// value must has been set or the property is wrongly annotated
//				if(value == null) {
//					throw new IllegalArgumentException("unable to parse from the property " + property);
//				}
//			} else {
//				value = null;
//			}
//		} catch(RuntimeException e) {
//			throw e;
//		} catch(Exception e) {
//			// errors that should happen only during the test phase
//			throw new RuntimeException(e);
//		}
//		return value;
//	}
	
//	protected void setAnnotatedProperty(String property, Object value) throws IllegalArgumentException {
//		Field field;
//		// verify the presence of the required property
//		try {
//			field = getClass().getDeclaredField(property);
//		} catch(NoSuchFieldException e) {
//			throw new IllegalArgumentException("Property not found: " + property, e);
//		}
//		try {
//			String strValue = null;
//			// apply the suitable formatter to parse the property value
//			if(value != null) {
//				DTOPropertyType dtoPropertyType;
//				Annotation[] annotations = field.getAnnotations();
//				for(int i=0; i<annotations.length && strValue == null; i++) {
//					dtoPropertyType = ANNOTATION_MAPPING.get(annotations[i].annotationType());
//					switch(dtoPropertyType) {
//					case Flag: 		strValue = ((Boolean)value).toString();		break;
//					case Date: 		strValue = dateFormat.format(value);		break;
//					case Time: 		strValue = timeFormat.format(value);		break;
//					case DateTime: 	strValue = dateTimeFormat.format(value);	break;
//					case Integer:  	strValue = integerFormat.format(value);		break;
//					case Decimal:  	strValue = decimalFormat.format(value);		break;
//					default: throw new IllegalArgumentException("unimplemented format: " + dtoPropertyType.name());
//					}
//				}
//				// strValue must has been set or the property is wrongly annotated
//				if(StringUtils.isBlank(strValue)) {
//					throw new IllegalArgumentException("unable to format to the property " + property);
//				}
//				PropertyUtils.setProperty(this, property, strValue);
//			}
//		} catch(RuntimeException e) {
//			throw e;			
//		} catch(Exception e) {
//			// errors that should happen only during the test phase
//			throw new RuntimeException(e);
//		}
//	}
	
}


