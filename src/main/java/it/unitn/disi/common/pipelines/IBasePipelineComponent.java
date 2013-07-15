package it.unitn.disi.common.pipelines;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.nlptools.components.PipelineComponentException;

/**
 * Interface for base pipeline component.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IBasePipelineComponent<E> extends IConfigurable {

    /**
     * Processes an instance of a pipeline subject matter.
     *
     * @param instance an instance of a pipeline subject matter.
     * @throws PipelineComponentException PipelineComponentException
     */
    void process(E instance) throws PipelineComponentException;

    /**
     * Occurs before pipeline starts processing an instance of a pipeline subject matter.
     *
     * @throws PipelineComponentException PipelineComponentException
     * @param instance an instance of a pipeline subject matter.
     */
    void beforeInstanceProcessing(E instance) throws PipelineComponentException;

    /**
     * Occurs after pipeline finished processing an instance of a pipeline subject matter.
     *
     * @throws PipelineComponentException PipelineComponentException
     * @param instance an instance of a pipeline subject matter.
     */
    void afterInstanceProcessing(E instance) throws PipelineComponentException;
}
