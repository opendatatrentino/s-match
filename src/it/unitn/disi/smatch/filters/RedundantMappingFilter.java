package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Filters mapping according to minimal links paper.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RedundantMappingFilter extends Configurable implements IMappingFilter {

    private static final Logger log = Logger.getLogger(RedundantMappingFilter.class);

    public IContextMapping<INode> filter(IContextMapping<INode> mapping) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering started...");
        }
        long start = System.currentTimeMillis();

        IContext sourceContext = mapping.getSourceContext();
        IContext targetContext = mapping.getTargetContext();

        // get the nodes of the contexts
        List<INode> sourceNodes = sourceContext.getAllNodes();
        List<INode> targetNodes = targetContext.getAllNodes();

        for (int i = 0; i < sourceNodes.size(); i++) {
            sourceNodes.get(i).getNodeData().setIndex(i);
        }
        for (int i = 0; i < targetNodes.size(); i++) {
            targetNodes.get(i).getNodeData().setIndex(i);
        }

        long counter = 0;
        long total = (long) mapping.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //check each mapping
        for (IMappingElement<INode> e : mapping) {
            if (isRedundant(mapping, e)) {
                mapping.setRelation(e.getSource(), e.getTarget(), IMappingElement.IDK);
            }

            counter++;
            if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                log.info(100 * counter / total + "%");
            }
        }

        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering finished: " + (System.currentTimeMillis() - start) + " ms");
        }
        return mapping;
    }

    /**
     * Checks the relation between source and target is redundant or not for minimal mapping.
     *

     * @param mapping a mapping
     * @param e a mapping element 
     * @return true for redundant relation
     */
    private boolean isRedundant(IContextMapping<INode> mapping, IMappingElement<INode> e) {
        switch (e.getRelation()) {
            case IMappingElement.LESS_GENERAL: {
                if (verifyCondition1(mapping, e)) {
                    return true;
                }
                break;
            }
            case IMappingElement.MORE_GENERAL: {
                if (verifyCondition2(mapping, e)) {
                    return true;
                }
                break;
            }
            case IMappingElement.DISJOINT: {
                if (verifyCondition3(mapping, e)) {
                    return true;
                }
                break;
            }
            case IMappingElement.EQUIVALENCE: {
                if (verifyCondition4(mapping, e)) {
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

    //because in filtering we have a matrix and we do not "discover" links
    //we need to check ancestors and descendants, and not only parents and children
    //otherwise, in case of series of redundant links we remove first by checking parent
    //and then all the rest is not removed because of the "gap"


    protected boolean verifyCondition1(IContextMapping<INode> mapping, IMappingElement<INode> e) {
        boolean result =
                findRelation(IMappingElement.LESS_GENERAL, e.getSource().getAncestors(), e.getTarget(), mapping) ||
                        findRelation(IMappingElement.LESS_GENERAL, e.getSource(), e.getTarget().getDescendants(), mapping) ||
                        findRelation(IMappingElement.LESS_GENERAL, e.getSource().getAncestors(), e.getTarget().getDescendants(), mapping);
        return result;
    }

    protected boolean verifyCondition2(IContextMapping<INode> mapping, IMappingElement<INode> e) {
        boolean result =
                findRelation(IMappingElement.MORE_GENERAL, e.getSource(), e.getTarget().getAncestors(), mapping) ||
                        findRelation(IMappingElement.MORE_GENERAL, e.getSource().getDescendants(), e.getTarget(), mapping) ||
                        findRelation(IMappingElement.MORE_GENERAL, e.getSource().getDescendants(), e.getTarget().getAncestors(), mapping);
        return result;
    }

    protected boolean verifyCondition3(IContextMapping<INode> mapping, IMappingElement<INode> e) {
        boolean result =
                findRelation(IMappingElement.DISJOINT, e.getSource(), e.getTarget().getAncestors(), mapping) ||
                        findRelation(IMappingElement.DISJOINT, e.getSource().getAncestors(), e.getTarget(), mapping) ||
                        findRelation(IMappingElement.DISJOINT, e.getSource().getAncestors(), e.getTarget().getAncestors(), mapping);
        return result;
    }

    protected boolean verifyCondition4(IContextMapping<INode> mapping, IMappingElement<INode> e) {
        boolean result =
                (findRelation(IMappingElement.EQUIVALENCE, e.getSource(), e.getTarget().getAncestors(), mapping) &&
                        findRelation(IMappingElement.EQUIVALENCE, e.getSource().getAncestors(), e.getTarget(), mapping))
                        ||
                        (findRelation(IMappingElement.EQUIVALENCE, e.getSource(), e.getTarget().getDescendants(), mapping) &&
                                findRelation(IMappingElement.EQUIVALENCE, e.getSource().getDescendants(), e.getTarget(), mapping))
                        ||
                        (findRelation(IMappingElement.EQUIVALENCE, e.getSource().getAncestors(), e.getTarget().getDescendants(), mapping) &&
                                findRelation(IMappingElement.EQUIVALENCE, e.getSource().getDescendants(), e.getTarget().getAncestors(), mapping));
        return result;
    }

    public boolean findRelation(char relation, List<INode> sourceNodes, INode targetNode, IContextMapping<INode> mapping) {
        for (INode sourceNode : sourceNodes) {
            if (relation == getRelation(mapping, sourceNode, targetNode)) {
                return true;
            }
        }
        return false;
    }

    public boolean findRelation(char relation, INode sourceNode, List<INode> targetNodes, IContextMapping<INode> mapping) {
        for (INode targetNode : targetNodes) {
            if (relation == getRelation(mapping, sourceNode, targetNode)) {
                return true;
            }
        }
        return false;
    }

    public boolean findRelation(char relation, List<INode> sourceNodes, List<INode> targetNodes, IContextMapping<INode> mapping) {
        for (INode sourceNode : sourceNodes) {
            for (INode targetNode : targetNodes) {
                if (relation == getRelation(mapping, sourceNode, targetNode)) {
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
