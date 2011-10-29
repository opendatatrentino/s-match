package it.unitn.disi.nlptools;

import it.unitn.disi.common.pipelines.IBasePipeline;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;

/**
 * Interface for short label processing pipelines.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface ILabelPipeline extends IBasePipeline<ILabel> {

    /**
     * Processes a short label.
     *
     * @param label short label to process
     * @return interface to a processed label instance
     * @throws it.unitn.disi.nlptools.components.PipelineComponentException
     *          PipelineComponentException
     */
    ILabel process(String label) throws PipelineComponentException;
}