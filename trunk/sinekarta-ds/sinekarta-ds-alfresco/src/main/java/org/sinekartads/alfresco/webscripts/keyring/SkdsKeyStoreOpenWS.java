package org.sinekartads.alfresco.webscripts.keyring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.sinekartads.alfresco.webscripts.BaseAlfrescoWS;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.domain.KeyStoreDTO;
import org.sinekartads.dto.request.SkdsKeyStoreRequest.SkdsKeyStoreOpenRequest;
import org.sinekartads.dto.response.SkdsKeyStoreResponse.SkdsKeyStoreOpenResponse;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.model.domain.SecurityLevel.KeyRingSupport;
import org.sinekartads.util.TemplateUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;


public class SkdsKeyStoreOpenWS 
		extends BaseAlfrescoWS<SkdsKeyStoreOpenRequest, SkdsKeyStoreOpenResponse> {

	@Override
	protected SkdsKeyStoreOpenResponse executeImpl (
			SkdsKeyStoreOpenRequest req, 
			Status status, 
			Cache cache ) {

		SkdsKeyStoreOpenResponse resp = new SkdsKeyStoreOpenResponse();
		try {
			String keyStorePin = req.getKeyStorePin();
			char[] ksPwd = null;
			if ( StringUtils.isNotBlank(keyStorePin) ) {
				ksPwd = keyStorePin.toCharArray();
			}
			
			String keyStoreName = "sinekarta.jks"; 
			String userName = authenticationService.getCurrentUserName ( );
			NodeRef personNodeRef = personService.getPerson ( userName );
			NodeRef homespaceNodeRef = (NodeRef) nodeService.getProperty ( 
					personNodeRef, ContentModel.PROP_HOMEFOLDER );
			
			NodeRef keyStoreRef = nodeService.getChildByName ( 
					homespaceNodeRef, ContentModel.ASSOC_CONTAINS, keyStoreName );
			
			ContentReader reader = contentService.getReader(keyStoreRef, ContentModel.PROP_CONTENT);
			InputStream is = reader.getContentInputStream ( );
			byte[] keyStoreBytes = IOUtils.toByteArray ( is );
			IOUtils.closeQuietly(is);
			
			
			KeyStoreType type = null;
			KeyStoreType[] types = KeyStoreType.values(); 
			KeyStore keyStore = null;
			for ( int i=0; i<types.length && keyStore == null; i++ ) {
				is = new ByteArrayInputStream ( keyStoreBytes );
				type = types[i];
				try {
					keyStore = KeyStore.getInstance ( type.getType(), type.getProvider() );
					keyStore.load ( is, ksPwd );
				} catch (Exception e) {
					tracer.info(String.format("%s - %s", type, e.getMessage()), e);
					keyStore = null;
				}
			}
			
			if ( keyStore != null ) {
				KeyStoreDTO ksDto = new KeyStoreDTO();
				ksDto.setName(keyStoreName);
				ksDto.setPin(keyStorePin);
				ksDto.setSupport(KeyRingSupport.ALFRESCO.name());
				ksDto.setType(type.getType());
				ksDto.setProvider(type.getProvider());
				ksDto.setAliases ( TemplateUtils.Conversion.enumerationToArray(keyStore.aliases()) );
				ksDto.setReference(keyStoreRef.toString());
				resp.setKeyStore(ksDto);
				resp.resultCodeToString(ResultCode.SUCCESS);
			} else {
				resp.resultCodeToString(ResultCode.BAD_REQUEST);
			}
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e.getMessage(), e);
			resp.resultCodeToString(ResultCode.INTERNAL_SERVER_ERROR);
		}	
		return resp;
	}
}

