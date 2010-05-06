package it.unitn.disi.smatch.preprocessors;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.IContext;

/**
 * An interface for preprocessors.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IContextPreprocessor extends IConfigurable {

    /**
     * This method translates natural language labels of a context into a logical formulas.
     *
     * @param context context to be preprocessed
     * @throws ContextPreprocessorException ContextPreprocessorException
     */
    public void preprocess(IContext context) throws ContextPreprocessorException;
}