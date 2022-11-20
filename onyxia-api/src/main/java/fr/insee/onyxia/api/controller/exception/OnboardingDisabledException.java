package fr.insee.onyxia.api.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
public class OnboardingDisabledException extends RuntimeException {
	public OnboardingDisabledException() {
		super("Onboarding is disabled on this onyxia instance");
	}
}
