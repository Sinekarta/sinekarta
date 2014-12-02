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
	<#macro signerInfo signer>
		<#if signer.verifyResult=='invalid'>
			<#assign bgcolor="#FA5858" />
		<#elseif signer.verifyResult=='untrusted'>
			<#assign bgcolor="#F4FA58" />
		<#else>
			<#assign bgcolor="#82FA58" />
		</#if>
		<tr style="
						<#if bgcolor?has_content> background-color: ${bgcolor}; </#if>
						border-top: 1px solid #0a1b2a;
					">
			<td>${msg("verifyResult." + signer.verifyResult)}</td>
			<td>${signer.subject}</td>
			<td>${signer.issuer}</td>
			<td>${signer.signingTime}</td>
			<td>
				${signer.signingPlace}
				<#if signer.signingCountry??>
					(${signer.signingCountry})
				</#if>
			</td>
			<td> 
				<img id="${el}-${certCounter}-openCertificate" 
						class="sinekartaCertificate" 
						src="${page.url.context}/components/sinekartads/documentlibrary/images/sinekarta-certificate.png" 
						onclick="toggleElementDisplay('${el}-${certCounter}-certificate')" alt="show certificate" />
			</td>
		</tr>
		<tr id="${el}-${certCounter}-certificate" style="
						<#if bgcolor?has_content> background-color: ${bgcolor}; </#if>
					">
			<td colspan="5" style="word-wrap:break-word;">
				<div class="skds-row">
					<span class="skds-caption">
						${msg("label.certificateNotBefore")}
					</span>
					<span class="skds-input"> 
						<label>${signer.certificateNotBefore}</label> 
					</span>
				</div>
				<div class="skds-row">
					<span class="skds-caption">
						<label>${msg("label.certificateNotAfter")}</label>
					</span>
					<span class="skds-input"> 
						${signer.certificateNotAfter} 
					</span>
				</div>
				<div class="skds-row">
					<span class="skds-caption">
						<label>${msg("label.certificateQualification")}</label>
					</span>
					<span class="skds-input"> 
						${signer.certificateQualification} 
					</span>
				</div>
				<div class="skds-row">
					<div>
						<span class="skds-caption">
							<label>${msg("label.hexCertificate")}</label>
						</span>
						<span class="skds-input"> 
							&nbsp; 
					</span>
					</div>
					<div style="padding: 0em 1em;">
						${signer.hexCertificate}
					</div>
				</div>						
			</td>
			<td>&nbsp</td>
		</tr>
		<#assign certCounter = certCounter + 1 />
	</#macro>  	

	<#if resultCode?exists && resultCode=='20'>
		<div class="section-bar">
			<label class="section">${msg("section.summary")}</label>
		</div>
		<div>
			<#if verifyOperation=='verifyAndExtract'>
				<label>${msg("label.extracted")}</label>
			<#else>
				<label>${msg("label.verified")}</label>
			</#if>
			<br />
			<label>${msg("label.download")}</label>
			<br />
			<ul>
				<li><a href="#" onClick="downloadFile('${destRef}', '${destName}')">${destName}</a>
			</ul>
		</div>
		<br/></br>
		
		
		<#if verifyInfoDto??>
			<#assign document = verifyInfoDto.document />
			<#-- 
			<div class="section-bar">
				<label class="section">${msg("section.documentDetails")}</label>
			</div>
			<div>
				<div class="skds-row">
					<span class="skds-caption">
						<label>${msg("label.fileName")}</label>
					</span>
					<span class="skds-input"> 
						${document.fileName}&nbsp;
					</span>
				</div>
				<div class="skds-row">
					<span class="skds-caption">
						<label>${msg("label.description")}</label>
					</span>
					<span class="skds-input"> 
						${document.description}&nbsp;
					</span>
				</div>
			</div>
			
			<div class="section-bar">
				<label class="section">${msg("section.signatureDetails")}</label>
			</div>
			<div>
				<div class="skds-row">
					<span class="skds-caption">
						${msg("label.signatureType")}&nbsp;
					</span>
					<span class="skds-input"> 
						${verifyInfoDto.signatureType}&nbsp;
					</span>
				</div>
				<div class="skds-row">
					<span class="skds-caption">
						<label>${msg("label.signatureDisposition")}</label>
					</span>
					<span class="skds-input"> 
						${verifyInfoDto.signatureDisposition} 
					</span>
				</div>
			</div>
			-->
			<div class="section-bar">
				<label class="section">${msg("section.signers")}</label>
			</div>
			<table style="table-layout: fixed; width:100%">
				<thead>
					<tr>
						<th>${msg("label.verifyResult")}</th>
						<th>${msg("label.signer")}</th>
						<th>${msg("label.issuer")}</th>
						<th>${msg("label.signingTime")}</th>
						<th>${msg("label.signingPlace")}</th>
						<th style="width: 2.2em">&nbsp;</th>
					</tr>
				</thead>
				<#assign certCounter = 1 />
				<tbody>
					<#list verifyInfoDto.invalidSigners as signer>
						<@signerInfo signer=signer />
					</#list>
					<#list verifyInfoDto.untrustedSigners as signer>
						<@signerInfo signer=signer />
					</#list>			
					<#list verifyInfoDto.validSigners as signer>
						<@signerInfo signer=signer />
					</#list>						
				</tbody>
			</table>
		</#if>
		<br /><br />
		<label>${msg("label.end")}</label>
	<#else>
		<label>${msg("label.failure")}</label>
	</#if>