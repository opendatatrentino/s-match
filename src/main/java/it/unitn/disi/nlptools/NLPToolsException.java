package it.unitn.disi.nlptools;

/**
 * Main NLPTools exception.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NLPToolsException extends Exception {

    /**
     * Constructor
     * Creates a new Exception by using super(msg) method
     *
     * @param errorDescription the description of the error
     */
    public NLPToolsException(String errorDescription) {
        super(errorDescription);
    }

    /**
     * Constructor
     * Creates a new Exception by using super(msg, cause) method
     *
     * @param errorDescription the description of the error
     * @param cause            the cause
     */
    public NLPToolsException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}