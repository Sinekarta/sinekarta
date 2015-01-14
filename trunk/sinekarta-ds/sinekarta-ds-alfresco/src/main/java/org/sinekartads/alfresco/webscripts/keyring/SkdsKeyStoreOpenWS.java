package org.sinekartads.alfresco.webscripts.keyring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinekartads.alfresco.webscripts.BaseAlfrescoWS;
import org.sinekartads.dto.domain.KeyStoreDTO;
import org.sinekartads.dto.request.SkdsKeyStoreRequest.SkdsKeyStoreOpenRequest;
import org.sinekartads.dto.response.SkdsKeyStoreResponse.SkdsKeyStoreOpenResponse;
import org.sinekartads.model.domain.KeyStoreType;
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

		InputStream is = null;
		SkdsKeyStoreOpenResponse resp = new SkdsKeyStoreOpenResponse();
		

		String keyStoreName = "sinekarta.jks";
		byte[] keyStoreBytes = null;
		NodeRef keyStoreRef = null;
		try {
			String userName = authenticationService.getCurrentUserName ( );
			NodeRef personNodeRef = personService.getPerson ( userName );
			NodeRef homespaceNodeRef = (NodeRef) nodeService.getProperty ( 
					personNodeRef, ContentModel.PROP_HOMEFOLDER );
			keyStoreRef = nodeService.getChildByName ( homespaceNodeRef, ContentModel.ASSOC_CONTAINS, keyStoreName );
			ContentReader reader = contentService.getReader(keyStoreRef, ContentModel.PROP_CONTENT);
			is = reader.getContentInputStream ( );
			keyStoreBytes = IOUtils.toByteArray ( is );
		} catch(Exception e) {
			keyStoreBytes = null;
			processError(resp, e, "skds.error.missingKeyStore");
		} finally {
			IOUtils.closeQuietly(is);
		}

		String keyStorePin = req.getKeyStorePin();
		KeyStore keyStore = null;
		KeyStoreType type = null;
		if ( keyStoreBytes != null ) {
			try {
				char[] ksPwd = null;
				if ( StringUtils.isNotBlank(keyStorePin) ) {
					ksPwd = keyStorePin.toCharArray();
				}
				KeyStoreType[] types = KeyStoreType.values(); 
				for ( int i=0; i<types.length && keyStore == null; i++ ) {
					is = new ByteArrayInputStream ( keyStoreBytes );
					type = types[i];
					keyStore = KeyStore.getInstance ( type.getType(), type.getProvider() );
					keyStore.load ( is, ksPwd );
				}
			} catch(Exception e) {
				if ( e.getCause() instanceof UnrecoverableKeyException ) {
					processError(resp, e, "skds.error.keyStorePinWrong");
				} else {
					processError(resp, e, "skds.error.keyStoreUnavailable");
				}
				keyStore = null;
			}
		}
			
		if ( keyStore != null ) {
			try {
				KeyStoreDTO ksDto = new KeyStoreDTO();
				if ( keyStore != null ) {
					ksDto.setName(keyStoreName);
					ksDto.setPin(keyStorePin);
					ksDto.setType(type.getType());
					ksDto.setProvider(type.getProvider());
					ksDto.setAliases ( TemplateUtils.Conversion.enumerationToArray(keyStore.aliases()) );
					ksDto.setReference(keyStoreRef.toString());
					resp.setKeyStore(ksDto);
				} 
			} catch(Exception e) {
				processError(resp, e, "skds.error.internalServerError");
			}
		}
		return resp;
	}
}

