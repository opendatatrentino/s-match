package it.unitn.disi.smatch.renderers.context;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.smatch.data.trees.IBaseContext;
import it.unitn.disi.smatch.loaders.ILoader;

/**
 * Base interface for context renderers.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IBaseContextRenderer<E extends IBaseContext> extends IConfigurable {

    /**
     * Renders context into file or database.
     *
     * @param context  context to save
     * @param fileName file name or connection string to DB
     * @throws ContextRendererException ContextRendererException
     */
    void render(E context, String fileName) throws ContextRendererException;

    /**
     * Returns the description of the format.
     *
     * @return the description of the format
     */
    String getDescription();

    /**
     * Returns the type of the renderer.
     *
     * @return the type of the renderer
     */
    ILoader.LoaderType getType();
}