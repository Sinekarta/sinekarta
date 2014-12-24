<#--
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
-->
<#include "skdsActionError.ftl" />
<#include "skdsFieldError.ftl" />

${msg("page.skds-header")}
${msg("page.description")}
<br/>
<br/>
<br/>
${result}




<style type="text/css">
	<!--
	 | FIXME move to ${url.context}/css/skds-webscripts.css when stable
	 -->
	
	
	/**
	 * generic skds page structure:
	 *
	 *		page
	 *			* skds-header
	 *				* skds-title-bar
	 *					* <title>
	 *					* skds-buttons
	 *				* @actionError 
	 *				* skds-header-bar
	 *					* <header>
	 *			* skds-form
	 *					* <currentForm>
	 *			* skds-test
	 *					* <wizardDataView>
	 *					* <jsConsole>
	 * 
	 * The buttons are displayed at the left of the title bar, and their visibility is driven by the template that 
	 * includes skdsWizard.html.ftl.
	 * The actionErrors are provided by the Java WebScript Controller, as well as the currentForm to be shown.
	 * The skds-test section is shown only if the "debug" FreeMarker property is set to true into skdsWizard.html.ftl
	 * The wizardDataView box is a skds-panel (see below) demanded to show the current wizardData status.
	 * The jsConsole box allow the programmer to display any logging message, as well as the Java debugging console. 
	 */ 
	
	.skds-header {
	
	}
	
 	.skds-form {
	    margin: 			2em 0em 0em 5em;
	    width: 				50em;
	}
	
	.skds-title-bar {
	    background-color: 	#e3eaec;
	    border-bottom: 		2px solid #f0f3f4;
	    font-size: 			116%;
	    font-weight: 		bold;
	    height: 			2em;
	    margin: 			1.5em 1.0em 0 0.5em;
	    padding: 			0.3em;
	}
	
	.skds-header-bar {
	    font-weight: 		bold;
	    height: 			2.5em;
	    margin: 			0.5em 0.0em 0.5em 1.5em;
	    
	}
	
	.skds-buttons {
		float:				right;
		z-index: 			10; 
	}
	
	.skds-debug {
		z-index: 			10; 
		position: 			relative; 
		right: 				0.5em; 
		float: 				right; 
		width: 				60em; 
		height:				60em; 
		background-color: 	#ffffff; 
		padding:			0.5em;
	}
	
	.skds-error {
		font-color:			red;
	}
	
	
	
	/**
	 * skds-panel structure:
	 *
	 * 		skds-panel
	 *			* skds-panel-header
	 *			* skds-panel-body
	 *	
	 * The JavaScript behing the skds-panels operates in order to toggle their visibility by collapsing 
	 * or exploding the skds-panel-body when the skds-panel-header is clicked by the user.
	 */ 
	
	.skds-panel {
		background-color:	#f2f2f2;	
		border: 			1px solid #a4a4a4;
		margin-bottom: 		1em;
	}
	
	.skds-panel-header {
	    background-color: 	#e3eaec;
	    height: 			2em;
	    margin: 			0.5em;
	    padding: 			0.5em 2.0em;
	}
	
	.skds-panel-body {
		background-color:	#ffffff;	
		margin: 			0.5em;
		padding: 			0.5em 1.0em;
		display:			block;
	}
	
	
	
	/**
	 * skds-group structure:
	 *
	 *		skds-group
	 *			* skds-group-header
	 *		skds-group
	 *			* skds-group-header
	 *			* skds-goup-body
	 *		skds-group
	 *			* skds-group-header
	 *		skds-group
	 *			* skds-group-header
	 * 
	 * The css skds-group structure is totally equivalent to the skds-panel, the difference beetween them is the 
	 * JavaScript behaviour. The group is intended to be used together with others: at any time, the JavaScript 
	 * controller operates in order to have exactly one group exploded.  
	 */
	
	.skds-group {
	    border-top: 		1px solid #a4a4a4;
	}
	
	.skds-group-header {
	    background-color: 	#f0f3f4;
	    height: 			2em;
	    margin: 			0.5em;
	    padding-left: 		2em;
	}
	
	.skds-group-body {
		background-color:	#ffffff;	
		margin: 			0.5em;
		padding: 			0.5em;	
	}
	
	
	
	/**
	 * skds-datagrid structure:
	 *
	 *		skds-datagrid
	 *			* skds-datarow													
	 *				* skds-data-caption
	 *				* skds-data-input
	 *				* @fieldError
	 *			* skds-datarow													
	 *				* skds-data-caption
	 *				* skds-data-input
	 *				* @fieldError
	 *			* skds-datarow													
	 *				* skds-data-caption
	 *				* skds-data-input
	 *				* @fieldError
	 * 
	 * The fieldError needs to be inserted inside the datarow container, it is already structured 
	 * with the skds-data-caption / skds-data-input pattern.
	 */
	
	.skds-datagrid {
	    margin: 			0.333em;
	    padding: 			0.333em;
	}
	
	.skds-datarow {
		margin-bottom:		0.5em
	}
	
	.skds-data-caption {
	    display: 			block;
	    float: 				left;
	    font-weight: 		bold;
	    padding-right: 		0.8em;
	    padding-top: 		0.2em;
	    text-align: 		right;
	    width: 				15em !important;
	}
	
	.skds-data-input {
	    width: 				20em !important;
	}
	
</style>



<script type="text/javascript">//<![CDATA[
/**
 * Alfresco.skds
 *
 */
	(function()
	{
		/**
		 * YUI Library aliases
		 */
		var Dom = YAHOO.util.Dom,
		    Event = YAHOO.util.Event;
			
		/**
		 * Alfresco Slingshot aliases
		 */
		Alfresco.skds = function(htmlId)
		{    
			this.id = htmlId;
	
		    Alfresco.skds.superclass.constructor.call(this, "Alfresco.skds", htmlId, ["button", "container"]);
	
			return this;
		};
	
		YAHOO.lang.extend ( Alfresco.skds, 
							Alfresco.component.Base, {
			
			// -----
			// --- SignWizard JavaScript controller
			// - 
			
			scAliases: '',
			scUserAlias: '',
			
			/**
			 * Fired by YUI when parent element is available for scripting.
			 * Component initialization, including instantiation of YUI widgets
			 * and event listener binding.
			 *
			 * @method onReady
			 */
			onReady: function skds_onReady ( )
			{
			  	document.getElementById("${htmlid}-applet-container").innerHTML += [
				  		'<object type="application/x-java-applet" width="1" height="1">',
				    	'	<param name="code" value="AppletTest"></param>',
				    	'	<param name="archive" value="/applet.jar?v=' + new Date().getTime() + '"></param>',
				    	'	<param name="id" value="1"></param>',
				  		'</object>'
			  		].join('\n');
			
				Event.addListener ( Dom.get('${htmlid}-scDriver'),					'change', this.onScDriverChange, 		this, true );
				Event.addListener ( Dom.get('${htmlid}-scPin'),						'change', this.onScPinChange, 			this, true );
				Event.addListener ( Dom.get("${htmlid}-scUserAlias"),				'change', this.onScUserAliasChange, 	this, true );
				Event.addListener ( Dom.get("${htmlid}-sign-button"),				'click',  this.onSignClick, 			this, true );
			},
				
				
			// -----
			// --- Applet Response parsing
			// -
			
			parseAppletResponse: function skds_parseAppletResponse ( appletResponseJSON ) 
			{
				var prettyJSON = this.prettifyJSON ( appletResponseJSON );
		    	var html = '<pre>' + prettyJSON + '</prev>';
		    	document.getElementById('${htmlid}-appletResponseView-body').innerHTML = html;
		    	
				var resultJSON = undefined;
				if ( appletResponseJSON !== undefined ) {
					var appletResponse = this.parseJSON ( appletResponseJSON );
					if ( appletResponse !== undefined ) {
						if ( appletResponse.resultCode === 'SUCCESS' ) {
							resultJSON = appletResponse.result;
						} else {
							this.displayErrors ( appletResponse.actionErrors, appletResponse.fieldErrors );
						}
					} else {
						alert('unable to parse the response object - ' + appletResponseJSON);
					}
				} else {
					alert('appletResponseJSON is undefined');
				}
				return resultJSON;
			},
			
			displayErrors: function skds_displayErrors ( actionErrors, fieldErrors ) 
			{
				var html;
				html = '';
				for ( i=0; i<actionErrors.length; i++ ) {
					html += '<li>' + actionErrors[i].errorMessage + '</li>'; 
				}
				document.getElementById('${htmlid}-actionErrors').innerHTML = html;
				
				document.getElementById('${htmlid}-scDriver-error').innerHTML = '';
				document.getElementById('${htmlid}-scPin-error').innerHTML = '';
				document.getElementById('${htmlid}-scUserAlias-error').innerHTML = '';
				var field;
				var errors;
				for ( i=0; i<fieldErrors.length; i++ ) {
					field = fieldErrors[i].field;
					errors = fieldErrors[i].errors;
					html = '';
					for ( j=0; j<errors.length; j++ ) {
						html += '<span class="skds-data-caption"></span>';
						html += '<span class="skds-data-input, skds-error">' + errors[j] + '</span>';
					}
					document.getElementById('${htmlid}-'+field+'-error').innerHTML = html;
				}
			},
			
			
			
			// -----
			// --- Generic JSON utilities
			// -
			
			parseJSON: function skds_parseJSON ( json ) {
				if ( json === undefined ) {
		    		return undefined;
		    	}										
				return JSON.parse(json);
			},
			
			formatJSON: function skds_formatJSON ( obj ) {
		    	if ( obj === undefined ) {
		    		return '<undefined>';
		    	}
				return JSON.stringify(obj);
			},
			
			prettifyJSON: function skds_prettifyJSON ( json ) {
				if ( json === undefined ) {
		    		return '<undefined>';
		    	}
				return JSON.stringify(JSON.parse(json),null,2);
			},
			
			onScDriverChange: function skds_onScDriverChange ( e ) {
		    	document.getElementById('${htmlid}-scDriver-error').innerHTML = '';
		    	
		    	var driver = e.target.value;
		    	document.getElementById('${htmlid}-hexCertificateChain').value = '';
				document.getElementById('${htmlid}-digitalSignature').value = '';
				this.scAliases = {};
				this.scUserAlias = '';
				fakeDriverDisplay = 'none';
		    	if ( driver !== '' ) {
		    		if ( driver === 'fake' ) {
						fakeDriverDisplay = 'block';
					}
			    	var appletResponseJSON = document.sinekartaApplet.selectDriver ( e.target.value );
					var appletResultJSON = this.parseAppletResponse ( appletResponseJSON );
				}
				document.getElementById('${htmlid}-scDriver-fake').style.display = fakeDriverDisplay;
		    },
		    
		 	onScUserAliasChange: function skds_onScUserAliasChange() {
		    	document.getElementById('${htmlid}-scUserAlias-error').innerHTML = '';
		    	var scUserAliasSelect = document.getElementById("${htmlid}-scUserAlias");
				this.scUserAlias = scUserAliasSelect.options[scUserAliasSelect.selectedIndex].value;
				if ( this.scUserAlias !== '' ) {
					var appletResponseJSON = document.sinekartaApplet.selectCertificate(this.scUserAlias);
					var appletResultJSON = this.parseAppletResponse ( appletResponseJSON );
					if ( appletResultJSON !== undefined ) {
						document.getElementById('${htmlid}-hexCertificateChain').value = this.parseAppletResponse ( appletResponseJSON );
					}
				} else {
					document.getElementById('${htmlid}-hexCertificateChain').value = '';
				}
			},
		
			onScPinChange: function skds_onScPinChange ( ) {
				document.getElementById('${htmlid}-scPin-error').innerHTML = '';
				var scPin = document.getElementById("${htmlid}-scPin").value;
				var aliases;
				var html;
				
				var appletResponseJSON = document.sinekartaApplet.login ( scPin );
				var appletResultJSON = this.parseAppletResponse ( appletResponseJSON );
				if ( appletResultJSON !== undefined ) {
					this.scAliases = JSON.parse ( appletResultJSON );
					var singleAlias = this.scAliases.length == 1;
					this.scUserAlias = this.scAliases[0]; 
					this.updateScUserAliasSelect ( );
					if ( singleAlias ) {
						this.onScUserAliasChange ( );
					}
				}
			},
			
		    updateScUserAliasSelect: function skds_updateScUserAliasSelect ( ) {
		    	this.updateSelect ( 'scUserAlias', this.scAliases, this.scUserAlias );
		    },
			
			updateSelect: function skds_updateSelect ( name, options, selection ) {
				var select = document.getElementById("${htmlid}-"+name);
				var selected;
				var found = false;
				select.options = new Array();
				select.options[0] = new Option('- select one -', '', false, false);
				for ( i=0; i<options.length; i++) {
					value = options[i];
					select.options[i+1] = new Option(value, value, false, false);
					if ( value === selection ) {
						select.options[i+1].selected = found = true;
					}
				}
				if ( !found ) {
					select.options[0].selected = true;
				} 
			},
			
			onSignClick: function skds_onSignClick() {
				var hexFingerPrint = document.getElementById('${htmlid}-fingerPrint').value;
				var appletResponseJSON = document.sinekartaApplet.signDigest ( hexFingerPrint );
				var appletResultJSON = this.parseAppletResponse ( appletResponseJSON );
				if ( appletResultJSON !== undefined ) {
					document.getElementById('${htmlid}-digitalSignature').value = appletResultJSON;  
				} 
			},
		});
	})();
	
	new Alfresco.skds("${htmlid}");
	
</script>
	
<div class="skds-debug">
	<div id="$${htmlid}-appletResponseView" class="skds-panel">
		<div id="${htmlid}-appletResponseView-header" class="skds-panel-header">
			<label>Applet Response View</label>
		</div>
		<div id="${htmlid}-appletResponseView-body" class="skds-panel-body">
		</div>
	</div>
</div>

<@actionError />
<div class="skds-form">
	<div class="skds-group">
	    <div id="${htmlid}-clientType-SMARTCARD-header" class="skds-group-header">
			<label>SmartCard applet HelloWorld test</label>
	    </div>
	    <div id="${htmlid}-clientType-SMARTCARD-body" class="skds-group-body">
	    	<div id="${htmlid}-clientType-SMARTCARD-enabled">
	    		<label>verify the SmartCard applet with a 64-byte long hex fingerPrint</label>
	    	
				<!-- fingerPrint -->
				<div class="skds-datarow">
					<span class="skds-data-caption">
						<label for="${htmlid}-fingerPrint">fingerPrint</label>
					</span>
					<span class="skds-data-input">
						<input id="${htmlid}-fingerPrint" type="text" value="2f265c664c0aa544a5c07b95b2e2e7756b7fddc9f4cdbce23befe35f755fcbf0" />
					</span>
					<@fieldError field='fingerPrint' />
				</div>
	    	
				<!-- wscWizardData.scDriver -->
				<div class="skds-datarow">
					<span class="skds-data-caption">
						<label for="${htmlid}-scDriver">driver</label>
					</span>
					<span class="skds-data-input">
						<select id="${htmlid}-scDriver">
							<option value="" selected> - select one - </option>
							<option value="fake"> SmartCard simulation </option>
							<option value="bit4ipki.dll"> libbit4ipki.so - win </option>
							<option value="libbit4ipki.so"> libbit4ipki.so - ux </option>
						</select>
					</span>
					<@fieldError field='scDriver' />
					<p id="${htmlid}-scDriver-fake" style="margin:0px; padding:0.5em,0em; display:none">
						SmartCard simulation pin: "123"<br/>
						<b>Warning: </b>the digital signature that will be generated are intended only for testing
						proposes and have <b>no legal value</b>. <br/>
						Use this driver only to try see how the digital signature application would work if
						you had a SmartCard reader attached to your local computer.
					</p>
				</div>
	    	
		    	<!-- wscWizardData.scPin -->
				<div class="skds-datarow">
					<span class="skds-data-caption">
						<label for="${htmlid}-scPin">pin</label>
					</span>
					<span class="skds-data-input">
						<input id="${htmlid}-scPin" type="password" />
					</span>
					<@fieldError field='scPin' />
				</div>
				
				<!-- wscWizardData.ksUserAlias -->
				<div class="skds-datarow">
					<span class="skds-data-caption">
						<label for="${htmlid}-scUserAlias">alias</label>
					</span>
					<span class="skds-data-input">
						<select id="${htmlid}-scUserAlias">
						</select>
					</span>
					<@fieldError field='scUserAlias' />
				</div>
				
				<!-- hexCertificateChain -->
				<div class="skds-datarow">
					<span class="skds-data-caption">
						<label for="${htmlid}-hexCertificateChain">hexCertificateChain</label>
					</span>
					<span class="skds-data-input">
						<input id="${htmlid}-hexCertificateChain" type="text" readonly />
					</span>
					<@fieldError field='hexCertificateChain' />
				</div>
				
				<!-- digitalSignature -->
				<div class="skds-datarow">
					<span class="skds-data-caption">
						<label for="${htmlid}-digitalSignature">digitalSignature</label>
					</span>
					<span class="skds-data-input">
						<input id="${htmlid}-digitalSignature" type="text" readonly />
						<button id="${htmlid}-sign-button" type="button" name="next"> 
							evaluate
						</button>
					</span>
					<@fieldError field='digitalSignature' />
				</div>
			</div>
			<div id="${htmlid}-clientType-SMARTCARD-disabled" style="display: none">
				<label>${msg("label.missing.SMARTCARD")}</label>
			</div>
	    </div>
	</div>
</div>

<div id="${htmlid}-applet-container">
</div>