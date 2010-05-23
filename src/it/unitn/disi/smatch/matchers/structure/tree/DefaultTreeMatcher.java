package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.ContextMapping;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultTreeMatcher extends BaseTreeMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(DefaultTreeMatcher.class);

    public IContextMapping<INode> treeMatch(IContext sourceContext, IContext targetContext, IContextMapping<IAtomicConceptOfLabel> acolMapping) throws TreeMatcherException {
        IContextMapping<INode> mapping = new ContextMapping<INode>(sourceContext, targetContext);

        //semantic relation for particular node matching task
        char relation;

        long counter = 0;
        long total = (long) (sourceContext.getRoot().getDescendantCount() + 1) * (long) (targetContext.getRoot().getDescendantCount() + 1);
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        Map<String, IAtomicConceptOfLabel> sourceAcols = createAcolsMap(sourceContext);
        Map<String, IAtomicConceptOfLabel> targetAcols = createAcolsMap(targetContext);

        for (Iterator<INode> i = sourceContext.getRoot().getSubtree(); i.hasNext();) {
            INode sourceNode = i.next();
            for (Iterator<INode> j = targetContext.getRoot().getSubtree(); j.hasNext();) {
                INode targetNode = j.next();
                relation = nodeMatcher.nodeMatch(acolMapping, sourceAcols, targetAcols, sourceNode, targetNode);
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