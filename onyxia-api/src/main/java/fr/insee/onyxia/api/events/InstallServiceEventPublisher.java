package fr.insee.onyxia.api.events;

import fr.insee.onyxia.model.dto.CreateServiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InstallServiceEventPublisher {
    @Autowired private ApplicationEventPublisher applicationEventPublisher;

    public void publishInstallServiceEvent(final CreateServiceDTO createServiceDTO) {
        applicationEventPublisher.publishEvent(new InstallServiceEvent(createServiceDTO));
    }

    public void publishUninstallServiceEvent(final String name) {
        applicationEventPublisher.publishEvent(new UninstallServiceEvent(name));
    }
}
