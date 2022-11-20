package fr.insee.onyxia.api.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class NamespaceAlreadyExistException extends RuntimeException{
	public NamespaceAlreadyExistException() {
		super("Namespace already exists");
	}
}
