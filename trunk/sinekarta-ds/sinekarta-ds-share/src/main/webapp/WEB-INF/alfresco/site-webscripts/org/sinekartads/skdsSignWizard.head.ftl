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
		display:			none;
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
<!--link   type="text/css" 	  href="${url.context}/css/skds-webscripts.css" rel="stylesheet"-->

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
			
			wizardData:  	{},
			actionErrors:	{},
			fieldErrors:	{},
			wizardForms:	new Array(),
			currentStep:	{},
			backupDataJSON: '',
			signApplet: 	document.sinekartaApplet,
			
			activeGroups: 	{
				tsSelection: '',
				clientType:  '',
			},
			
			/**
			 * Fired by YUI when parent element is available for scripting.
			 * Component initialization, including instantiation of YUI widgets
			 * and event listener binding.
			 *
			 * @method onReady
			 */
			onReady: function skds_onReady ( )
			{
				// Refresh the page and force the first form to be displayed
				this.refresh ( <#escape x as jsonUtils.encodeJSONString(x)> "${wizardDataJSON}" </#escape> );
				
				// DebugBox buttons listeners
				Event.addListener ( Dom.get('${htmlid}-clearJsConsole-button'), 'click', this.onClearJsConsoleClick, this, true );
				
				// Navigation buttons listeners
				Event.addListener ( Dom.get('${htmlid}-cancel-button'), 'click', this.onCancelClick, this, true );
				Event.addListener ( Dom.get('${htmlid}-back-button'),	'click', this.onBackClick,   this, true );
				Event.addListener ( Dom.get('${htmlid}-undo-button'),	'click', this.onUndoClick,	 this, true );
				Event.addListener ( Dom.get('${htmlid}-next-button'),   'click', this.onNextClick,   this, true );
				Event.addListener ( Dom.get('${htmlid}-end-button'),    'click', this.onEndClick,    this, true );
				
				// Debug panels listeners
				Event.addListener ( Dom.get('${htmlid}-appletResponseView-header'), 'click', this.onAppletResponseViewClick,this, true );
				Event.addListener ( Dom.get('${htmlid}-wizardDataView-header'),    	'click', this.onWizardDataViewClick,    this, true );
				Event.addListener ( Dom.get('${htmlid}-jsConsole-header'),    		'click', this.onJsConsoleClick,    		this, true );
				Event.addListener ( Dom.get('${htmlid}-freemarkerTest-header'),    	'click', this.onFreemarkerTestClick,    this, true );
				
				// skdsSignOptions.form panels and groups listeners
				Event.addListener ( Dom.get('${htmlid}-signCategory-header'),    	'click', this.onSignCategoryClick,    	this, true );
				Event.addListener ( Dom.get('${htmlid}-tsSelection-NONE-header'),   'click', this.onTsSelectionNoneClick,   this, true );
				Event.addListener ( Dom.get('${htmlid}-tsSelection-DEFAULT-header'),'click', this.onTsSelectionDefaultClick,this, true );
				Event.addListener ( Dom.get('${htmlid}-tsSelection-CUSTOM-header'), 'click', this.onTsSelectionCustomClick, this, true );
				Event.addListener ( Dom.get('${htmlid}-destOptions-header'), 		'click', this.onTsDestOptionsClick, 	this, true );
				
				// skdsSignOptions.form input components listeners
				Event.addListener ( Dom.get('${htmlid}-signCategory-cms'),  		'change', this.onSignCategoryCmsChange,	this, true );
				Event.addListener ( Dom.get('${htmlid}-signCategory-pdf'),  		'change', this.onSignCategoryPdfChange, this, true );
				Event.addListener ( Dom.get('${htmlid}-signCategory-xml'),  		'change', this.onSignCategoryXmlChange, this, true );
				Event.addListener ( Dom.get('${htmlid}-tsUrl'), 			 		'change', this.onTsUrlChange, 			this, true );
				Event.addListener ( Dom.get('${htmlid}-tsUsername'), 			 	'change', this.onTsUsernameChange, 		this, true );
				Event.addListener ( Dom.get('${htmlid}-tsPassword'), 			 	'change', this.onTsPasswordChange, 		this, true );
				Event.addListener ( Dom.get('${htmlid}-destName'), 			 		'change', this.onDestNameChange, 		this, true );
				Event.addListener ( Dom.get('${htmlid}-reason'), 			 		'change', this.onReasonChange, 			this, true );
				Event.addListener ( Dom.get('${htmlid}-location'), 			 		'change', this.onLocationChange, 		this, true );
				
				// skdsSignClient.form panels and groups listeners								  
				Event.addListener ( Dom.get('${htmlid}-clientType-KEYSTORE-header'),	'click', this.onClientTypeKeyStoreClick,	this, true );
				Event.addListener ( Dom.get('${htmlid}-clientType-SMARTCARD-header'),	'click', this.onClientTypeSmartCardClick,	this, true );
				Event.addListener ( Dom.get('${htmlid}-clientType-SIGN_WS-header'),		'click', this.onClientTypeSignWsClick,		this, true );
				
				// skdsSignClient.form input components listeners
				Event.addListener ( Dom.get('${htmlid}-ksPin'),						'change', this.onKsPinChange, 			this, true );
				Event.addListener ( Dom.get('${htmlid}-ksUserAlias'),				'change', this.onKsUserAliasChange, 	this, true );
				Event.addListener ( Dom.get('${htmlid}-ksUserPassword'),			'change', this.onKsUserPasswordChange, 	this, true );
				
				Event.addListener ( Dom.get('${htmlid}-scDriver'),					'change', this.onScDriverChange, 		this, true );
				Event.addListener ( Dom.get('${htmlid}-scPin'),						'change', this.onScPinChange, 			this, true );
				Event.addListener ( Dom.get("${htmlid}-scUserAlias"),				'change', this.onScUserAliasChange, 	this, true );
				
			},
			
			
		    
			// -----
			// --- Generic JSON utilities
			// -
			
			parseJSON: function skds_parseJSON ( json ) {
				if ( json === undefined ) {
		    		this.warn ( 'parseJSON', 'WARNING: target json is undefinited' );
		    		return undefined;
		    	}										
				return JSON.parse(json);
			},
			
			formatJSON: function skds_formatJSON ( obj ) {
		    	if ( obj === undefined ) {
		    		this.warn ( 'formatJSON', 'WARNING: target object is undefinited' );
		    		return '<undefined>';
		    	}
				return JSON.stringify(obj);
			},
			
			prettifyJSON: function skds_prettifyJSON ( json ) {
				if ( json === undefined ) {
					this.warn ( 'prettifyJSON', 'WARNING: target json is undefinited' );
		    		return '<undefined>';
		    	}
				return JSON.stringify(JSON.parse(json),null,2);
			},
			
			
			
			// -----
			// --- Form Operations
			// - 
			
			callFormOperation: function skds_callFormOperation ( stepName ) {
				this.callFormOperation ( stepName, undefined );
			},
			
			callFormOperation: function skds_callFormOperation ( stepName, nextForms ) {
				
				var targetStep = this.wizardData.wizardSteps [ stepName ];
				this.info ( 'callFormOperation', 'stepName: ' + stepName );
				this.info ( 'callFormOperation', 
						'targetStep: ' + this.formatJSON(targetStep) );
				document.getElementById("${htmlid}-wizardDataJSON").value = this.formatJSON ( this.wizardData );
				
				Alfresco.util.Ajax.jsonPost({ 
					method : 'POST',
					url : Alfresco.constants.URL_SERVICECONTEXT + 'sinekartads/' + stepName,
					dataForm : Dom.get("${htmlid}-form"),
					successCallback : {
						fn : function(res) {
							this.info ( 'callFormOperation', 'targetStep: ' + this.formatJSON(targetStep) );
							this.refresh ( res.serverResponse.responseText );
							// Recursivelly call on the next forms
							if ( this.wizardData.resultCode === 'SUCCESS' ) {
								if ( nextForms !== undefined && nextForms.length > 0 ) {
									var target = nextForms[0];
									var forms = new Array();
									for ( i=1; i<nextForms.length; i++) {
										forms[i-1] = nextForms[i];
									}
									this.info('callFormOperation', 
											'stepName:   '+stepName + '\n' +
											'nextForms:  '+nextForms + '\n' +
											'target:     '+target + '\n' +
											'forms:      '+forms + '\n');
									if ( typeof target === 'string' ) {
										this.callFormOperation ( target, forms );
									} else {
										target.call ( this );
									}
								}
							}
						}, 
						scope : this
					},
					failureCallback : {
						fn : function() {
							alert('failure');
							this.error ( 'failure', this.formatJSON(res) );
							
						},
						scope : this
					},
				});
			},
			
			
			
			// -----
			// --- Applet Response parsing
			// -
			/*
			parseAppletResponse: function skds_parseAppletResponse ( appletResponseJSON ) {
				var appletResponse = this.parseJSON ( appletResponseJSON );
				var resultJSON = undefined;
				if ( appletResponse.resultCode === 'SUCCESS' ) {
					resultJSON = appletResponse.result;
				} else {
					this.error('skds_parseAppletResponse', this.formatJSON(appletResponse) );
					this.displayErrors ( appletResponse.actionErrors, appletResponse.fieldErrors );
				}
				return resultJSON;
			},*/
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
			
			
			
			// -----
			// --- Panels and groups visibility controller
			// -
			
			togglePanelDisplay: function skds_togglePanelDisplay ( panelName ) {
				var panelBodyId = '${htmlid}-'+panelName+'-body'; 
		    	var panelBody = document.getElementById(panelBodyId);
				var display;
		    	if(panelBody.style.display == 'none') {
		    		display = 'block';
		    	} else {
		    		display = 'none';
		    	}
				this.setPanelDisplay ( panelName, display );
			},
		    
		    setPanelDisplay: function skds_setPanelDisplay ( panelName, display ) 
		    {
		    	var panelBodyId = '${htmlid}-'+panelName+'-body'; 
		    	var panelBody = document.getElementById(panelBodyId);
		    	panelBody.style.display = display;
		    },
		    
		    switchActiveGroup: function skds_switchActiveGroup ( macrogroup, group ) 
		    {
				var prevGroup = this.activeGroups [ macrogroup ];
				if ( group !== prevGroup ) {
					this.activeGroups [ macrogroup ] = group;
					if ( prevGroup !== '' ) {
 						this.setPanelDisplay ( macrogroup + '-' + prevGroup, 'none' );
					}
					this.setPanelDisplay ( macrogroup + '-' + group, 'block' );
				}
			},
			
			updateSelect: function skds_updateSelect ( name, options, selection ) {
				var select = document.getElementById("${htmlid}-"+name);
				var selected;
				var found = false;
				select.options = new Array();
				select.options[0] = new Option("${msg('select.dummy')}", '', false, false);
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
			
			
			
			// -----
			// --- DebugBox - Panels and groups visibility
			// -
			
			onWizardDataViewClick: function skds_onWizardDataViewClick ( ) 
			{
		    	this.togglePanelDisplay ( 'wizardDataView' );
		    },
		    
			onAppletResponseViewClick: function skds_onAppletResponseClick ( ) 
			{
		    	this.togglePanelDisplay ( 'appletResponseView' );
		    },
		    
		    onJsConsoleClick: function skds_onJsConsoleClick ( ) 
			{
		    	this.togglePanelDisplay ( 'jsConsole' );
		    },
		    
		    onFreemarkerTestClick: function skds_onFreemarkerTestClick ( ) 
			{
		    	this.togglePanelDisplay ( 'freemarkerTest' );
		    },
		    
		    
		    
		    // -----
		    // --- DebugBox - Contents visualization
		    // -
					    
		    updateWizardDataView: function skds_updateWizardDataView ( ) {
		    	var prettyJSON = this.prettifyJSON ( this.formatJSON(this.wizardData) );
		    	var html = '<pre>' + prettyJSON + '</prev>';
		    	document.getElementById('${htmlid}-wizardDataView-body').innerHTML = html;
		    },
		    
		    onClearJsConsoleClick: function skds_onClearJsConsoleClick(e) 
			{
				this.clearJsConsole ( );
			},
		    
		    clearJsConsole: function skds_clearJsConsole ( ) {
		    	document.getElementById('${htmlid}-jsConsole-console').innerHTML = '>>>console cleared<br/><br/>';
		    },
		    
		    info: function skds_info ( method, message ) {
		    	this.displayTestMessage ( 'INFO - ' + method , message );
		    },
		    
		    warn: function skds_info ( method, message ) {
		    	this.displayTestMessage ( 'WARN - ' + method, message );
		    },
		    
		    error: function skds_info ( method, message ) {
		    	this.displayTestMessage ( 'ERROR - ' + method, message );
		    },
		    
		    displayTestMessage: function skds_displayTestMessage ( message ) {
		    	this.displayTestMessage ( '', message );
		    },
		    
		    displayTestMessage: function skds_displayTestMessage ( title, message ) {
		    	var testBody = '';
		    	if ( title !== '' ) {
		    		testBody += '<p>'+'>>> '+title+'</p>';
		    	}
		    	testBody += '<p><pre>'+message+'</prev></p><br/>';
		    	this.displayTestHTML(testBody);
		    },
		    
		    displayTestHTML: function skds_displayTestHTML ( html ) {
		    	
		    	document.getElementById('${htmlid}-jsConsole-console').innerHTML += html;
		    },

			
			
			// -----
			// --- Navigation buttons
			// -
			
			onCancelClick: function skds_onCancelClick(e) 
			{
				this.info ( 'onCancelClick',
						'backUrl: ' + this.wizardData.backUrl );
				location.href = this.wizardData.backUrl;
			},
			
			onBackClick: function skds_onBackClick(e) 
			{
				// Determinate the previous form into the wizard
				var prevForm = this.wizardData.wizardForms[0];
				for ( i=0; i<this.wizardForms.length; i++ ) {
					this.info ( 'onBackClick',
							'prevForm: ' + prevForm + '\n' +
							'i:        ' + this.wizardForms[i] + '\n' +
							'i+1:      ' + this.wizardForms[i+1] + '\n' + 
							'current:  ' + this.wizardData.currentStep.form + '\n' +
							'match:    ' + (this.wizardForms[i+1] === this.wizardData.currentStep.form));
					if ( this.wizardStep[i+1] === this.wizardData.currentStep.form ) {
						prevForm = this.wizardForms [ i ];
					}
				}
				
				// Ask the controller to display the previous form but keeping the current wizard data status
		    	this.refresh ( this.formatJSON ( this.wizardData ), prevForm );
			},
			
			onUndoClick: function skds_onUndoClick(e) 
			{
				// Ask the controller to undo the form changes to the last saved wizard data status 
				this.refresh ( this.backupDataJSON,  undefined );
			},
			
			onNextClick: function skds_onNextClick(e) 
			{
				var callerForm = this.wizardData.currentStep.form;
				if ( callerForm === 'skdsSignClient' ) {
					var nextForms;
					if ( this.wizardData.clientType === 'KEYSTORE' ) {
						this.callFormOperation ( 'skdsSignClient', 
								['skdsSignCallPreSign', 'skdsSignCallPostSign', 'skdsSignResults'] );	
					} else if ( this.wizardData.clientType === 'SMARTCARD' ) {
						this.callFormOperation ( 'skdsSignClient', 
								['skdsSignCallPreSign', this.applySignatureSmartCard] );
					}
				} else {
					// callFormOperation() will refresh the whole form and change the value of 
					//   - the same form if any error occurres
					//   - the next form if succeed
					// if the form changed it needs to be prepared
					this.callFormOperation ( callerForm );
				}
			},
			
			onEndClick: function skds_onEndClick(e) 
			{
				location.href = this.wizardData.backUrl;
			},
			
			refresh: function skds_refresh ( wizardDataJSON, targetStep ) 
			{
				this.info ( 'refresh', 'targetStep: ' + this.formatJSON(targetStep) );
				
				// update the wizard data view box 
				this.updateWizardDataView ( );
				
				// jsConsole refresh
				//this.clearJsConsole ( );
				
				// update the header and title bar
				<#-- document.getElementById('${htmlid}-header').value = "${msg('header.'+this.wizardData.currentStep.form)}";
				document.getElementById('${htmlid}-description').value = "${msg('description.'+this.wizardData.currentStep.form)}"; -->
				
				// wizardData parsing and backup
				this.backupDataJSON = wizardDataJSON;
		    	this.wizardData = this.parseJSON ( this.backupDataJSON );

				// Update the displayed form if changed
		    	if ( targetStep !== undefined ) {
		    		// Update the currentStep with the targetStep if provided
					this.wizardData.currentStep = targetStep;
				} else if ( this.wizardData.currentStep === undefined ) {
					this.wizardData.currentStep = this.currentStep;
				}
				this.currentStep = this.wizardData.currentStep
				this.wizardForms = this.wizardData.wizardForms;
				
				var display;
				var form;
				var currentIndex;
				for ( i=0; i<this.wizardForms.length; i++ ) {
					form = this.wizardForms[i];
					if ( form === this.wizardData.currentStep.form ) {
						currentIndex = i;
						display = 'block';
					} else {
						display = 'none';
					}
					document.getElementById('${htmlid}-'+form+'-form').style.display = display;
				}
				
				// Navigation buttons abilitation state
				var display; 
				
				display = 'block';
				if ( currentIndex == this.wizardForms.length ) {
					display = 'none';
				}
				document.getElementById('${htmlid}-cancel').style.display = display;
				
				display = 'block';
				if ( currentIndex == this.wizardForms.length-1 || currentIndex == 0 ) {
					display = 'none';
				}
				document.getElementById('${htmlid}-back').style.display = display;
				
				display = 'block';
				if ( currentIndex == this.wizardForms.length-1 ) {
					display = 'none';
				}
				document.getElementById('${htmlid}-undo').style.display = display;
				
				display = 'block';
				if ( currentIndex == this.wizardForms.length-1 ) {
					display = 'none';
				}
				document.getElementById('${htmlid}-next').style.display = display;
				
				display = 'block';
				if ( currentIndex < this.wizardForms.length-1 ) {
					display = 'none';
				}
				document.getElementById('${htmlid}-end').style.display = display;
				
				// Update the actionErrors and the fieldErrors
				this.displayErrors ( this.wizardData.actionErrors, this.wizardData.fieldErrors );				

		    	// Update the skdsSignOptions form 
		    	document.getElementById("${htmlid}-tsUsername").value 	= this.wizardData.signature.timeStampRequest.tsUsername;
		    	document.getElementById("${htmlid}-tsPassword").value 	= this.wizardData.signature.timeStampRequest.tsPassword;
		    	document.getElementById("${htmlid}-tsUrl").value 		= this.wizardData.signature.timeStampRequest.tsUrl;
		    	document.getElementById("${htmlid}-destName").value 	= this.wizardData.documents[0].destName;
		    	document.getElementById("${htmlid}-location").value 	= this.wizardData.signature.location;
		    	document.getElementById("${htmlid}-reason").value 		= this.wizardData.signature.reason;
		    	// updateTsSelection() will update destName and wizardDataView as side-effect
				this.updateTsSelection ( this.wizardData.tsSelection );
				
				// updateClientType() will update wizardDataView as side-effect
				this.updateClientType  ( this.wizardData.clientType );
				
				// Update the skdsSignClient keyStore and smartCard aliases select components
				this.updateKsUserAliasSelect ( );
				this.updateScUserAliasSelect ( );
				this.updateScDriverSelect ( );
			},

			displayErrors: function skds_displayErrors ( actionErrors, fieldErrors ) {

				document.getElementById('${htmlid}-tsUrl-error').innerHTML = '';
				document.getElementById('${htmlid}-tsUsername-error').innerHTML = '';
				document.getElementById('${htmlid}-tsPassword-error').innerHTML = '';
				document.getElementById('${htmlid}-destName-error').innerHTML = '';
				document.getElementById('${htmlid}-reason-error').innerHTML = '';
				document.getElementById('${htmlid}-location-error').innerHTML = '';
				document.getElementById('${htmlid}-ksPin-error').innerHTML = '';
				document.getElementById('${htmlid}-ksUserAlias-error').innerHTML = '';
				document.getElementById('${htmlid}-ksUserPassword-error').innerHTML = '';
				document.getElementById('${htmlid}-scDriver-error').innerHTML = '';
				document.getElementById('${htmlid}-scPin-error').innerHTML = '';
				document.getElementById('${htmlid}-scUserAlias-error').innerHTML = '';
				
				var html;
				html = '';
				for ( i=0; i<actionErrors.length; i++ ) {
					html += '<li>' + actionErrors[i].errorMessage + '</li>'; 
				}
				document.getElementById('${htmlid}-actionErrors').innerHTML = html;
				
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
			
			
			applySignatureSmartCard: function skds_applySignatureSmartCard ( ) {
				this.info ( 'applySignatureSmartCard', 'start' );
				var last = this.wizardData.documents[0].signatures.length-1;
				var signature = this.wizardData.documents[0].signatures[last];
				var hexFingerPrint = signature.digest.hexFingerPrint;
				this.info ( 'applySignatureSmartCard', 'fingerPrint: ' + signature.digest.hexFingerPrint );
				var appletResponseJSON = document.sinekartaApplet.signDigest ( hexFingerPrint );
				var appletResultJSON = this.parseAppletResponse ( appletResponseJSON );
				if ( appletResultJSON !== undefined ) {
					signature.hexDigitalSignature = appletResultJSON;  
					this.updateWizardDataView ( );
					this.info ( 'applySignatureSmartCard', 'digsig:    ' + signature.hexDigitalSignature );
					this.callFormOperation ( 'skdsSignSetDigitalSignature', 
							['skdsSignCallPostSign', 'skdsSignResults'] );
				} else {
					this.error ( 'refresh', 'unexpected response: ' + appletResultJSON );
				}
			},
			
			
			
			// ------
			// --- SignOptions - Panels and groups visibility
			// -
			
		    onSignCategoryClick: function skds_onSignCategoryClick ( ) 
			{
		    	this.togglePanelDisplay ( 'signCategory' );
		    },
		    
		    onTsSelectionNoneClick: function skds_onTsSelectionNoneClick ( ) 
		    {
		    	this.updateTsSelection ( 'NONE' );
		    },
		    
		    onTsSelectionDefaultClick: function skds_onTsSelectionDefaultClick ( ) 
		    {
		    	this.updateTsSelection ( 'DEFAULT' );
		    },
		    
		    onTsSelectionCustomClick: function skds_onTsSelectionCustomClick ( ) 
		    {
		    	this.updateTsSelection ( 'CUSTOM' );
		    },
		    
		    updateTsSelection: function skds_updateTsSelection ( tsSelection ) 
		    {
		    	this.switchActiveGroup ( 'tsSelection', tsSelection );
		    	this.wizardData.tsSelection = tsSelection;
		    	// updateDestName() will update wizardDataView as side-effect
		    	this.updateDestName();
		    },
		    
		    onTsDestOptionsClick: function onTsDestOptionsClick ( ) 
		    {
		    	this.togglePanelDisplay ( 'destOptions' );
		    },
		    
		    
		    
		    // -----
		    // --- SignOptions - Input widget change events
		    // -
		    
   		    onSignCategoryCmsChange: function skds_onSignCategoryCmsChange ( e ) 
		    {
   		    	if ( e.target.checked ) {
		    		this.updateSignCategory ( 'CMS' );
   		    	}
		    },
		    
   		    onSignCategoryPdfChange: function skds_onSignCategoryPdfChange ( e ) 
		    {
   		    	if ( e.target.checked ) {
		    		this.updateSignCategory ( 'PDF' );
   		    	}
		    },
		    
   		    onSignCategoryXmlChange: function skds_onSignCategoryXmlChange ( e ) 
		    {
   		    	if ( e.target.checked ) {
		    		this.updateSignCategory ( 'XML' );
   		    	}
		    },
		    
		    updateSignCategory: function skds_onSignCategoryChange ( signCategory ) 
			{
		    	this.wizardData.signature.signCategory = signCategory;
		    	// updateDestName() will update the destination name and the wizardDataView panel
		    	this.updateDestName();
		    },
		    
		    updateDestName: function skds_updateDestName() {
		    	var baseName 		= this.wizardData.documents[0].baseName;			
		    	var extension 		= this.wizardData.documents[0].extension;		
		    	var signCategory 	= this.wizardData.signature.signCategory;	
		    	var applyMark 		= this.wizardData.tsSelection !== "NONE";							
		    	var destName 		= baseName;
		    	
		    	if ( signCategory == 'CMS' ) {
		    		if ( applyMark ) {
		    			destName += '.' + extension + '.m7m';
		    		} else {
		    			destName += '.' + extension + '.p7m';
		    		}
		    	} else {
		    		if ( applyMark ) {
		    			destName += '_sign.' + extension;
		    		} else {
		    			destName += '_mark.' + extension;
		    		}
		    	}
		    	
		    	this.wizardData.documents[0].destName = destName;
		    	document.getElementById('${htmlid}-destName').value = destName;
		    	this.updateWizardDataView ( );
		    },
		    
		    onTsUrlChange: function skds_onTsUrlChange ( e ) 
		    {
		    	document.getElementById('${htmlid}-tsUrl-error').innerHTML = '';
		    	this.wizardData.signature.timeStampRequest.tsUrl = e.target.value;
		    	this.updateWizardDataView ( );
		    },
		    
		    onTsUsernameChange: function skds_onTsUsernameChange ( e ) 
		    {
		    	document.getElementById('${htmlid}-tsUsername-error').innerHTML = '';
		    	this.wizardData.signature.timeStampRequest.tsUsername = e.target.value;
		    	this.updateWizardDataView ( );
		    },
		    
		    onTsPasswordChange: function skds_onTsPasswordChange ( e ) 
		    {
		    	document.getElementById('${htmlid}-tsPassword-error').innerHTML = '';
		    	this.wizardData.signature.timeStampRequest.tsPassword = e.target.value;
		    	this.updateWizardDataView ( );
		    },
		    
		    onDestNameChange: function skds_onTsDestNameChange ( e ) 
		    {
		    	document.getElementById('${htmlid}-destName-error').innerHTML = '';
		    	this.wizardData.documents[0].destName = e.target.value;
		    	this.updateWizardDataView ( );
		    },
		    
		    onReasonChange: function skds_onReasonChange ( e ) 
		    {
		    	document.getElementById('${htmlid}-reason-error').innerHTML = '';
		    	this.wizardData.signature.reason = e.target.value;
		    	this.updateWizardDataView ( );
		    },
		    
		    onLocationChange: function skds_onLocationChange ( e ) 
		    {
		    	document.getElementById('${htmlid}-location-error').innerHTML = '';
		    	this.wizardData.signature.location = e.target.value;
		    	this.updateWizardDataView ( );
		    },
		    
		    
		    
		    
		    
		    // -----
		    // --- SignClient - Panels and groups visibility
		    // -
		    
		    onClientTypeKeyStoreClick: function skds_onClientTypeKeyStoreClick ( e ) 
		    {
		    	this.updateClientType ( 'KEYSTORE' );
		    },
		    
		    onClientTypeSmartCardClick: function skds_onClientTypeSmartCardClick ( e ) 
		    {
		    	this.updateClientType ( 'SMARTCARD' );
		    },
		    
		    onClientTypeSignWsClick: function skds_onClientTypeSignWsClick ( ) 
		    {
		    	this.updateClientType ( 'SIGN_WS' );
		    },
		    
		    updateClientType: function skds_updateClientType ( clientType ) 
		    {
		    	this.switchActiveGroup ( 'clientType', clientType );
		    	this.wizardData.clientType = clientType;
		    	this.updateWizardDataView ( );
		    },
		    
		    
		    
		    // -----
		    // --- SignClient - Select options update
		    // -
		    
		    updateKsUserAliasSelect: function skds_updateKsUserAliasSelect ( ) {
		    	this.info ( '', 
		    			'ksAliases: ' + this.wizardData.ksAliases + '\n' +
		    			'ksUserAlias: ' + this.wizardData.ksUserAlias );
		    	this.updateSelect ( 'ksUserAlias', this.wizardData.ksAliases, this.wizardData.ksUserAlias );
		    },
		    
		    updateScDriverSelect: function skds_updateScDriverSelect ( ) {
		    	var driver = this.wizardData.scDriver;
		    	var values = this.wizardData.scDriverNames;
		    	var names  = this.wizardData.scDriverDescriptions;
		    	var select = document.getElementById("${htmlid}-scDriver");
				var selected;
				var found = false;
				select.options = new Array();
				select.options[0] = new Option("${msg('select.dummy')}", '', false, false);
				for ( i=0; i<values.length; i++) {
					select.options[i+1] = new Option ( names[i], values[i], false, false );
					if ( values[i] === driver ) {
						select.options[i+1].selected = found = true;
					}
				}
				if ( !found ) {
					select.options[0].selected = true;
				}
		    },
		    
		    updateScUserAliasSelect: function skds_updateScUserAliasSelect ( ) {
		    	this.updateSelect ( 'scUserAlias', this.wizardData.scAliases, this.wizardData.scUserAlias );
		    },
			
			
		    
		    // -----
		    // --- SignClient - Input widget change events
		    // -
		    
		    onKsPinChange: function skds_onKsPinChange ( e ) {
		    	document.getElementById('${htmlid}-ksPin-error').innerHTML = '';
		    	this.wizardData.ksPin = e.target.value;
		    	this.updateWizardDataView ( );
		    	this.callFormOperation ( 'skdsSignCallKeyStoreOpen' );
		    },
		    
		    onKsUserAliasChange: function skds_onKsUserAliasChange ( e ) {
		    	document.getElementById('${htmlid}-ksUserAlias-error').innerHTML = '';
		    	this.wizardData.ksUserAlias = e.target.value;
		    	this.updateWizardDataView ( );
		    	this.callFormOperation ( 'skdsSignCallKeyStoreRead' );
		    },
		    
		    onKsUserPasswordChange: function skds_onKsUserPasswordChange ( e ) {
		    	document.getElementById('${htmlid}-ksUserPassword-error').innerHTML = '';
		    	this.wizardData.ksUserPassword = e.target.value;
		    	this.updateWizardDataView ( );
		    },

		    onScDriverChange: function skds_onScDriverChange ( e ) {
		    	document.getElementById('${htmlid}-scDriver-error').innerHTML = '';
		    	this.wizardData.scDriver = e.target.value;
		    	this.updateWizardDataView ( );
		    	
		    	var driver = this.wizardData.scDriver;
		    	var appletResponseJSON = document.sinekartaApplet.selectDriver ( this.wizardData.scDriver );
				var appletResultJSON = this.parseAppletResponse ( appletResponseJSON );
				if ( appletResultJSON !== undefined ) {
					if ( appletResultJSON === driver ) {
						this.info ( 'refresh', 'driver: ' + appletResultJSON );
					} else {
						this.error ( 'refresh', 'unexpected result: ' + appletResultJSON );
					}
				} else {
					this.error ( 'refresh', 'unexpected response: ' + appletResultJSON );
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
					this.wizardData.scAliases = JSON.parse ( appletResultJSON );
					this.wizardData.scUserAlias = '';
					var singleAlias = this.wizardData.scAliases.length == 1; 
					if ( singleAlias ) {
						this.wizardData.scUserAlias = this.wizardData.scAliases[0];
					}
					this.updateScUserAliasSelect ( );
					if ( singleAlias ) {
						this.onScUserAliasChange ( );
					}
					/*document.getElementById('${htmlid}-clientType-SMARTCARD-enabled').style.display = 'block';
					document.getElementById('${htmlid}-clientType-SMARTCARD-disabled').style.display = 'none';
				} else {
					document.getElementById('${htmlid}-clientType-SMARTCARD-enabled').style.display = 'none';
					document.getElementById('${htmlid}-clientType-SMARTCARD-disabled').style.display = 'block';*/
				}
				this.updateWizardDataView ( );
			},
		    
		    onScUserAliasChange: function skds_onScUserAliasChange() {
		    	document.getElementById('${htmlid}-scUserAlias-error').innerHTML = '';
		    	var scUserAliasSelect = document.getElementById("${htmlid}-scUserAlias");
				var scUserAlias = scUserAliasSelect.options[scUserAliasSelect.selectedIndex].value;
				if ( scUserAlias !== '' ) {
					var appletResponseJSON = document.sinekartaApplet.selectCertificate(scUserAlias);
					var appletResultJSON = this.parseAppletResponse ( appletResponseJSON );
					if ( appletResultJSON !== undefined ) {
						this.wizardData.signature.hexCertificateChain[0] = this.parseAppletResponse ( appletResponseJSON );
						this.info ( 'onScUserAliasChange', 'signing certificate: ' + this.wizardData.signature.hexCertificateChain[0] );
					} else {
						this.error ( 'onScUserAliasChange', 'unexpected response: ' + appletResultJSON );
					}
				} else {
					this.wizardData.signature.hexCertificateChain[0] = '';
				}
				this.updateWizardDataView ( );
			},

		});
	})();
	
	new Alfresco.skds("${htmlid}");
	
	
	// JS Sign Applet START
	

	

	
	function signDigest() {
		var pin = document.getElementById("${htmlid}-sc-sign-id").value;
		var alias = document.getElementById("${htmlid}-scUserAlias").value;
		var digestToSign = document.getElementById("${htmlid}-sc-digest-id").value;
		var signString = document.sinekartaApplet.signDigest(pin, alias, digestToSign);
		document.getElementById("${htmlid}-sc-sign-id").value = signString;
	}
	
	// JS Sign Applet END
	
	
	
	
	
	
//]]></script>
