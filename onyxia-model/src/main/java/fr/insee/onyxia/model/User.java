package fr.insee.onyxia.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "")
public class User implements Serializable {

    private static final long serialVersionUID = 2663796098930599255L;

    @Schema(description = "")
    String email;

    @Schema(description = "")
    String idep;

    @Schema(description = "")
    String nomComplet;

    @Schema(description = "")
    String password;

    @Schema(description = "")
    String ip;

    @JsonIgnore
    @Schema(description = "")
    Map<String, Object> attributes = new HashMap<>();

    @Schema(description = "")
    List<String> groups = new ArrayList<>();

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        if (idep != null) {
            return this.idep.hashCode();
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof User)) {
            return false;
        }
        final User other = (User) object;
        if (this.idep == null) {
            return other.idep == null;
        }
        return this.idep.equals(other.idep);
    }

    @Override
    public String toString() {
        if (this.idep == null) {
            return "Idep null";
        }
        return this.idep;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdep() {
        return idep;
    }

    public void setIdep(String idep) {
        this.idep = idep;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public static Builder newInstance() {
        return new Builder();
    }

    public static class Builder {
        private User u;

        private Builder() {
            u = new User();
        }

        public User build() {
            return u;
        }

        /* ** */
        public Builder setIp(String ip) {
            u.ip = ip;
            return this;
        }

        public Builder setEmail(String email) {
            u.email = email;
            return this;
        }

        public Builder setIdep(String idep) {
            u.idep = idep;
            return this;
        }

        public Builder setNomComplet(String nomComplet) {
            u.nomComplet = nomComplet;
            return this;
        }

        public Builder setPassword(String password) {
            u.password = password;
            return this;
        }

        public Builder addGroup(String group) {
            u.groups.add(group);
            return this;
        }

        public Builder addGroups(List<String> groups) {
            u.groups.addAll(groups);
            return this;
        }
    }
}
