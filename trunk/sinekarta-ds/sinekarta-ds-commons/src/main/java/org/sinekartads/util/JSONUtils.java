package org.sinekartads.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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


public class JSONUtils {

	public static String toJSON(String[] array) {
		return toJSON((Object)array);
	}

	public static String toJSON(String str) {
		return toJSON((Object)str);
	}
	
	public static String toJSON(Object obj) {
		if ( obj == null)										return null;
		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String json;
			if ( net.sf.json.util.JSONUtils.isArray(obj.getClass()) ) {
				JSONArray jsonArray = JSONArray.fromObject(obj);
				json = jsonArray.toString();
			} else {
				JSONObject jsonobj = JSONObject.fromObject(obj);
				json = jsonobj.toString();
			}
//			baos.write(json.getBytes());
//			
//			baos.flush();
//			return new String(baos.toByteArray());
			return json;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String strFromJSON(String json) {
		return (String)fromJSON(json, String.class);
	}
	public String[] strArrayFromJSON(String json) {
		return (String[])fromJSON(json, String[].class);
	}
	
	public static Object fromJSON(String json, Class<?> clazz) {
		if ( StringUtils.isEmpty(json) )							return null; 
		try {
			if( net.sf.json.util.JSONUtils.isArray(clazz) ) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(new ByteArrayInputStream(json.getBytes()), baos);
				String data = new String(baos.toByteArray());
				JSONArray jsonArray=JSONArray.fromObject(data);
//				JsonConfig jsonConfig = new JsonConfig();
//				jsonConfig.setRootClass(clazz);
//				jsonConfig.setIgnoreTransientFields(true);
//				for ( int i=0; i<jsonArray.size(); i++) {
//					array = 
//				}
				return jsonArray.toArray(new String[jsonArray.size()]);
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(new ByteArrayInputStream(json.getBytes()), baos);
				String data = new String(baos.toByteArray());
				JSONObject jsonObject=JSONObject.fromObject(data);
				JsonConfig jsonConfig = new JsonConfig();
				jsonConfig.setRootClass(clazz);
				jsonConfig.setIgnoreTransientFields(true);
				return JSONObject.toBean(jsonObject, jsonConfig);
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
