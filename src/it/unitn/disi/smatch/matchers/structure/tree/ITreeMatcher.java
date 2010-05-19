package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;

/**
 * An interface for tree matchers.
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface ITreeMatcher extends IConfigurable {

    /**
     * Matches two trees.
     *
     * @param sourceContext interface of source context
     * @param targetContext interface of target context
     * @param ClabMatrix    a matrix of relations between ACoLs of contexts
     * @return a mapping between nodes
     * @throws TreeMatcherException TreeMatcherException
     */
    IContextMapping<INode> treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) throws TreeMatcherException;
}
