/* Copyright Rene Mayrhofer, 2006-03-19
 * 
 * This file may be copied under the terms of the GNU GPL version 2.
 */ 

package org.sinekartads.certificate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.qualified.ETSIQCObjectIdentifiers;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.sinekartads.test.SkdsTestCase;
import org.sinekartads.util.DNParser;

import xades4j.utils.HexUtils;

import com.itextpdf.text.pdf.codec.Base64;

/** This class uses the Bouncycastle lightweight API to generate X.509 certificates programmatically.
 * It assumes a CA certificate and its private key to be available and can sign the new certificate with
 * this CA. Some of the code for this class was taken from 
 * org.bouncycastle.x509.X509V3CertificateGenerator, but adapted to work with the lightweight API instead of
 * JCE (which is usually not available on MIDP2.0). 
 * 
 * @author Rene Mayrhofer
 */
public class X509CertificateGenerator extends SkdsTestCase {
	/** Our log4j logger. */
	private static Logger logger = Logger.getLogger(X509CertificateGenerator.class);
	
	/** This holds the certificate of the CA used to sign the new certificate. The object is created in the constructor. */
	private X509Certificate caCert;
	/** This holds the private key of the CA used to sign the new certificate. The object is created in the constructor. */
	private RSAPrivateCrtKeyParameters caPrivateKey;
	
	
	public X509CertificateGenerator(String caFile, String caPassword, String caAlias) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, InvalidKeyException, NoSuchProviderException, SignatureException {
		
		KeyStore caKs = KeyStore.getInstance("jks");
		caKs.load(new FileInputStream(new File(caFile)), caPassword.toCharArray());
		
		// load the key entry from the keystore
		Key key = caKs.getKey(caAlias, caPassword.toCharArray());
		if (key == null) {
			throw new RuntimeException("Got null key from keystore!"); 
		}
		RSAPrivateCrtKey privKey = (RSAPrivateCrtKey) key;
		caPrivateKey = new RSAPrivateCrtKeyParameters(privKey.getModulus(), privKey.getPublicExponent(), privKey.getPrivateExponent(),
				privKey.getPrimeP(), privKey.getPrimeQ(), privKey.getPrimeExponentP(), privKey.getPrimeExponentQ(), privKey.getCrtCoefficient());
		// and get the certificate
		caCert = (X509Certificate) caKs.getCertificate(caAlias);

		logger.info("CA Private Key Hex:    \n" + HexUtils.encodeHex(key.getEncoded()));
		logger.info("CA Certificate Hex:    \n" + HexUtils.encodeHex(caCert.getEncoded()));
		logger.info("CA Certificate Base64: \n" + Base64.encodeBytes(caCert.getEncoded()));
		
		if (caCert == null) {
			throw new RuntimeException("Got null cert from keystore!"); 
		}
		logger.debug("Successfully loaded CA key and certificate. CA DN is '" + caCert.getSubjectDN().getName() + "'");
		caCert.verify(caCert.getPublicKey());
		logger.debug("Successfully verified CA certificate with its own public key.");
	}
	
	public boolean createCertificate(String dn, int validityDays, String exportFile, String exportPassword) throws 
			IOException, InvalidKeyException, SecurityException, SignatureException, NoSuchAlgorithmException, DataLengthException, CryptoException, KeyStoreException, NoSuchProviderException, CertificateException, InvalidKeySpecException {
		logger.info("Generating certificate for distinguished subject name '" + 
				dn + "', valid for " + validityDays + " days");
		SecureRandom sr = new SecureRandom();
		
		PublicKey pubKey;
		PrivateKey privKey;
		
		logger.debug("Creating RSA keypair");
		// generate the keypair for the new certificate
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair keypair = keyGen.generateKeyPair();
		privKey = keypair.getPrivate();
		pubKey = keypair.getPublic();
		
		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.DAY_OF_YEAR, validityDays);
 
		X509Name x509Name = new X509Name(dn);

		V3TBSCertificateGenerator certGen = new V3TBSCertificateGenerator();
	    certGen.setSerialNumber(new DERInteger(BigInteger.valueOf(System.currentTimeMillis())));
		certGen.setIssuer(PrincipalUtil.getSubjectX509Principal(caCert));
		certGen.setSubject(x509Name);
		DERObjectIdentifier sigOID = new ASN1ObjectIdentifier("1.2.840.113549.1.1.11");
		AlgorithmIdentifier sigAlgId = new AlgorithmIdentifier(sigOID, new DERNull());
		certGen.setSignature(sigAlgId);
		certGen.setSubjectPublicKeyInfo(new SubjectPublicKeyInfo((ASN1Sequence)new ASN1InputStream(
                new ByteArrayInputStream(pubKey.getEncoded())).readObject()));
		certGen.setStartDate(new Time(new Date(System.currentTimeMillis())));
		certGen.setEndDate(new Time(expiry.getTime()));
		
        Vector          order = new Vector();
        Hashtable       extensions = new Hashtable();
        order.addElement(X509Extensions.BasicConstraints);
        order.addElement(X509Extensions.QCStatements);
        order.addElement(X509Extensions.KeyUsage);
        order.addElement(X509Extensions.AuthorityKeyIdentifier);
        order.addElement(X509Extensions.CRLDistributionPoints);
        order.addElement(X509Extensions.SubjectKeyIdentifier);
        
        //generate the distribution point
        GeneralName gn = new GeneralName(GeneralName.uniformResourceIdentifier, "ldap://ldap.infocert.it/cn%3dInfoCert%20Firma%20Qualificata%20CRL02,ou%3dCertificatore%20Accreditato,o%3dINFOCERT%20SPA,c%3dIT?certificateRevocationList");
        DistributionPointName distributionPointname = new DistributionPointName(DistributionPointName.FULL_NAME, gn);
        DistributionPoint distributionPoint = new DistributionPoint(distributionPointname, null, null/*new ReasonFlags(ReasonFlags.keyCompromise), new GeneralNames(new GeneralName(PrincipalUtil.getSubjectX509Principal(caCert)))*/);
        
        //generate the qcStatements
        ASN1EncodableVector qcVect = new ASN1EncodableVector();
        qcVect.add(new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_QcCompliance));
        ASN1EncodableVector vect1 = new ASN1EncodableVector();
        vect1.add(ETSIQCObjectIdentifiers.id_etsi_qcs_RetentionPeriod);
        vect1.add(new DERInteger(new BigInteger("20")));
        qcVect.add(new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_RetentionPeriod, new DERInteger(new BigInteger("20"))));
        qcVect.add(new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_QcSSCD));
        
        extensions.put(X509Extensions.BasicConstraints, new X509Extension(false, new DEROctetString(new BasicConstraints(false))));
        extensions.put(X509Extensions.QCStatements, new X509Extension(false, new DEROctetString(new DERSequence(qcVect))));
        extensions.put(X509Extensions.KeyUsage, new X509Extension(true, new DEROctetString(new KeyUsage(KeyUsage.nonRepudiation))));
        extensions.put(X509Extensions.AuthorityKeyIdentifier, new X509Extension(false, new DEROctetString(new AuthorityKeyIdentifierStructure(caCert.getPublicKey()))));        
        extensions.put(X509Extensions.CRLDistributionPoints, new X509Extension(false, new DEROctetString(new CRLDistPoint(new DistributionPoint[]{distributionPoint}))));
        extensions.put(X509Extensions.SubjectKeyIdentifier, new X509Extension(false, new DEROctetString(new SubjectKeyIdentifierStructure(pubKey))));
        
        
        certGen.setExtensions(new X509Extensions(order, extensions));
		
//		v3CertGen.addExtension(
//                new ASN1ObjectIdentifier("2.5.29.15"),
//                true,
//                new X509KeyUsage(
//                   X509KeyUsage.nonRepudiation
//                   ));
		
		logger.debug("Certificate structure generated, creating SHA256 digest");
		// attention: hard coded to be SHA256+RSA!
		SHA256Digest digester = new SHA256Digest();
		AsymmetricBlockCipher rsa = new PKCS1Encoding(new RSAEngine());
		TBSCertificateStructure tbsCert = certGen.generateTBSCertificate();

		ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
		DEROutputStream         dOut = new DEROutputStream(bOut);
		dOut.writeObject(tbsCert);

		// and now sign
		byte[] signature;
		// or the JCE way
        PrivateKey caPrivKey = KeyFactory.getInstance("RSA").generatePrivate(
        		new RSAPrivateCrtKeySpec(caPrivateKey.getModulus(), caPrivateKey.getPublicExponent(),
        				caPrivateKey.getExponent(), caPrivateKey.getP(), caPrivateKey.getQ(), 
        				caPrivateKey.getDP(), caPrivateKey.getDQ(), caPrivateKey.getQInv()));
		
        Signature sig = Signature.getInstance(sigOID.getId());
        sig.initSign(caPrivKey, sr);
        sig.update(bOut.toByteArray());
        signature = sig.sign();
		logger.debug("SHA256/RSA signature of digest is '" + new String(Hex.encodeHex(signature)) + "'");

		// and finally construct the certificate structure
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(tbsCert);
        v.add(sigAlgId);
        v.add(new DERBitString(signature));

        X509CertificateObject clientCert = new X509CertificateObject(new X509CertificateStructure(new DERSequence(v))); 
        logger.debug("Verifying certificate for correct signature with CA public key");
        clientCert.verify(caCert.getPublicKey());

        // and export as PKCS12 formatted file along with the private key and the CA certificate 
        logger.debug("Exporting certificate in JKS format");

//        PKCS12BagAttributeCarrier bagCert = clientCert;
//        bagCert.setBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
//        		new DERBMPString("Certificate for IPSec WLAN access"));
//        bagCert.setBagAttribute(
//                PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
//                new SubjectKeyIdentifierStructure(pubKey));
        
        KeyStore store = KeyStore.getInstance("jks");

        store.load(null, null);

        X509Certificate[] chain = new X509Certificate[2];
        // first the client, then the CA certificate
        chain[0] = clientCert;
        chain[1] = caCert;
        
        String alias = DNParser.parse(dn, "CN");
        store.setKeyEntry(alias, privKey, exportPassword.toCharArray(), chain);
        FileOutputStream fOut = new FileOutputStream(exportFile);
        X509Certificate cert = (X509Certificate)store.getCertificate(alias);
        logger.info("Alias:              \n" + alias);
		logger.info("Private Key Hex:    \n" + HexUtils.encodeHex(privKey.getEncoded()));
		logger.info("Certificate Hex:    \n" + HexUtils.encodeHex(cert.getEncoded()));
		logger.info("Certificate Base64: \n" + Base64.encodeBytes(cert.getEncoded()));
		logger.info("Private Key Base64: \n" + Base64.encodeBytes(privKey.getEncoded()));
		org.sinekartads.util.x509.X509Utils.privateKeyFromHex ( HexUtils.encodeHex(privKey.getEncoded()), "RSA" );
        store.store(fOut, exportPassword.toCharArray());
        
        return true;
	}
	
	/** The test CA can e.g. be created with
	 * 
	 * echo -e "AT\nUpper Austria\nSteyr\nMy Organization\nNetwork tests\nTest CA certificate\nme@myserver.com\n\n\n" | \
	     openssl req -new -x509 -outform PEM -newkey rsa:2048 -nodes -keyout /tmp/ca.key -keyform PEM -out /tmp/ca.crt -days 365;
	   echo "test password" | openssl pkcs12 -export -in /tmp/ca.crt -inkey /tmp/ca.key -out ca.p12 -name "Test CA" -passout stdin
	 * 
	 * The created certificate can be displayed with
	 * 
	 * openssl pkcs12 -nodes -info -in test.p12 > /tmp/test.cert && openssl x509 -noout -text -in /tmp/test.cert
	 */
	
	public static void main(String[] args) throws Exception {
		System.out.println(new X509CertificateGenerator(getTestResource(X509CertificateGenerator.class, "keystore.jks").getAbsolutePath(), "skdscip", "InfoCert")
				.createCertificate("C=IT, OU=SineKarta, O=Jenia Software, L=Casalecchio di Reno, SN=SKDS2015TI001, DN=2010111255718, CN=SineKarta", 3650, getTestResource(X509CertificateGenerator.class, "sinekarta.jks").getAbsolutePath(), "skdscip"));
	}
}