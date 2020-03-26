package fr.insee.onyxia.api.services.ssh;

public class PaireClefs {
	String privateKey;
	String publicKey;
	

	public void setPrivateKey(String key) {
		this.privateKey = key;
	}

	public void setPublicKey(String key) {
		this.publicKey = key;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}
	
	public static Builder newInstance() {
		return new Builder();
	}
	
	public static class Builder {
		private PaireClefs p;

		private Builder() {
			p = new PaireClefs();
		}

		public PaireClefs build() {
			return p;
		}

		/* ** */
		public Builder setPublicKey(String publicKey) {
			p.publicKey = publicKey;
			return this;
		}

		public Builder setPrivateKey(String privateKey) {
			p.privateKey = privateKey;
			return this;
		}
	}
}
