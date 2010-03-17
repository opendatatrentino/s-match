package it.unitn.disi.smatch.loaders;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.SMatchException;

/**
 * Interface allowing to load contexts from various representations such as files, DBs, etc
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ILoader {

    /**
     * Loads the context to a file or database.
     *
     * @param fileName file to load (or DB connection string)
     * @return interface to data structure of context
     * @throws SMatchException
     */
    public IContext loadContext(String fileName) throws SMatchException;
}
