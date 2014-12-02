package org.sinekartads.core.provider;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.JDKMessageDigest;
import org.bouncycastle.jce.provider.JDKMessageDigest.GOST3411;
import org.bouncycastle.jce.provider.JDKMessageDigest.MD2;
import org.bouncycastle.jce.provider.JDKMessageDigest.MD5;
import org.bouncycastle.jce.provider.JDKMessageDigest.RIPEMD128;
import org.bouncycastle.jce.provider.JDKMessageDigest.RIPEMD160;
import org.bouncycastle.jce.provider.JDKMessageDigest.RIPEMD256;
import org.bouncycastle.jce.provider.JDKMessageDigest.SHA1;
import org.bouncycastle.jce.provider.JDKMessageDigest.SHA224;
import org.bouncycastle.jce.provider.JDKMessageDigest.SHA256;
import org.bouncycastle.jce.provider.JDKMessageDigest.SHA384;
import org.bouncycastle.jce.provider.JDKMessageDigest.SHA512;
import org.sinekartads.model.domain.DigestInfo;
import org.sinekartads.model.oid.DigestAlgorithm;

import com.itextpdf.text.pdf.security.ExternalDigest;

public class ExternalDigester extends MessageDigest implements ExternalDigest {

	// FIXME use ProviderDigest(conf.getProviderName()) instead 
	String hashAlgorithm;
	ThreadLocal<JDKMessageDigest> nestedDigest;
	ThreadLocal<byte[]> fingerPrint;
	
	public ExternalDigester() {
		// use an algorithm only for the initialization, it will be never used
		super(DigestAlgorithm.SHA256.getName());
		nestedDigest = new ThreadLocal<JDKMessageDigest>();
		fingerPrint = new ThreadLocal<byte[]>();
	}
	
	
	
	// -----
	// --- ExternalDigest protocol
	// -
	
	@Override
	public MessageDigest getMessageDigest(String hashAlgorithm) throws GeneralSecurityException {
		this.hashAlgorithm = hashAlgorithm;
		JDKMessageDigest digest;
		if (StringUtils.isBlank(hashAlgorithm)) {
            throw new NoSuchAlgorithmException(hashAlgorithm);
		}
        if (hashAlgorithm.equals("MD2")) { 
            digest = new MD2();
        }
        else if (hashAlgorithm.equals("MD5")) { 
            digest = new MD5();
        }
        else if (hashAlgorithm.equals("SHA1")) { 
            digest = new SHA1();
        }
        else if (hashAlgorithm.equals("SHA224")) { 
            digest = new SHA224();
        }
        else if (hashAlgorithm.equals("SHA256")) { 
            digest = new SHA256();
        }
        else if (hashAlgorithm.equals("SHA384")) { 
            digest = new SHA384();
        }
        else if (hashAlgorithm.equals("SHA512")) { 
            digest = new SHA512();
        }
        else if (hashAlgorithm.equals("RIPEMD128")) { 
            digest = new RIPEMD128();
        }
        else if (hashAlgorithm.equals("RIPEMD160")) { 
            digest = new RIPEMD160();
        }
        else if (hashAlgorithm.equals("RIPEMD256")) { 
            digest = new RIPEMD256();
        }
        else if (hashAlgorithm.equals("GOST3411")) { 
            digest = new GOST3411();
        } else {
        	throw new NoSuchAlgorithmException(hashAlgorithm); //shouldn't get here
        }
		nestedDigest.set(digest);
		return this;
	}
	
	
	
	// -----
	// --- MessageDigest protocol: use the nested JDKMessageDigest
	// -
	
	@Override
	protected void engineUpdate(byte input) {
		nestedDigest.get().update(input);
	}

	@Override
	protected void engineUpdate(byte[] input, int offset, int len) {
		nestedDigest.get().update(input, offset, len);
	}

	@Override
	protected byte[] engineDigest() {
		byte[] digestBytes = nestedDigest.get().engineDigest();
		fingerPrint.set(digestBytes);
        return digestBytes;
	}

	@Override
	protected void engineReset() {
		nestedDigest.get().reset();
	}
	
	
	
	// -----
	// --- External access to the generated fingerPrint
	// -
	
	public byte[] getFingerPrint() {
		// TODO recognize the conformity with the given algorithm specifications
		return this.fingerPrint.get();
	}
	
	public DigestInfo getDigestInfo() {
		// TODO recognize the conformity with the given algorithm specifications
		return DigestInfo.getInstance(hashAlgorithm, fingerPrint.get());
	}

}
