package it.unitn.disi.nlptools;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;

/**
 * Interface for processing of short labels.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface INLPPipeline extends IConfigurable {

    /**
     * Processes a short label.
     *
     * @param label short label to process
     * @return interface to a processed label instance
     * @throws it.unitn.disi.nlptools.components.PipelineComponentException PipelineComponentException
     */
    ILabel process(String label) throws PipelineComponentException;

    /**
     * Processes a short label.
     *
     * @param label short label to process
     * @throws it.unitn.disi.nlptools.components.PipelineComponentException PipelineComponentException
     */
    void process(ILabel label) throws PipelineComponentException;
}