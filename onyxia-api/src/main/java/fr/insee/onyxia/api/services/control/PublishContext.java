package fr.insee.onyxia.api.services.control;

import java.util.UUID;

public class PublishContext {

    private String randomizedId;
    private String universeId;

    public PublishContext() {

    }

    public PublishContext(String universeId) {
        this.universeId = universeId;
    }

    /**
     *
     * @return
     */
    public String getRandomizedId() {
        if (randomizedId == null) {
            UUID uuid = UUID.randomUUID();
            randomizedId = Long.toString(-uuid.getLeastSignificantBits());;
        }
        return randomizedId;
    }

    public String getUniverseId() {
        return universeId;
    }
}
