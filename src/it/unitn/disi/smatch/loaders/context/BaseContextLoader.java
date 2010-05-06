package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.Context;
import it.unitn.disi.smatch.data.IContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Base loader.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class BaseContextLoader extends Configurable implements IContextLoader {

    private static final Logger log = Logger.getLogger(TabContextLoader.class);

    public IContext loadContext(String fileName) throws ContextLoaderException {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loading nodes from " + fileName);
        }
        IContext result = internalLoad(fileName);

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Loaded nodes (" + fileName + "): " + result.getRoot().getDescendantCount());
        }

        return result;
    }

    protected IContext internalLoad(String fileName) throws ContextLoaderException {
        return null;
    }
}