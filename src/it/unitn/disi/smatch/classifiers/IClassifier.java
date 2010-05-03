package it.unitn.disi.smatch.classifiers;

import it.unitn.disi.smatch.data.IContext;

/**
 * Interface for classifiers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IClassifier {
    /**
     * Constructs concept@node formulas for all the nodes in the context.
     * Must be executed after buildCLabs.
     *
     * @param context the context with concept at label formula
     */
    public void buildCNodeFormulas(IContext context);
}
