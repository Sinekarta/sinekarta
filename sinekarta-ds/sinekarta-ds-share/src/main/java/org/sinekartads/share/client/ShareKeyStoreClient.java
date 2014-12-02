package org.sinekartads.share.client;

import java.security.cert.X509Certificate;

import org.sinekartads.dto.ResultCode;
import org.sinekartads.dto.request.SkdsKeyStoreRequest.SkdsKeyStoreOpenRequest;
import org.sinekartads.dto.request.SkdsKeyStoreRequest.SkdsKeyStoreReadRequest;
import org.sinekartads.dto.response.SkdsKeyStoreResponse.SkdsKeyStoreOpenResponse;
import org.sinekartads.dto.response.SkdsKeyStoreResponse.SkdsKeyStoreReadResponse;
import org.sinekartads.model.client.KeyStoreClient;
import org.sinekartads.model.oid.EncryptionAlgorithm;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.share.util.AlfrescoException;
import org.sinekartads.share.util.JavaWebscriptTools;
import org.sinekartads.util.x509.X509Utils;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public class ShareKeyStoreClient extends KeyStoreClient {

	public ShareKeyStoreClient ( String sessionId ) {
		super ( sessionId );
	}

	private ConnectorService connectorService;
	
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@Override
	protected String[] openKeyStore ( String keyStorePin ) 
			throws IllegalArgumentException {
		
		// Retrieve from the repository-tier the document details
		SkdsKeyStoreOpenRequest ksoreq = new SkdsKeyStoreOpenRequest();
		ksoreq.setKeyStorePin(keyStorePin);
		SkdsKeyStoreOpenResponse ksoresp;
		try {
			ksoresp = JavaWebscriptTools.postJsonRequest ( 
					ksoreq, SkdsKeyStoreOpenResponse.class, connectorService );
		} catch(AlfrescoException e) {
			throw new RuntimeException(e);
		}
		ResultCode resultCode = ResultCode.valueOf(ksoresp.getResultCode());
		switch ( resultCode ) {
			case SUCCESS: {
				keyStore = ksoresp.getKeyStore();
				break;
			}
			case BAD_REQUEST: {
				throw new IllegalArgumentException ( "invalid keystore structre or password" );
			}
			default: {
				throw new RuntimeException ( resultCode.name() );
			}
		}
		loadAliases();
		
		return aliases;
	}

	@Override
	protected String[] selectIdentity(String userAlias, String userPassword) {
		// Retrieve from the repository-tier the document details
		SkdsKeyStoreReadRequest ksrreq = new SkdsKeyStoreReadRequest();
		ksrreq.setKeyStore ( keyStore );
		ksrreq.setUserAlias(userAlias);
		ksrreq.setUserPassword(userPassword);
		SkdsKeyStoreReadResponse ksrresp;
		try {
			ksrresp = JavaWebscriptTools.postJsonRequest ( 
					ksrreq, SkdsKeyStoreReadResponse.class, connectorService );
		} catch(AlfrescoException e) {
			throw new RuntimeException(e);
		}
		ResultCode resultCode = ResultCode.valueOf(ksrresp.getResultCode());
		switch ( resultCode ) {
			case SUCCESS: {
				try {
					X509Certificate certificate = X509Utils.rawX509CertificateFromHex(ksrresp.getCertificate());
					EncryptionAlgorithm encAlgorithm = SignatureAlgorithm.getInstance ( 
							certificate.getSigAlgName() ).getEncryptionAlgorithm();
					certificateChain = ksrresp.getCertificateChain();
					privateKey = X509Utils.privateKeyFromHex(ksrresp.getPrivateKey(), encAlgorithm);
				} catch(Exception e) {
					throw new RuntimeException ( e ); 
				}
				break;
			}
			case BAD_REQUEST: {
				throw new IllegalArgumentException ( "invalid keystore structre or password" );
			}
			default: {
				throw new RuntimeException ( resultCode.name() );
			}
		}
		return certificateChain;
	}

}
