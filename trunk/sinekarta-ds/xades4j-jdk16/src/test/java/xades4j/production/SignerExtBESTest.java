package xades4j.production;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.properties.DataObjectDesc;
import xades4j.providers.impl.ExtKeyringDataProvider;
import xades4j.utils.X509Utils;

public class SignerExtBESTest extends SignerTestBase {

	Logger tracer = Logger.getLogger(getClass());

	static final String SIGN_ALIAS       = "Roberto Colombari"; 
	static final String SIGN_PRIVATE_KEY = "30820276020100300d06092a864886f70d0101010500048202603082025c02010002818100853c9950706ed4c495424cb1a739bab8639da5e4390e77c69d4de2e3d4ed1ceea2cf96cb784f7f9a0c7561ce471ed655e928ef56003e4dd8053bb3cafd83d665142529f249d29fdd730ec582a0320be028946f53e6a3603aebb8e68538837227c5922360790d8ffc1e5fec8e02592a1aafdeb938307516205a4f6d877f9e1c7d0203010001028180791347733ca8cabad5b449038ba63f52be5b8d5be6a98a18b7ec0649e9bd8b742409a6cbc1c9e477f5e85977dd535d8cf6739782bc77e1bf7389fc69739571a65af4279ecd2a8078e6ef9661a972f7eb233c10e03f735eb3fe4b48fa61a9a0a3c5f0ecb7e245a69331053a3e89b35b7b39b9bde9d4915fd00be10044a3beb281024100c1543dba4cce5af8a8f50b054a83343f412fc7cdcd46efdee7ec9c72fa06c059c689d6ed66ae50688a4985f8a8eb62ece5f3bb0a001c4e95b660ef60e71134dd024100b06d7c7b81ad598899de62bab7fbd731a4d799f3b3b82286297fd6a81d4f947a6f2b803b0a4d280ae86b3d7168cf39af741b2e60a90b7a1f774b9829aea4bc21024100b28903404ab1be9d281ab384bd5d1120e12828d24ba218deb73b70f755226afbfd3749fe8ef6a757036e0684ae2a427f1794cfc3da7a49b0446e9c61d6c1b31902405747428ec2df23ecccd9d413b4d2d4694db80f041d835928efbcbb4f5d78b1e643bacc6be8b3b4bc78b01cac4f023cf24c48ea0f8d710d1025eef2aea42400a1024001c63d53c6a22904e0bb08381a47510d4328ca8172392b104cee6afc476ebb46bd5945e4e445abcd223db96c03da57edde57219bc9839ff9ea7d32b657e2efac";
	static final String SIGN_CERTIFICATE = "308201a43082010d020101300d06092a864886f70d01010b050030193117301506035504030c0e4a656e696120536f667477617265301e170d3134313132333030303030305a170d3135313132343030303030305a301c311a301806035504030c11526f626572746f20436f6c6f6d6261726930819f300d06092a864886f70d010101050003818d0030818902818100853c9950706ed4c495424cb1a739bab8639da5e4390e77c69d4de2e3d4ed1ceea2cf96cb784f7f9a0c7561ce471ed655e928ef56003e4dd8053bb3cafd83d665142529f249d29fdd730ec582a0320be028946f53e6a3603aebb8e68538837227c5922360790d8ffc1e5fec8e02592a1aafdeb938307516205a4f6d877f9e1c7d0203010001300d06092a864886f70d01010b0500038181007e07a03eed95d8dd621bac2468959217e38202eddb7188fc2e674ec24f59952e12a272badd64f6ed360dd701b8266a1c5e15d489dc7af9e2ba7ca3e6068312356e0d8ce9a252cf304da4901d574325e231b77d19974ba969098d9386dcb0dd20f015b643d18edb251253a92e5b2401e4b25fd9536a0d1276c6a496f6ff3f277a";

	
	
	@Test
    public void testSignBES() throws Exception
    {
        tracer.info("signBES");
        try {
        	// Source document and relative root element
        	Document doc = getTestDocument();
	        Element root = doc.getDocumentElement();
	        
	        // Signature identity
	        PrivateKey privateKey = X509Utils.privateKeyFromHex ( SIGN_PRIVATE_KEY, "RSA" );
        	X509Certificate certificate = X509Utils.rawX509CertificateFromHex ( SIGN_CERTIFICATE );
        	
        	// External XAdES-BES Signer 
	        ExtKeyringDataProvider extKdp = new ExtKeyringDataProvider();
	        SignerExtBES signer = (SignerExtBES)new XadesExtBesSigningProfile(extKdp).newSigner();
	        
	        // Creation of the reference to the root element
	        DataObjectDesc obj1 = new DataObjectReference('#' + root.getAttribute("Id")).withTransform(new EnvelopedSignatureTransform());
	        SignedDataObjects dataObjs = new SignedDataObjects(obj1);
	        
	        // Digest evaluation
	        extKdp.setSigningCertificate(certificate);
	        byte[] digest = signer.digest(dataObjs, root);
	        
	        // Evaluate the digital signature externally 
	        Signature signature = Signature.getInstance("SHA256withRSA");
	        signature.initSign(privateKey);
	        signature.update(digest);
	        byte[] digitalSignature = signature.sign();
	        
	        // Inject the digitalSignature into the signer
	        signer.setDigitalSignature(digitalSignature);
	        
	        // Evaluate the
	        signer.sign(dataObjs, root, SignatureAppendingStrategies.AsFirstChild);
	
	        outputDocument(doc, DOCUMENT_BASE+"_extbes.xml");
	    } catch(Exception e) {
	    	tracer.error(e.getMessage(), e);
	    	throw e;
	    }
    }
}
