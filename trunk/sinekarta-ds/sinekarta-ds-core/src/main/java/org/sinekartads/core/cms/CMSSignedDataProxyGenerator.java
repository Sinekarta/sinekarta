package org.sinekartads.core.cms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.Store;
import org.sinekartads.model.domain.SignDisposition;

public class CMSSignedDataProxyGenerator {
	
	private static final CMSProvider provider = new CMSProvider();
	private CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
	
	public void embedCertificateChain(X509Certificate[] signingCertificateChain) throws CertificateEncodingException, CMSException {
		if (null != signingCertificateChain) {
			
			// take the certification list
			Store certStore = new JcaCertStore(Arrays.asList(signingCertificateChain));
			generator.addCertificates(certStore);

			// init a contentSigner on a fake-key and the manually-coded certificates
			ContentSigner sigGen;
			try {
				sigGen = new JcaContentSignerBuilder("SHA256withRSA")
						.setProvider(provider).build(new DummyPrivateKey());
			} catch (OperatorCreationException e) {
				// hide the implementation-dependent exception
				throw new CertificateEncodingException("Unable to obtain the certificate", e);
			}
			MessageDigest md = null;
			InputStream is = null;
			byte[] encoded;
			for (X509Certificate cert : signingCertificateChain) {
				encoded = cert.getEncoded();
				try {
					is = new ByteArrayInputStream(encoded);
					md =  MessageDigest.getInstance("SHA256", BouncyCastleProvider.PROVIDER_NAME);
					byte[] buffer = new byte[1024];
					int readLen = is.read(buffer);
					while (readLen != -1) {
						md.update(buffer, 0, readLen);
						readLen = is.read(buffer);
					}
				} catch(NoSuchAlgorithmException e) {
					// never thrown, SHA256 programmatically set
					throw new RuntimeException(e);
				} catch(NoSuchProviderException e) {
					// never thrown, BC programmatically set
					throw new RuntimeException(e);
				} catch(IOException e) {
					// never thrown, byte array I/O
					throw new RuntimeException(e);
				} finally {
					IOUtils.closeQuietly(is);
				}
				byte[] certHash = md.digest();
				
				ESSCertIDv2 essCert1 = new ESSCertIDv2(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), certHash);
				ESSCertIDv2[] essCert1Arr = { essCert1 };
				SigningCertificateV2 scv2 = new SigningCertificateV2(essCert1Arr);
				Attribute certHAttribute = new Attribute(PKCSObjectIdentifiers.id_aa_signingCertificateV2, new DERSet(scv2));
				ASN1EncodableVector v = new ASN1EncodableVector();
				v.add(certHAttribute);
				AttributeTable at = new AttributeTable(v);

				CMSAttributeTableGenerator attrGen = new ExtSignedAttributeTableGenerator(at);
				ExtSignerInfoGeneratorBuilder genBuild =  new ExtSignerInfoGeneratorBuilder(new BcDigestCalculatorProvider());
				genBuild.setSignedAttributeGenerator(attrGen);
			
				sifGen = null;
				try {
					sifGen = genBuild.build(sigGen, new X509CertificateHolder(encoded));
				} catch (OperatorCreationException e) {
					// hide the implementation-dependent exception
					throw new CertificateEncodingException("Unable to obtain the certificate", e);
				} catch(IOException e) {
					// never thrown, encoded has to be valid 
				}

				generator.addSignerInfoGenerator(sifGen);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public byte[] evaluateDigest(CMSProcessable content, SignDisposition.CMS disposition) throws CMSException {
		boolean encapsulate = true;
		// FIXME cancellare la SignatureDisposition da qui appena si vede che è ininfluente
		/*if(disposition == SignatureDisposition.DETACHED) {
			encapsulate = false;
		} else {
			encapsulate = true;
		}*/
		
		SHA256WithRSAProxySignature.reset();
		try {
			generator.generate(content, encapsulate, provider);
			signingTime = sifGen.getSigningTime();
		} catch (NoSuchAlgorithmException e) {
			// never thrown, programmatically set values
		}
		return SHA256WithRSAProxySignature.getDigestValue();
	}
	
	@SuppressWarnings("deprecation")
	public CMSSignedData generateSignedData(CMSProcessable content, byte[] digitalSignature, SignDisposition.CMS disposition) throws CMSException {
		boolean encapsulate;
		if(disposition == SignDisposition.CMS.DETACHED) {
			encapsulate = false;
		} else {
			encapsulate = true;
		}
		
		CMSProvider provider = new CMSProvider();
		SHA256WithRSAProxySignature.reset();
		SHA256WithRSAProxySignature.setSignatureValue(digitalSignature);
		CMSSignedData signedData;
		try {
			sifGen.setSigningTime(signingTime);
			signedData = generator.generate(content, encapsulate, provider);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}		

		return signedData;
	}
	
	private ExtSignerInfoGenerator sifGen = null;
    private Date signingTime;
    
    public Date getSigningTime() {
		return signingTime;
	}

	public void setSigningTime(Date signingTime) {
		this.signingTime = signingTime;
	}
}
