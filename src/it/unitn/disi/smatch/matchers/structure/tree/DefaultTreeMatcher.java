package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.ContextMapping;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

public class DefaultTreeMatcher extends BaseTreeMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(DefaultTreeMatcher.class);

    public IContextMapping<INode> treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrix) throws TreeMatcherException {
        // get the nodes of the contexts
        List<INode> sourceNodes = sourceContext.getAllNodes();
        List<INode> targetNodes = targetContext.getAllNodes();

        IContextMapping<INode> mapping = new ContextMapping<INode>(sourceContext, targetContext);

        //semantic relation for particular node matching task
        char relation;

        long counter = 0;
        long total = (long) sourceNodes.size() * (long) targetNodes.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        for (INode sourceNode : sourceNodes) {
            for (INode targetNode : targetNodes) {
                relation = nodeMatcher.nodeMatch(ClabMatrix, sourceNode, targetNode);
                mapping.setRelation(sourceNode, targetNode, relation);

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
            }
        }

        return mapping;
    }
}