package it.unitn.disi.smatch.oracles;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Linguistic Oracles.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SenseMatcherException extends SMatchException {

    public SenseMatcherException(String errorDescription) {
        super(errorDescription);
    }

    public SenseMatcherException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
