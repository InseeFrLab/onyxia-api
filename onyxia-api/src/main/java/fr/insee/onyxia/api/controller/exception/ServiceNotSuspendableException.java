package fr.insee.onyxia.api.controller.exception;

import fr.insee.onyxia.api.services.impl.HelmAppsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ServiceNotSuspendableException extends RuntimeException {
    public ServiceNotSuspendableException() {
        super(
                "This service is not suspendable. To be suspendable, a service must define "
                        + HelmAppsService.SUSPEND_KEY
                        + " as a key in values.schema.json");
    }
}
