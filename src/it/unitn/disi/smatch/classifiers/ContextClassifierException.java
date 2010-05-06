package it.unitn.disi.smatch.classifiers;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Context Classifiers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ContextClassifierException extends SMatchException {

    public ContextClassifierException(String errorDescription) {
        super(errorDescription);
    }

    public ContextClassifierException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
