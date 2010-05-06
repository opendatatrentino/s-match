package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.SMatchException;

/**
 * Exception for Mapping Renderers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class MappingRendererException extends SMatchException {

    public MappingRendererException(String errorDescription) {
        super(errorDescription);
    }

    public MappingRendererException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
