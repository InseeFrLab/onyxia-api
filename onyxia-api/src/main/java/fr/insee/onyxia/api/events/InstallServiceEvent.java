package fr.insee.onyxia.api.events;

import fr.insee.onyxia.model.dto.CreateServiceDTO;
import org.springframework.stereotype.Component;

@Component
public class InstallServiceEvent {
    CreateServiceDTO createServiceDTO;

    public InstallServiceEvent(CreateServiceDTO createServiceDTO) {
        this.createServiceDTO = createServiceDTO;
    }
}
