package xades4j.production;

import org.w3c.dom.Node;

import xades4j.XAdES4jException;

public interface XadesSignerExt extends XadesSigner {

	public byte[] digest(
            SignedDataObjects signedDataObjects,
            Node parent) throws XAdES4jException;
	
	 public byte[] digest(
	            SignedDataObjects signedDataObjects,
	            Node referenceNode,
	            SignatureAppendingStrategy appendingStrategy) throws XAdES4jException;
	
	public void setDigitalSignature(byte[] digitalSignature);
	
	public void setDigest(byte[] digest);
}
