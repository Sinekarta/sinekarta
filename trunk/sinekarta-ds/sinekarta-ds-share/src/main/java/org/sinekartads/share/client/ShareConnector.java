package org.sinekartads.share.client;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sinekartads.model.client.SmartCardClient.SmartCardStatus;

public class ShareConnector {
	
	public static enum Fields {
		SESSION_ID,
		PROPERTY,
		VALUE,
	}

	public static enum Property {
		STATUS,
		PIN,
		ALIASES,
		ALIAS,
		CHAIN,
		SIGN_ALGO,
		DIGEST,
		DIG_SIGN
	}
	
	// -----
	// --- Singleton protocol
	// -
	
	public static ShareConnector singleton;
	
	public static ShareConnector getInstance() {
		return singleton;
	}
	
	private ShareConnector() { }
	
	
	
	// -----
	// --- Share Connector implementation
	// -
	
	Map<String, JavaScriptConnector> jsConnectors = new HashMap<String, JavaScriptConnector>();
	ShareClientFactory clientFactory = ShareClientFactory.getInstance();
	ObjectMapper mapper = new ObjectMapper();
	
	public void sendToJavascript ( Map<String, String> datagram ) throws IOException {
		String sessionId = (String)datagram.get ( Fields.SESSION_ID.name() );
		Property property = Property.valueOf( (String)datagram.get(Fields.PROPERTY) );
		JavaScriptConnector connector = jsConnectors.get ( sessionId );
		connector.sendToJavaScript(datagram);
		switch ( property ) {
			case PIN: {
				break;
			}
			case ALIAS: {
				break;
			}
			case SIGN_ALGO: {
				break;
			}
			case DIGEST: {
				break;
			}
			default: {
				 throw new IllegalArgumentException(String.format ( "unexpected outcoming property - %s", property ));
			}
		}
	}
	
	public void sendToShare ( Map<String, String> datagram ) {
		try {
			String sessionId = sessionId ( datagram );
			Property property = property( datagram );
			String value = value ( datagram );
			ShareSmartCardClient client = (ShareSmartCardClient) clientFactory.getSmartCardClient ( sessionId );
			switch ( property ) {
				case STATUS: {
					SmartCardStatus smartCardStatus = SmartCardStatus.valueOf ( value );
//					client.receiveSmartCardStatus(smartCardStatus);
					break;
				}
				case ALIASES: {
					String[] aliases = mapper.readValue(value, String[].class);
//					client.receiveAliases(aliases);
					break;
				}
				case CHAIN: {
					X509Certificate[] untrustedChain = mapper.readValue(value, X509Certificate[].class);
//					client.receiveUntrustedChain(untrustedChain);
					break;
				}
				case DIG_SIGN: {
					break;
				}
				default: {
					 throw new IllegalArgumentException(String.format ( "unexpected incoming property - %s", property ));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public JavaScriptConnector createJsConnector ( ) {
		return new JavaScriptConnector();
	}
	
	private String sessionId ( Map<String, String> datagram ) {
		return (String) datagram.get ( Fields.SESSION_ID.name() );
	}
	
	private Property property ( Map<String, String> datagram ) {
		String property = (String) datagram.get(Fields.PROPERTY.name());
		return Property.valueOf( property );
	}
	
	private String value ( Map<String, String> datagram ) {
		return (String) datagram.get ( Fields.VALUE.name() );
	}
	
	
	
	// -----
	// --- JavaScript Connector implementation
	// -
	
	public class JavaScriptConnector {

		public void sendToShare ( String jsonBuffer ) 
				throws JsonParseException, JsonMappingException, IOException {
			
			Map<String, String> datagram = mapper.readValue(jsonBuffer, 
					    new TypeReference<HashMap<String,String>>() { });
			ShareConnector.this.sendToShare(datagram);
		}
		
		public void sendToJavaScript ( Map<String, String> datagram ) {
			
		}
	}		
}
