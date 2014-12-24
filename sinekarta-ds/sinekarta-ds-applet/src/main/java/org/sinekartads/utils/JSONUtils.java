/*
 * Copyright (C) 2014 - 2015 Jenia Software.
 *
 * This file is part of Sinekarta-ds
 *
 * Sinekarta-ds is Open SOurce Software: you can redistribute it and/or modify
 * it under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sinekartads.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.io.IOUtils;

public class JSONUtils {

	public static String serializeJSON ( Object item, OutputStream os ) throws IOException {
		String jsonEnc = serializeJSON ( item, false );
		IOUtils.write ( jsonEnc.getBytes(), os );
		return jsonEnc;
	}
	
	public static String serializeJSON ( Object item ) {
		return serializeJSON ( item, false );
	}
	
	public static String serializeJSON ( Object item, boolean prettify ) {
		if ( item == null )													return "";
		String jsonEnc;
		try {
			Class<?> tClass = item.getClass();
			JsonConfig jsonConfig = standardJsonConfig ( tClass );
			JSON json;
			if ( Object[].class.isAssignableFrom(tClass) ) {
				json = JSONArray.fromObject ( item, jsonConfig );
			} else {
				json = JSONObject.fromObject ( item, jsonConfig );
			}
			if ( prettify ) {
				jsonEnc = json.toString(4);
			} else {
				jsonEnc = json.toString();
			}
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return jsonEnc;
	}

	public static <T> T deserializeJSON ( Class<T> tClass, InputStream is ) throws IOException {
		String jsonEnc = new String ( IOUtils.toByteArray(is) );
		return deserializeJSON(tClass, jsonEnc);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T deserializeJSON ( Class<T> tClass, String jsonEnc ) {
		JsonConfig jsonConfig = standardJsonConfig ( tClass );
		T item;
		if ( Object[].class.isAssignableFrom(tClass) ) {
			JSONArray jsonArray = JSONArray.fromObject ( jsonEnc, jsonConfig );
			Object[] array = (Object[])JSONArray.toArray(jsonArray);
			item = (T)Array.newInstance(tClass.getComponentType(), array.length);
			for(int i=0; i<array.length; i++) {
				((Object[])item)[i] = array[i];
			}
		} else {
			JSONObject jsonObject = JSONObject.fromObject ( jsonEnc, jsonConfig );
			item = (T)JSONObject.toBean(jsonObject, jsonConfig);
		}
		return item;
	}

	
	public static String prettifyJSON ( String json ) {
		String prettyJSON;
		try {
			JSONObject jsonobj = JSONObject.fromObject(json);
			prettyJSON = jsonobj.toString(4);
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return prettyJSON;
	}
	
	public static JsonConfig standardJsonConfig ( Class<?> tClass) {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setRootClass(tClass);
		jsonConfig.setIgnoreTransientFields(true);
		return jsonConfig;
	}}
