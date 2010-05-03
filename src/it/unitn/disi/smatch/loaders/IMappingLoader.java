package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.mappings.IMapping;

/**
 * Interface for mapping loaders.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMappingLoader {

    /**
     * Loads the mapping.
     *
     * @param ctxSource interface of data structure of source
     * @param ctxTarget interface of data structure of target
     * @param fileName  file with a mapping
     * @return interface to a mapping
     * @throws SMatchException SMatchException
     */
    public IMapping loadMapping(IContext ctxSource, IContext ctxTarget, String fileName) throws SMatchException;
}
