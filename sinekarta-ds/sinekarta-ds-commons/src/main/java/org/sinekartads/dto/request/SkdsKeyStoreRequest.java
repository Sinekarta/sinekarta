package org.sinekartads.dto.request;

public abstract class SkdsKeyStoreRequest extends BaseRequest {

	private static final long serialVersionUID = -4080940702371830719L;
	
	public static class SkdsKeyStoreOpenRequest extends SkdsKeyStoreRequest {

		private static final long serialVersionUID = -8570061001975124221L;
		
		private String keyStorePin;
		
		public String getKeyStorePin() {
			return keyStorePin;
		}

		public void setKeyStorePin(String keyStorePin) {
			this.keyStorePin = keyStorePin;
		}

	}
	
	public static class SkdsKeyStoreReadRequest extends SkdsKeyStoreRequest {

		private static final long serialVersionUID = 431722588450348169L;
		
		private String ksRef;
		private String ksPin;
		private String userAlias;	
		private String userPassword;	
		
		public String getKsRef() {
			return ksRef;
		}

		public void setKsRef(String ksRef) {
			this.ksRef = ksRef;
		}

		public String getKsPin() {
			return ksPin;
		}

		public void setKsPin(String ksPin) {
			this.ksPin = ksPin;
		}

		public String getUserAlias() {
			return userAlias;
		}

		public void setUserAlias(String userAlias) {
			this.userAlias = userAlias;
		}

		public String getUserPassword() {
			return userPassword;
		}

		public void setUserPassword(String userPassword) {
			this.userPassword = userPassword;
		}
		
	}

}
