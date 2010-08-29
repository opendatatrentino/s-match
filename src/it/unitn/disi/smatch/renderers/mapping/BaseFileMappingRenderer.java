package it.unitn.disi.smatch.renderers.mapping;

import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Base class for file mapping renderers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class BaseFileMappingRenderer extends BaseMappingRenderer {

    private static final Logger log = Logger.getLogger(BaseFileMappingRenderer.class);

    @Override
    protected void process(IContextMapping<INode> mapping, String outputFile) throws MappingRendererException {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            try {
                process(mapping, out);
            } catch (IOException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new MappingRendererException(errMessage, e);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new MappingRendererException(errMessage, e);
        }
    }

    protected abstract void process(IContextMapping<INode> mapping, BufferedWriter out) throws IOException, MappingRendererException;

    public ILoader.LoaderType getType() {
        return ILoader.LoaderType.FILE;
    }
}