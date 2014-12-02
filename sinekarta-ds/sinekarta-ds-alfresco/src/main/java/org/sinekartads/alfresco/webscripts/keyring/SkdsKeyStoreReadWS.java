package org.sinekartads.alfresco.webscripts.keyring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;
import org.sinekartads.alfresco.webscripts.BaseAlfrescoWS;
import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.domain.KeyStoreDTO;
import org.sinekartads.dto.request.SkdsKeyStoreRequest.SkdsKeyStoreReadRequest;
import org.sinekartads.dto.response.SkdsKeyStoreResponse.SkdsKeyStoreReadResponse;
import org.sinekartads.model.domain.KeyStoreType;
import org.sinekartads.util.TemplateUtils;
import org.sinekartads.util.x509.X509Utils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.util.Assert;

public class SkdsKeyStoreReadWS 
		extends BaseAlfrescoWS<SkdsKeyStoreReadRequest, SkdsKeyStoreReadResponse> {

	static final Logger tracer = Logger.getLogger(SkdsKeyStoreReadWS.class);
	
	@Override
	protected SkdsKeyStoreReadResponse executeImpl (
			SkdsKeyStoreReadRequest req,
			Status status, Cache cache ) {
		
		SkdsKeyStoreReadResponse resp = new SkdsKeyStoreReadResponse();
		KeyStoreDTO ksDTO = req.getKeyStore();
		String userAlias = req.getUserAlias();
//		String userPassword = req.getUserPassword();
		
		try {
			Assert.notNull ( ksDTO );
			Assert.isTrue ( StringUtils.isNotBlank(userAlias) );
			
			NodeRef keyStoreRef = new NodeRef ( ksDTO.getReference() );
			ContentReader reader = contentService.getReader(keyStoreRef, ContentModel.PROP_CONTENT);
			InputStream is = reader.getContentInputStream ( );
			byte[] keyStoreBytes = IOUtils.toByteArray ( is );
			IOUtils.closeQuietly(is);
			
			String keyStorePin = ksDTO.getPin();
			char[] ksPwd = null;
			if ( StringUtils.isNotBlank(keyStorePin) ) {
				ksPwd = keyStorePin.toCharArray();
			}
//			char[] userPwd = null;
//			if ( StringUtils.isNotBlank(userPassword) ) {
//				userPwd = userPassword.toCharArray();
//			}
			
			is = new ByteArrayInputStream ( keyStoreBytes );
			KeyStoreType type = KeyStoreType.getInstance(ksDTO.getType());
			KeyStore keyStore = KeyStore.getInstance ( type.getType(), type.getProvider() );
			keyStore.load ( is, ksPwd );
			
			KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
					keyStore.getEntry(userAlias, new PasswordProtection(ksPwd));
			X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
			X509Certificate[] certificateChain = TemplateUtils.Cast.cast ( 
					X509Certificate.class, privateKeyEntry.getCertificateChain() );
			PrivateKey privateKey = privateKeyEntry.getPrivateKey();
			
			resp.setCertificate ( X509Utils.rawX509CertificateToHex(certificate) );
			resp.setCertificateChain ( X509Utils.rawX509CertificatesToHex(certificateChain) );
			resp.setPrivateKey ( X509Utils.privateKeyToHex(privateKey) );
			resp.resultCodeToString(ResultCode.SUCCESS);
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e.getMessage(), e);
			resp.resultCodeToString(ResultCode.INTERNAL_SERVER_ERROR);
		} 
		
		return resp;
	}
	
}
