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
     * @param instance an instance to process
     * @throws PipelineComponentException PipelineComponentException
     */
    void process(E instance) throws PipelineComponentException;

    /**
     * Should be called by a client before pipeline starts processing any instance of pipeline subject matter.
     *
     * @throws PipelineComponentException PipelineComponentException
     */
    void beforeProcessing() throws PipelineComponentException;

    /**
     * Should be called by a client after pipeline finished processing all instances of pipeline subject matter.
     *
     * @throws PipelineComponentException PipelineComponentException
     */
    void afterProcessing() throws PipelineComponentException;
}