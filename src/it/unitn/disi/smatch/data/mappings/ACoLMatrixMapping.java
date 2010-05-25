package it.unitn.disi.smatch.data.mappings;

import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.matrices.IMatchMatrixFactory;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;

/**
 * Mapping between acols based on a matrix.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ACoLMatrixMapping extends MatrixMapping<IAtomicConceptOfLabel> {

    public ACoLMatrixMapping(IMatchMatrixFactory factory, IContext source, IContext target) {
        super(factory, source, target);
    }

    @Override
    protected int getRowCount(IContext c) {
        return getACoLCount(c);
    }

    @Override
    protected int getColCount(IContext c) {
        return getACoLCount(c);
    }

    private int getACoLCount(IContext c) {
        int result = 0;
        for (INode node : c.getNodesList()) {
            for (IAtomicConceptOfLabel acol : node.getNodeData().getACoLsList()) {
                acol.setIndex(result);
                result++;
            }
        }
        return result;
    }
}
