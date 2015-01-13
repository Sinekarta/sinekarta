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

<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />
<#include "skdsActionError.ftl" />
<#include "skdsFieldError.ftl" />
<#include "skdsSignWizard.head.ftl" />

<#assign debug = 'AC' />
<#if debug?contains('A')>
	<#assign arvDisplay = 'block' />
<#else>
	<#assign arvDisplay = 'none' />
</#if>
<#if debug?contains('D')>
	<#assign wdvDisplay = 'block' />
<#else>
	<#assign wdvDisplay = 'none' />
</#if>
<#if debug?contains('C')>
	<#assign jscDisplay = 'block' />
<#else>
	<#assign jscDisplay = 'none' />
</#if>
<#if debug?contains('F')>
	<#assign ftaDisplay = 'block' />
<#else>
	<#assign ftaDisplay = 'none' />
</#if>

<#if debug != ''>
	<#assign bodyWidth = "118em" />
	<#assign debugDisplay = "block" />
<#else>
	<#assign bodyWidth = "100%" />
	<#assign debugDisplay = "none" />
</#if>

<div id="${htmlid}-body" style="width: ${bodyWidth}">
	<div  class="skds-debug" style="display: ${debugDisplay}">
		<div id="${htmlid}-wizardDataView" class="skds-panel">
			<div id="${htmlid}-wizardDataView-header" class="skds-panel-header">
				<label>Wizard Data View</label>
			</div>
			<div id="${htmlid}-wizardDataView-body" class="skds-panel-body" style="display: ${wdvDisplay}">
			</div>
		</div>
		<div id="$${htmlid}-appletResponseView" class="skds-panel">
			<div id="${htmlid}-appletResponseView-header" class="skds-panel-header">
				<label>Applet Response View</label>
			</div>
			<div id="${htmlid}-appletResponseView-body" class="skds-panel-body" style="display: ${arvDisplay}">
			</div>
		</div>
		<div id="${htmlid}-jsConsole" class="skds-panel">
			<div id="${htmlid}-jsConsole-header" class="skds-panel-header">
				<div class="skds-buttons">
					<span id="${htmlid}-clearJsConsole" class="yui-button yui-submit-button">
						<span class="first-child">
							<button id="${htmlid}-clearJsConsole-button" type='button'> 
								clear
							</button>
						</span>
					</span>
				</div>
				<label>JavaScript Console</label>
			</div>
			<div id="${htmlid}-jsConsole-body" class="skds-panel-body" style="display: ${jscDisplay}">
				<label>This space displays any Javascript test message</label>
				<br/><br/>
				<div id="${htmlid}-jsConsole-console">
				</div>
			</div>
		</div>
		
		<#attempt>
			<div id="${htmlid}-freemarkerTest" class="skds-panel">
				<div id="${htmlid}-freemarkerTest-header" class="skds-panel-header" >
					<label>Freemarker Test Area</label>
				</div>
				<div id="${htmlid}-freemarkerTest-body" class="skds-panel-body" style="display: ${ftaDisplay}">
					<label>Try any unsecure freemarker code into this protected code space</label>
					<br/><br/>
					<!-- If you don't know what to do or how to do it, you can simply try here -->
					
					<!-- Then just display the page on the browser -->
				</div>
			</div>
		<#recover>
			<div class="skds-panel">
				<!-- If any freemarker error occurres, it just will displayed here -->
				${.error}
				<!-- Yeah, and the page will be shown anyway - take it easy buddy!! -->
			</div>
		</#attempt>
	</div>
<#attempt>
	<#-- macro json_string string>${string?js_string?replace("\\'", "\'")?replace("\\>", ">")}</#macro -->
	<form id="${htmlid}-form" action="${wscWizardData.currentStep.form}" method="post" class="skds-form">
	
		<input type="hidden" id="${htmlid}-htmlid"			name="htmlid" 		  value='${htmlid}' />
		<input type="hidden" id="${htmlid}-wizardDataJSON" 	name="wizardDataJSON" value="${wizardDataJSON}" />
		
		<!-- 
		 | Form header, displays the static navigation structure:
		 |	* title-bar
		 |		* buttons
		 |		* title
		 |	* actionErrors
		 |	* header-bar
		 |		*header
		 | The button appearance policy need to be defined into the calling template with a sequence of letters "CBNE"
		 | named "buttons". If any of the letters above will appear into "buttons", then the relative button will be
		 | displayed.
		 | The actionError space will be filled only if the Java WebScript Controller notified a general error. 
		 | Any error among the form data will be sent to the relative fieldError that must be provided by the 
		 | calling template.
		 | -->
		 <#attempt>
			<div>
				<div class="skds-title-bar">
					<div class="skds-buttons">
						<span class="yui-button yui-submit-button">
							<span id="${htmlid}-cancel" class="first-child">
								<button id="${htmlid}-cancel-button" type='button' name="cancel"> 
									${msg("skds-button.cancel")}
								</button>
							</span>
						</span>
						<span class="yui-button yui-submit-button">
							<span id="${htmlid}-back" class="first-child">
								<button id="${htmlid}-back-button" type='button' name="back"> 
									${msg("skds-button.back")} 
								</button>
							</span>
						</span>			
						<span class="yui-button yui-submit-button">
							<span id="${htmlid}-undo" class="first-child">
								<button id="${htmlid}-undo-button" type="button" name="next"> 
									${msg("skds-button.undo")} 
								</button>
							</span>
						</span>
						<span class="yui-button yui-submit-button">
							<span id="${htmlid}-next" class="first-child">
								<button id="${htmlid}-next-button" type="button" name="next"> 
									${msg("skds-button.next")} 
								</button>
							</span>
						</span>
						<span class="yui-button yui-submit-button">
							<span id="${htmlid}-end">
								<button id="${htmlid}-end-button" type="button" name="end">
									${msg("skds-button.end")}
								</button>
							</span>
						</span>
					</div>
				
					<div id="${htmlid}-header">
					</div>
				</div>
				
				<div class="skds-error">
					<@actionError />
				</div>
			
				<div id="${htmlid}-description" class="skds-header-bar">
				</div>
			</div>
		<#recover>
			<div class="skds-error">
				${.error}
			</div>
		</#attempt>
<#recover>		
	${.error}
</#attempt>
	
		<!-- 
		 | Form body, displays the structure inside the included htmlTemplate.
		 | The htmlTemplate to be displayed is provided by the WSController into the currentStep property of the  
		 | wizard DTO. If any form data is wrong, or if any process error occurs, this value will be equal to the 
		 | form that has caused the error.
		 | Any FreeMarker error will be caught and displayed into the recover statement. 
		 -->
		<#attempt>
			<#list wscWizardData.wizardForms as form> 
				<div id="${htmlid}-${form}-form">
					<#include form + '.form.ftl' />
				</div>	
			</#list>
		<#recover>
			<div class="skds-error">
				${.error}
			</div>
		</#attempt>
	</form>
	
</div>
