package fr.insee.onyxia.api.controller;

import java.net.URI;

// These are mostly examples
public class RestExceptionTypes {
    public static final URI ACCESS_DENIED = URI.create("urn:org:onyxia:api:error:access-denied");
    public static final URI VALIDATION_FAILED =
            URI.create("urn:org:onyxia:api:error:validation-failed");
    public static final URI INVALID_ARGUMENT =
            URI.create("urn:org:onyxia:api:error:invalid-argument");
    public static final URI INSTALLATION_FAILURE =
            URI.create("urn:org:onyxia:api:error:installation-failure");
    public static final URI NAMESPACE_NOT_FOUND =
            URI.create("urn:org:onyxia:api:error:namespace-not-found");
    public static final URI HELM_LIST_FAILURE =
            URI.create("urn:org:onyxia:api:error:helm-list-failure");
    public static final URI HELM_RELEASE_FETCH_FAILURE =
            URI.create("urn:org:onyxia:api:error:helm-release-fetch-failure");
    public static final URI GENERIC_ERROR = URI.create("urn:org:onyxia:api:error:unknown-error");

    private RestExceptionTypes() {
        // Prevent instantiation
    }
}
