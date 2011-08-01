package it.unitn.disi.smatch.loaders.mapping;

import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Base class for file mapping loaders.
 *
* @author <a rel="author" href="http://autayeu.com">Aliaksandr Autayeu</a>
 */
public abstract class BaseFileMappingLoader extends BaseMappingLoader {

    private static final Logger log = Logger.getLogger(BaseFileMappingLoader.class);

    @Override
    protected void process(IContextMapping<INode> mapping, IContext source, IContext target, String fileName) throws MappingLoaderException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            process(mapping, source, target, reader);
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MappingLoaderException(errMessage, e);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                    log.error(errMessage, e);
                }
            }
        }
    }

    protected abstract void process(IContextMapping<INode> mapping, IContext source, IContext target, BufferedReader reader) throws IOException, MappingLoaderException;

    public ILoader.LoaderType getType() {
        return ILoader.LoaderType.FILE;
    }
}
