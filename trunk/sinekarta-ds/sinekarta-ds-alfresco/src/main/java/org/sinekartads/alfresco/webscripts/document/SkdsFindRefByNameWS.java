package org.sinekartads.alfresco.webscripts.document;

import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.repo.model.Repository;
import org.sinekartads.alfresco.webscripts.BaseAlfrescoWS;
import org.sinekartads.dto.request.SkdsFindRefByNameRequest;
import org.sinekartads.dto.response.SkdsFindRefByNameResponse;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;


public class SkdsFindRefByNameWS 
		extends BaseAlfrescoWS<SkdsFindRefByNameRequest, SkdsFindRefByNameResponse> {

	private Repository repository;
	private CMISQueryService cmisQueryService;
	
	@Override
	protected SkdsFindRefByNameResponse executeImpl(
			SkdsFindRefByNameRequest req, Status status, Cache cache) {
					
		SkdsFindRefByNameResponse resp = new SkdsFindRefByNameResponse();
		CMISResultSet result = cmisQueryService.query("select cmis:objectId from cmis:document where cmis:name = '" + req.getName() + "'");
		if ( result.getLength() == 0) {
			throw new RuntimeException(String.format("document not found: ", req.getName()));
		}
		resp.setNodeRef(result.getNodeRef(0).toString());
		return resp;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if(serviceRegistry != null) {
			cmisQueryService = serviceRegistry.getCMISQueryService();
		}
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}
