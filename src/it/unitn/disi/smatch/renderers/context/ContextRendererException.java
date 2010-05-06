package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Context Renderers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ContextRendererException extends SMatchException {

    public ContextRendererException(String errorDescription) {
        super(errorDescription);
    }

    public ContextRendererException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
