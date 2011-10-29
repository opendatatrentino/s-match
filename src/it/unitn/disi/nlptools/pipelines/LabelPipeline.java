package it.unitn.disi.nlptools.pipelines;

import it.unitn.disi.common.pipelines.BasePipeline;
import it.unitn.disi.nlptools.ILabelPipeline;
import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.data.Label;

/**
 * Label processing pipeline.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class LabelPipeline extends BasePipeline<ILabel> implements ILabelPipeline {

    public ILabel process(String label) throws PipelineComponentException {
        ILabel result = new Label(label);
        process(result);
        return result;
    }
}