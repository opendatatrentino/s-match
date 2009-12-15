package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.matchers.structure.node.EvalNodeMatcher;
import it.unitn.disi.smatch.matchers.structure.node.INodeMatcher;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.SMatchConstants;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * For formula evaluation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class EvalTLMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(EvalTLMatcher.class);

    private final INodeMatcher smatchMatcher = new EvalNodeMatcher();

    public IMatchMatrix treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) throws SMatchException {
        //get the nodes of the contexts, should be equal
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();

        //initialize CnodMatrix to keep comparison results
        //should be filled with MatchManager.SYNOMYM (as a positive flag)
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

            relation = smatchMatcher.nodeMatch(ClabMatrix, sourceNode, targetNode);
            CnodMatrix.setElement(i, 0, relation);

            counter++;
            if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                log.info(100 * counter / total + "%");
            }
        }

        return CnodMatrix;
    }
}
