package fr.insee.onyxia.api.events;

public class UninstallServiceEvent {

    private String name;

    public UninstallServiceEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
