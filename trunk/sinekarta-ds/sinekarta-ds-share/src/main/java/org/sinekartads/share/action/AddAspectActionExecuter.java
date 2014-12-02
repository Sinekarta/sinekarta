package org.sinekartads.share.action;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public class AddAspectActionExecuter extends ActionExecuterAbstractBase
{
	   public static final String NAME = "add-aspect";
	   public static final String PARAM_ASPECT_NAME = "aspect-name";
	
   /**
    * the node service
    */
   private NodeService nodeService;
   
   /**
    * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(Action, NodeRef)
    */
   @Override
   public void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
      // Check that the node still exists
      if (this.nodeService.exists(actionedUponNodeRef) == true)
      {
         // Get the qname of the aspect to apply, we know it must have been set since it is mandatory parameter
         QName aspectQName = (QName)action.getParameterValue(PARAM_ASPECT_NAME);
        
         // Use the node service to apply the aspect to the node
         this.nodeService.addAspect(actionedUponNodeRef, aspectQName, null);
      }
   }

   /**
    * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
    */
   @Override
   protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
   {
      // Add definitions for action parameters
      paramList.add(
         new ParameterDefinitionImpl(                       // Create a new parameter defintion to add to the list
            PARAM_ASPECT_NAME,                              // The name used to identify the parameter
            DataTypeDefinition.QNAME,                       // The parameter value type
            true,                                           // Indicates whether the parameter is mandatory
            getParamDisplayLabel(PARAM_ASPECT_NAME)));      // The parameters display label
   }  

   /**
    * Set the node service
    *
    * @param nodeService  the node service
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

}
