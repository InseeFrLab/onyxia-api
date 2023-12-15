package io.github.inseefrlab.helmwrapper.events;

import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InstallAppEventPublisher {
    @Autowired private ApplicationEventPublisher applicationEventPublisher;

    public void publishInstallAppEvent(final HelmInstaller helmInstaller) {
        applicationEventPublisher.publishEvent(new InstallAppEvent(helmInstaller));
    }
}
