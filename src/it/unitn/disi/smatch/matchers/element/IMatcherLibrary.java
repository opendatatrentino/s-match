package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

/**
 * Interface for collections of matchers, which perform element-level matching.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface IMatcherLibrary extends IConfigurable {

    /**
     * Performs Step 3 of semantic matching algorithm.
     *
     * @param sourceContext interface of source context
     * @param targetContext interface of target context
     * @return matrix of semantic relations between labels in both contexts
     * @throws MatcherLibraryException MatcherLibraryException
     */
    public IMatchMatrix elementLevelMatching(IContext sourceContext, IContext targetContext) throws MatcherLibraryException;
}
