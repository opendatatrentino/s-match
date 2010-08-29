package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.loaders.ILoader;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Base class for file context renderers.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class BaseFileContextRenderer extends BaseContextRenderer {

    private static final Logger log = Logger.getLogger(BaseFileContextRenderer.class);

    @Override
    protected void process(IContext context, String fileName) throws ContextRendererException {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
            try {
                process(context, out);
            } catch (IOException e) {
                final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.error(errMessage, e);
                throw new ContextRendererException(errMessage, e);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            final String errMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(errMessage, e);
            throw new ContextRendererException(errMessage, e);
        }
    }

    protected abstract void process(IContext context, BufferedWriter out) throws IOException, ContextRendererException;

    public ILoader.LoaderType getType() {
        return ILoader.LoaderType.FILE;
    }
}
