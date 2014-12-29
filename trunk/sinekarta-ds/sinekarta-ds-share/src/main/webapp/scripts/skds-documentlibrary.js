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

/**
 * sinekarta-ds Document library actions
 * 
 * @namespace Alfresco
 * @class Alfresco.doclib.Actions
 */
(function() {

   /**
    * YUI Library aliases
    */
	var Dom = YAHOO.util.Dom;
   	var Event = YAHOO.util.Event;
   	var $html = Alfresco.util.encodeHTML;
   	var $combine = Alfresco.util.combinePaths;
   	var $siteURL = Alfresco.util.siteURL;
  
   
   
   	YAHOO.Bubbling.fire("registerAction",  { 
		actionName: "onActionSkdsSign", 
		fn: function DL_onActionSkdsSign(asset) {
			var url = Alfresco.constants.URL_CONTEXT+"page/skdsSignWizard";
			
			var wizardData = {};
			wizardData.nodeRefs = [asset.nodeRef];
			wizardData.tsSelection = "NONE";
			wizardData.backUrl = escape(window.location.href);

			var parameters = {};
			parameters.wizardDataJSON = JSON.stringify(wizardData);
			location.href = buildUrl(url, parameters);
		}
   	});
   	
   	YAHOO.Bubbling.fire("registerAction",  { 
		actionName: "onActionSkdsSignAndMark", 
		fn: function DL_onActionSkdsSignAndMark(asset) {
			var url = Alfresco.constants.URL_CONTEXT+"page/skdsSignWizard";
			
			var wizardData = {};
			wizardData.nodeRefs = [asset.nodeRef];
			wizardData.tsSelection = "DEFAULT";
			wizardData.backUrl = escape(window.location.href);

			var parameters = {};
			parameters.wizardDataJSON = JSON.stringify(wizardData);
			location.href = buildUrl(url, parameters);
		}
   	});
   	
   	YAHOO.Bubbling.fire("registerAction",  { 
		actionName: "onActionSkdsVerify", 
		fn: function DL_onActionSkdsVerify(asset) {
			var url = Alfresco.constants.URL_CONTEXT+"page/skdsVerifyPrepare";
			var parameters = new Array();
			parameters["nodeRefs"] = [asset.nodeRef];
			parameters["backUrl"] = escape(window.location.href);
			location.href = buildUrl(url, parameters);
		}
   	});
   	
   	/**
   	 * Build an URL with queryString which can be redirected with a get call
   	 * @param url the full URL to be redirected
   	 * @param parameters a JavaScript object with the queryString parameters
   	 * @returns the composed URL
   	 */
   	function buildUrl ( url, parameters ) {
   		var href = "";
   		var qs = "";
   		for(var key in parameters) {
   			var value = parameters[key];
   			qs += encodeURIComponent(key) + "=" + encodeURIComponent(value) + "&";
   		}
   		if (qs.length > 0) {
   			qs = qs.substring(0, qs.length-1); 
   			href = url + "?" + qs;
   		}
   		return href;
   	}


})();