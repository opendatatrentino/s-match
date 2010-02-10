package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.matchers.structure.node.DefaultNodeMatcher;
import it.unitn.disi.smatch.matchers.structure.node.INodeMatcher;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.SMatchConstants;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

public class DefaultTreeMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(DefaultTreeMatcher.class);

    private final INodeMatcher smatchMatcher = new DefaultNodeMatcher();

    public IMatchMatrix treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) throws SMatchException {
        // get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();

        //initialize CnodMatrix
        IMatchMatrix CnodMatrix = MatrixFactory.getInstance(sourceNodes.size(), targetNodes.size());

        //semantic relation for particular node matching task
        char relation;

        long counter = 0;
        long total = (long) sourceNodes.size() * (long) targetNodes.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        //  For every concept in source context
        for (int i = 0; i < sourceNodes.size(); i++) {
            INode sourceNode = sourceNodes.get(i);
            for (int j = 0; j < targetNodes.size(); j++) {
                INode targetNode = targetNodes.get(j);

                relation = smatchMatcher.nodeMatch(ClabMatrix, sourceNode, targetNode);

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
                CnodMatrix.setElement(i, j, relation);
            }
            CnodMatrix.endOfRow();
        }
        return CnodMatrix;
    }
}
