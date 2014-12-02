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
		<input type="hidden" id="${el}-verifyOperation" name="verifyOperation" value="${verifyOperation?js_string}" />

		<div class="skds-group-container">									   
		    <div id="${el}-justVerify-skds-header" class="skds-group-header" onclick="showGroupPanel('${el}', 'verifyOperation', 'justVerify')">
				<label>${msg("group.justVerify")}</label>
		    </div>
		    <div id="${el}-justVerify-panel" class="skds-group-body" style="display:<#if verifyOperation=='justVerify'>block<#else>none</#if>">
		    	<div style="height: 4em; padding: 0.33em">
					<label>${msg("label.justVerify")}</label>
				</div>				
			</div>
		</div>
		<div class="skds-group-container">									   
		    <div id="${el}-verifyAndExtract-skds-header" class="skds-group-header" onclick="showGroupPanel('${el}', 'verifyOperation', 'verifyAndExtract')">
				<label>${msg("group.verifyAndExtract")}</label>
		    </div>
		    <div id="${el}-verifyAndExtract-panel" class="skds-group-body" style="display:<#if verifyOperation=='verifyAndExtract'>block<#else>none</#if>">
   				<fieldset>
    				<div class="skds-row">
						<span class="skds-caption">
							<label class="sinekartads">${msg("label.destName")}</label>
						</span>
						<span class="skds-input"> 
							<input id="${el}-destName" type="text" name="destName" value="${destName}" /> 
						</span>
						<@fieldError field='destName' />
					</div>
					<div class="skds-row">
						<span class="skds-caption">
						</span>
						<span class="skds-input">
							<#if flagReplaceFile=='true'>
								<input id="${el}-flagReplaceFile" type="checkbox" name="flagReplaceFile" checked />
							<#else>
								<input id="${el}-flagReplaceFile" type="checkbox" name="flagReplaceFile" />
							</#if>
							<label for="${el}-flagReplaceFile">${msg("label.flagReplaceFile")}</label> 
						</span>
					</div>
    				<div class="skds-row">
						<span class="skds-caption">
							<label class="sinekartads">${msg("label.extractionParent")}</label>
						</span>
						<span class="skds-input"> 
							<#if pathChoice == 'sameFolder'>
								<input id="${el}-sameFolder" type="radio" name="pathChoice" value="sameFolder" checked />
							<#else>
								<input id="${el}-sameFolder" type="radio" name="pathChoice" value="sameFolder" />
							</#if>
							<label for="${el}-sameFolder">${msg("label.sameFolder")}</label>
						</span>
					</div>
					<div class="skds-row">
						<span class="skds-caption">
							&nbsp;
						</span>
						<span class="skds-input"> 
							<#if pathChoice == 'pickFolder'>
								<input id="${el}-pickFolder" type="radio" name="pathChoice" value="pickFolder" checked />
							<#else>
								<input id="${el}-pickFolder" type="radio" name="pathChoice" value="pickFolder" />
							</#if>
							<label for="${el}-pickFolder">${msg("label.pickFolder")}</label> 
							<input id="${el}-extractionParent" type="text" name="extractionParent" value="${extractionParent}" /> 
							<button id="${el}-pick-button" type='button' name="pick" onclick="pickFolder();">${msg("button.pick")}</button>	
						</span>
					</div>
					<div>
						<@fieldError field='extractionParent' />
					</div>
			    </fieldset>	
		    </div>