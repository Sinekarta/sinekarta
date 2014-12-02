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
package org.sinekartads.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

public class TextUtils {
	

	private static final char LEFT_FILLER = 'L';
	private static final char RIGHT_FILLER = 'R';
	private static final String ENCODING = "UTF-8";
	private static final Base64 base64 = new Base64();

	/**
	 * filling a string to a length with a specified char
	 * if input string is longer than the required len, result will be trunked
	 * @param in
	 * @param leftRight
	 * @param filler
	 * @param len
	 * @return filled (or trunked string
	 */
	public static String FILL(String in, char leftRight, char filler, int len) {
		StringBuffer out = new StringBuffer();
		int lenout = (in!=null?in.length():0);
		if (lenout > len) lenout=len;
		if (leftRight == RIGHT_FILLER) {
			if (in!=null && in.length()>len)
				in=in.substring(0, len);
			if (in != null)
				out.append(in);
			for (int i = 0; i < len - lenout; i++) 
				out.append(filler);
		} else if (leftRight == LEFT_FILLER) { 
			for (int i = 0; i < len - lenout; i++) 
				out.append(filler);
			if (in!=null && in.length()>len)
				in=in.substring(in.length() - len);
			if (in != null)
				out.append(in);
		}
		return out.toString();
	}

	
	public static String appendToken(String str, String token) {
		if ( StringUtils.isBlank(str) ) {
			str = "";
		}
		return appendToken(new StringBuilder(str), token).toString();
	}
	
	public static String appendToken(String str, String token, String separator) {
		if ( StringUtils.isBlank(str) ) {
			str = "";
		}
		return appendToken(new StringBuilder(str), token, separator).toString();
	}
	
	public static StringBuilder appendToken(StringBuilder buf, String token) {
		return appendToken(buf, token, ", ");
	}
	
	public static StringBuilder appendToken(StringBuilder buf, String token, String separator) {
		if ( StringUtils.isEmpty(separator) ) {
			separator = " ";
		}
		if ( buf.length() > 0 ) {
			buf.append(separator);
		}
		if ( StringUtils.isNotBlank(token) ) {
			buf.append(token.trim());
		}
		return buf;
	}

	
	/**
	 * serialization of the class using json and base64 encoder
	 * @return
	 */
	public static String toBase64(Object obj) {
		JSONObject jsonobj = JSONObject.fromObject(obj);
		byte[] outputBuffer;
		try {
			outputBuffer = jsonobj.toString().getBytes(ENCODING);
		} catch (UnsupportedEncodingException e) {
			// never thrown, fixed encoding
			throw new RuntimeException(e);
		}
		return new String(base64.encode(outputBuffer));
	}

	/**
	 * deserialization of the class using json and base64 decoder
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Object fromBase64(String inputString, Class targetClass) {
		JSONObject inputJson;
		try {
			inputJson = JSONObject.fromObject(new String(base64.decode(inputString), ENCODING));
		} catch (UnsupportedEncodingException e) {
			// never thrown, fixed encoding
			throw new RuntimeException(e);
		}
		return JSONObject.toBean(inputJson, targetClass);
	}
	
	
	
	// -----
	// --- Arrays and Collections format
	// -
	
	public static String fromArray(Object[] items) {
		return fromArray(items, null, ", ", "[", "]");
	}
	
	public static String fromArray(Object[] items, ObjectFormatter<?> formatter) {
		return fromArray(items, formatter, ", ", "[", "]");
	}
	
	public static String fromArray(Object[] items, String separator, String prefix, String postfix) {
		return fromArray(items, null, separator, prefix, postfix);
	}
	
	public static String fromArray(Object[] items, ObjectFormatter<?> formatter, String separator, String prefix, String postfix) {
		if(formatter == null) {
			formatter = ObjectFormatter.DEFAULT;
		}
		StringBuilder buf = new StringBuilder();
		for(Object obj : items) {
			buf.append(formatter.formatObj(obj)).append(", ");
		}
		String strItems = buf.toString();
		if(StringUtils.isNotBlank(strItems)) {
			strItems = strItems.substring(0, strItems.length() - 2);
		}
		return prefix + strItems + postfix;
	}
	
	public static String fromCollection(Collection<? extends Object> items) {
		return fromCollection(items, null, ", ", "[", "]");
	}
	
	public static String fromCollection(Collection<? extends Object> items, ObjectFormatter<? extends Object> formatter) {
		return fromCollection(items, formatter, ", ", "[", "]");
	}
	
	public static String fromCollection(Collection<? extends Object> items, ObjectFormatter<? extends Object> formatter, String separator, String prefix, String postfix) {
		if(formatter == null) {
			formatter = ObjectFormatter.DEFAULT;
		}
		StringBuilder buf = new StringBuilder();
		for(Object obj : items) {
			buf.append(formatter.formatObj(obj)).append(separator);
		}
		String strItems = buf.toString();
		if(StringUtils.isNotBlank(strItems)) {
			strItems = strItems.substring(0, strItems.length() - 2);
		}
		return prefix + strItems + postfix;
	}
	
	
	
	// -----
	// --- Input type format
	// -

	public static String toLowerCase ( String str ) {
		return str.toLowerCase();
	}
	
	public static String formatClasses(Object[] params) {
		String[] classNames = new String[params.length];
		for(int i=0; i<params.length; i++) {
			classNames[i] = params[i].getClass().getName();
		}
		return TextUtils.fromArray(classNames);
	}
	
	public static String toUpperCase ( String str ) {
		String normalizedStr = normalizeChars ( str );
		for (String token : normalizedStr.split("[^\\s\\w]+") ) {
			token = normalizeChars(token);
		}
		return str.toUpperCase();
	}
	
	public static String toCamelCase ( String str ) {
		return str.toLowerCase();
	}
	
	public static String normalizeChars ( String str ) {
		return normalizeChars ( str, " " );
	}
	
	public static String normalizeChars ( String str, String replacement ) {
		if ( StringUtils.isBlank(str) ) 										return "";
		return singularize ( str, replacement );
	}
	
	public static String singularize ( String str, String regexp ) {
		
		if ( StringUtils.isBlank(str) ) 										return "";
		if ( StringUtils.isBlank(regexp) ) 										return str;
		
		return str.replaceAll(
				String.format("(%s)((%s)+)", regexp),	// replace the multiple occurrences 
				regexp);											   // with a single one
	}
}

