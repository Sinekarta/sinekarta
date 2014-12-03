package org.sinekartads.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.io.IOUtils;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.TemplateUtils.Instantiation;

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
		if ( obj == null)										return "";
		try {
			JSONObject jsonobj = JSONObject.fromObject(obj, standardJsonConfig(obj.getClass()));
			return jsonobj.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static String toJSONArray(Object[] array) {
		if ( array == null)										return "";
		try {
			JSONArray jsonArray = JSONArray.fromObject(array, standardJsonConfig(array.getClass()));
			return jsonArray.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static Object[] fromJSONArray ( Class<? extends Object[]> arrayClass, String json ) {
		JSONArray jsonArray = JSONArray.fromObject ( json, standardJsonConfig(arrayClass) );
		Class<? extends Object> tClass = arrayClass.getComponentType();
		Object[] tArray = Instantiation.nullFilledArray ( tClass, jsonArray.size() );
		return jsonArray.toArray ( tArray );
	}
	
	public static Object fromJSON(String json, Class<?> clazz) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(new ByteArrayInputStream(json.getBytes()), baos);
			String data = new String(baos.toByteArray());
			JSONObject jsonObject=JSONObject.fromObject(data);
			return JSONObject.toBean(jsonObject, standardJsonConfig(clazz));
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static JsonConfig standardJsonConfig ( Class<?> tClass ) {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setRootClass(tClass);
		jsonConfig.setIgnoreTransientFields(true);
		return jsonConfig;
	}
}
