package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.INode;

import java.util.Hashtable;
import java.util.Vector;

/**
 * // TODO Need comments
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 */
public class IntegrationFilter implements IFilter {

    Hashtable<INode, Integer> sourceHash;
    Hashtable<INode, Integer> targetHash;
    Vector<Integer> toEraseX = new Vector<Integer>();
    Vector<Integer> toEraseY = new Vector<Integer>();

    public IMatchMatrix filter(Vector args) {

        resetGlobals();
        String fileName = (String) args.get(0);
        IMatchMatrix CnodMatrix = (IMatchMatrix) args.get(1);
        IMatchMatrix ClabMatrix = (IMatchMatrix) args.get(2);
        IContext sourceContext = (IContext) args.get(3);
        IContext targetContext = (IContext) args.get(4);

        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();
        sourceHash = fillHash(sourceNodes);
        targetHash = fillHash(targetNodes);

        for (int i = 0; i < sourceNodes.size(); i++) {
            INode sNode = sourceNodes.get(i);
            Vector<Integer> sourceKids = getDescendantsPos(sNode, sourceHash);
            for (int j = 0; j < targetNodes.size(); j++) {
                INode tNode = targetNodes.get(j);
                Vector<Integer> targetKids = getDescendantsPos(tNode, targetHash);

                if ((MatchManager.SYNOMYM == CnodMatrix.getElement(i, j)) ||
                        (MatchManager.LESS_GENERAL_THAN == CnodMatrix.getElement(i, j))) {
                    for (int k = 0; k < sourceKids.size(); k++) {
                        int pos = sourceKids.get(k);
                        if (MatchManager.LESS_GENERAL_THAN == CnodMatrix.getElement(pos, j)) {
                            toEraseX.add(pos);
                            toEraseY.add(j);
                        }
                    }

                }

                if ((MatchManager.SYNOMYM == CnodMatrix.getElement(i, j)) ||
                        (MatchManager.MORE_GENERAL_THAN == CnodMatrix.getElement(i, j))) {
                    for (int k = 0; k < targetKids.size(); k++) {
                        int pos = targetKids.get(k);
                        if (MatchManager.MORE_GENERAL_THAN == CnodMatrix.getElement(i, pos)) {
                            toEraseX.add(i);
                            toEraseY.add(pos);
                        }

                    }
                }

            }
        }

        for (int i = 0; i < toEraseX.size(); i++) {
            Integer x = toEraseX.get(i);
            Integer y = toEraseY.get(i);
            CnodMatrix.setElement(x, y, MatchManager.IDK_RELATION);
        }


        //return filterMatrix(CnodMatrix, sourceNodes.size(), targetNodes.size());
        return CnodMatrix;
    }

    /**
	 * Filters the matrix which have relation between nodes for minimal mapping.
	 *
	 * @param matrix relational matrix between nodes
	 * @param x number of source node
	 * @param y number of target nodes
	 * @return a matrix which have minimal relation
	 */
    public IMatchMatrix filterMatrix(IMatchMatrix matrix, int x, int y) {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (MatchManager.SYNOMYM == matrix.getElement(i, j)) {
                    for (int k = 0; k < x; k++)
                        if (k != i)
                            if (MatchManager.SYNOMYM != matrix.getElement(k, j)) {
                                matrix.setElement(k, j, MatchManager.IDK_RELATION);
                            }
                    for (int l = 0; l < y; l++)
                        if (l != j)
                            if (MatchManager.SYNOMYM != matrix.getElement(i, l)) {
                                matrix.setElement(i, l, MatchManager.IDK_RELATION);
                            }
                }
            }
        }
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if (MatchManager.WEAK_EQUIVALENCE == matrix.getElement(i, j))
                    matrix.setElement(i, j, MatchManager.SYNOMYM);
            }
        }
        return matrix;
    }

    Hashtable<INode, Integer> fillHash(Vector<INode> nodes) {
        Hashtable<INode, Integer> hash = new Hashtable<INode, Integer>();
        for (int i = 0; i < nodes.size(); i++) {
            INode iNode = nodes.get(i);
            hash.put(iNode, i);
        }
        return hash;
    }

    /**
     * Gets the position of all descendants of the given node.
     *
     * @param node the interface of node for which the descendants will be found
     * @param hash hashtable for nodes
     * @return a vector which have the all positions of descendants
     */
    Vector<Integer> getDescendantsPos(INode node, Hashtable<INode, Integer> hash) {
        Vector<Integer> result = new Vector<Integer>();
        Vector<INode> descendants = node.getChildren();
        for (int i = 0; i < descendants.size(); i++) {
            INode iNode = descendants.get(i);
            result.add(hash.get(iNode));
        }
        return result;
    }

    void resetGlobals() {
        sourceHash = new Hashtable<INode, Integer>();
        targetHash = new Hashtable<INode, Integer>();
        toEraseX = new Vector<Integer>();
        toEraseY = new Vector<Integer>();

    }

}
