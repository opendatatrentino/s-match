package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Matcher Libraries.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MatcherLibraryException extends SMatchException {

    public MatcherLibraryException(String errorDescription) {
        super(errorDescription);
    }

    public MatcherLibraryException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
