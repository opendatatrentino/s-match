package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.data.INode;
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
    protected boolean verifyCondition1(INode C, INode D) {
        boolean result =
                findRelation(IMappingElement.LESS_GENERAL, C.getAncestors(), D) ||
                        findRelation(IMappingElement.EQUIVALENCE, C.getAncestors(), D) ||

                        findRelation(IMappingElement.LESS_GENERAL, C, D.getDescendants()) ||
                        findRelation(IMappingElement.EQUIVALENCE, C, D.getDescendants()) ||

                        findRelation(IMappingElement.LESS_GENERAL, C.getAncestors(), D.getDescendants()) ||
                        findRelation(IMappingElement.EQUIVALENCE, C.getAncestors(), D.getDescendants());
        return result;
    }

    protected boolean verifyCondition2(INode C, INode D) {
        boolean result =
                findRelation(IMappingElement.MORE_GENERAL, C, D.getAncestors()) ||
                        findRelation(IMappingElement.EQUIVALENCE, C, D.getAncestors()) ||

                        findRelation(IMappingElement.MORE_GENERAL, C.getDescendants(), D) ||
                        findRelation(IMappingElement.EQUIVALENCE, C.getDescendants(), D) ||

                        findRelation(IMappingElement.MORE_GENERAL, C.getDescendants(), D.getAncestors()) ||
                        findRelation(IMappingElement.EQUIVALENCE, C.getDescendants(), D.getAncestors());
        return result;
    }

}
