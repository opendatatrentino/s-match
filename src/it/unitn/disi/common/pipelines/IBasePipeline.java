package it.unitn.disi.common.pipelines;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.nlptools.components.PipelineComponentException;

/**
 * Common pipeline interface.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IBasePipeline<E> extends IConfigurable {

    /**
     * Processes an instance of E.
     *
     * @param instance short label to process
     * @throws it.unitn.disi.nlptools.components.PipelineComponentException
     *          PipelineComponentException
     */
    void process(E instance) throws PipelineComponentException;

}