package it.unitn.disi.common.pipelines;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.nlptools.components.PipelineComponentException;

/**
 * Interface for base pipeline component.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IBasePipelineComponent<E> extends IConfigurable {

    void process(E instance) throws PipelineComponentException;
}
