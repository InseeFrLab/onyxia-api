package fr.insee.onyxia.api.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class SchemaNotFoundException extends ErrorResponseException {

    public SchemaNotFoundException(String schemaName) {
        super(HttpStatus.NOT_FOUND);
    }
}
