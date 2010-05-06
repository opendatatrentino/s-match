package it.unitn.disi.smatch.deciders;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for SAT Solvers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SATSolverException extends SMatchException {

    public SATSolverException(String errorDescription) {
        super(errorDescription);
    }

    public SATSolverException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
