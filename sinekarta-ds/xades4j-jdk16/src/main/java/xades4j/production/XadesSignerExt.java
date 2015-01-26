package xades4j.production;

import org.w3c.dom.Node;

import xades4j.XAdES4jException;

/**
 * Extension of the XadesSigner protocol which allows the signers to
 * evaluate the digest and exchange the signatureId and digitalSignature
 * information.
 * @author amommo
 */
public interface XadesSignerExt extends XadesSigner {

	public byte[] digest(
            SignedDataObjects signedDataObjects,
            Node parent) throws XAdES4jException;
	
	 public byte[] digest(
	            SignedDataObjects signedDataObjects,
	            Node referenceNode,
	            SignatureAppendingStrategy appendingStrategy) throws XAdES4jException;
	
	 public String getSignatureId();
	 
	 public void setSignatureId(String signatureId);
	 
	 public void setDigitalSignature(byte[] digitalSignature);
	
	 public void setDigest(byte[] digest);
}
