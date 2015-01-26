package org.sinekartads.dto.response;


public abstract class SkdsKeyStoreResponse extends BaseResponse {
	
	private static final long serialVersionUID = -8884560676308929539L;

	public static class SkdsKeyStoreOpenResponse extends SkdsKeyStoreResponse {
		
		private static final long serialVersionUID = -862553768962627L;
		
		private String ksRef;
		private String aliases[];

		public String getKsRef() {
			return ksRef;
		}

		public void setKsRef(String ksRef) {
			this.ksRef = ksRef;
		}

		public String[] getAliases() {
			return aliases;
		}

		public void setAliases(String aliases[]) {
			this.aliases = aliases;
		}
		
	}
	
	
	public static class SkdsKeyStoreReadResponse extends SkdsKeyStoreResponse {
		
		private static final long serialVersionUID = -4633957048037327402L;
		
		private String[] certificateChain;
		private String privateKey;
		

		
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
