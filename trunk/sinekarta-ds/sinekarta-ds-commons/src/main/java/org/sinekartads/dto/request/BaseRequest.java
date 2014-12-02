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
package org.sinekartads.dto.request;

import org.apache.commons.lang3.StringUtils;
import org.sinekartads.dto.BaseDTO;

public abstract class BaseRequest extends BaseDTO {
	
	private static final long serialVersionUID = -4894748877824011215L;

	public String getJSONUrl() {
		String className = getClass().getSimpleName(); 
		String cleanName = className.substring(0, className.lastIndexOf("Request"));
		String serviceName = StringUtils.uncapitalize(cleanName);
		return "/sinekartads/" + serviceName; 
	}
	
}
