package fr.insee.onyxia.api.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OnyxiaEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public OnyxiaEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(OnyxiaEvent onyxiaEvent) {
        applicationEventPublisher.publishEvent(onyxiaEvent);
    }
}
