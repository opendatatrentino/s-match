package it.unitn.disi.nlptools.pipelines;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ISentence;

/**
 * A component of a pipeline.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface IPipelineComponent extends IConfigurable {

    void process(ISentence sentence) throws PipelineComponentException;

}