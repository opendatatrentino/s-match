package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.SMatchException;

/**
 * An interface for tree matchers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ITreeMatcher {

    /**
     * Matches two tress.
     *
     * @param sourceContext interface of source context
     * @param targetContext interface of target context
     * @param ClabMatrix    a matrix of relations between ACoLs of contexts
     * @return a matrix of relations between nodes
     */
    IMatchMatrix treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) throws SMatchException;
}
