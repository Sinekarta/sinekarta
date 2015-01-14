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
	<div id="${htmlid}-destOptions-header" class="skds-panel-header">
		<label>${msg("panel.destOptions")}</label>
    </div>
    <div id="${htmlid}-destOptions-body" class="skds-panel-body">
    
	    <!-- wizardData.signature.documents[0].destName -->
		<div class="skds-datarow">
			<span class="skds-data-caption">
				<label for="${htmlid}-destName">${msg("label.destName")}</label>
			</span>
			<span class="skds-data-input">
				<input id="${htmlid}-destName" type="text" />
			</span>
			<@fieldError field='destName' />
		</div>
	
	    <!-- wizardData.signature.reason -->
		<div class="skds-datarow">
			<span class="skds-data-caption">
				<label for="${htmlid}-reason">${msg("label.reason")}</label>
			</span>
			<span class="skds-data-input">
				<input id="${htmlid}-reason" type="text" />
			</span>
			<@fieldError field='reason' />
		</div>
		
		<!-- wizardData.signature.location -->
		<div class="skds-datarow">
			<span class="skds-data-caption">
				<label for="${htmlid}-location">${msg("label.location")}</label>
			</span>
			<span class="skds-data-input">
				<input id="${htmlid}-location" type="text" />
			</span>
			<@fieldError field='location' />
		</div>
	</div>		
</div>

<div class="skds-panel">
	<div id="${htmlid}-signCategory-header" class="skds-panel-header">
		<label>${msg("panel.signCategory")}</label>
    </div>
    <div id="${htmlid}-signCategory-body" class="skds-panel-body">
    	<fieldset>
	    	<div class="skds-datagrid">
	    	
				<div class="skds-datarow">
					<span class="skds-data-caption">
						<label class="sinekartads">${msg("label.signCategory")}</label>
					</span>
					<span class="skds-data-input"> 
						<input id="${htmlid}-signCategory-cms" name="signCategory" type="radio" value="CMS" />
						<label for="${htmlid}-signCategory-cms">${msg("label.cms")}</label>
					</span>
				</div>
				
				<div class="skds-datarow">
					<span class="skds-data-caption"> &nbsp; </span>
					<span class="skds-data-input">
						<input id="${htmlid}-signCategory-pdf" name="signCategory" type="radio" value="PDF" />
						<label for="${htmlid}-signCategory-pdf">${msg("label.pdf")}</label> 
					</span>
				</div>
				
				<div class="skds-datarow">
					<span class="skds-data-caption"> &nbsp; </span>
					<span class="skds-data-input"> 
						<input id="${htmlid}-signCategory-xml" name="signCategory" type="radio" value="XML" />
						<label for="${htmlid}-signCategory-xml">${msg("label.xml")}</label> 
					</span>
				</div>
				
			</div>
		</fieldset>
	</div>
</div>

<div id="${htmlid}-tsSelection-group" class="skds-panel">
	<div id="${htmlid}-tsSelection-header" class="skds-panel-header">
		<label>${msg("panel.tsSelection")}</label>
    </div>
    <div id="${htmlid}-tsSelection-body" class="skds-panel-body">
		<div class="skds-group">
		    <div id="${htmlid}-tsSelection-DEFAULT-header" class="skds-group-header">
				<label>${msg("group.tsSelection.DEFAULT")}</label>
		    </div>
		    <div id="${htmlid}-tsSelection-DEFAULT-body" class="skds-group-body">
		    	<label>${msg("label.tsSelection.DEFAULT")}</label>
		    </div>
		</div>
		<div class="skds-group">
		    <div id="${htmlid}-tsSelection-CUSTOM-header" class="skds-group-header">
				<label>${msg("group.tsSelection.CUSTOM")}</label>
		    </div>
		    <div id="${htmlid}-tsSelection-CUSTOM-body" class="skds-group-body">
				<div class="skds-datagrid">
					<label>${msg("label.tsSelection.CUSTOM")}</label>
				
					<!-- wizardData.signature.timeStampRequest.tsUrl -->
					<div class="skds-datarow">
						<span class="skds-data-caption">
							<label for="${htmlid}-tsUrl">${msg("label.tsUrl")}</label>
						</span>
						<span class="skds-data-input">
							<input id="${htmlid}-tsUrl" type="text" />
						</span>
						<@fieldError field='tsUrl' />
					</div>
					
					<!-- wizardData.signature.timeStampRequest.tsUsername -->
					<div class="skds-datarow">
						<span class="skds-data-caption">
							<label for="${htmlid}-tsUsername">${msg("label.tsUsername")}</label>
						</span>
						<span class="skds-data-input">
							<input id="${htmlid}-tsUsername" type="text" />
						</span>
						<@fieldError field='tsUsername' />
					</div>
					
					<!-- wizardData.signature.timeStampRequest.tsPassword -->
					<div class="skds-datarow">
						<span class="skds-data-caption">
							<label for="${htmlid}-tsPassword">${msg("label.tsPassword")}</label>
						</span>
						<span class="skds-data-input">
							<input id="${htmlid}-tsPassword" type="password" />
						</span>
						<@fieldError field='tsPassword' />
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
