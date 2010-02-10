package it.unitn.disi.smatch.preprocessors;

import it.unitn.disi.smatch.data.IContext;

/**
 * An interface for preprocessors.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IPreprocessor {

    /**
     * This method translates natural language labels of a context into
     * a logical formulas.
     *
     * @param context context to be prepocessed
     * @return preprocessed context
     */
    public IContext preprocess(IContext context);
}
