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
 * Generates entailed mappings according to pseudo code from minimal mappings paper.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class RedundantGeneratorMappingFilter extends Configurable implements IMappingFilter {

    private static final Logger log = Logger.getLogger(RedundantGeneratorMappingFilter.class);

    protected IMatchMatrix cNodMatrix;
    List<INode> sourceNodes;
    List<INode> targetNodes;

    public IMapping filter(IMapping mapping) {
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Filtering started...");
        }
        long start = System.currentTimeMillis();

        IContext sourceContext = mapping.getSourceContext();
        IContext targetContext = mapping.getTargetContext();

        // get the nodes of the contexts
        sourceNodes = sourceContext.getAllNodes();
        targetNodes = targetContext.getAllNodes();

        for (int i = 0; i < sourceNodes.size(); i++) {
            sourceNodes.get(i).getNodeData().setIndex(i);
            sourceNodes.get(i).getNodeData().setSource(true);
        }
        for (int i = 0; i < targetNodes.size(); i++) {
            targetNodes.get(i).getNodeData().setIndex(i);
        }

        //TODO rewrite algorithm to use mapping
        cNodMatrix = MatrixFactory.getInstance(sourceNodes.size(), targetNodes.size());
        for (IMappingElement e : mapping) {
            cNodMatrix.setElement(e.getSourceNode().getNodeData().getIndex(), e.getTargetNode().getNodeData().getIndex(), e.getRelation());
        }

        long counter = 0;
        long total = (long) sourceNodes.size() * (long) targetNodes.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //check each mapping
        for (int i = 0; i < cNodMatrix.getX(); i++) {
            for (int j = 0; j < cNodMatrix.getY(); j++) {
                cNodMatrix.setElement(i, j, computeMapping(i, j));

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
            }
        }

        //replace ENTAILED with normal relations for rendering
        for (int i = 0; i < cNodMatrix.getX(); i++) {
            for (int j = 0; j < cNodMatrix.getY(); j++) {
                switch (cNodMatrix.getElement(i, j)) {
                    case IMappingElement.ENTAILED_LESS_GENERAL: {
                        cNodMatrix.setElement(i, j, IMappingElement.LESS_GENERAL);
                        break;
                    }
                    case IMappingElement.ENTAILED_MORE_GENERAL: {
                        cNodMatrix.setElement(i, j, IMappingElement.MORE_GENERAL);
                        break;
                    }
                    case IMappingElement.ENTAILED_DISJOINT: {
                        cNodMatrix.setElement(i, j, IMappingElement.DISJOINT);
                        break;
                    }
                    default: {
                    }
                }
            }
        }

        IMapping result = new Mapping(sourceContext, targetContext);
        for (int i = 0; i < sourceNodes.size(); i++) {
            INode sourceNode = sourceNodes.get(i);
            for (int j = 0; j < targetNodes.size(); j++) {
                INode targetNode = targetNodes.get(j);
                char relation = cNodMatrix.getElement(i, j);
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

    // TODO needs comments

    private char computeMapping(int i, int j) {
        if (IMappingElement.DISJOINT == cNodMatrix.getElement(i, j)) {
            return IMappingElement.DISJOINT;
        }
        //if (MatchManager.DISJOINT == cNodMatrix.getElement(i, j) || isRedundant(i, j, MatchManager.DISJOINT)) {
        if (isRedundant(i, j, IMappingElement.DISJOINT)) {
            return IMappingElement.ENTAILED_DISJOINT;
        }
        if (IMappingElement.EQUIVALENCE == cNodMatrix.getElement(i, j)) {
            return IMappingElement.EQUIVALENCE;
        }
        boolean isLG = (IMappingElement.LESS_GENERAL == cNodMatrix.getElement(i, j) || isRedundant(i, j, IMappingElement.LESS_GENERAL));
        boolean isMG = (IMappingElement.MORE_GENERAL == cNodMatrix.getElement(i, j) || isRedundant(i, j, IMappingElement.MORE_GENERAL));
        if (isLG && isMG) {
            return IMappingElement.EQUIVALENCE;
        }
        if (isLG) {
            if (IMappingElement.LESS_GENERAL == cNodMatrix.getElement(i, j)) {
                return IMappingElement.LESS_GENERAL;
            } else {
                return IMappingElement.ENTAILED_LESS_GENERAL;
            }
        }
        if (isMG) {
            if (IMappingElement.MORE_GENERAL == cNodMatrix.getElement(i, j)) {
                return IMappingElement.MORE_GENERAL;
            } else {
                return IMappingElement.ENTAILED_MORE_GENERAL;
            }
        }

        return IMappingElement.IDK;
    }

    private boolean isRedundant(int C, int D, char R) {
        return isRedundant(sourceNodes.get(C), targetNodes.get(D), R);
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
                if (verifyCondition1(C, D) && verifyCondition2(C, D)) {
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
        return findRelation(IMappingElement.LESS_GENERAL, C.getAncestors(), D) ||
                findRelation(IMappingElement.LESS_GENERAL, C, D.getDescendants()) ||
                findRelation(IMappingElement.LESS_GENERAL, C.getAncestors(), D.getDescendants()) ||

                findRelation(IMappingElement.EQUIVALENCE, C.getAncestors(), D) ||
                findRelation(IMappingElement.EQUIVALENCE, C, D.getDescendants()) ||
                findRelation(IMappingElement.EQUIVALENCE, C.getAncestors(), D.getDescendants());
    }

    protected boolean verifyCondition2(INode C, INode D) {
        return findRelation(IMappingElement.MORE_GENERAL, C, D.getAncestors()) ||
                findRelation(IMappingElement.MORE_GENERAL, C.getDescendants(), D) ||
                findRelation(IMappingElement.MORE_GENERAL, C.getDescendants(), D.getAncestors()) ||

                findRelation(IMappingElement.EQUIVALENCE, C, D.getAncestors()) ||
                findRelation(IMappingElement.EQUIVALENCE, C.getDescendants(), D) ||
                findRelation(IMappingElement.EQUIVALENCE, C.getDescendants(), D.getAncestors());
    }

    protected boolean verifyCondition3(INode C, INode D) {
        return findRelation(IMappingElement.DISJOINT, C, D.getAncestors()) ||
                findRelation(IMappingElement.DISJOINT, C.getAncestors(), D) ||
                findRelation(IMappingElement.DISJOINT, C.getAncestors(), D.getAncestors());
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
        return cNodMatrix.getElement(a.getNodeData().getIndex(), b.getNodeData().getIndex());
    }
}
