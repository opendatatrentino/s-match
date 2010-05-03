package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.INode;

/**
 * Filters the matrix according to the minimal links paper, expanding EQ into MG&LG.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RedundantFilterEQ extends RedundantFilter {

    //because in filtering we have a matrix and we do not "discover" links
    //we need to check ancestors and descendants, and not only parents and children
    //otherwise, in case of series of redundant links we remove first by checking parent
    //and then all the rest is not removed because of the "gap"
    protected boolean verifyCondition1(INode C, INode D) {
        boolean result =
                findRelation(MatchManager.LESS_GENERAL_THAN, C.getAncestors(), D) ||
                        findRelation(MatchManager.SYNOMYM, C.getAncestors(), D) ||

                        findRelation(MatchManager.LESS_GENERAL_THAN, C, D.getDescendants()) ||
                        findRelation(MatchManager.SYNOMYM, C, D.getDescendants()) ||

                        findRelation(MatchManager.LESS_GENERAL_THAN, C.getAncestors(), D.getDescendants()) ||
                        findRelation(MatchManager.SYNOMYM, C.getAncestors(), D.getDescendants());
        return result;
    }

    protected boolean verifyCondition2(INode C, INode D) {
        boolean result =
                findRelation(MatchManager.MORE_GENERAL_THAN, C, D.getAncestors()) ||
                        findRelation(MatchManager.SYNOMYM, C, D.getAncestors()) ||

                        findRelation(MatchManager.MORE_GENERAL_THAN, C.getDescendants(), D) ||
                        findRelation(MatchManager.SYNOMYM, C.getDescendants(), D) ||

                        findRelation(MatchManager.MORE_GENERAL_THAN, C.getDescendants(), D.getAncestors()) ||
                        findRelation(MatchManager.SYNOMYM, C.getDescendants(), D.getAncestors());
        return result;
    }

}
