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
package org.sinekartads.alfresco.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;

/**
 * utility class for working with nodes
 * 
 * @author andrea.tessaro
 *
 */
public class NodeTools {  
	
	private static Logger tracer = Logger.getLogger(NodeTools.class);
	
	/**
	 * verify that a filename or folder name contains only valid character
	 * @param name
	 * @return
	 */
	public static String validateFileFolderName(String name) {
		if (name!=null) {
			name = name.replaceAll("~", "-");
			name = name.replaceAll("&", "-");
			name = name.replaceAll("#", "-");
			name = name.replaceAll("\\*", "-");
			name = name.replaceAll("\\?", "-");
			name = name.replaceAll(":", "-");
			
			name = name.replaceAll("\"", "'");
			
			name = name.replaceAll("\\|", "-");
			name = name.replaceAll("\\\\", "-");
			name = name.replaceAll("/", "-");
			
			name = name.replaceAll("<", "(");
			name = name.replaceAll(">", ")");
			name = name.replaceAll("\\{", "(");
			name = name.replaceAll("}", ")");
		}
		return name.trim();
	}
	
	/**
	 * Create a node with the given name and the given properties, as a child of
	 * a certain parent.
	 * The properties are forced to contain the given name as PROP_NAME
	 * @param nodeService the service to be used 
	 * @param parentRef the parent's NodeRef 
	 * @param name the 
	 * @return the NodeRef to the generated child node
	 * @throws InvalidNodeRefException if the parentRef is invalid or if a child 
	 * with that name already exists
	 */
	public static NodeRef createNode(
			NodeService nodeService, NodeRef parentRef, String name)  
					throws InvalidNodeRefException {
		return createNode(nodeService, parentRef, name, null);
	}	
	
	/**
	 * Create a node with the given name and the given properties, as a child of
	 * a certain parent.
	 * The properties are forced to contain the given name as PROP_NAME
	 * @param nodeService the service to be used 
	 * @param parentRef the parent's NodeRef 
	 * @param name the name to assign to the child node
	 * @param properties the properties to be assigned to the child node 
	 * @return the NodeRef to the generated child node
	 * @throws InvalidNodeRefException if the parentRef is invalid or if a child 
	 * with that name already exists
	 */
	public static NodeRef createNode(
			NodeService nodeService, NodeRef parentRef, String name, Map<QName, Serializable> properties)  
					throws InvalidNodeRefException {
		
		if(MapUtils.isEmpty(properties)) {
			properties = new HashMap<QName, Serializable>();
		}
		properties.put(ContentModel.PROP_NAME, name);
		return nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS, 
	            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),	            
	            ContentModel.TYPE_CONTENT, properties).getChildRef();
	}

	/**
	 * convert a path into a lucene compliant String
	 * all namespace are converted to corresponding prefix
	 * 
	 * @param namespaceService the namespace service used to translate namespace into prefix 
	 * @param path the path to translate
	 * @return a string representation of the path with prefixes 
	 */
	public static String translateNamespacePath(NamespaceService namespaceService, Path path) {
		StringBuffer ret = new StringBuffer();
		Iterator<Path.Element> iter = path.iterator();
		iter.next(); // skipping first "/" path
		while (iter.hasNext()) {
			ret.append("/");
			Path.Element el = iter.next();
			ret.append(el.getPrefixedString(namespaceService));
		}
		if (tracer.isDebugEnabled()) tracer.debug("translateNamespacePath : " + path + " = " + ret);
		return ret.toString();
	}
	
	/**
	 * convert a path into a human readable String
	 * 
	 * @param nodeService used to ask to each node his readable name
	 * @param path the path to translate
	 * @return a human readable string representation of the path
	 */
	public static String translatePath(NodeService nodeService, NodeRef nodeRef) {
		Path path = nodeService.getPath(nodeRef);
		StringBuffer ret = new StringBuffer();
		Iterator<Path.Element> iter = path.iterator();
		iter.next(); // skipping first "/" path
		while (iter.hasNext()) {
			ret.append("/");
			Path.Element el = iter.next();
			ChildAssociationRef elementRef = ((ChildAssocElement)el).getRef();
			ret.append(nodeService.getProperty(elementRef.getChildRef(), ContentModel.PROP_NAME));
		}
		QName nodeType = nodeService.getType(nodeRef);
		if(nodeType.isMatch(ContentModel.TYPE_FOLDER)) {
			ret.append("/");
		}
		if (tracer.isDebugEnabled()) tracer.debug("translatePath : " + path + " = " + ret);
		return ret.toString();
	}
	
	public static NodeRef getParentRef(NodeService nodeService, NodeRef nodeRef) {
		Iterator<ChildAssociationRef> it = nodeService.getParentAssocs(nodeRef).iterator();
		NodeRef parentRef = null;
		ChildAssociationRef childAssoc;
		while (it.hasNext() && parentRef == null) {
			childAssoc = it.next();
			if (childAssoc.getTypeQName().equals(ContentModel.ASSOC_CONTAINS)) {
				parentRef = childAssoc.getParentRef();
			}
		}
		return parentRef;
	}

//	/**
//	 * get or create the given (sinekarta archive) lucene path
//	 * 
//	 * @param searchService needed to search for the lucene path
//	 * @param fileFolderService needed to create (if necessary) the path
//	 * @param storeRef needed to know the soreRef into where create the path
//	 * @param lucenePath the path to find and, if necessary, create
//	 * @return the nodeRef of the requested lucene path
//	 */
//	public static NodeRef deepCreateArchiveFolder(NodeService nodeService, SearchService searchService, FileFolderService fileFolderService, OwnableService ownableService, StoreRef storeRef, String lucenePath) {
//		if (tracer.isDebugEnabled()) tracer.debug("deepCreateArchiveFolder, path to create : " + lucenePath);
//		// scompongo il path da cercare
//		String[] pp = lucenePath.split("/");
//		// get node of path requested
//		NodeRef parent = getNodeRefByPath(searchService, nodeService, storeRef, "/"+pp[1]);
//		NodeRef folder = parent; 
//		for (int i=2;i<pp.length;i++) {
//			String folderName = pp[i].split(":")[1];
//			folderName = validateFileFolderName(folderName);
//			parent=folder;
//			folder = fileFolderService.searchSimple(parent,folderName);
//			if (folder==null) {
//				folder = fileFolderService.create(parent, folderName,SinekartaModel.TYPE_QNAME_ARCHIVE).getNodeRef();
//				if (tracer.isDebugEnabled()) tracer.debug("deepCreateArchiveFolder, setting properties and ownership of " + folderName);
//				// setting space attribute and ownership
//				String rcsUserId = (String)nodeService.getProperty(parent, SinekartaModel.PROP_QNAME_RCS_USER_ID);
//				String sinekartaAdminUserId = (String)nodeService.getProperty(parent, SinekartaModel.PROP_QNAME_SINEKARTA_ADMIN_USER_ID);
//				nodeService.setProperty(folder, SinekartaModel.PROP_QNAME_ICON, "sinekarta-archive-icon");
//				nodeService.setProperty(folder, SinekartaModel.PROP_QNAME_RCS_USER_ID, rcsUserId);
//				nodeService.setProperty(folder, SinekartaModel.PROP_QNAME_SINEKARTA_ADMIN_USER_ID, sinekartaAdminUserId);
//				nodeService.setProperty(folder, ContentModel.PROP_CREATOR, sinekartaAdminUserId);
//				nodeService.setProperty(folder, ContentModel.PROP_MODIFIER, sinekartaAdminUserId);
//				ownableService.setOwner(folder, sinekartaAdminUserId);
//			}
//		}
//		if (tracer.isDebugEnabled()) tracer.debug("deepCreateArchiveFolder, path " + lucenePath + " created.");
//		return folder;
//	}

	/**
	 * get or create the given (simple) lucene path
	 * 
	 * @param searchService needed to search for the lucene path
	 * @param fileFolderService needed to create (if necessary) the path
	 * @param storeRef needed to know the soreRef into where create the path
	 * @param lucenePath the path to find and, if necessary, create
	 * @return the nodeRef of the requested lucene path
	 */
	public static NodeRef deepCreateFolder(NodeService nodeService, SearchService searchService, FileFolderService fileFolderService, StoreRef storeRef, String lucenePath) {
		if (tracer.isDebugEnabled()) tracer.debug("deepCreateFolder, path to create : " + lucenePath);
		// scompongo il path da cercare
		String[] pp = lucenePath.split("/");
		String path="";
		int idxStart;
		for (idxStart=1;idxStart<pp.length;idxStart++) {
			if (pp[idxStart].startsWith("app"))
				path=path+"/"+pp[idxStart];
			else 
				break;
		}
		// get node of path requested
		NodeRef parent = getNodeRefByPath(searchService, nodeService, storeRef, path);
		NodeRef folder = parent; 
		for (int i=idxStart;i<pp.length;i++) {
			String folderName = pp[i].split(":")[1];
			folderName = validateFileFolderName(folderName);
			parent=folder;
			folder = fileFolderService.searchSimple(parent,folderName);
			if (folder==null)
				folder = fileFolderService.create(parent, folderName,ContentModel.TYPE_FOLDER).getNodeRef();
		}
		if (tracer.isDebugEnabled()) tracer.debug("deepCreateFolder, path " + lucenePath + " created.");
		return folder;
	}

	/**
	 * 	calculate the NodeRef of a giving lucenePath (the path should return only one result)
	 *  lucenePath must be a path: query format (without PATH:)
	 *  
	 * @param searchService needed to search for the lucene path
	 * @param storeRef needed to know the soreRef into where search for the path
	 * @param lucenePath the path to find and 
	 * @return the nodeRef of the requested lucene path
	 */
	public static NodeRef getNodeRefByPath(SearchService searchService, NodeService nodeService, StoreRef storeRef, String lucenePath) {
		if (tracer.isDebugEnabled()) tracer.debug("getNodeRefByPath, searching noderef of this path : " + lucenePath);
		// execute the lucene query (MUST BE a PATH: query format)
		ResultSet rs = null; 
		try {
			try {
				rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\""+encodeLucenePath(lucenePath)+"\"");
			} catch (LuceneQueryParserException ex) {
				rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\""+encodeLucenePath(lucenePath)+"\"");
			}
			// if no nodes found, retur null
			if (rs.length()==0)
				return null;
			else if (rs.length()==1) {
				NodeRef ret =  rs.getNodeRef(0);
				if (!nodeService.exists(ret)) return null;
				// if 1 node found, it's good!
				else return ret;
			} else {
				// if more than 1, throw exception
				tracer.error("Lucene query returns more than 1 noderef.");
				throw new IllegalArgumentException("Lucene query returns more than 1 noderef.");
			}
		} finally {
			if (rs!=null) rs.close();
		}
	}
	
	/**
	 * 	calculate the NodeRef of a given noderef id
	 *  
	 * @param searchService needed to search for the lucene path
	 * @param storeRef needed to know the soreRef into where search for the path
	 * @param lucenePath the path to find and 
	 * @return the nodeRef of the requested lucene path
	 */
	public static NodeRef getNodeByID(SearchService searchService, NodeService nodeService, StoreRef storeRef, String id) {
		if (tracer.isDebugEnabled()) tracer.debug("getNodeByID, searching noderef of this id : " + id);
		// execute the lucene query (MUST BE a PATH: query format)
		ResultSet rs = null;
		try {
			rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "ID:\"" + storeRef.getProtocol() + "://" + storeRef.getIdentifier()+"/"+id+"\"");
			// if no nodes found, retur null
			if (rs.length()==0)
				return null;
			else {
				NodeRef ret =  rs.getNodeRef(0);
				if (!nodeService.exists(ret)) return null;
				// if 1 node found, it's good!
				else return ret;
			}
		} finally {
			if (rs!=null) rs.close();
		}
	}
	
	/**
	 * 	ISO9075 encoding of a lucene path
	 *  lucenePath must be a path: query format (without PATH:)
	 *  
	 * @param lucenePath the path to endoce 
	 * @return the lucenePath encoded
	 */
	public static String encodeLucenePath(String lucenePath) {
		if (tracer.isDebugEnabled()) tracer.debug("encodeLucenePath, encoding path : " + lucenePath);
		// if lucenePath is null or empty, nothing to do
		if (lucenePath==null || lucenePath.equals("")) return lucenePath;
		// find first / for deep recursive encoding
		int i = lucenePath.lastIndexOf('/');
		// calculate last spacePrefix and spaceName
		String completeSpaceName = lucenePath.substring(i+1);
		String spaceName = completeSpaceName.substring(completeSpaceName.indexOf(':')+1);
		String spacePrefix = completeSpaceName.substring(0,completeSpaceName.indexOf(':'));
		// deep recursive encoding
		String spaceParent = encodeLucenePath(lucenePath.substring(0,i));
		if (tracer.isDebugEnabled()) tracer.debug("encodeLucenePath, path encoded : " + spaceParent + "/" + spacePrefix + ":" + ISO9075.encode(spaceName));
		return spaceParent + "/" + spacePrefix + ":" + ISO9075.encode(spaceName);
	}

//	public static boolean isArchived(NodeService nodeService, NamespaceService namespaceService, NodeRef nodeRef, String companyHomePath) {
//		String givenPath = NodeTools.translateNamespacePath(namespaceService, nodeService.getPath(nodeRef));
//		String archivioPath = Configuration.getInstance().getLuceneArchivePath();
//		String fullArchivioPath = companyHomePath + archivioPath;
//		if (givenPath.startsWith(fullArchivioPath)) return true;
//		else return false;
//	}
//	
//	public static NodeRef getArchivio(NodeService nodeService, SearchService searchService, String companyHomePath) {
//		// preparing for query 
//		StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
//		// getting noderef of archivio
//		String archivioPath = Configuration.getInstance().getLuceneArchivePath();
//		NodeRef archivio = NodeTools.getNodeRefByPath(searchService, nodeService, storeRef, companyHomePath + archivioPath);
//		if (archivio == null) {
//			throw new SignFailedException("archivio does not exists : " + companyHomePath + archivioPath);
//		}
//		return archivio;
//	}
//
//	public static String getSinekartaAdminUserId(NodeService nodeService, SearchService searchService, String companyHomePath) {
//		NodeRef archivio = getArchivio(nodeService, searchService, companyHomePath);
//		// obtaining sinekartaAdmin userid of specified archivio
//		return (String)nodeService.getProperty(archivio, SinekartaModel.PROP_QNAME_SINEKARTA_ADMIN_USER_ID);
//
//	}
}
