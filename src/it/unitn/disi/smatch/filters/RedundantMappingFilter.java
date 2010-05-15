package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.Configurable;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.IMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.Mapping;
import it.unitn.disi.smatch.data.mappings.MappingElement;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
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

    protected IMatchMatrix CnodMatrix;

    public IMapping filter(IMapping mapping) {
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

        //TODO rewrite algorithm to use mapping
        CnodMatrix = MatrixFactory.getInstance(sourceNodes.size(), targetNodes.size());
        for (IMappingElement e : mapping) {
            CnodMatrix.setElement(e.getSourceNode().getNodeData().getIndex(), e.getTargetNode().getNodeData().getIndex(), e.getRelation());
        }

        long counter = 0;
        long total = (long) sourceNodes.size() * (long) targetNodes.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //check each mapping
        for (int i = 0; i < CnodMatrix.getX(); i++) {
            for (int j = 0; j < CnodMatrix.getY(); j++) {
                char relation = CnodMatrix.getElement(i, j);
                if (IMappingElement.IDK != relation) {
                    if (isRedundant(sourceNodes.get(i), targetNodes.get(j), relation)) {
                        CnodMatrix.setElement(i, j, IMappingElement.IDK);
                    }
                }

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
            }
        }

        IMapping result = new Mapping(sourceContext, targetContext);
        for (int i = 0; i < sourceNodes.size(); i++) {
            INode sourceNode = sourceNodes.get(i);
            for (int j = 0; j < targetNodes.size(); j++) {
                INode targetNode = targetNodes.get(j);
                char relation = CnodMatrix.getElement(i, j);
                if (IMappingElement.IDK != relation) {
                    result.add(new MappingElement(sourceNode, targetNode, relation));
                }
            }
        }


        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering finished: " + (System.currentTimeMillis() - start) + " ms");
        }
        return result;
    }

    /**
     * Checks the relation between source and target is redundant or not for minimal mapping.
     *
     * @param C interface of source node
     * @param D interface of target node
     * @param R relation between source and target node
     * @return true for redundant relation
     */
    private boolean isRedundant(INode C, INode D, char R) {
        switch (R) {
            case IMappingElement.LESS_GENERAL: {
                if (verifyCondition1(C, D)) {
                    return true;
                }
                break;
            }
            case IMappingElement.MORE_GENERAL: {
                if (verifyCondition2(C, D)) {
                    return true;
                }
                break;
            }
            case IMappingElement.DISJOINT: {
                if (verifyCondition3(C, D)) {
                    return true;
                }
                break;
            }
            case IMappingElement.EQUIVALENCE: {
                if (verifyCondition4(C, D)) {
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


    protected boolean verifyCondition1(INode C, INode D) {
       	boolean result =
                findRelation(IMappingElement.LESS_GENERAL, C.getAncestors(), D) ||
                        findRelation(IMappingElement.LESS_GENERAL, C, D.getDescendants()) ||
                        findRelation(IMappingElement.LESS_GENERAL, C.getAncestors(), D.getDescendants());
        return result;
    }

    protected boolean verifyCondition2(INode C, INode D) {
        boolean result =
                findRelation(IMappingElement.MORE_GENERAL, C, D.getAncestors()) ||
                        findRelation(IMappingElement.MORE_GENERAL, C.getDescendants(), D) ||
                        findRelation(IMappingElement.MORE_GENERAL, C.getDescendants(), D.getAncestors());
        return result;
    }

    protected boolean verifyCondition3(INode C, INode D) {
        boolean result =
                findRelation(IMappingElement.DISJOINT, C, D.getAncestors()) ||
                        findRelation(IMappingElement.DISJOINT, C.getAncestors(), D) ||
                        findRelation(IMappingElement.DISJOINT, C.getAncestors(), D.getAncestors());
        return result;
    }

    protected boolean verifyCondition4(INode C, INode D) {
        boolean result =
                (findRelation(IMappingElement.EQUIVALENCE, C, D.getAncestors()) &&
                        findRelation(IMappingElement.EQUIVALENCE, C.getAncestors(), D))
                        ||
                        (findRelation(IMappingElement.EQUIVALENCE, C, D.getDescendants()) &&
                                findRelation(IMappingElement.EQUIVALENCE, C.getDescendants(), D))
                        ||
                        (findRelation(IMappingElement.EQUIVALENCE, C.getAncestors(), D.getDescendants()) &&
                                findRelation(IMappingElement.EQUIVALENCE, C.getDescendants(), D.getAncestors()));
        return result;
    }

    public boolean findRelation(char relation, INode sourceNode, INode targetNode) {
        return (null != sourceNode) && (null != targetNode) && (getRelation(sourceNode, targetNode) == relation);
    }

    public boolean findRelation(char relation, List<INode> sourceNodes, INode targetNode) {
        for (INode sourceNode : sourceNodes) {
            if (relation == getRelation(sourceNode, targetNode)) {
                return true;
            }
        }
        return false;
    }

    public boolean findRelation(char relation, INode sourceNode, List<INode> targetNodes) {
        for (INode targetNode : targetNodes) {
            if (relation == getRelation(sourceNode, targetNode)) {
                return true;
            }
        }
        return false;
    }

    public boolean findRelation(char relation, List<INode> sourceNodes, List<INode> targetNodes) {
        for (INode sourceNode : sourceNodes) {
            for (INode targetNode : targetNodes) {
                if (relation == getRelation(sourceNode, targetNode)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected char getRelation(INode a, INode b) {
        return CnodMatrix.getElement(a.getNodeData().getIndex(), b.getNodeData().getIndex());
    }
}
