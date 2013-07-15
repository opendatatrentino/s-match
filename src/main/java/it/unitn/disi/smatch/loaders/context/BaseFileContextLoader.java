package it.unitn.disi.smatch.loaders.context;

import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.data.trees.IBaseNode;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Base class for file loaders.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class BaseFileContextLoader<E extends IBaseContext<? extends IBaseNode>> extends BaseContextLoader<E> implements IBaseContextLoader<E> {

    private static final Logger log = Logger.getLogger(BaseFileContextLoader.class);

    public E loadContext(String fileName) throws ContextLoaderException {
        E result = null;
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            try {
                result = process(input);
                createIds(result);
                log.info("Parsed nodes: " + nodesParsed);
            } catch (IOException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new ContextLoaderException(errMessage, e);
            } finally {
                input.close();
            }
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextLoaderException(errMessage, e);
        }
        return result;
    }

    abstract protected E process(BufferedReader input) throws IOException, ContextLoaderException;

    public ILoader.LoaderType getType() {
        return ILoader.LoaderType.FILE;
    }
}