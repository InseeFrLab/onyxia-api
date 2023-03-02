package fr.insee.onyxia.api.services.control.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PublishContext {

    private Context globalContext = new Context();
    private Map<String, Context> localContexts = new HashMap<>();
    private String catalogId;

    public PublishContext() {}

    public PublishContext(String catalogId) {}

    private Context getLocalContext(String key) {
        if (!localContexts.containsKey(key)) {
            localContexts.put(key, new Context());
        }

        return localContexts.get(key);
    }

    public Context getGlobalContext() {
        return globalContext;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public static class Context {
        private String randomizedId;

        private Map<String, Object> data = new HashMap<>();

        public String getRandomizedId() {
            if (randomizedId == null) {
                UUID uuid = UUID.randomUUID();
                randomizedId = Long.toString(-uuid.getLeastSignificantBits());
                ;
            }
            return randomizedId;
        }

        public Map<String, Object> getData() {
            return data;
        }
    }
}
