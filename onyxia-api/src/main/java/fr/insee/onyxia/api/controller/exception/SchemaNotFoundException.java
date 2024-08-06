package fr.insee.onyxia.api.controller.exception;

public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException(String schemaName) {
        super("Schema not found: " + schemaName);
    }
}
