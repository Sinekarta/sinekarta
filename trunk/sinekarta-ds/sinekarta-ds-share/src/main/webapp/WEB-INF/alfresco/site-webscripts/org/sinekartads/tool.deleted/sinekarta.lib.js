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
function calculatePdfName(fileName) {
	var dotIdx = fileName.lastIndexOf('.');
	if (dotIdx>=0) {
		return fileName.substring(0, dotIdx) + "_pdfa.pdf";
	} else {
		return fileName + "_pdfa.pdf";
	}
}
function getAllDocumentType(model, full) {
	var params = new Object();
	params.full=(full);
	var sinekartaResponse = remoteJsonGet("/sinekarta/documentType",params);
	model.documentTypes = sinekartaResponse.documentTypes;
	return;
}
function getAllDocumentTypeLanguage(model) {
	var sinekartaResponse = remoteJsonGet("/sinekarta/documentTypeLanguage");
	model.documentTypeLanguages = sinekartaResponse.documentTypeLanguages;
	model.userDefaultLanguage = sinekartaResponse.userDefaultLanguage;
	return;
}
function getAllDriverName(model) {
	var sinekartaResponse = remoteJsonGet("/sinekarta/smartCardDriver");
	model.smartCardDrivers = sinekartaResponse.smartCardDrivers;
	model.userDefaultDriver = sinekartaResponse.userDefaultDriver;
	return;
}
function setDocumentName(nodeRef, newName) {
	var script = "{ \"result\" : (search.findNode(\""+nodeRef+"\").name=\""+newName+"\") }";
	var res = remoteExec(script);
	return res.result;
}
function getDocumentName(nodeRef) {
	var script = "{ \"result\" : search.findNode(\""+nodeRef+"\").name }";
	var res = remoteExec(script);
	return res.result;
}
function getParentNodeRef(nodeRef) {
	var script = "{ \"result\" : search.findNode(\""+nodeRef+"\").parent.nodeRef }";
	var res = remoteExec(script);
	return res.result;
}
function hasChild(nodeRef, name) {
	var script = "{ \"result\" : !(search.findNode(\""+nodeRef+"\").childByNamePath(\""+name+"\")==null) }";
	var res = remoteExec(script);
	return res.result;
}
function remoteExec(script) {
	var connector = remote.connect("alfresco");
	var result = connector.post("/sinekarta/internal/theTool",script,"text/plain");
	if (result.status == 200) {
		return eval('(' + result + ')');
	} else {
		throw ("error on execute remote json tool; result.status : " + prettyPrintResult(result.status));
	}
}
function remoteJsonGet(url,parameters) {
	var connector = remote.connect("alfresco");
	var params="";
	if (parameters!=null) {
		for (var name in parameters) {
		  params=params+"&"+name+"="+encodeURIComponent(eval("parameters."+name));
		}
		url = url + ".json?" + params.substring(1);
	} else {
		url = url + ".json";
	}
	var result = connector.get(url);
	if (result.status == 200) {
		return eval('(' + result + ')');
	} else {
		throw ("error on execute remote json get; result.status : " + prettyPrintResult(result.status));
	}
}
function remoteJsonPost(url,parameters) {
	var connector = remote.connect("alfresco");
	var result = connector.post(url+".json?requestType=json",jsonUtils.toJSONString(parameters),"application/json");
	if (result.status == 200) {
		return eval('(' + result + ')');
	} else {
		throw ("error on execute remote json post; result.status : " + prettyPrintResult(result.status));
	}
}
function prettyPrintResult(status) {
	var ret = "statusCode : " + status.code + 
			" ; codeName : " + status.codeName + 
			" ; codeDescription : " + status.codeDescription + 
			" ; message : " + status.message  + 
			" ; exception : " + status.exception;
	logger.log(ret);
	return ret;
}
function getAlfrescoVersion(model) {
	var alfrescoEdition = "Unknown";
	var alfrescoVersion = "Unknown";
	var alfrescoBuild = "Unknown";
	var alfrescoSchema = "Unknown";

	var json = remoteJsonGet("/api/server");
	if (json.data)
	{
		var split = json.data.version.indexOf(" ");
		var length = json.data.version.length;

		alfrescoEdition = json.data.edition;
		alfrescoVersion = json.data.version.substring(0, split);
		alfrescoBuild = json.data.version.substring(split+1, length);
		alfrescoSchema = json.data.schema;
	}
	
	model.alfrescoEdition = alfrescoEdition;
	model.alfrescoVersion = alfrescoVersion;
	model.alfrescoBuild = alfrescoBuild;
	model.alfrescoSchema = alfrescoSchema;
	
	return;
}
//----------------------------
/*
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */

var dateFormat = function () {
	var	token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g,
		timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g,
		timezoneClip = /[^-+\dA-Z]/g,
		pad = function (val, len) {
			val = String(val);
			len = len || 2;
			while (val.length < len) val = "0" + val;
			return val;
		};

	// Regexes and supporting functions are cached through closure
	return function (date, mask, utc) {
		var dF = dateFormat;

		// You can't provide utc if you skip other args (use the "UTC:" mask prefix)
		if (arguments.length == 1 && Object.prototype.toString.call(date) == "[object String]" && !/\d/.test(date)) {
			mask = date;
			date = undefined;
		}

		// Passing date through Date applies Date.parse, if necessary
		date = date ? new Date(date) : new Date;
		if (isNaN(date)) throw SyntaxError("invalid date");

		mask = String(dF.masks[mask] || mask || dF.masks["default"]);

		// Allow setting the utc argument via the mask
		if (mask.slice(0, 4) == "UTC:") {
			mask = mask.slice(4);
			utc = true;
		}

		var	_ = utc ? "getUTC" : "get",
			d = date[_ + "Date"](),
			D = date[_ + "Day"](),
			m = date[_ + "Month"](),
			y = date[_ + "FullYear"](),
			H = date[_ + "Hours"](),
			M = date[_ + "Minutes"](),
			s = date[_ + "Seconds"](),
			L = date[_ + "Milliseconds"](),
			o = utc ? 0 : date.getTimezoneOffset(),
			flags = {
				d:    d,
				dd:   pad(d),
				ddd:  dF.i18n.dayNames[D],
				dddd: dF.i18n.dayNames[D + 7],
				m:    m + 1,
				mm:   pad(m + 1),
				mmm:  dF.i18n.monthNames[m],
				mmmm: dF.i18n.monthNames[m + 12],
				yy:   String(y).slice(2),
				yyyy: y,
				h:    H % 12 || 12,
				hh:   pad(H % 12 || 12),
				H:    H,
				HH:   pad(H),
				M:    M,
				MM:   pad(M),
				s:    s,
				ss:   pad(s),
				l:    pad(L, 3),
				L:    pad(L > 99 ? Math.round(L / 10) : L),
				t:    H < 12 ? "a"  : "p",
				tt:   H < 12 ? "am" : "pm",
				T:    H < 12 ? "A"  : "P",
				TT:   H < 12 ? "AM" : "PM",
				Z:    utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
				o:    (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
				S:    ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
			};

		return mask.replace(token, function ($0) {
			return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
		});
	};
}();

// Some common format strings
dateFormat.masks = {
	"default":      "ddd mmm dd yyyy HH:MM:ss",
	shortDate:      "m/d/yy",
	mediumDate:     "mmm d, yyyy",
	longDate:       "mmmm d, yyyy",
	fullDate:       "dddd, mmmm d, yyyy",
	shortTime:      "h:MM TT",
	mediumTime:     "h:MM:ss TT",
	longTime:       "h:MM:ss TT Z",
	isoDate:        "yyyy-mm-dd",
	isoTime:        "HH:MM:ss",
	isoDateTime:    "yyyy-mm-dd'T'HH:MM:ss",
	isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
};

// Internationalization strings
dateFormat.i18n = {
	dayNames: [
		"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat",
		"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
	],
	monthNames: [
		"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
		"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
	]
};

