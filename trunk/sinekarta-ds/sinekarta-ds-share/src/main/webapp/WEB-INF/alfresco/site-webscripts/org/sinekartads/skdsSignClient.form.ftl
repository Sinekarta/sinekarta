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
<div class="skds-panel">
	<div id="${htmlid}-clientType-header" class="skds-panel-header">
		<label>${msg("panel.clientType")}</label>
    </div>
    <div id="${htmlid}-clientType-body" class="skds-panel-body">
    	<div class="skds-group">
		    <div id="${htmlid}-clientType-KEYSTORE-header" class="skds-group-header">
				<label>${msg("group.clientType.KEYSTORE")}</label>
		    </div>
		    <div id="${htmlid}-clientType-KEYSTORE-body" class="skds-group-body">
				<div id="${htmlid}-clientType-KEYSTORE-enabled">
					<label>${msg("label.clientType.KEYSTORE")}</label>
							    	
			    	<!-- wizardData.ksPin -->
					<div class="skds-datarow">
						<span class="skds-data-caption">
							<label for="${htmlid}-ksPin">${msg("label.ksPin")}</label>
						</span>
						<span class="skds-data-input">
							<input id="${htmlid}-ksPin" type="password" />
						</span>
						<button id="${htmlid}-ksLoadAliases-button" type='button'> 
							${msg("skds-button.load")}
						</button>
						<@fieldError field='ksPin' />
					</div>
					
					<!-- wizardData.ksUserAlias -->
					<div class="skds-datarow">
						<span class="skds-data-caption">
							<label for="${htmlid}-ksUserAlias">${msg("label.ksUserAlias")}</label>
						</span>
						<span class="skds-data-input">
							<select id="${htmlid}-ksUserAlias">
								<option value="${msg("select.dummy")}" selected>${msg("select.dummy")}</option>
							</select>
						</span>
						<@fieldError field='ksUserAlias' />
					</div>
			    	
	   				<!-- wizardData.ksUserPassword -->
					<div class="skds-datarow" style="display:none">
						<span class="skds-data-caption">
							<label for="${htmlid}-ksUserPassword">${msg("label.ksUserPassword")}</label>
						</span>
						<span class="skds-data-input">
							<input id="${htmlid}-ksUserPassword" type="text" />
						</span>
						<@fieldError field='ksUserPassword' />
					</div>
				</div>
				<div id="${htmlid}-clientType-KEYSTORE-disabled" style="display: none">
					<label>${msg("label.missing.KEYSTORE")}</label>
				</div>
		    </div>
		</div>
		<div class="skds-group">
		    <div id="${htmlid}-clientType-SMARTCARD-header" class="skds-group-header">
				<label>${msg("group.clientType.SMARTCARD")}</label>
		    </div>
		    <div id="${htmlid}-clientType-SMARTCARD-body" class="skds-group-body">
		    	<div>
		    		<div id="${htmlid}-clientType-SMARTCARD-enabled">
						<div class="skds-datarow">
							<span class="skds-data-caption" style="text-align: left; width:80% !important">
						    	<label>${msg("label.clientType.SMARTCARD")}</label>
							</span>
							<span class="skds-data-input" style="width:20% !important">
								<applet name="sinekartaApplet" code="org.sinekartads.applet.SignApplet.class"
									width="60" height="35"     
									codebase="${page.url.context}/res/components/sinekarta-ds/applet/lib" 
									archive="sinekarta-ds-applet.jar">                
								</applet>
								<!-- applet communication -->
								<input type="hidden" id="skds-applet-do" value=""/>
								<input type="hidden" id="skds-applet-function" value=""/>
								<input type="hidden" id="skds-applet-parms" value=""/>
								<input type="hidden" id="skds-applet-resp" value=""/>
							</span>
					    </div>
				    </div>
			    </div>
		    	<div>
		    		<div id="${htmlid}-clientType-SMARTCARD-enabled">
						<!-- wizardData.scDriver -->
						<div class="skds-datarow">
							<span class="skds-data-caption">
								<label for="${htmlid}-scDriver">${msg("label.scDriver")}</label>
							</span>
							<span class="skds-data-input">
								<select id="${htmlid}-scDriver">
									<option value="${msg("select.dummy")}" selected>${msg("select.dummy")}</option>
								</select>
							</span>
							<@fieldError field='scDriver' />
						</div>
			    	
				    	<!-- wizardData.scPin -->
						<div class="skds-datarow">
							<span class="skds-data-caption">
								<label for="${htmlid}-scPin">${msg("label.scPin")}</label>
							</span>
							<span class="skds-data-input">
								<input id="${htmlid}-scPin" type="password" />
							</span>
							<button id="${htmlid}-scLoadAliases-button" type='button'> 
								${msg("skds-button.load")}
							</button>
							<@fieldError field='scPin' />
						</div>
						
						<!-- wizardData.ksUserAlias -->
						<div class="skds-datarow">
							<span class="skds-data-caption">
								<label for="${htmlid}-scUserAlias">${msg("label.scUserAlias")}</label>
							</span>
							<span class="skds-data-input">
								<select id="${htmlid}-scUserAlias">
									<option value="${msg("select.dummy")}" selected>${msg("select.dummy")}</option>
								</select>
							</span>
							<@fieldError field='scUserAlias' />
						</div>
					</div>
					<!--div id="${htmlid}-clientType-SMARTCARD-disabled" style="display: none">
						<label>${msg("label.missing.SMARTCARD")}</label>
					</div-->
				</div>
		    </div>
		</div>
		<div class="skds-group">
		    <div id="${htmlid}-clientType-SIGN_WS-header" class="skds-group-header">
				<label>${msg("group.clientType.SIGN_WS")}</label>
		    </div>
		    <div id="${htmlid}-clientType-SIGN_WS-body" class="skds-group-body">
		    	<label>${msg("label.clientType.SIGN_WS")}</label>
		    </div>
		</div>
    </div>
</div>