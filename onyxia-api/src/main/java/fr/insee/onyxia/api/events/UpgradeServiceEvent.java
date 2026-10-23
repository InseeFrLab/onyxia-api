package fr.insee.onyxia.api.events;

public class UpgradeServiceEvent extends InstallServiceEvent {

    public static final String TYPE = "service.upgrade";

    public UpgradeServiceEvent() {}

    public UpgradeServiceEvent(
            String username,
            String namespace,
            String releaseName,
            String packageName,
            String catalogId,
            String friendlyName) {
        super(username, namespace, releaseName, packageName, catalogId, friendlyName);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
