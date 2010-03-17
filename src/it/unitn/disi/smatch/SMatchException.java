package it.unitn.disi.smatch;

/**
 * Main component exception.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SMatchException extends Exception {

    /**
     * Constructor.
     * Creates a new Exception by using super(msg) method.
     *
     * @param errorDescription the description of the error
     */
    public SMatchException(String errorDescription) {
        super(errorDescription);
    }

    /**
     * Constructor.
     * Creates a new Exception by using super(msg, cause) method.
     *
     * @param errorDescription the description of the error
     * @param cause            the cause
     */
    public SMatchException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
