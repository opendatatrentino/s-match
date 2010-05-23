package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.components.ConfigurableException;
import it.unitn.disi.smatch.data.ling.IAtomicConceptOfLabel;
import it.unitn.disi.smatch.data.trees.IContext;
import it.unitn.disi.smatch.data.trees.INode;
import it.unitn.disi.smatch.data.mappings.ContextMapping;
import it.unitn.disi.smatch.data.mappings.IContextMapping;
import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.data.mappings.ReversingMappingElement;
import it.unitn.disi.smatch.matchers.structure.node.OptimizedStageNodeMatcher;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Matches first disjoint, then subsumptions, then joins subsumption into equivalence.
 *
 * For more details see:
 * <p/>
 * <a href="http://eprints.biblio.unitn.it/archive/00001525/">http://eprints.biblio.unitn.it/archive/00001525/</a>
 * <p/>
 * Giunchiglia, Fausto and Maltese, Vincenzo and Autayeu, Aliaksandr. Computing minimal mappings.
 * Technical Report DISI-08-078, Department of Information Engineering and Computer Science, University of Trento.
 * Proc. of the Fourth Ontology Matching Workshop at ISWC 2009.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class OptimizedStageTreeMatcher extends BaseTreeMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(OptimizedStageTreeMatcher.class);

    private OptimizedStageNodeMatcher smatchMatcher;
    private Map<String, IAtomicConceptOfLabel> sourceAcols;
    private Map<String, IAtomicConceptOfLabel> targetAcols;

    private IContextMapping<IAtomicConceptOfLabel> acolMapping;

    // need another mapping because here we allow < and > between the same pair of nodes
    private HashSet<IMappingElement<INode>> mapping;

    private long counter = 0;
    private long total;
    private long reportInt;

    private boolean direction;

    @Override
    public void setProperties(Properties newProperties) throws ConfigurableException {
        super.setProperties(newProperties);

        if (nodeMatcher instanceof OptimizedStageNodeMatcher) {
            smatchMatcher = (OptimizedStageNodeMatcher) nodeMatcher;
        } else {
            final String errMessage = "OptimizedStageTreeMatcher works only with OptimizedStageNodeMatcher";
            log.error(errMessage);
            throw new TreeMatcherException(errMessage);
        }
    }

    public IContextMapping<INode> treeMatch(IContext sourceContext, IContext targetContext, IContextMapping<IAtomicConceptOfLabel> acolMapping) throws TreeMatcherException {
        this.acolMapping = acolMapping;

        total = (long) (sourceContext.getRoot().getDescendantCount() + 1) * (long) (targetContext.getRoot().getDescendantCount() + 1);
        reportInt = (total / 20) + 1;//i.e. report every 5%

        for (Iterator<INode> i = sourceContext.getRoot().getSubtree(); i.hasNext();) {
            INode sourceNode = i.next();
            // this is to distinguish below, in matcher, for axiom creation
            sourceNode.getNodeData().setSource(true);
        }

        sourceAcols = createAcolsMap(sourceContext);
        targetAcols = createAcolsMap(targetContext);

        mapping = new HashSet<IMappingElement<INode>>();

        log.info("DJ...");
        treeDisjoint(sourceContext.getRoot(), targetContext.getRoot());
        int dj = mapping.size();
        log.info("Links found DJ: " + dj);
        counter = 0;

        log.info("LG...");
        direction = true;
        treeSubsumedBy(sourceContext.getRoot(), targetContext.getRoot());
        int lg = mapping.size() - dj;
        log.info("Links found LG: " + lg);
        counter = 0;

        log.info("MG...");
        direction = false;
        treeSubsumedBy(targetContext.getRoot(), sourceContext.getRoot());
        int mg = mapping.size() - dj - lg;
        log.info("Links found MG: " + mg);
        counter = 0;

        log.info("TreeEquiv...");
        IContextMapping<INode> result = treeEquiv(mapping, sourceContext, targetContext);
        log.info("TreeEquiv finished");

        return result;
    }

    private void treeDisjoint(INode n1, INode n2) throws TreeMatcherException {
        nodeTreeDisjoint(n1, n2);
        for (Iterator<INode> i = n1.getChildren(); i.hasNext();) {
            treeDisjoint(i.next(), n2);
        }
    }

    private void nodeTreeDisjoint(INode n1, INode n2) throws TreeMatcherException {
        if (findRelation(n1.getAncestors(), n2, IMappingElement.DISJOINT)) {
            // we skip n2 subtree, so adjust the counter
            final long skipTo = counter + n2.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }

            return;
        }

        if (smatchMatcher.nodeDisjoint(acolMapping, sourceAcols, targetAcols, n1, n2)) {
            addRelation(n1, n2, IMappingElement.DISJOINT);
            // we skip n2 subtree, so adjust the counter
            final long skipTo = counter + n2.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }
            return;
        }

        progress();

        for (Iterator<INode> i = n2.getChildren(); i.hasNext();) {
            nodeTreeDisjoint(n1, i.next());
        }
    }

    private boolean treeSubsumedBy(INode n1, INode n2) throws TreeMatcherException {
        if (findRelation(n1, n2, IMappingElement.DISJOINT)) {
            // we skip n1 subtree, so adjust the counter
            final long skipTo = counter + n1.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }

            return false;
        }

        progress();
        if (!smatchMatcher.nodeSubsumedBy(acolMapping, sourceAcols, targetAcols, n1, n2)) {
            for (Iterator<INode> i = n1.getChildren(); i.hasNext();) {
                treeSubsumedBy(i.next(), n2);
            }
        } else {
            boolean lastNodeFound = false;
            for (Iterator<INode> i = n2.getChildren(); i.hasNext();) {
                if (treeSubsumedBy(n1, i.next())) {
                    lastNodeFound = true;
                }
            }
            if (!lastNodeFound) {
                addSubsumptionRelation(n1, n2);
            }

            // we skip n1 subtree, so adjust the counter
            final long skipTo = counter + n1.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }
            return true;
        }

        return false;
    }

    private IContextMapping<INode> treeEquiv(HashSet<IMappingElement<INode>> mapping, IContext sourceContext, IContext targetContext) {
        IContextMapping<INode> result = new ContextMapping<INode>(sourceContext, targetContext);
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Mapping before TreeEquiv: " + mapping.size());
        }
        for (IMappingElement<INode> me : mapping) {
            if (IMappingElement.LESS_GENERAL == me.getRelation()) {
                IMappingElement<INode> mg = createMappingElement(me.getSource(), me.getTarget(), IMappingElement.MORE_GENERAL);
                if (mapping.contains(mg)) {
                    result.setRelation(me.getSource(), me.getTarget(), IMappingElement.EQUIVALENCE);
                } else {
                    result.add(me);
                }
            } else {
                if (IMappingElement.MORE_GENERAL == me.getRelation()) {
                    IMappingElement<INode> lg = createMappingElement(me.getSource(), me.getTarget(), IMappingElement.LESS_GENERAL);
                    if (mapping.contains(lg)) {
                        result.setRelation(me.getSource(), me.getTarget(), IMappingElement.EQUIVALENCE);
                    } else {
                        result.add(me);
                    }
                } else {
                    result.add(me);
                }
            }
        }
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Mapping after TreeEquiv: " + result.size());
        }
        return result;
    }

    private void addSubsumptionRelation(INode n1, INode n2) {
        if (direction) {
            mapping.add(createMappingElement(n1, n2, IMappingElement.LESS_GENERAL));
        } else {
            mapping.add(createMappingElement(n2, n1, IMappingElement.MORE_GENERAL));
        }
    }


    private void addRelation(INode n1, INode n2, char relation) {
        mapping.add(createMappingElement(n1, n2, relation));
    }

    private boolean findRelation(INode sourceNode, INode targetNode, char relation) {
        return mapping.contains(createMappingElement(sourceNode, targetNode, relation));
    }

    private boolean findRelation(Iterator<INode> sourceNodes, INode targetNode, char relation) {
        while (sourceNodes.hasNext()) {
            INode sourceNode = sourceNodes.next();
            if (mapping.contains(createMappingElement(sourceNode, targetNode, relation))) {
                return true;
            }
        }
        return false;
    }

    private void progress() {
        counter++;
        if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
            log.info(100 * counter / total + "%");
        }
    }

    private static IMappingElement<INode> createMappingElement(INode source, INode target, char relation) {
        return new ReversingMappingElement(source, target, relation);
    }
}