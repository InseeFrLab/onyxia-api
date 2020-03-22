package fr.insee.onyxia.api.services.ssh;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.common.base.MoreObjects;

public class PaireClefs {
	String privateKey;
	String publicKey;
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.privateKey + this.publicKey);
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (!(object instanceof PaireClefs)) {
			return false;
		}
		final PaireClefs other = (PaireClefs) object;
		return Objects.equal(this.privateKey, other.privateKey) && Objects.equal(this.publicKey, other.publicKey);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PaireClefs.class)
					      .add("Clef privée : ", this.privateKey)
					      .add("\nClef publique : ", this.publicKey)
					      .toString();
	}

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
