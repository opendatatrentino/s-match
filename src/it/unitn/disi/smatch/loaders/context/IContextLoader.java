package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.trees.IContext;

/**
 * Interface allowing to load contexts from various representations such as files, DBs, etc.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IContextLoader extends IConfigurable {

    /**
     * Loads the context to a file or database.
     *
     * @param fileName file to load (or DB connection string)
     * @return interface to data structure of context
     * @throws ContextLoaderException ContextLoaderException
     */
    IContext loadContext(String fileName) throws ContextLoaderException;
}
