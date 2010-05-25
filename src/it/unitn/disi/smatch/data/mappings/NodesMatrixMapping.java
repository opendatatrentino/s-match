package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.matrices.IMatchMatrixFactory;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * Mapping between context nodes based on a matrix.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class NodesMatrixMapping extends MatrixMapping<INode> {

    public NodesMatrixMapping(IMatchMatrixFactory factory, IContext source, IContext target) {
        super(factory, source, target);
    }

    @Override
    protected int getRowCount(IContext c) {
        return getNodeCount(c);
    }

    @Override
    protected int getColCount(IContext c) {
        return getNodeCount(c);
    }

    private int getNodeCount(IContext c) {
        int result = 0;
        for (INode node : c.getNodesList()) {
            node.setIndex(result);
            result++;
        }
        return result;
    }
}
