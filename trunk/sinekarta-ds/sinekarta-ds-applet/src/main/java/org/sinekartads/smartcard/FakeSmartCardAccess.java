/*
 * Copyright (C) 2014 - 2015 Jenia Software.
 *
 * This file is part of Sinekarta-ds
 *
 * Sinekarta-ds is Open SOurce Software: you can redistribute it and/or modify
 * it under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sinekartads.smartcard;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartads.exception.CertificateListException;
import org.sinekartads.exception.InvalidPKCS11DriverException;
import org.sinekartads.exception.InvalidPinException;
import org.sinekartads.exception.InvalidSmartCardException;
import org.sinekartads.exception.PKCS11DriverNotFoundException;
import org.sinekartads.exception.PinLockedException;
import org.sinekartads.exception.SmartCardAccessException;
import org.sinekartads.exception.SmartCardReaderNotFoundException;
import org.sinekartads.utils.X509Utils;

public class FakeSmartCardAccess implements ISmartCardAccess {

	private static final Logger tracer = Logger.getLogger(SmartCardAccess.class);
	public static String FAKE_DRIVER = "fake";
	public static String FAKE_PIN	 = "123";
	
	Map<String, String> privateKeys = new HashMap<String, String>();
	Map<String, String> certChains  = new HashMap<String, String>();
	PrivateKey privateKey;

	public FakeSmartCardAccess ( ) {
		privateKeys.put("SineKarta", 			"30820277020100300d06092a864886f70d0101010500048202613082025d02010002818100e8394266b2aea68801cf3d149b95ac03ebb21b73e2f268efc60a6e50539b43b0fcc1725b67d361b50e23c4ec60c16cb4dcd2c314fddbc510d660de615f8911ea1803719aac42deda6e765e0acedaa021bf5f04cde948d41965b0ac130559205adcdf3eb442baa07cdd717794e19d9722055761a2bd6af0db72eaabb63e393cab020301000102818100d1f15a3fa050d70b649f67f8268494c3e2a4ddbed231177f960a3e1aeecb12d2825f9d3457d439a447093d71fa334444ead2bc3d51180bbc2223e7481ee7bbc1af1ca2be2b43501a78655fddc816b7e001cf44339a2dff4e47966ff3166c2ba068fec1d476aad63c1e51b996412844494718bc416809f8fdffc43e77c6598e91024100fc73e7ca5922bc8e47adbf5f6391f56030b3bd9836d971755cb31f646b4e5341c82873a1c7f4351d87fc32ea747b19f7e6e97582c01d420d96d4218426c59d89024100eb7c9691c827bca603235f2531fadd0be48f5db793b996766e19fa4d2d07529fb55aba3dee636b25dd8ff41d3660294f35f91c02ddc9494d1ea8217823eecf93024100c925a8d827716fff85c940a44677b3a532e8e3f5f62e0722d5fbbad58e5258301fc56b6cca1f207b29309f7903da59f4963e09ed661969d9de05a3b6e215aed1024040e2c17245de7d229936c3deb1d8d7d39114d7d3df8681a8fc4978288fc1b6c87ee612ef41a26f41adbd1e9c76012520c8546d9d749323f975fb09cddac8d0b502400b967f0a7a1af98b12df44bd97d4dfef2b472134831294866b9180d4bffed69dce99da160d0880fef51b07320a518f9e6807a8136fd869041763367450500722" );
		certChains.put("SineKarta", 			"308202f8308201e0a0030201020206014b03700673300d06092a864886f70d01010b0500307e310b3009060355040613024954310b300906035504081302424f311c301a06035504071313436173616c65636368696f2064692052656e6f31173015060355040a130e4a656e696120536f66747761726531123010060355040b130953696e654b61727461311730150603550403130e4a656e696120536f667477617265301e170d3135303131393138323430385a170d3235303131363138323430385a306c3112301006035504030c0953696e654b6172746131123010060355040b0c0953696e654b6172746131173015060355040a0c0e4a656e696120536f667477617265311c301a06035504070c13436173616c65636368696f2064692052656e6f310b300906035504061302495430819f300d06092a864886f70d010101050003818d0030818902818100e8394266b2aea68801cf3d149b95ac03ebb21b73e2f268efc60a6e50539b43b0fcc1725b67d361b50e23c4ec60c16cb4dcd2c314fddbc510d660de615f8911ea1803719aac42deda6e765e0acedaa021bf5f04cde948d41965b0ac130559205adcdf3eb442baa07cdd717794e19d9722055761a2bd6af0db72eaabb63e393cab0203010001a3123010300e0603551d0f0101ff040403020640300d06092a864886f70d01010b050003820101009633ba546d9d03129212fba04859e65ec67b47e902c7184917040ea7ed8516113ea298b1b590302ebf406f576f8002d5a4df174ae89d03cdc43ada1bd015da0b3e9ecf509b731998211f72c2544949d874e777061d0c0405c9df97626b1dfc93a8b0b204f6ac3e137a271522c5da53a37e58fefffc485e7099aedc96a8f7ca3f2161f386e28158cf8a3c3d1591e0edffd916a73a2b488d5bde78bbc2c2d30f57a9fbde02d513763af014e5d793354c7266a6a8a2a334aa9a952698a36258c3b6266382309caf3323ff3add9b3ee54156a521627862037611b4eae757d1e6d7040291c13bd23d6c1b92902463cc209004059c03995d2b032f2d23fa5addb6cc3e" );
	}

	public void selectDriver ( String pkcs11Driver ) throws SmartCardReaderNotFoundException, PKCS11DriverNotFoundException, InvalidPKCS11DriverException, InvalidSmartCardException, SmartCardAccessException {
		if ( !StringUtils.equals(pkcs11Driver, FakeSmartCardAccess.FAKE_DRIVER) ) { 
			tracer.error("only \"fake\" driver is allowed");
			throw new InvalidPKCS11DriverException("only \"fake\" driver is allowed");
		}
	}
	
	public String[] loginAndCertificateList ( String pin ) throws IllegalStateException, InvalidPinException, PinLockedException, SmartCardAccessException {
		login(pin);
		return certificateList();
	}
	
	public void login(String pin) throws IllegalStateException, SmartCardAccessException {
		if ( !StringUtils.equals(pin, FakeSmartCardAccess.FAKE_PIN) ) { 
			tracer.error(String.format("pin for the fake smartCard: %s", FAKE_PIN));
			throw new InvalidPinException(String.format("pin for the fake smartCard: %s", FAKE_PIN));
		}
	}

	public String[] certificateList() throws IllegalStateException, SmartCardAccessException {
		return certChains.keySet().toArray(new String[certChains.size()]);
	}

	public X509Certificate selectCertificate(String userAlias) throws CertificateListException {
		
		try {
			privateKey = X509Utils.privateKeyFromHex ( privateKeys.get(userAlias) );
			return X509Utils.rawX509CertificateFromHex ( certChains.get(userAlias) );
		} catch (CertificateException e) {
			tracer.error(String.format("pin for the fake smartCard: %s", FAKE_PIN));
			throw new CertificateListException(e); 
		}
	}

	public byte[] signFingerPrint(byte[] fingerPrint) throws IllegalStateException, IllegalArgumentException, SmartCardAccessException {
		
		byte[] digitalSignature;
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			signature.update(fingerPrint);
			digitalSignature = signature.sign();
		} catch(Exception e) {
			tracer.error("sign fails...",e);
			throw new SmartCardAccessException(e);
		}
		return digitalSignature;
	}

	public void logout() throws IllegalStateException,SmartCardAccessException {
	}
	
	public void open() throws SmartCardAccessException {
		// nothing to do by now
	}

	public void close() throws SmartCardAccessException {
		// nothing to do by now
	}

}
