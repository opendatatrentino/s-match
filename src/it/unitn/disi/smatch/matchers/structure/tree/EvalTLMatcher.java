package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.matchers.structure.node.NodeMatcherException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * For formula evaluation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class EvalTLMatcher extends BaseTreeMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(EvalTLMatcher.class);

    public IMatchMatrix treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) throws NodeMatcherException {
        //get the nodes of the contexts, should be equal
        List<INode> sourceNodes = sourceContext.getAllNodes();
        List<INode> targetNodes = targetContext.getAllNodes();

        //initialize cNodMatrix to keep comparison results
        //should be filled with MatchManager.EQUIVALENCE (as a positive flag)
        //0 row shows equality of the formulas
        IMatchMatrix CnodMatrix = MatrixFactory.getInstance(sourceNodes.size(), 1);

        //semantic relation for particular node matching task
        char relation;

        long counter = 0;
        long total = (long) sourceNodes.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        for (int i = 0; i < sourceNodes.size(); i++) {
            //should be the same nodes!
            INode sourceNode = sourceNodes.get(i);
            INode targetNode = targetNodes.get(i);

            //check
            String s = sourceNode.getNodeData().getPathToRootString();
            String t = targetNode.getNodeData().getPathToRootString();
            if (!s.equals(t) && log.isEnabledFor(Level.WARN)) {
                log.warn("The nodes mismatch!");
                log.warn(s);
                log.warn(t);
            }

            relation = nodeMatcher.nodeMatch(ClabMatrix, sourceNode, targetNode);
            CnodMatrix.setElement(i, 0, relation);

            counter++;
            if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                log.info(100 * counter / total + "%");
            }
        }

        return CnodMatrix;
    }
}