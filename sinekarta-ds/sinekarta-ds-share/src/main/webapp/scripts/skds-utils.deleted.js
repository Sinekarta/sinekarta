///*
// * Copyright (C) 2010 - 2012 Jenia Software.
// *
// * This file is part of Sinekarta
// *
// * Sinekarta is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Sinekarta is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// */
//
///**
// * Alfresco.skds
// *
// */
//(function()
//{
//	/**
//	 * YUI Library aliases
//	 */
//	var Dom = YAHOO.util.Dom,
//	    Event = YAHOO.util.Event;
//		
//	/**
//	 * Alfresco Slingshot aliases
//	 */
//	Alfresco.skds = function(htmlId)
//	{    
//		this.id = htmlId;
//
//	    Alfresco.skds.superclass.constructor.call(this, "Alfresco.skds", htmlId, ["button", "container"]);
//
//		return this;
//	};
//
//	YAHOO.lang.extend(Alfresco.skds, Alfresco.component.Base, 
//	{
//		/**
//		 * Object container for initialization options
//		 *
//		 * @property options
//		 * @type object
//		 */
//		options:
//		{
//			/**
//			 * The component id
//			 *
//			 * @property componentId
//			 * @type string
//			 * @default ""
//			 */
//			componentId: "",
//		},
//		
//		/**
//		 * The wizardData 
//		 *
//		 * @property wizardData
//		 * @type string
//		 * @default ""
//		 */
//		wizardData: "",
//		
//		
//
//		/**
//		 * Fired by YUI when parent element is available for scripting.
//		 * Component initialization, including instantiation of YUI widgets
//		 * and event listener binding.
//		 *
//		 * @method onReady
//		 */
//		onReady: function skds_onReady()
//		{
//			Event.addListener ( Dom.get(this.id + "-cancel-button"), "click", this.onCancelClick, this, true );
//			Event.addListener ( Dom.get(this.id + "-back-button"),   "click", this.onBackClick,   this, true );
//			Event.addListener ( Dom.get(this.id + "-next-button"),   "click", this.onNextClick,   this, true );
//			Event.addListener ( Dom.get(this.id + "-end-button"),    "click", this.onEndClick,    this, true );
//			
//			var json = '{"employees":[		    {"firstName":"John", "lastName":"Doe"},		    {"firstName":"Anna", "lastName":"Smith"},		    {"firstName":"Peter", "lastName":"Jones"}		]}';
//			this.displayTest ( 'test json', 			json );
//			this.displayTest ( 'test json escaped',		this.escapeJSON(json) );
//			var obj = JSON.parse ( json );
//			this.displayTest ( 'test json content',		obj.employees[0].firstName + ' ' + obj.employees[2].firstName );
//
//			var wizardDataJSON = Dom.get(this.id + "-wizardDataJSON").value;
//			this.displayTest ( 'wizard json',			wizardDataJSON ) ;
//			this.displayTest ( 'wizard json escaped',	this.escapeJSON(wizardDataJSON) ) ;
//			this.wizardData = JSON.parse ( wizardDataJSON );
//			this.displayTest ( 'currentForm', 			wizardData.currentForm ) ;
//			
//			this.skds_updateDestName();
//		},
//
//		/**
//		 * Next button listener.
//		 * This can be called only if the form data are consistent
//		 *
//		 * @method onNextClick
//		 * @param e The Next click event
//		 */
//		onNextClick: function skds_onNextClick(e) 
//		{
//			Dom.get(this.id + "-jscWizardData").value = this.updateWizardData();
//			Dom.get(this.id + "-formOperation").value = "process";
//			Alfresco.util.Ajax.jsonPost({ 
//				method : 'POST',
//				url : Alfresco.constants.URL_SERVICECONTEXT + 'sinekartads/' + Dom.get(this.id + "-currentForm").value,
//				dataForm : Dom.get(this.id + "-form"),
//				successCallback : {
//					fn : function(res) {
//						Dom.get(this.id + "-body").innerHTML=res.serverResponse.responseText;
//					}, 
//					scope : this
//				},
//				failureCallback : {
//					fn : function() {
//						Dom.get(this.id + "-body").innerHTML=res.serverResponse.responseText;
//						alert('failure');
//					}, 
//					scope : this
//				},
//			}); 
//		},
//	    
//	    onSignCategoryChange: function skds_onSignCategoryChange() 
//		{
//	    	this.updateDestName();
//	    },
//	    
//	    onTsUrlChange: function skds_onTsUrlChange() 
//	    {
//	    	this.updateDestName();
//	    },
//
//	    updateWizardData: function skds_updateWizardData() 
//	    {
//	    	var wizardDataJSON = Dom.get(this.id + '-wizardDataJSON').value; 
////	    	var json = JSON.parse("{applyMark:'false',backUrl:'http%3A//localhost%3A7080/share/page/document-details%3FnodeRef%3Dworkspace%3A//SpacesStore/d094eb45-c110-44e5-be13-387e387adab6%23'}");
//	    	
//	    	var currentForm = Dom.get(this.id + '-currentForm').value;
//	    	var wizardData = JSON.parse( Dom.get(this.id + '-wizardDataJSON').value );
//	    	if ( currentForm == 'skdsSignOptions' ) {
//	    		var signCategory;
//    			var applyMark = Dom.get(this.id +'-tsUrl').value !== '';
//    			var signature = wizardData.signature;
//	    		var timeStampRequest = signature.timeStampRequest;
//	    		var document = wizardData.documents[0];
//	    		var node;
//	    		
//	    		if ( Dom.get(this.id +'-cms').checked ) {
//	    			signCategory = 'CMS';
//	    		} else if ( Dom.get(this.id +'-pdf').checked ) {
//	    			signCategory = 'PDF'; 
//	    		} else if ( Dom.get(this.id +'-xml').checked ) {
//	    			signCategory = 'XML';
//	    		}
//	    		
//	    		// TimeStamp details - if any - and destination node selection
//	    		if ( applyMark ) {
//	    			timeStampRequest.tsUrl 	    = Dom.get(this.id +'-tsUrl').value;
//	    			timeStampRequest.tsUsername = Dom.get(this.id +'-tsUsername').value;
//	    			timeStampRequest.tsPassword = Dom.get(this.id +'-tsPassword').value;
//	    			node = document.markedSign;
//	    		} else {
//	    			timeStampRequest.tsUrl 	    = '';
//	    			timeStampRequest.tsUsername = '';
//	    			timeStampRequest.tsPassword = '';
//	    			node = document.embeddedSign;
//	    		}
//	    		
//	    		// Signature details
//	    		signature.signCategory 	= signCategory;
//	    		signature.location		= Dom.get(this.id +'-location').value;
//    			signature.reason		= Dom.get(this.id +'-reason').value;
//    			
//    			// Destination node
//    			node.destName			= Dom.get(this.id +'-destName').value;
//    			node.description		= Dom.get(this.id +'-reason').value;
//	    	}
//	    	
//	    	return wizardData;
//	    },
//	    
//	    updateDestName: function skds_updateDestName() {
//	    	
//	    	var tsUrl 	   = Dom.get(this.id +'-tsUrl').value;
//	    	var baseName = Dom.get(this.id +'-tsUsername').value;
//	    	var extension = Dom.get(this.id +'-tsPassword').value;
//	    	
//	    	var baseName = wizardDto.documents[0].baseName;			
//	    	var extension = wizardDto.documents[0].extension;		
//	    	var signCategory = wizardDto.signature.signCategory;	
//	    	var tsUrl = wizardDto.signature.timeStampRequest.tsUrl;	
//	    	var applyMark = tsUrl !== "";							
//	    	var destName = baseName;
//	    	
//	    	if ( signCategory == 'CMS' ) {
//	    		if ( applyMark ) {
//	    			destName += '.' + extension + '.tsa';
//	    		} else {
//	    			destName += '.' + extension + '.p7m';
//	    		}
//	    	} else {
//	    		if ( applyMark ) {
//	    			destName += '_sign.' + extension;
//	    		} else {
//	    			destName += '_mark.' + extension;
//	    		}
//	    	}
//	    	
//	    	wizardDto.documents[0].destName = destName;
//	    },
//	    
//	    displayTest: function skds_displayTestMessage ( message ) {
//	    	this.displayTest ( '', message );
//	    },
//	    
//	    displayTest: function skds_displayTestMessage ( title, message ) {
//
//	    	
//	    	var testBody = '';
//	    	if ( title !== '' ) {
//	    		testBody += '<br/><div>'+'>>> '+title+'</div>';
//	    	}
//	    	testBody += '<div>'+message+'</div><br/>';
//	    	
//	    	document.getElementById(this.id +'-js-test').innerHTML += testBody;
//	    },
//	    
//	    escapeJSON: function skds_escapeJSON ( json ) {
//	    	if ( json === undefined ) 						return '<undefined>';
//			return json.replace(/\\n/g, "\\n")
//					   .replace(/\\'/g, "\\'")
//					   .replace(/\\"/g, '\\"')
//					   .replace(/\\&/g, "\\&")
//					   .replace(/\\r/g, "\\r")
//					   .replace(/\\t/g, "\\t")
//					   .replace(/\\b/g, "\\b")
//					   .replace(/\\f/g, "\\f");
//		}
//	});
//})();