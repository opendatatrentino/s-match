package it.unitn.disi.smatch.classifiers;

import it.unitn.disi.smatch.data.IContext;

/**
 * Does nothing.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
//TODO This class implements the classifier component and build cNode formulas. So why does nothing?
public class ZeroClassifier implements IClassifier {

    public IContext buildCNodeFormulas(IContext context) {
        return context;
    }
}
