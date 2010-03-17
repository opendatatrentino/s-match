package it.unitn.disi.smatch.filters;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.INode;

import java.util.Vector;

/**
 * // TODO needs comment
 *
 * @author Mikalai Yatskevich mikalai.yatskevich@comlab.ox.ac.uk
 */
public class DefaultFilter implements IFilter {
    public IMatchMatrix filter(Vector args) {
        String fileName = (String) args.get(0);
        IMatchMatrix CnodMatrix = (IMatchMatrix) args.get(1);
        IMatchMatrix ClabMatrix = (IMatchMatrix) args.get(2);
        IContext sourceContext = (IContext) args.get(3);
        IContext targetContext = (IContext) args.get(4);

        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();
        return filterMatrix(CnodMatrix, sourceNodes.size(), targetNodes.size());

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

}
