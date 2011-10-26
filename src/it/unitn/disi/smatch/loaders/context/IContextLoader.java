package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.loaders.ILoader;

/**
 * Interface for contexts loaders. Context loaders load contexts from various representations such as files, DBs, etc.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IContextLoader extends IConfigurable {

    /**
     * Loads the context from a file or database.
     *
     * @param fileName file to load (or DB connection string)
     * @return interface to data structure of context
     * @throws ContextLoaderException ContextLoaderException
     */
    IContext loadContext(String fileName) throws ContextLoaderException;

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
