package fr.insee.onyxia.api.events;

public class SuspendResumeServiceEvent extends InstallServiceEvent {

    private boolean isSuspend;

    public SuspendResumeServiceEvent() {}

    public SuspendResumeServiceEvent(
            String username,
            String namespace,
            String releaseName,
            String packageName,
            String catalogId,
            boolean suspend) {
        super(username, namespace, releaseName, packageName, catalogId);
        this.isSuspend = suspend;
    }

    @Override
    public String getType() {
        return isSuspend ? "service.suspend" : "service.install";
    }
}
