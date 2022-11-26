package fr.insee.onyxia.api.services.control.xgenerated;

import fr.insee.onyxia.model.catalog.Config.Property;
import java.util.HashMap;
import java.util.Map;

public class XGeneratedContext {
    private String groupIdKey;
    private Map<String, Scope> scopes = new HashMap<>();

    public String getGroupIdKey() {
        return groupIdKey;
    }

    public void setGroupIdKey(String groupIdKey) {
        this.groupIdKey = groupIdKey;
    }

    public Map<String, Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Map<String, Scope> scopes) {
        this.scopes = scopes;
    }

    public static class Scope {
        private String scopeName;
        private Map<String, Property.XGenerated> xGenerateds = new HashMap<>();

        public String getScopeName() {
            return scopeName;
        }

        public void setScopeName(String scopeName) {
            this.scopeName = scopeName;
        }

        public Map<String, Property.XGenerated> getxGenerateds() {
            return xGenerateds;
        }

        public void setxGenerateds(Map<String, Property.XGenerated> xGenerateds) {
            this.xGenerateds = xGenerateds;
        }
    }
}
