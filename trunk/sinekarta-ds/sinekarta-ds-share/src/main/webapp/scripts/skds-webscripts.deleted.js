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


// -----
// --- Automatic wizard redirection on success
// -

/**
 * 
 * @param htmlid
 * @param target
 * @param group
 * @returns
 */
function showGroupPanel ( htmlid, target, group ) {
		
	var component=document.getElementById(htmlid+'-'+target);
	var prev = component.value;
	var panel;
	
	panel = document.getElementById(htmlid+'-'+prev+'-panel');
	panel.style.display = 'none';
	
	panel = document.getElementById(htmlid+'-'+group+'-panel');
	panel.style.display = 'block';
	
	component.value = group;
}



/**
 * 
 * @param elementId
 * @returns
 */
function toggleElementDisplay(elementId) {
	var element=document.getElementById(elementId);
	var display = element.style.display;
	if(display == 'none')
		display = 'block';
	else	
		display = 'none';
	element.style.display = display;
}



/**
 * 
 * @param nodeRef
 * @param fileName
 * @returns
 */
function downloadFile(nodeRef, fileName) {
	// FIXME far puntare dinamicamente al server Alfresco
	baseUrl = 'http://localhost:8080/alfresco/';
	nodeRef = nodeRef.replace('workspace://', 'workspace/');
	href = baseUrl + 'download/attach/' + nodeRef + '/' + fileName; 
	redirectTo ( href );
};
