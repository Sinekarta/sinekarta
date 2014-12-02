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
<#assign el='skdsVerifyInit'>

<#include "/org/alfresco/include/alfresco-template.ftl" />
<#include "/org/alfresco/include/documentlibrary.inc.ftl" />
<@templateHeader>
   <@markup id="location-hash">
   <@documentLibraryJS />
   </@>
   <@markup id="resizer">
   <script type="text/javascript">//<![CDATA[
      new Alfresco.widget.Resizer("Repository");
   //]]></script>
   </@>

	<script type="text/javascript"  src="${url.context}/sinekarta-ds-share/scripts/skds-utils.js"></script>
	<script type="text/javascript"  src="${url.context}/sinekarta-ds-share/scripts/skds-webscripts.js"></script>
	<script type="text/javascript"  src="${url.context}/sinekarta-ds-share/scripts/skds-documentlibrary.js"></script>
	<@link  type="text/css"        href="${url.context}/sinekarta-ds-share/css/skds-documentlibrary.css" />
	<@link  type="text/css"        href="${url.context}/sinekarta-ds-share/css/skds-webscripts.css" />
	
</@>

<@templateBody>
   <@markup id="alf-hd">
   <div id="alf-hd">
      <@region scope="global" id="share-header" chromeless="true"/>
   </div>
   </@>
   <@markup id="bd" style="margin:50px;">
	
	<div id="bd">
		<@region id="skdsVerifyInit" scope="template"/>
	</div>
   </@>
</@>

<@templateFooter>
   <@markup id="alf-ft">
   <div id="alf-ft">
      <@region id="footer" scope="global" />
   </div>
   </@>
</@>
