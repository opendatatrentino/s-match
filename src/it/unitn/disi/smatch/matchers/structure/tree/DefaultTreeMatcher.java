package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

public class DefaultTreeMatcher extends BaseTreeMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(DefaultTreeMatcher.class);

    public IMatchMatrix treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) throws TreeMatcherException {
        // get the nodes of the contexts
        List<INode> sourceNodes = sourceContext.getAllNodes();
        List<INode> targetNodes = targetContext.getAllNodes();

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

                relation = nodeMatcher.nodeMatch(ClabMatrix, sourceNode, targetNode);

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