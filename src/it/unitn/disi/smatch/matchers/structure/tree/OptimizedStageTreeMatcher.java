package it.unitn.disi.smatch.matchers.structure.tree;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.IContext;
import it.unitn.disi.smatch.data.INode;
import it.unitn.disi.smatch.data.mappings.MappingNodeElement;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;
import it.unitn.disi.smatch.deciders.openSAT;
import it.unitn.disi.smatch.matchers.structure.node.OptimizedStageNodeMatcher;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Vector;

/**
 * Matches first disjoint, then subs, then joins subsumption. For minimal links paper.
 * This is the optimized version from "pseudocode-Final Version.doc"
 * User: Aliaksandr
 */
public class OptimizedStageTreeMatcher implements ITreeMatcher {

    private static final Logger log = Logger.getLogger(OptimizedStageTreeMatcher.class);

    private static OptimizedStageNodeMatcher smatchMatcher;

    private IMatchMatrix ClabMatrix;

    private HashSet<MappingNodeElement> mapping;

    private long counter = 0;
    private long total;
    private long reportInt;

    private boolean direction;


    public OptimizedStageTreeMatcher() {
        smatchMatcher = new OptimizedStageNodeMatcher();
    }

    public IMatchMatrix treeMatch(IContext sourceContext, IContext targetContext, IMatchMatrix ClabMatrixParam) throws SMatchException {
        ClabMatrix = ClabMatrixParam;

        //get the nodes of the contexts
        Vector<INode> sourceNodes = sourceContext.getAllNodes();
        Vector<INode> targetNodes = targetContext.getAllNodes();

        total = (long) sourceContext.getRoot().getDescendantCount() * (long) targetContext.getRoot().getDescendantCount();
        reportInt = (total / 20) + 1;//i.e. report every 5%

        for (int i = 0; i < sourceNodes.size(); i++) {
            sourceNodes.get(i).getNodeData().setIndex(i);
            //this is to distinguish below, in matcher, for axiom creation
            sourceNodes.get(i).getNodeData().setSource(true);
        }
        for (int i = 0; i < targetNodes.size(); i++) {
            targetNodes.get(i).getNodeData().setIndex(i);
        }

        mapping = new HashSet<MappingNodeElement>();

        log.info("DJ...");
        treeDisjoint(sourceContext.getRoot(), targetContext.getRoot());
        int dj = mapping.size();
        log.info("Links found DJ: " + dj);
        int djHits = openSAT.hits;
        log.debug("DJ SAT hits: " + djHits);
        counter = 0;

        log.info("LG...");
        direction = true;
        treeSubsumedBy(sourceContext.getRoot(), targetContext.getRoot());
        int lg = mapping.size() - dj;
        log.info("Links found LG: " + lg);
        int lgHits = openSAT.hits - djHits;
        log.debug("LG SAT hits: " + lgHits);
        counter = 0;

        log.info("MG...");
        direction = false;
        treeSubsumedBy(targetContext.getRoot(), sourceContext.getRoot());
        int mg = mapping.size() - dj - lg;
        log.info("Links found MG: " + mg);
        int mgHits = openSAT.hits - djHits - lgHits;
        log.debug("MG SAT hits: " + mgHits);
        counter = 0;

        log.info("TreeEquiv...");
        mapping = treeEquiv(mapping);
        log.info("TreeEquiv finished");

        //to free abit of memory
        smatchMatcher.clearAxiomsCache();

        IMatchMatrix CnodMatrix = MatrixFactory.getInstance(sourceNodes.size(), targetNodes.size());
        mappingToMatrix(mapping, CnodMatrix);
        return CnodMatrix;
    }

    private void treeDisjoint(INode n1, INode n2) throws SMatchException {
        nodeTreeDisjoint(n1, n2);
        for (INode c1 : n1.getChildren()) {
            treeDisjoint(c1, n2);
        }
    }

    private void nodeTreeDisjoint(INode n1, INode n2) throws SMatchException {
        if (findRelation(n1.getAncestors(), n2, MatchManager.OPPOSITE_MEANING)) {
            //we skip n2 subtree, so adjust the counter
            final long skipTo = counter + n2.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }

            return;
        }

        if (smatchMatcher.nodeDisjoint(ClabMatrix, n1, n2)) {
            addRelation(n1, n2, MatchManager.OPPOSITE_MEANING);
            //we skip n2 subtree, so adjust the counter
            final long skipTo = counter + n2.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }
            return;
        }

        progress();

        for (INode c2 : n2.getChildren()) {
            nodeTreeDisjoint(n1, c2);
        }
    }

    private boolean treeSubsumedBy(INode n1, INode n2) throws SMatchException {
        if (findRelation(n1, n2, MatchManager.OPPOSITE_MEANING)) {
            //we skip n1 subtree, so adjust the counter
            final long skipTo = counter + n1.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }

            return false;
        }

        progress();
        if (!smatchMatcher.nodeSubsumedBy(ClabMatrix, n1, n2)) {
            for (INode c1 : n1.getChildren()) {
                treeSubsumedBy(c1, n2);
            }
        } else {
            boolean lastNodeFound = false;
            for (INode c2 : n2.getChildren()) {
                if (treeSubsumedBy(n1, c2)) {
                    lastNodeFound = true;
                }
            }
            if (!lastNodeFound) {
                addSubsumptionRelation(n1, n2);
            }

            //we skip n1 subtree, so adjust the counter
            final long skipTo = counter + n1.getDescendantCount();
            while (counter < skipTo) {
                progress();
            }
            return true;
        }

        return false;
    }

    private HashSet<MappingNodeElement> treeEquiv(HashSet<MappingNodeElement> mapping) {
        HashSet<MappingNodeElement> result = new HashSet<MappingNodeElement>();
        if (log.isEnabledFor(Level.INFO)) {
            log.info("Mapping before TreeEquiv: " + mapping.size());
        }
        for (MappingNodeElement me : mapping) {
            if (MatchManager.LESS_GENERAL_THAN == me.getRelation()) {
                MappingNodeElement mg = new MappingNodeElement(me.getSourceNode(), me.getTargetNode(), MatchManager.MORE_GENERAL_THAN);
                if (mapping.contains(mg)) {
                    result.add(new MappingNodeElement(me.getSourceNode(), me.getTargetNode(), MatchManager.SYNOMYM));
                } else {
                    result.add(me);
                }
            } else {
                if (MatchManager.MORE_GENERAL_THAN == me.getRelation()) {
                    MappingNodeElement lg = new MappingNodeElement(me.getSourceNode(), me.getTargetNode(), MatchManager.LESS_GENERAL_THAN);
                    if (mapping.contains(lg)) {
                        result.add(new MappingNodeElement(me.getSourceNode(), me.getTargetNode(), MatchManager.SYNOMYM));
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
            mapping.add(new MappingNodeElement(n1, n2, MatchManager.LESS_GENERAL_THAN));
        } else {
            mapping.add(new MappingNodeElement(n2, n1, MatchManager.MORE_GENERAL_THAN));
        }
    }


    private void addRelation(INode n1, INode n2, char relation) {
        mapping.add(new MappingNodeElement(n1, n2, relation));
    }

    private boolean findRelation(INode sourceNode, INode targetNode, char relation) {
        return mapping.contains(new MappingNodeElement(sourceNode, targetNode, relation));
    }

    private boolean findRelation(INode sourceNode, Vector<INode> targetNodes, char relation) {
        for (INode targetNode : targetNodes) {
            if (mapping.contains(new MappingNodeElement(sourceNode, targetNode, relation))) {
                return true;
            }
        }
        return false;
    }

    private boolean findRelation(Vector<INode> sourceNodes, INode targetNode, char relation) {
        for (INode sourceNode : sourceNodes) {
            if (mapping.contains(new MappingNodeElement(sourceNode, targetNode, relation))) {
                return true;
            }
        }
        return false;
    }

    private void mappingToMatrix(HashSet<MappingNodeElement> mapping, IMatchMatrix cnodMatrix) {
        for (MappingNodeElement me : mapping) {
            final char element = cnodMatrix.getElement(me.getSourceNode().getNodeData().getIndex(), me.getTargetNode().getNodeData().getIndex());
            if (MatchManager.IDK_RELATION == element) {
                cnodMatrix.setElement(me.getSourceNode().getNodeData().getIndex(), me.getTargetNode().getNodeData().getIndex(), me.getRelation());
            } else {
                if (log.isEnabledFor(Level.WARN)) {
                    log.warn("Override attempt " + element + " with: " + me.getSourceNode() + "\t" + me.getTargetNode() + "\t" + me.getRelation());
                }
            }
        }
    }

    private void progress() {
        counter++;
        if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
            log.info(100 * counter / total + "%");
        }
    }
}