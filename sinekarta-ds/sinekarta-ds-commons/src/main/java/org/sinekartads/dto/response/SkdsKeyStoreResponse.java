package org.sinekartads.dto.response;

import org.sinekartads.dto.domain.KeyStoreDTO;

public abstract class SkdsKeyStoreResponse extends BaseResponse {
	
	private static final long serialVersionUID = -8884560676308929539L;

	public static class SkdsKeyStoreOpenResponse extends SkdsKeyStoreResponse {
		
		private static final long serialVersionUID = -862553768962627L;
		
		private KeyStoreDTO keyStore;
		
		public KeyStoreDTO getKeyStore() {
			return keyStore;
		}

		public void setKeyStore(KeyStoreDTO keyStore) {
			this.keyStore = keyStore;
		}
	}
	
	
	public static class SkdsKeyStoreReadResponse extends SkdsKeyStoreResponse {
		
		private static final long serialVersionUID = -4633957048037327402L;
		
		private String certificate;
		private String[] certificateChain;
		private String privateKey;
		
		
		
		public String getCertificate() {
			return certificate;
		}

		public void setCertificate(String certificate) {
			this.certificate = certificate;
		}
		
		public String[] getCertificateChain() {
			return certificateChain;
		}

		public void setCertificateChain(String[] certificateChain) {
			this.certificateChain = certificateChain;
		}

		public String getPrivateKey() {
			return privateKey;
		}

		public void setPrivateKey(String privateKey) {
			this.privateKey = privateKey;
		}
		
	}
}
