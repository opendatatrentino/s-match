package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Base class for context renderers.
 * <p/>
 * Accepts sort parameter.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class BaseContextRenderer<E extends IBaseContext> extends Configurable implements IBaseContextRenderer<E> {

    private static final Logger log = Logger.getLogger(BaseContextRenderer.class);

    protected long counter;
    protected long total;
    protected long reportInt;

    protected final static String SORT_KEY = "sort";
    protected boolean sort = false;

    @Override
    public boolean setProperties(Properties newProperties) throws ConfigurableException {
        boolean result = super.setProperties(newProperties);
        if (result) {
            if (newProperties.containsKey(SORT_KEY)) {
                sort = Boolean.parseBoolean(newProperties.getProperty(SORT_KEY));
            }
        }
        return result;
    }

    public void render(E context, String fileName) throws ContextRendererException {
        counter = 0;
        total = context.getNodesList().size();
        reportInt = (total / 20) + 1;//i.e. report every 5%

        process(context, fileName);

        reportStats(context);
    }

    protected abstract void process(E context, String fileName) throws ContextRendererException;

    protected void reportStats(E context) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Rendered nodes: " + context.getNodesList().size());
        }
    }

    protected void reportProgress() {
        counter++;
        if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
            log.info(100 * counter / total + "%");
        }
    }
}