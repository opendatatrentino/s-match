package it.unitn.disi.smatch.components;

/**
 * Exception for Configurables.
 *
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public class ConfigurableException extends Exception {

    public ConfigurableException(String errorDescription) {
        super(errorDescription);
    }

    public ConfigurableException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
