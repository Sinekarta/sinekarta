package org.sinekartads.share.client;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.sinekartads.model.client.DigitalSignatureException;
import org.sinekartads.model.client.SmartCardClient;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.oid.SignatureAlgorithm;
import org.sinekartads.share.client.ShareConnector.Property;

public class ShareSmartCardClient extends SmartCardClient {

	public ShareSmartCardClient ( String sessionId ) {
		super ( sessionId );
	}
	
	private ShareConnector connector = ShareConnector.getInstance();  
	
	private void send ( ShareConnector.Property property, String value ) {
		try {
			Map<String, String> datagram = new HashMap<String, String>();
			datagram.put ( ShareConnector.Fields.SESSION_ID.name(), sessionId );
			datagram.put ( property.name(), value );
			connector.sendToJavascript(datagram);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void login ( String smartCardPin ) {
		send ( Property.PIN, smartCardPin );
	}


	@Override
	protected String[] selectIdentity(String alias, String password) {
		send ( Property.ALIAS, alias );
		// TODO method body not implemented yet
		throw new UnsupportedOperationException ( "method body not implemented yet" );
	}

	@Override
	protected byte[] doSign(SignatureAlgorithm sigAlgorithm,
			DigestInfo digestInfo) throws DigitalSignatureException {
		// TODO method body not implemented yet
		throw new UnsupportedOperationException ( "method body not implemented yet" );
	}

	@Override
	public void receiveSmartCardStatus(SmartCardStatus smartCardStatus) {
		this.smartCardStatus = smartCardStatus;
	}
	
	@Override
	public void receiveAliases ( String[] aliases ) {
		this.aliases = aliases;
	}
	
	@Override
	public void receiveUntrustedChain ( X509Certificate[] untrustedChain ) {
		this.untrustedChain = untrustedChain;
	}

	@Override
	protected void loadAliases() {
		// TODO method body not implemented yet
		throw new UnsupportedOperationException ( "method body not implemented yet" );
	}


	
}
