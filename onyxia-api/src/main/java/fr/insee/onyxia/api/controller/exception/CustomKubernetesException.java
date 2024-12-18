package fr.insee.onyxia.api.controller.exception;

import org.springframework.http.ProblemDetail;

/**
 * Custom exception class for propagating Kubernetes-related errors
 * with structured ProblemDetail information.
 */
public class CustomKubernetesException extends RuntimeException {
    
    private final ProblemDetail problemDetail;

    /**
     * Constructor to create a CustomKubernetesException.
     * 
     * @param problemDetail The ProblemDetail containing error details
     */
    public CustomKubernetesException(ProblemDetail problemDetail) {
        super(problemDetail.getDetail());
        this.problemDetail = problemDetail;
    }

    /**
     * Getter for the ProblemDetail object.
     * 
     * @return ProblemDetail containing structured error details
     */
    public ProblemDetail getProblemDetail() {
        return problemDetail;
    }
}
