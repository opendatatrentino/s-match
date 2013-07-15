package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IBaseNode;
import it.unitn.disi.smatch.loaders.ILoader;

/**
 * Base interface for context loaders.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IBaseContextLoader<E extends IBaseContext<? extends IBaseNode>> extends IConfigurable {

    /**
     * Loads the context from a file or database.
     *
     * @param fileName file to load (or DB connection string)
     * @return interface to data structure of context
     * @throws ContextLoaderException ContextLoaderException
     */
    E loadContext(String fileName) throws ContextLoaderException;

    /**
     * Returns the description of the format.
     *
     * @return the description of the format
     */
    String getDescription();

    /**
     * Returns the type of the loader.
     *
     * @return the type of the loader
     */
    ILoader.LoaderType getType();
}