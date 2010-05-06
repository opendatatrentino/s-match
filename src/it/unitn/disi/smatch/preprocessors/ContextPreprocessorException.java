package it.unitn.disi.smatch.preprocessors;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Context Preprocessors.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ContextPreprocessorException extends SMatchException {

    public ContextPreprocessorException(String errorDescription) {
        super(errorDescription);
    }

    public ContextPreprocessorException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
