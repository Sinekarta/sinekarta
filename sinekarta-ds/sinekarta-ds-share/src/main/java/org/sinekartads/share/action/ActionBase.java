package org.sinekartads.share.action;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.InitializingBean;

public abstract class ActionBase 
		extends ActionExecuterAbstractBase
				implements InitializingBean {

	protected ServiceRegistry serviceRegistry;
	protected Repository repository;
	protected NodeService nodeService;
	protected ActionService actionService;
	protected ContentService contentService;
	protected AuthenticationService authenticationService;
	protected PersonService personService;	

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}


	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

	}

	public void afterPropertiesSet() throws Exception {
		if(serviceRegistry != null) {
			nodeService = serviceRegistry.getNodeService();
			authenticationService = serviceRegistry.getAuthenticationService();
			personService = serviceRegistry.getPersonService();
			actionService = serviceRegistry.getActionService();
			contentService = serviceRegistry.getContentService();
		}
	}


}