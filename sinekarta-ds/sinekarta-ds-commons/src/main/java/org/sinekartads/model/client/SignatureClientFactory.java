package org.sinekartads.model.client;

import java.util.HashMap;
import java.util.Map;

import org.sinekartads.model.client.KeyStoreClient.KeyStoreClientCtrl;
import org.sinekartads.model.client.SignatureClient.SignatureClientCtrl;
import org.sinekartads.model.client.SignatureClient.SignatureClientType;
import org.sinekartads.model.client.SmartCardClient.SmartCardClientCtrl;

public abstract class SignatureClientFactory {
	
	// -----
	// --- Singleton protocol
	// -
	
	private static SignatureClientFactory singleton;
	
	public static SignatureClientFactory getInstance() {
		if ( singleton == null ) {
			throw new NullPointerException ( "missing singleton instance, generate it by means of a subclass" );
		}
		return singleton;
	}
	
	protected SignatureClientFactory ( ) {
		if ( singleton != null ) {
			throw new UnsupportedOperationException ( "singleton protocol violation" );
		}
		singleton = this;
	}
	
	
	
	// -----
	// --- SignatureClient mapping
	// -
	
	protected Map<String, KeyStoreClient> keyStoreClients = new HashMap<String, KeyStoreClient>();
	protected Map<String, SmartCardClient> smartCardClients = new HashMap<String, SmartCardClient>();
	private ThreadLocal<String> sessionId = new ThreadLocal<String>();
	
	public void createSignatureClients ( String sessionId ) {
		
		this.sessionId.set ( sessionId );
		
		keyStoreClients.put	 ( sessionId, createKeyStoreClient ( sessionId ) );
		smartCardClients.put ( sessionId, createSmartCardClient ( sessionId ) );
	}
	
	public SignatureClientCtrl<?> getSignatureCtrl ( 
			String sessionId, 
			SignatureClientType clientType ) {
		
		SignatureClientCtrl<?> client;
		switch ( clientType ) {
			case KEYSTORE: {
				client = getKeyStoreCtrl(sessionId);
				break;
			}
			case SMARTCARD: {
				client = getSmartCardCtrl(sessionId);
				break;
			}
			default: {
				throw new UnsupportedOperationException(String.format ( "unsupported clientType - ", clientType ));
			}
		}
		
		return client;
	}
	
	public KeyStoreClientCtrl getKeyStoreCtrl ( String sessionId ) {
		
		this.sessionId.set ( sessionId );
		
		KeyStoreClient keyStoreClient = keyStoreClients.get(sessionId);
		if ( keyStoreClient == null ) {
			throw new IllegalStateException(String.format ( 
					"no keyStoreClient has been found for the sessioneId %s, " +
					"it might have been not generated yet, or have been already finalized", sessionId ));
		}
		return keyStoreClient.getController();
	}
	
	public SmartCardClientCtrl getSmartCardCtrl ( String sessionId ) {
		
		this.sessionId.set ( sessionId );
		
		SmartCardClient smartCardClient = smartCardClients.get(sessionId);
		if ( smartCardClient == null ) {
			throw new IllegalStateException(String.format ( 
					"no smartCardClient has been found for the sessioneId %s, " +
					"it might have been not generated yet, or have been already finalized", sessionId ));
		}
		return smartCardClient.getController();
	}
	
	
	public void destroySignatureClients ( 
			String sessionId ) 
					throws DigitalSignatureException {
		
		this.sessionId.set ( sessionId );
		SignatureClient client;
		
		client = keyStoreClients.get ( sessionId );
		keyStoreClients.remove ( sessionId );
		client.finalize();
		
		client = smartCardClients.get ( sessionId );
		smartCardClients.remove ( sessionId );
		client.finalize();
	}
	
	protected abstract KeyStoreClient createKeyStoreClient ( String sessionId ) ;
	
	protected abstract SmartCardClient createSmartCardClient ( String sessionId ) ;
}
