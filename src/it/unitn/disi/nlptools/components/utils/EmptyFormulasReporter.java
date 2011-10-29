package it.unitn.disi.nlptools.components.utils;

import it.unitn.disi.nlptools.components.PipelineComponentException;
import it.unitn.disi.nlptools.data.ILabel;
import it.unitn.disi.nlptools.pipelines.PipelineComponent;
import org.apache.log4j.Logger;

/**
 * Reports labels with empty formulas.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class EmptyFormulasReporter extends PipelineComponent {

    private static final Logger log = Logger.getLogger(EmptyFormulasReporter.class);

    public void process(ILabel label) throws PipelineComponentException {
        if (null == label.getFormula() || "".equals(label.getFormula())) {
            log.debug("Empty formula for label: " + label.getText());
        }
    }
}