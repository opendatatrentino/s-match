package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Generates entailed mappings according to pseudo code from minimal mappings paper.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RedundantGeneratorMappingFilter extends Configurable implements IMappingFilter {

    private static final Logger log = Logger.getLogger(RedundantGeneratorMappingFilter.class);

    public IContextMapping<INode> filter(IContextMapping<INode> mapping) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering started...");
        }
        long start = System.currentTimeMillis();

        IContext sourceContext = mapping.getSourceContext();
        IContext targetContext = mapping.getTargetContext();

        long counter = 0;
        long total = (long) (sourceContext.getRoot().getDescendantCount() + 1) * (long) (targetContext.getRoot().getDescendantCount() + 1);
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        for (Iterator<INode> i = sourceContext.getRoot().getSubtree(); i.hasNext();) {
            INode source = i.next();
            for (Iterator<INode> j = targetContext.getRoot().getSubtree(); j.hasNext();) {
                INode target = j.next();
                mapping.setRelation(source, target, computeMapping(mapping, source, target));

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
            }
        }

        for (IMappingElement<INode> e : mapping) {
            switch (e.getRelation()) {
                case IMappingElement.ENTAILED_LESS_GENERAL: {
                    mapping.setRelation(e.getSource(), e.getTarget(), IMappingElement.LESS_GENERAL);
                    break;
                }
                case IMappingElement.ENTAILED_MORE_GENERAL: {
                    mapping.setRelation(e.getSource(), e.getTarget(), IMappingElement.MORE_GENERAL);
                    break;
                }
                case IMappingElement.ENTAILED_DISJOINT: {
                    mapping.setRelation(e.getSource(), e.getTarget(), IMappingElement.DISJOINT);
                    break;
                }
                default: {
                }
            }
        }

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering finished: " + (System.currentTimeMillis() - start) + " ms");
        }

        return mapping;
    }

    private char computeMapping(IContextMapping<INode> mapping, INode source, INode target) {
        final char relation = mapping.getRelation(source, target);
        if (IMappingElement.DISJOINT == relation) {
            return IMappingElement.DISJOINT;
        }
        if (isRedundant(mapping, source, target, IMappingElement.DISJOINT)) {
            return IMappingElement.ENTAILED_DISJOINT;
        }
        if (IMappingElement.EQUIVALENCE == relation) {
            return IMappingElement.EQUIVALENCE;
        }
        boolean isLG = (IMappingElement.LESS_GENERAL == relation || isRedundant(mapping, source, target, IMappingElement.LESS_GENERAL));
        boolean isMG = (IMappingElement.MORE_GENERAL == relation || isRedundant(mapping, source, target, IMappingElement.MORE_GENERAL));
        if (isLG && isMG) {
            return IMappingElement.EQUIVALENCE;
        }
        if (isLG) {
            if (IMappingElement.LESS_GENERAL == relation) {
                return IMappingElement.LESS_GENERAL;
            } else {
                return IMappingElement.ENTAILED_LESS_GENERAL;
            }
        }
        if (isMG) {
            if (IMappingElement.MORE_GENERAL == relation) {
                return IMappingElement.MORE_GENERAL;
            } else {
                return IMappingElement.ENTAILED_MORE_GENERAL;
            }
        }

        return IMappingElement.IDK;
    }

    /**
     * Checks whether the relation between source and target is redundant or not for minimal mapping.
     *
     * @param mapping a mapping
     * @param source  source
     * @param target  target
     * @param R       relation between source and target node  @return true for redundant relation
     * @return whether the relation between source and target is redundant
     */
    private boolean isRedundant(IContextMapping<INode> mapping, INode source, INode target, char R) {
        switch (R) {
            case IMappingElement.LESS_GENERAL: {
                if (verifyCondition1(mapping, source, target)) {
                    return true;
                }
                break;
            }
            case IMappingElement.MORE_GENERAL: {
                if (verifyCondition2(mapping, source, target)) {
                    return true;
                }
                break;
            }
            case IMappingElement.DISJOINT: {
                if (verifyCondition3(mapping, source, target)) {
                    return true;
                }
                break;
            }
            case IMappingElement.EQUIVALENCE: {
                if (verifyCondition1(mapping, source, target) && verifyCondition2(mapping, source, target)) {
                    return true;
                }
                break;
            }
            default: {
                return false;
            }

        }//switch

        return false;
    }

    //because in filtering we do not "discover" links
    //we need to check ancestors and descendants, and not only parents and children
    //otherwise, in case of series of redundant links we remove first by checking parent
    //and then all the rest is not removed because of the "gap"

    protected boolean verifyCondition1(IContextMapping<INode> mapping, INode source, INode target) {
        return findRelation(mapping, IMappingElement.LESS_GENERAL, source.getAncestors(), target) ||
                findRelation(mapping, IMappingElement.LESS_GENERAL, source, target.getDescendants()) ||
                findRelation(mapping, IMappingElement.LESS_GENERAL, source.getAncestors(), target.getDescendants()) ||

                findRelation(mapping, IMappingElement.EQUIVALENCE, source.getAncestors(), target) ||
                findRelation(mapping, IMappingElement.EQUIVALENCE, source, target.getDescendants()) ||
                findRelation(mapping, IMappingElement.EQUIVALENCE, source.getAncestors(), target.getDescendants());
    }

    protected boolean verifyCondition2(IContextMapping<INode> mapping, INode source, INode target) {
        return findRelation(mapping, IMappingElement.MORE_GENERAL, source, target.getAncestors()) ||
                findRelation(mapping, IMappingElement.MORE_GENERAL, source.getDescendants(), target) ||
                findRelation(mapping, IMappingElement.MORE_GENERAL, source.getDescendants(), target.getAncestors()) ||

                findRelation(mapping, IMappingElement.EQUIVALENCE, source, target.getAncestors()) ||
                findRelation(mapping, IMappingElement.EQUIVALENCE, source.getDescendants(), target) ||
                findRelation(mapping, IMappingElement.EQUIVALENCE, source.getDescendants(), target.getAncestors());
    }

    protected boolean verifyCondition3(IContextMapping<INode> mapping, INode source, INode target) {
        return findRelation(mapping, IMappingElement.DISJOINT, source, target.getAncestors()) ||
                findRelation(mapping, IMappingElement.DISJOINT, source.getAncestors(), target) ||
                findRelation(mapping, IMappingElement.DISJOINT, source.getAncestors(), target.getAncestors());
    }

    public boolean findRelation(IContextMapping<INode> mapping, char relation, Iterator<INode> sourceNodes, INode targetNode) {
        while (sourceNodes.hasNext()) {
            if (relation == getRelation(mapping, sourceNodes.next(), targetNode)) {
                return true;
            }
        }
        return false;
    }

    public boolean findRelation(IContextMapping<INode> mapping, char relation, INode sourceNode, Iterator<INode> targetNodes) {
        while (targetNodes.hasNext()) {
            if (relation == getRelation(mapping, sourceNode, targetNodes.next())) {
                return true;
            }
        }
        return false;
    }

    public boolean findRelation(IContextMapping<INode> mapping, char relation, Iterator<INode> sourceNodes, Iterator<INode> targetNodes) {
        while (sourceNodes.hasNext()) {
            INode sourceNode = sourceNodes.next();
            while (targetNodes.hasNext()) {
                if (relation == getRelation(mapping, sourceNode, targetNodes.next())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected char getRelation(IContextMapping<INode> mapping, INode a, INode b) {
        return mapping.getRelation(a, b);
    }
}
