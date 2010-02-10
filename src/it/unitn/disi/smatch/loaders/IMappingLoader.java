package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

import java.io.IOException;

/**
 * Interface for mapping loaders.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingLoader {

    public IMatchMatrix loadMapping(IContext ctxSource, IContext ctxTarget, String fileName) throws IOException;
}
