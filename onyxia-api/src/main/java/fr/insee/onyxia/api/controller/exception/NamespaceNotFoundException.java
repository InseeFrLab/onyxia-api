package fr.insee.onyxia.api.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NamespaceNotFoundException extends RuntimeException{
	public NamespaceNotFoundException() {
		super("Namespace not found");
	}
}
