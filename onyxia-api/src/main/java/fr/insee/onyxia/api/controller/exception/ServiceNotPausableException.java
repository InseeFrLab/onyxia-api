package fr.insee.onyxia.api.controller.exception;

import fr.insee.onyxia.api.services.impl.HelmAppsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ServiceNotPausableException extends RuntimeException {
    public ServiceNotPausableException() {
        super(
                "This service is not pausable. To be pausable, a service must define "
                        + HelmAppsService.SUSPEND_KEY
                        + " as a key in values.schema.json");
    }
}
