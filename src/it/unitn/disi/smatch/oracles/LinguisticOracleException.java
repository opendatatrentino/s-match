package it.unitn.disi.smatch.oracles;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Linguistic Oracles.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class LinguisticOracleException extends SMatchException {

    public LinguisticOracleException(String errorDescription) {
        super(errorDescription);
    }

    public LinguisticOracleException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
