package io.github.inseefrlab.helmwrapper.events;

import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import org.springframework.stereotype.Component;

@Component
public class InstallAppEvent {
    HelmInstaller helmInstaller;

    public InstallAppEvent(HelmInstaller helmInstaller) {
        this.helmInstaller = helmInstaller;
    }
}
