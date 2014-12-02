package org.sinekartads.share.client;

import org.sinekartads.model.client.KeyStoreClient;
import org.sinekartads.model.client.SignatureClientFactory;
import org.sinekartads.model.client.SmartCardClient;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public class ShareClientFactory extends SignatureClientFactory {

	private ShareClientFactory ( ) { }
	
	ConnectorService connectorService;
	
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	
	
	// -----
	// --- Singleton protocol
	// -
	
	private static ShareClientFactory singleton;
	
	public static ShareClientFactory getInstance() {
		if ( singleton == null ) {
			singleton = new ShareClientFactory();
		}
		return singleton;
	}

	
	
	// -----
	// --- SignatureClientFactory implementation
	// -
	
	@Override
	protected KeyStoreClient createKeyStoreClient ( String sessionId ) {
		ShareKeyStoreClient keyStoreClient = new ShareKeyStoreClient ( sessionId ); 
		keyStoreClient.setConnectorService(connectorService);
		return keyStoreClient;
	}

	@Override
	protected SmartCardClient createSmartCardClient ( String sessionId ) {
		ShareSmartCardClient smartCardClient = new ShareSmartCardClient ( sessionId ); 
		return smartCardClient;
	}

	
	
	// -----
	// --- Package-restricted direct SmartClientClient access 
	// -

	public SmartCardClient getSmartCardClient ( String sessionId ) {
		
		SmartCardClient smartCardClient = smartCardClients.get(sessionId);
		if ( smartCardClient == null ) {
			throw new IllegalStateException(String.format ( 
					"no smartCardClient has been found for the sessioneId %s, " +
					"it might have been not generated yet, or have been already finalized", sessionId ));
		}
		return smartCardClient;
	}
	
}
