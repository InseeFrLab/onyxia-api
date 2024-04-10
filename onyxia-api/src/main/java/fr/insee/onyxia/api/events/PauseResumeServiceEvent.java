package fr.insee.onyxia.api.events;

public class PauseResumeServiceEvent extends InstallServiceEvent {

    private boolean isPause;

    public PauseResumeServiceEvent() {}

    public PauseResumeServiceEvent(
            String username,
            String namespace,
            String releaseName,
            String packageName,
            String catalogId,
            boolean pause) {
        super(username, namespace, releaseName, packageName, catalogId);
        this.isPause = pause;
    }

    @Override
    public String getType() {
        return isPause ? "service.pause" : "service.install";
    }
}
