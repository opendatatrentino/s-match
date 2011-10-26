package it.unitn.disi.nlptools.components.utils;

import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ISentence;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;
import org.apache.log4j.Logger;

/**
 * Reports sentences with empty formulas.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class EmptyFormulasReporter extends PipelineComponent {

    private static final Logger log = Logger.getLogger(EmptyFormulasReporter.class);

    public void process(ISentence sentence) throws PipelineComponentException {
        if (null == sentence.getFormula() || "".equals(sentence.getFormula())) {
            log.debug("Empty formula for sentence: " + sentence.getText());
        }
    }
}