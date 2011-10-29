package it.unitn.disi.nlptools.pipelines;

import it.unitn.disi.common.pipelines.BasePipelineComponent;
import it.unitn.disi.nlptools.data.ILabel;

/**
 * Base component for label processing pipelines.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class LabelPipelineComponent extends BasePipelineComponent<ILabel> implements ILabelPipelineComponent {
}
