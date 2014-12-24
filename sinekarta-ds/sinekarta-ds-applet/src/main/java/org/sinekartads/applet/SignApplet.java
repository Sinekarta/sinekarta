package org.sinekartads.applet;

import java.applet.Applet;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.smartcard.FakeSmartCardAccess;
import org.sinekartads.smartcard.InvalidPKCS11DriverException;
import org.sinekartads.smartcard.InvalidPinException;
import org.sinekartads.smartcard.InvalidSmartCardException;
import org.sinekartads.smartcard.PKCS11DriverNotFoundException;
import org.sinekartads.smartcard.PinLockedException;
import org.sinekartads.smartcard.SmartCardAccess;
import org.sinekartads.smartcard.SmartCardAccessException;
import org.sinekartads.smartcard.SmartCardReaderNotFoundException;
import org.sinekartads.smartcard.SmartCardUtils;
import org.sinekartads.utils.HexUtils;
import org.sinekartads.utils.JSONUtils;
import org.sinekartads.utils.X509Utils;

public class SignApplet extends Applet {

	private static final long serialVersionUID = -2886113966359858032L;
	private static final Logger tracer = Logger.getLogger(SignApplet.class);
	
	String driver;
	String pin;
	String alias;
	
	@Override
	public void init ( ) {
		tracer.info("Initializing the signing applet.");
	}
	
	public String selectDriver ( String driver ) {
		driver = StringUtils.trim(driver);
		tracer.info("selectDriver");
		tracer.info(String.format("driver: %s", driver));
		
		AppletResponseDTO resp = new AppletResponseDTO ( "selectDriver" );
		SmartCardAccess sca = createSmartCardAccess ( driver );
		try {
			if ( verifyDriver(resp, sca, driver) ) {
				resp.setResult(this.driver);
			}
		} finally {
			SmartCardUtils.finalizeQuietly ( sca );
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON ( resp )));
		return JSONUtils.serializeJSON ( resp );
	}
	
	public String login ( String pin ) {
		pin = StringUtils.trim(pin);
		tracer.info("login");
		tracer.info(String.format("driver: %s", driver));
		tracer.info(String.format("pin:    %s", pin));
		
		AppletResponseDTO resp = new AppletResponseDTO ( "login" );
		SmartCardAccess sca = createSmartCardAccess ( driver );
		try {
			if ( verifyDriver(resp, sca, driver) ) {
				String[] aliases = loginWithPin ( resp, sca, pin );
				if ( ArrayUtils.isNotEmpty(aliases) ) {
					String aliasesJSON = JSONUtils.serializeJSON ( aliases );
					resp.setResult ( aliasesJSON );
				}
			}
		} finally {
			SmartCardUtils.finalizeQuietly ( sca );
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON(resp)));
		return JSONUtils.serializeJSON ( resp );
	}
	
	public String selectCertificate ( String alias ) {
		alias = StringUtils.trim(alias);
		tracer.info("selectCertificate");
		tracer.info(String.format("driver: %s", driver));
		tracer.info(String.format("pin:    %s", pin));
		tracer.info(String.format("alias:  %s", alias));
		
		AppletResponseDTO resp = new AppletResponseDTO ( "selectCertificate" );
		SmartCardAccess sca = createSmartCardAccess ( driver );
		try {
			X509Certificate signingCertificate = loginWithAlias(resp, sca, pin, alias);
			if ( signingCertificate != null ) {
				resp.setResult ( X509Utils.rawX509CertificateToHex(signingCertificate) );
			}
		} catch(CertificateException e) {
			// never thrown - the certificate stored into the smartCard is expected to be corrected 
			processError ( resp, "impossibile reperire il certificato per l'identità %s", e );
		} catch(Exception e) {
			processError ( resp, "impossibile reperire il certificato per l'identità %s", e );
		} finally {
			SmartCardUtils.finalizeQuietly ( sca );
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON( resp )));
		return JSONUtils.serializeJSON(resp);
	}
	
	public String signDigest ( String hexDigest ) {
		hexDigest = StringUtils.trim(hexDigest);
		tracer.info("signDigest");
		tracer.info(String.format("driver: %s", driver));
		tracer.info(String.format("pin:    %s", pin));
		tracer.info(String.format("alias:  %s", alias));
		
		AppletResponseDTO resp = new AppletResponseDTO ( "signDigest" );
		SmartCardAccess sca = createSmartCardAccess ( driver );
		try {
			if ( loginWithAlias ( resp, sca, pin, alias ) != null ) {
				byte[] digest = HexUtils.decodeHex(hexDigest);
				byte[] digitalSignature = sca.signFingerPrint(digest);
				resp.setResult ( HexUtils.encodeHex(digitalSignature) );
			}
		} catch (Exception e) {
			processError ( resp, "errore rilevato nell'applicazione della firma digitale da SmartCard", e);
		} finally {
			SmartCardUtils.finalizeQuietly ( sca );
		}
		
		tracer.info(String.format("respJSON: %s", JSONUtils.serializeJSON ( resp )));
		return JSONUtils.serializeJSON(resp);
	}
	
	public void destroy() {
		tracer.info("signing applet destroyed");
	}
	
	
	private boolean verifyDriver (
			AppletResponseDTO resp,
			SmartCardAccess sca,
			String driver ) {
		
		boolean validDriver = false;
		try {
			tracer.info(String.format("verifying driver %s with %s", driver, sca));
			tracer.info(String.format("selectDriver - %s", driver));
			sca.selectDriver ( driver );
			validDriver = true;
		} catch (SmartCardReaderNotFoundException e) {
			tracer.error("Impossibile trovare il lettore di SmartCard", e);
			resp.addActionError("Impossibile trovare il lettore di SmartCard", e);
		} catch (InvalidSmartCardException e) {
			tracer.error(e.getMessage(), e);
			resp.addActionError("SmartCard non riconosciuta", e);
		} catch (PKCS11DriverNotFoundException e) {
			tracer.error(e.getMessage(), e);
			resp.addFieldError("scDriver", "Driver SmartCard non trovato");
		} catch (InvalidPKCS11DriverException e) {
			tracer.error(e.getMessage(), e);
			resp.addFieldError("scDriver", "Driver SmartCard non riconosciuto");
		} catch (SmartCardAccessException e) {
			tracer.error(e.getMessage(), e);
			resp.addActionError("Error SmartCard", e);
		}
		
		this.driver = driver;
		return validDriver;
	}
	
	private String[] loginWithPin ( 
			AppletResponseDTO resp,
			SmartCardAccess sca,
			String pin ) {

		String[] aliases = null;
		if ( verifyDriver(resp, sca, driver) ) {
			boolean validPin = false;
			tracer.info(String.format("loginWithPin"));
			tracer.info(String.format("pin:    %s", pin));
			try {
				aliases = sca.login ( pin );
				validPin = true;
			} catch (InvalidPinException e) {
				tracer.error("pin non riconosciuto", e);
				if ( StringUtils.equals(driver, FakeSmartCardAccess.FAKE_DRIVER) ) {
					resp.addFieldError("scPin", String.format("il pin della smartCard di simulazione è: \"%s\"", FakeSmartCardAccess.FAKE_PIN));
				} else {
					resp.addFieldError("scPin", "pin non riconosciuto");
				}
			} catch (PinLockedException e) {
				tracer.error("pin bloccato", e);
				resp.addFieldError("scPin", "pin bloccato");
			} catch (SmartCardReaderNotFoundException e) {
				tracer.error("lettore SmartCard non trovato", e);
				resp.addFieldError("scPin", "lettore SmartCard non trovato");
			} catch (Exception e) {
				tracer.error("login fallito a causa di un errore interno", e);
				resp.addFieldError("scPin", "login fallito a causa di un errore interno");
			}
			if ( validPin ) {
				this.pin = pin;
			} else {
				this.pin = null;
			}
		}
		return aliases;
	}
	
	private X509Certificate loginWithAlias ( 
			AppletResponseDTO resp,
			SmartCardAccess sca,
			String pin,
			String alias ) {
		
		boolean validAlias = false;
		X509Certificate signingCertificate = null;
		String[] aliases = loginWithPin ( resp, sca, pin );
		if ( ArrayUtils.isNotEmpty(aliases) ) {
			if ( ArrayUtils.contains(aliases, alias) ) {
				try {
					signingCertificate = sca.selectCertificate ( alias );
					validAlias = true;
				} catch(Exception e) {
					String errorMessage = String.format("impossibile reperire il certificato per l'identità %s", alias);
					processError ( resp, errorMessage, e );
				}
			} else {
				resp.addFieldError("scUserAlias", "alias non riconosciuto");
			}
		}
		
		if ( validAlias) {
			this.alias = alias;
		} else {
			this.alias = null;
		}
		
		return signingCertificate;
	}

	
	
	
	
	
	// -----
	// --- Utility protocol
	// -
	
	private SmartCardAccess createSmartCardAccess ( String driver ) {
		SmartCardAccess sca;
		if ( StringUtils.equals(driver, FakeSmartCardAccess.FAKE_DRIVER) ) {
			sca = new FakeSmartCardAccess ( );
		} else {
			sca = new SmartCardAccess ( );
		}
		return sca;
	}
	
	private void processError ( AppletResponseDTO resp, String errorMessage, Exception errorCause ) {
		if ( StringUtils.isBlank(errorMessage) ) {
			errorMessage = errorCause.getClass().getName();
		}
		resp.addActionError(errorMessage, errorCause);
		tracer.error(errorMessage, errorCause);
	}
}