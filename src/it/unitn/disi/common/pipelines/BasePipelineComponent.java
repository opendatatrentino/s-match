package it.unitn.disi.common.pipelines;

import it.unitn.disi.common.components.Configurable;
import it.unitn.disi.nlptools.components.PipelineComponentException;

/**
 * Base class for pipeline components.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class BasePipelineComponent<E> extends Configurable implements IBasePipelineComponent<E> {

    public void beforeInstanceProcessing(E instance) throws PipelineComponentException {
        //nop
    }

    public void afterInstanceProcessing(E instance) throws PipelineComponentException {
        //nop
    }
}