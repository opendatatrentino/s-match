package it.unitn.disi.nlptools.components.mwrecognizers;

import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;

/**
 * Recognizes multiwords within non-consecutive tokens. Given:
 * [a] [cappella] [and] [gospel] [singing], it finds multiwords {0,1}, {0,1,4} and {3,4}.
 * <p/>
 * Optionally joins multiwords, replacing original tokens with multiwords.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ComplexMultiwordRecognizer extends PipelineComponent {

    public void process(ILabel label) throws PipelineComponentException {
        //TODO
    }
}
