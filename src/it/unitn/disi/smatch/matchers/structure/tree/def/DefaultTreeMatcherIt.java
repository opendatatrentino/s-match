package it.unitn.disi.smatch.matchers.structure.tree.def;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.matchers.structure.tree.TreeMatcherException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Version with an iterator.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DefaultTreeMatcherIt extends DefaultTreeMatcher {

    private static final Logger log = Logger.getLogger(DefaultTreeMatcherIt.class);

    @Override
    public IContextMapping<INode> treeMatch(IContext sourceContext, IContext targetContext, IContextMapping<IAtomicConceptOfLabel> acolMapping) throws TreeMatcherException {
        IContextMapping<INode> mapping = mappingFactory.getContextMappingInstance(sourceContext, targetContext);

        // semantic relation for particular node matching task
        char relation;

        long counter = 0;
        long total = (long) (sourceContext.getRoot().getDescendantCount() + 1) * (long) (targetContext.getRoot().getDescendantCount() + 1);
        long reportInt = (total / 20) + 1;//i.e. report every 5%

        Map<String, IAtomicConceptOfLabel> sourceAcols = new HashMap<String, IAtomicConceptOfLabel>();
        Map<String, IAtomicConceptOfLabel> targetAcols = new HashMap<String, IAtomicConceptOfLabel>();

        Map<INode, ArrayList<IAtomicConceptOfLabel>> nmtAcols = new HashMap<INode, ArrayList<IAtomicConceptOfLabel>>();

        for (Iterator<INode> i = sourceContext.getNodes(); i.hasNext();) {
            INode sourceNode = i.next();
            for (Iterator<INode> j = targetContext.getNodes(); j.hasNext();) {
                INode targetNode = j.next();
                relation = nodeMatcher.nodeMatch(acolMapping, nmtAcols, sourceAcols, targetAcols, sourceNode, targetNode);
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
