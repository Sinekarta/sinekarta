package org.sinekartads.applet;

import java.applet.Applet;
import java.security.cert.X509Certificate;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.smartcard.SmartCardAccess;
import org.sinekartads.smartcard.SmartCardUtils;
import org.sinekartads.utils.HexUtils;
import org.sinekartads.utils.JSONUtils;
import org.sinekartads.utils.X509Utils;

public class SignApplet extends Applet {

	private static final long serialVersionUID = -2886113966359858032L;
	private static final Logger tracer = Logger.getLogger(SignApplet.class);
	
	SmartCardAccess sca;
	String[] matchingDrivers;
	
	@Override
	public void init ( ) {
		tracer.info("mandi mandi");
		sca = new SmartCardAccess ( );
	}
	
	public String verifySmartCard ( String knownDriversJSON ) {
		tracer.info("verifySmartCard");
		tracer.info(String.format("knownDriversJSON: %s", knownDriversJSON));
		
		AppletResponseDTO resp = new AppletResponseDTO ( );
		try {
			String[] knownDrivers = (String[]) JSONUtils.fromJSONArray(String[].class, knownDriversJSON);
			matchingDrivers = SmartCardUtils.detectDrivers(knownDrivers);
			String driver = matchingDrivers[0];
			sca.selectDriver ( driver );
			resp.setResult ( JSONUtils.toJSONArray(matchingDrivers) );
			resp.setResultCode(AppletResponseDTO.SUCCESS);
		} catch(Exception e) {
			processError(resp, e);
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.toJSON ( resp )));
		return JSONUtils.toJSON ( resp );
	}
	
	public String selectDriver ( String driver ) {
		tracer.info("selectDriver");
		tracer.info(String.format("driver: %s", driver));
		
		AppletResponseDTO resp = new AppletResponseDTO ( );
		try {
//			boolean missing = true;
//			for ( int i=0; i<matchingDrivers.length && missing; i++ ) {
//				missing = StringUtils.equalsIgnoreCase(driver, matchingDrivers[i]);
//			}
//			if ( missing ) {
//				throw new DriverNotFoundException(String.format ( "indivalid driver", driver ));
//			}
			sca.selectDriver ( driver );
			resp.setResult(driver);
			resp.setResultCode(AppletResponseDTO.SUCCESS);
		} catch(Exception e) {
			processError ( resp, e );
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.toJSON ( resp )));
		return JSONUtils.toJSON ( resp );
	}
	
	public String login ( String pin ) {
		tracer.info("login");
		tracer.info(String.format("pin: %s", pin));
		
		AppletResponseDTO resp = new AppletResponseDTO ( );
		try {
			String[] aliases = sca.login ( pin );
			sca.logout();
			String aliasesJSON = JSONUtils.toJSONArray(aliases);
			resp.setResult ( aliasesJSON );
			resp.setResultCode(AppletResponseDTO.SUCCESS);
		} catch(Exception e) {
			processError ( resp, e );
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.toJSON ( resp )));
		return JSONUtils.toJSON ( resp );
	}
	
	public String selectCertificate(String alias) {
		tracer.info("selectCertificate");
		tracer.info(String.format("alias: %s", alias));
		
		AppletResponseDTO resp = new AppletResponseDTO ( );
		try {
			X509Certificate signingCertificate;
			sca.login();
			signingCertificate = sca.selectCertificate ( alias );
			sca.logout();
			resp.setResult ( X509Utils.rawX509CertificateToHex(signingCertificate) );
			resp.setResultCode(AppletResponseDTO.SUCCESS);
		} catch(Exception e) {
			processError(resp, e);
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.toJSON ( resp )));
		return JSONUtils.toJSON(resp);
	}
	
	public String signDigest(String digestEnc) {
		tracer.info("signDigest");
		tracer.info(String.format("digestEnc: %s", digestEnc));
		
		AppletResponseDTO resp = new AppletResponseDTO ( );
		try {
			byte[] digest = HexUtils.decodeHex(digestEnc);
			byte[] digitalSignature;
			sca.login();
			digitalSignature = sca.signFingerPrint(digest);
			sca.logout();
			resp.setResult ( HexUtils.encodeHex(digitalSignature) );
			resp.setResultCode(AppletResponseDTO.SUCCESS);
		} catch(Exception e) {
			processError(resp, e);
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.toJSON ( resp )));
		return JSONUtils.toJSON(resp);
	}
	
	public void destroy() {
		SmartCardUtils.finalizeQuietly ( sca );
	}
	
	
	// -----
	// --- Error management
	// -
	
	public void processError ( AppletResponseDTO resp, Exception e ) {
		String errorMessage = e.getMessage();
		if ( StringUtils.isBlank(errorMessage) ) {
			errorMessage = e.getClass().getName();
		}
		resp.setErrorMessage ( errorMessage );
		resp.setResultCode(AppletResponseDTO.ERROR);
		tracer.error(errorMessage, e);
	}
}