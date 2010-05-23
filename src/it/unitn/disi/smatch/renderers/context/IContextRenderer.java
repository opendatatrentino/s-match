package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.trees.IContext;

/**
 * An interface for context renderers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IContextRenderer extends IConfigurable {

    /**
     * Renders context into file or database.
     *
     * @param context  context to save
     * @param fileName file name or connection string to DB
     * @throws ContextRendererException ContextRendererException
     */
    void render(IContext context, String fileName) throws ContextRendererException;
}
