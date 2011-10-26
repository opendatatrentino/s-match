package it.unitn.disi.nlptools;

import it.unitn.disi.common.components.IConfigurable;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ISentence;

/**
 * Interface for processing of short sentences.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface INLPPipeline extends IConfigurable {

    /**
     * Processes a short sentence.
     *
     * @param sentence short sentence to process
     * @return interface to a processed sentence instance
     */
    ISentence process(String sentence) throws PipelineComponentException;

    /**
     * Processes a short sentence.
     *
     * @param sentence short sentence to process
     */
    void process(ISentence sentence) throws PipelineComponentException;
}