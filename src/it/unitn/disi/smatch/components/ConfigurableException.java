package it.unitn.disi.smatch.components;

/**
 * Exception for Configurables.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ConfigurableException extends Exception {

    public ConfigurableException(String errorDescription) {
        super(errorDescription);
    }

    public ConfigurableException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
