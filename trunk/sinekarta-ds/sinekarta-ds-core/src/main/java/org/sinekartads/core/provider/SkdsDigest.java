package org.sinekartads.core.provider;

//import org.bouncycastle.jce.provider.JDKMessageDigest.GOST3411;
//import org.bouncycastle.jce.provider.JDKMessageDigest.MD2;
//import org.bouncycastle.jce.provider.JDKMessageDigest.MD5;
//import org.bouncycastle.jce.provider.JDKMessageDigest.RIPEMD128;
//import org.bouncycastle.jce.provider.JDKMessageDigest.RIPEMD160;
//import org.bouncycastle.jce.provider.JDKMessageDigest.RIPEMD256;
//import org.bouncycastle.jce.provider.JDKMessageDigest.SHA1;
//import org.bouncycastle.jce.provider.JDKMessageDigest.SHA224;
//import org.bouncycastle.jce.provider.JDKMessageDigest.SHA256;
//import org.bouncycastle.jce.provider.JDKMessageDigest.SHA384;
//import org.bouncycastle.jce.provider.JDKMessageDigest.SHA512;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;

public class SkdsDigest implements ExternalDigest {

    public MessageDigest getMessageDigest(String hashAlgorithm) throws GeneralSecurityException {
        String oid = DigestAlgorithms.getAllowedDigests(hashAlgorithm);
        if (oid == null)
            throw new NoSuchAlgorithmException(hashAlgorithm);
        if (oid.equals("1.2.840.113549.2.2")) { //MD2
            return new MD2();
        }
        else if (oid.equals("1.2.840.113549.2.5")) { //MD5
            return new MD5();
        }
        else if (oid.equals("1.3.14.3.2.26")) { //SHA1
            return new SHA1();
        }
        else if (oid.equals("2.16.840.1.101.3.4.2.4")) { //SHA224
            return new SHA224();
        }
        else if (oid.equals("2.16.840.1.101.3.4.2.1")) { //SHA256
            return new SHA256();
        }
        else if (oid.equals("2.16.840.1.101.3.4.2.2")) { //SHA384
            return new SHA384();
        }
        else if (oid.equals("2.16.840.1.101.3.4.2.3")) { //SHA512
            return new SHA512();
        }
        else if (oid.equals("1.3.36.3.2.2")) { //RIPEMD128
            return new RIPEMD128();
        }
        else if (oid.equals("1.3.36.3.2.1")) { //RIPEMD160
            return new RIPEMD160();
        }
        else if (oid.equals("1.3.36.3.2.3")) { //RIPEMD256
            return new RIPEMD256();
        }
        else if (oid.equals("1.2.643.2.2.9")) { //GOST3411
            return new GOST3411();
        }
        throw new NoSuchAlgorithmException(hashAlgorithm); //shouldn't get here
    }
}
