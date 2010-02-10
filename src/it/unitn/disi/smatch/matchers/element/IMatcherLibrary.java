package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.SMatchException;

/**
 * Interface for collections of matchers, which perform
 * element-level matching.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatcherLibrary {

    /**
     * Performs Step 3 of semantic matching algorithm
     *
     * @param sourceContext source context
     * @param targetContext target context
     * @return matrix of semantic relatons between nodes in both contexts
     */
    public IMatchMatrix elementLevelMatching(IContext sourceContext, IContext targetContext) throws SMatchException;
}
