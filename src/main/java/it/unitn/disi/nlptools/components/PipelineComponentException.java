package it.unitn.disi.nlptools.components;

import it.unitn.disi.common.components.ConfigurableException;

/**
 * Exception for pipeline components.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PipelineComponentException extends ConfigurableException {

    public PipelineComponentException(String errorDescription) {
        super(errorDescription);
    }

    public PipelineComponentException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
