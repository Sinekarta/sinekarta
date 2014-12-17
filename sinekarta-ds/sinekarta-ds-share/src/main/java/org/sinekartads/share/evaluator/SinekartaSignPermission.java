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
package org.sinekartads.share.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.alfresco.web.scripts.DictionaryQuery;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sinekartads.share.SinekartaDsModel;

public class SinekartaSignPermission extends BaseEvaluator{

	private DictionaryQuery dictionary;

	/**
	 * Dictionary Query bean reference
	 * 
	 * @param dictionary
	 */
	public void setDictionary(DictionaryQuery dictionary) {
		this.dictionary = dictionary;
	}

	@Override
	public boolean evaluate(JSONObject jsonObject) {
		String nodeType = getNodeType(jsonObject);
		JSONArray nodeAspects = getNodeAspects(jsonObject);
		if (nodeAspects == null) {
            nodeAspects = new JSONArray();
        }
		try {
			// enabled if is a document (CONTENT), NOT a FOLDER, has NOT SIGNED_DOCUMENT aspect 
			if (dictionary.isSubType(nodeType, SinekartaDsModel.TYPE_CONTENT) && 
				!SinekartaDsModel.TYPE_ARCHIVE.equals(nodeType) &&
				!nodeAspects.contains(SinekartaDsModel.ASPECT_SIGNED_DOCUMENT) && 
				!nodeAspects.contains(SinekartaDsModel.ASPECT_TIMESTAMP_MARK)) {
				return true;
			}
			else return false;
		} catch (Exception err) {
			throw new AlfrescoRuntimeException(
					"Failed to run action evaluator: " + err.getMessage(),err);
		}
	}

}
