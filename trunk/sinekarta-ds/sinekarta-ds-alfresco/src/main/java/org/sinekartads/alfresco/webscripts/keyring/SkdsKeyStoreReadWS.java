package org.sinekartads.alfresco.webscripts.keyring;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.alfresco.webscripts.BaseAlfrescoWS;
import org.sinekartads.dto.ResultCode;
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
		String ksRef = req.getKsRef();
		String ksPin = req.getKsPin();
		String userAlias = req.getUserAlias();
//		String userPassword = req.getUserPassword();
		
		
		Assert.isTrue ( StringUtils.isNotBlank(ksRef) );
		Assert.isTrue ( StringUtils.isNotBlank(ksRef) );
		Assert.isTrue ( StringUtils.isNotBlank(ksPin) );
		Assert.isTrue ( StringUtils.isNotBlank(userAlias) );
			
		
		byte[] keyStoreBytes = null;
		InputStream is = null;
		try {
			NodeRef keyStoreRef = new NodeRef ( ksRef );
			ContentReader reader = contentService.getReader(keyStoreRef, ContentModel.PROP_CONTENT);
			is = reader.getContentInputStream ( );
			keyStoreBytes = IOUtils.toByteArray ( is );
		} catch(Exception e) {
			keyStoreBytes = null;
			processError(resp, e, "skds.error.missingKeyStore");
		} finally {
			IOUtils.closeQuietly(is);
		}

		String keyStorePin = req.getKsPin();
		KeyStore keyStore = null;
		char[] ksPwd = null;
		if ( keyStoreBytes != null ) {
			try {
				if ( StringUtils.isNotBlank(keyStorePin) ) {
					ksPwd = keyStorePin.toCharArray();
				}
				is = new ByteArrayInputStream ( keyStoreBytes );
				keyStore = KeyStore.getInstance ( "jks" );
				keyStore.load ( is, ksPwd );
			} catch(Exception e) {
				if ( e.getCause() instanceof UnrecoverableKeyException ) {
					processError(resp, e, "skds.error.keyStorePinWrong");
				} else {
					processError(resp, e, "skds.error.keyStoreUnavailable");
				}
				keyStore = null;
			}
		}
		
		String[] hexCertificateChain = null;
		String hexPrivateKey = null;
		if ( keyStore != null ) {
			try {
				KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) 
						keyStore.getEntry(userAlias, new PasswordProtection(ksPwd));
				X509Certificate[] certificateChain = TemplateUtils.Cast.cast ( 
						X509Certificate.class, privateKeyEntry.getCertificateChain() );
				PrivateKey privateKey = privateKeyEntry.getPrivateKey();
				
				hexCertificateChain = X509Utils.rawX509CertificatesToHex(certificateChain);
				hexPrivateKey = X509Utils.privateKeyToHex(privateKey);
				resp.resultCodeToString(ResultCode.SUCCESS);
			} catch ( Exception e ) {
				processError(resp, e, "skds.error.keyStoreUserNotFound");
			}
		}
		resp.setCertificateChain ( hexCertificateChain );
		resp.setPrivateKey ( hexPrivateKey );
		return resp;
	}
	
}
