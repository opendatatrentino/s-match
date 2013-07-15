package it.unitn.disi.nlptools;

import it.unitn.disi.common.components.ConfigurableException;
import it.unitn.disi.common.components.IConfigurable;

/**
 * An interface for NLP operations on short labels.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface INLPTools extends IConfigurable {

    /**
     * Returns a pipeline instance.
     *
     * @return a pipeline instance
     * @throws ConfigurableException ConfigurableException
     */
    ILabelPipeline getPipeline() throws ConfigurableException;
}