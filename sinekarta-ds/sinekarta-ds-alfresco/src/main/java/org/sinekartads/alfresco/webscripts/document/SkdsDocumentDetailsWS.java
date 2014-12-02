package org.sinekartads.alfresco.webscripts.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.sinekartads.alfresco.util.NodeTools;
import org.sinekartads.alfresco.webscripts.BaseAlfrescoWS;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.domain.DocumentDTO;
import org.sinekartads.dto.domain.NodeDTO;
import org.sinekartads.dto.request.SkdsDocumentDetailsRequest;
import org.sinekartads.dto.response.SkdsDocumentDetailsResponse;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;


public class SkdsDocumentDetailsWS 
		extends BaseAlfrescoWS<SkdsDocumentDetailsRequest, SkdsDocumentDetailsResponse> {

	private MimetypeService mimetypeService;
	
	@Override
	protected SkdsDocumentDetailsResponse executeImpl(
			SkdsDocumentDetailsRequest req, Status status, Cache cache) {
					
		SkdsDocumentDetailsResponse resp = new SkdsDocumentDetailsResponse();
		String[] nodeRefs = req.getNodeRefs();
		
		List<DocumentDTO> documentList = new ArrayList<DocumentDTO>();
		DocumentDTO document;
		Map<QName, Serializable> props;
		String fileName;
		NodeDTO baseDocument;
		NodeRef nodeRef = null;		
		try {
			for(String ref : nodeRefs) {
				nodeRef = new NodeRef(ref);
				props = nodeService.getProperties(nodeRef);
				fileName = (String)props.get(ContentModel.PROP_NAME);
				
				baseDocument = new NodeDTO();
				baseDocument.setNodeRef(ref);
				baseDocument.setFileName(fileName);
				baseDocument.setParentRef(NodeTools.getParentRef(nodeService, nodeRef).toString());
				baseDocument.setFilePath(NodeTools.translatePath(nodeService, nodeRef));
				baseDocument.setDescription(((String)props.get(ContentModel.PROP_DESCRIPTION)).trim());
				baseDocument.setMimetype(mimetypeService.guessMimetype(fileName, 
						contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)));

				document = new DocumentDTO();
				document.setBaseDocument ( baseDocument );
				documentList.add ( document );
				resp.documentsToBase64 ( documentList.toArray(new DocumentDTO[documentList.size()]) );
				resp.resultCodeToString(ResultCode.SUCCESS);
			}
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e.getMessage(), e);
			resp.resultCodeToString(ResultCode.INTERNAL_SERVER_ERROR);
		}			
			
		return resp;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if(serviceRegistry != null) {
			mimetypeService = serviceRegistry.getMimetypeService();
		}
	}
}
