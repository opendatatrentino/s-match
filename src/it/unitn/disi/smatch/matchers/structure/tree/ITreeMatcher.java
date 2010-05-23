package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.components.IConfigurable;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;

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
     * @param acolMapping   mapping between ACoLs of contexts
     * @return a mapping between nodes
     * @throws TreeMatcherException TreeMatcherException
     */
    IContextMapping<INode> treeMatch(IContext sourceContext, IContext targetContext, IContextMapping<IAtomicConceptOfLabel> acolMapping) throws TreeMatcherException;
}
