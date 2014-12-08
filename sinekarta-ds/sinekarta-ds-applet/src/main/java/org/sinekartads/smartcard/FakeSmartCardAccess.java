/*
 * Copyright (C) 2010 - 2012 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.sinekartads.smartcard;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sinekartads.utils.X509Utils;

public class FakeSmartCardAccess extends SmartCardAccess {

	public static String FAKE_DRIVER = "fake";
	public static String FAKE_PIN	 = "fake";
	
	Map<String, String> privateKeys = new HashMap<String, String>();
	Map<String, String> certChains  = new HashMap<String, String>();
	PrivateKey privateKey;

	public FakeSmartCardAccess ( ) {
		privateKeys.put("Roberto Colombari", 	"30820276020100300d06092a864886f70d0101010500048202603082025c02010002818100853c9950706ed4c495424cb1a739bab8639da5e4390e77c69d4de2e3d4ed1ceea2cf96cb784f7f9a0c7561ce471ed655e928ef56003e4dd8053bb3cafd83d665142529f249d29fdd730ec582a0320be028946f53e6a3603aebb8e68538837227c5922360790d8ffc1e5fec8e02592a1aafdeb938307516205a4f6d877f9e1c7d0203010001028180791347733ca8cabad5b449038ba63f52be5b8d5be6a98a18b7ec0649e9bd8b742409a6cbc1c9e477f5e85977dd535d8cf6739782bc77e1bf7389fc69739571a65af4279ecd2a8078e6ef9661a972f7eb233c10e03f735eb3fe4b48fa61a9a0a3c5f0ecb7e245a69331053a3e89b35b7b39b9bde9d4915fd00be10044a3beb281024100c1543dba4cce5af8a8f50b054a83343f412fc7cdcd46efdee7ec9c72fa06c059c689d6ed66ae50688a4985f8a8eb62ece5f3bb0a001c4e95b660ef60e71134dd024100b06d7c7b81ad598899de62bab7fbd731a4d799f3b3b82286297fd6a81d4f947a6f2b803b0a4d280ae86b3d7168cf39af741b2e60a90b7a1f774b9829aea4bc21024100b28903404ab1be9d281ab384bd5d1120e12828d24ba218deb73b70f755226afbfd3749fe8ef6a757036e0684ae2a427f1794cfc3da7a49b0446e9c61d6c1b31902405747428ec2df23ecccd9d413b4d2d4694db80f041d835928efbcbb4f5d78b1e643bacc6be8b3b4bc78b01cac4f023cf24c48ea0f8d710d1025eef2aea42400a1024001c63d53c6a22904e0bb08381a47510d4328ca8172392b104cee6afc476ebb46bd5945e4e445abcd223db96c03da57edde57219bc9839ff9ea7d32b657e2efac" );
		certChains.put("Roberto Colombari", 	"308201a43082010d020101300d06092a864886f70d01010b050030193117301506035504030c0e4a656e696120536f667477617265301e170d3134313132333030303030305a170d3135313132343030303030305a301c311a301806035504030c11526f626572746f20436f6c6f6d6261726930819f300d06092a864886f70d010101050003818d0030818902818100853c9950706ed4c495424cb1a739bab8639da5e4390e77c69d4de2e3d4ed1ceea2cf96cb784f7f9a0c7561ce471ed655e928ef56003e4dd8053bb3cafd83d665142529f249d29fdd730ec582a0320be028946f53e6a3603aebb8e68538837227c5922360790d8ffc1e5fec8e02592a1aafdeb938307516205a4f6d877f9e1c7d0203010001300d06092a864886f70d01010b0500038181007e07a03eed95d8dd621bac2468959217e38202eddb7188fc2e674ec24f59952e12a272badd64f6ed360dd701b8266a1c5e15d489dc7af9e2ba7ca3e6068312356e0d8ce9a252cf304da4901d574325e231b77d19974ba969098d9386dcb0dd20f015b643d18edb251253a92e5b2401e4b25fd9536a0d1276c6a496f6ff3f277a" );
	
		privateKeys.put("Andrea Tessaro Porta", "30820277020100300d06092a864886f70d0101010500048202613082025d02010002818100d9851f3e8a2fb8550b6e80380fa31fe9bd9f9ffbd2e5261997995f8249f1f0ccddc83dbf08200641afba303a6a627af8f6eb90ceea6be6e9e567ba3f1638616d595c8346fa485b3563b3a410706715bd9195a4101f10e8fa095ddf360b6522a8ad5d1274f45292af5b9e3a7b9a13e9b91aefd270065f1742c3051ee002f66cfd02030100010281802408112ce2e714b9c1b3043a451cd86477acc16b8d7bb7dbf568ca91627e594d164fdd2a8fc5fe4353409c4c90e2cf4bfe5af719cf6b204d0e5b2856daef6bf3caf370c45dbb2b30beaa8cf9a153a50e4e1dd2b04893b7c69a99198d6fb7662fbecd1218658fabf02c0e790427a628c4fa5a2a4e1e257d8b3138dc190ac99f61024100ed91a5e2d7e3060616ff8aac407a6551a1596527b410729b8a2d1f900726367dec0825ed3ecfae14babb6c74b700c5a5825de54b035e515ebcfa25124b48c3e7024100ea65485c6f6067f526b4f4e20ec942c4e836f1cb49e4b8e888a146919cc032be29bb26e214c7568d9c9434c7ac4fb02bab749356fe63eaa961d210f67e13ab7b024071be746041e10e7747f79ddb8f1b8afb24777ab921bb6644164a387c6b0fdeb33799471f6a1d6149c786090d70b94be84c9ccc35b82d266467b294a147ae552d024100a9863d32955f0d70f1e0b8a7f277bf71b85d5cbd61a129d70c52503eaa371bbc0b648f37be3d79ad0c0ce8ab6d45692dfc6060dbef3b069220ec945622534997024100ab9810bf6ef64d0b770f4b79d26297b14bfdc3aa0779e758882e5526a4125ca00eb0af80005f7af10ff00dafcc35d8d9525dfae7d2689222d4418330bf0f0ade" );
		certChains.put("Andrea Tessaro Porta", 	"308201a730820110020101300d06092a864886f70d01010b050030193117301506035504030c0e4a656e696120536f667477617265301e170d3134313132333030303030305a170d3135313132343030303030305a301f311d301b06035504030c14416e64726561205465737361726f20506f72746130819f300d06092a864886f70d010101050003818d0030818902818100d9851f3e8a2fb8550b6e80380fa31fe9bd9f9ffbd2e5261997995f8249f1f0ccddc83dbf08200641afba303a6a627af8f6eb90ceea6be6e9e567ba3f1638616d595c8346fa485b3563b3a410706715bd9195a4101f10e8fa095ddf360b6522a8ad5d1274f45292af5b9e3a7b9a13e9b91aefd270065f1742c3051ee002f66cfd0203010001300d06092a864886f70d01010b05000381810077809abcc495dc80b66fb913febdfea07c9b2cf763ed0c83a627e0bd925065e5fc8bccf7f4770023c0d43edf2078560f6ec5dc8c0a23acaa28c1058a8a04577b96215e854b1d1029f0d27953acc9e419dd0537e5fd8bce01707a7cc573598faa8c1d44c5075f778c1c10b960bf61d443091286a8fff927aee7168801ce6158aa" );
	
		privateKeys.put("Alessandro De Prato", 	"30820277020100300d06092a864886f70d0101010500048202613082025d02010002818100cffcb26feb77847d9146296f1aaf6997d6c33794e1849462a19ba0f1f17fcdcaa1bfc608c76500a5c13695f9c3561eb865e909e0e571071cf596a054b481df6b159e6ac81f2c3e25dab1e19d93a5879bd6ab8394120edd7c07bb8a328e37b4db4a20e0b5f483c16acfb4afcdccabd21b8f317eba03a0d958a59282622ba16e9b02030100010281803fe9ac3f9e4114f2ad30bd4cca0b7b4c508f9ec5de632a6200bd5d40dbf06521ce80aaef49aeacfb429b6e8cd8ccdf15d5233e88e098d211c11a2f5cb8376c9ae2461f138e04efa4e1194607d9a6309168338c753d7c056e51396c7286dba06027a00173138c85a449a6b3cc22adfb87f37cccc346a275013c54a1cf7a5d8841024100f938e5c0adf235d75a2ad2685f1ad51f29e4302a976f402c973d2aefd6e3b55f6d9984ab76973ba813081215c39755392c84b0da8d4e5e89f4dc18ab148a5be1024100d5a4b7b06ac6906663f9aa81295460a78de3cde43934bc6953220d31de8aa7b66fcdce7ba36a6a92a86639d871b2088c5089e51219b21d33e22cdefdd40c79fb024100be5de3c1861d195b62188b9805ed6d15dcdddc9cec07a2b16ce4e0434b95ac193492ab707da98f7c16ed774300a03c0cec207f26b20146dd82180d0fc124de81024100bfe80f0c8c4d42e686857cf03aa4cd9194b5fd7b351bb6e6d143342108aafa0db98e601cd564c0c86e362d5dcc7b448c6dc800196b35805fdb99bcd0baca5e4302401b4b232b347ea776173aecc3de918b559e2f3b8386ce62d08bbd1de21b56a59c98f88f2d4a414247a2de9af05311dc3e61c949bb941f6daebc6845ce23cee498" );
		certChains.put("Alessandro De Prato", 	"308201a63082010f020101300d06092a864886f70d01010b050030193117301506035504030c0e4a656e696120536f667477617265301e170d3134313132333030303030305a170d3135313132343030303030305a301e311c301a06035504030c13416c657373616e64726f20446520507261746f30819f300d06092a864886f70d010101050003818d0030818902818100cffcb26feb77847d9146296f1aaf6997d6c33794e1849462a19ba0f1f17fcdcaa1bfc608c76500a5c13695f9c3561eb865e909e0e571071cf596a054b481df6b159e6ac81f2c3e25dab1e19d93a5879bd6ab8394120edd7c07bb8a328e37b4db4a20e0b5f483c16acfb4afcdccabd21b8f317eba03a0d958a59282622ba16e9b0203010001300d06092a864886f70d01010b0500038181008a45ad41dd9996f62c7ad514e8075cde1e64de17371c33ac6870a55ac212124b809c8b708b0abfd422595d35a2249b3947f272ae1699000abc44df0509ae4ada526e570fdf7ada46d85fe651083c520059742a23adcb15773557deb254945654e736ee7111f6e78bbe37ef1272691b903395c306606ff3db5a38f83279e3da3e" );
	}

	@Override
	public void selectDriver ( String pkcs11Driver ) 
			throws SmartCardReaderNotFoundException, 
				   PKCS11DriverNotFoundException, 
				   InvalidPKCS11DriverException, 
				   InvalidSmartCardException, 
				   SmartCardAccessException {
		
		if ( !StringUtils.equals(pkcs11Driver, FakeSmartCardAccess.FAKE_DRIVER) ) { 
			throw new InvalidPKCS11DriverException("only \"fake\" driver is allowed");
		}
	}
	
	@Override
	public String[] login ( String pin ) 
			throws IllegalStateException, 
				   InvalidPinException, 
				   PinLockedException, 
				   SmartCardAccessException {

		if ( !StringUtils.equals(pin, FakeSmartCardAccess.FAKE_PIN) ) { 
			throw new InvalidPinException(String.format("pin for the fake smartCard: %s", FAKE_PIN));
		}
		return certChains.keySet().toArray(new String[certChains.size()]);
	}
	
	@Override
	public X509Certificate selectCertificate(String userAlias) 
			throws CertificateListException, CertificateException {
		
		privateKey = X509Utils.privateKeyFromHex ( privateKeys.get(userAlias) );
		return X509Utils.rawX509CertificateFromHex ( certChains.get(userAlias) );
	}

	@Override
	public byte[] signFingerPrint(byte[] fingerPrint) 
			throws IllegalStateException, 
					IllegalArgumentException, 
					SmartCardAccessException {
		
		byte[] digitalSignature;
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			signature.update(fingerPrint);
			digitalSignature = signature.sign();
		} catch(Exception e) {
			throw new SmartCardAccessException(e);
		}
		return digitalSignature;
	}

	@Override
	public void logout() 
			throws IllegalStateException,
					SmartCardAccessException {
	}
	
	@Override
	public void finalize() 
			throws SmartCardAccessException {
		
	}

}
