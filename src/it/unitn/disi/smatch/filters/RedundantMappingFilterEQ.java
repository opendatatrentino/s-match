package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;

/**
 * Filters the matrix according to the minimal links paper, expanding EQ into MG&LG.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RedundantMappingFilterEQ extends RedundantMappingFilter {

    //because in filtering we have a matrix and we do not "discover" links
    //we need to check ancestors and descendants, and not only parents and children
    //otherwise, in case of series of redundant links we remove first by checking parent
    //and then all the rest is not removed because of the "gap"

    protected boolean verifyCondition1(IContextMapping<INode> mapping, IMappingElement<INode> e) {
        boolean result =
                findRelation(IMappingElement.LESS_GENERAL, e.getSource().getAncestors(), e.getTarget(), mapping) ||
                        findRelation(IMappingElement.EQUIVALENCE, e.getSource().getAncestors(), e.getTarget(), mapping) ||

                        findRelation(IMappingElement.LESS_GENERAL, e.getSource(), e.getTarget().getDescendants(), mapping) ||
                        findRelation(IMappingElement.EQUIVALENCE, e.getSource(), e.getTarget().getDescendants(), mapping) ||

                        findRelation(IMappingElement.LESS_GENERAL, e.getSource().getAncestors(), e.getTarget().getDescendants(), mapping) ||
                        findRelation(IMappingElement.EQUIVALENCE, e.getSource().getAncestors(), e.getTarget().getDescendants(), mapping);
        return result;
    }

    protected boolean verifyCondition2(IContextMapping<INode> mapping, IMappingElement<INode> e) {
        boolean result =
                findRelation(IMappingElement.MORE_GENERAL, e.getSource(), e.getTarget().getAncestors(), mapping) ||
                        findRelation(IMappingElement.EQUIVALENCE, e.getSource(), e.getTarget().getAncestors(), mapping) ||

                        findRelation(IMappingElement.MORE_GENERAL, e.getSource().getDescendants(), e.getTarget(), mapping) ||
                        findRelation(IMappingElement.EQUIVALENCE, e.getSource().getDescendants(), e.getTarget(), mapping) ||

                        findRelation(IMappingElement.MORE_GENERAL, e.getSource().getDescendants(), e.getTarget().getAncestors(), mapping) ||
                        findRelation(IMappingElement.EQUIVALENCE, e.getSource().getDescendants(), e.getTarget().getAncestors(), mapping);
        return result;
    }
}
