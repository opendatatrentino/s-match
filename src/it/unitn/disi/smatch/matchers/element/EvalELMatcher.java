package it.unitn.disi.smatch.matchers.element;

import it.unitn.disi.smatch.MatchManager;
import it.unitn.disi.smatch.SMatchConstants;
import it.unitn.disi.smatch.SMatchException;
import it.unitn.disi.smatch.data.*;
import it.unitn.disi.smatch.data.matrices.IMatchMatrix;
import it.unitn.disi.smatch.data.matrices.MatrixFactory;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * Matches acols of formulas for evaluation.
 * Expects the source and target to be the same context, preprocessed in a different way.
 * To be used with EvalTLMatcher for formulas evaluation.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class EvalELMatcher implements IMatcherLibrary {

    private static final Logger log = Logger.getLogger(EvalELMatcher.class);

    //constants for error types

    //exact sense match: sourceSense = targetSenses
    public static final char EXACT_SENSE_MATCH = '=';
    //there some extra sourceSenses
    public static final char APPROXIMATE_SENSE_MATCH = '~';
    //not all target senses are present in source
    public static final char SENSE_MISMATCH = '!';
    //no senses on both sides, matching tokens
    public static final char TOKEN_MATCH = 'T';
    //no senses on both sides, matching lemmas
    public static final char LEMMA_MATCH = 'L';
    //no senses where they should be
    public static final char NO_SENSES = '<';
    //extra senses where they should not be
    public static final char EXTRA_SENSES = '>';


    /**
     * Matches acols of same nodes.
     *
     * @param sourceContext context to be evaluated
     * @param targetContext golden context
     * @return cLabMatrix
     */
    public IMatchMatrix elementLevelMatching(IContext sourceContext, IContext targetContext) throws SMatchException {
        IMatchMatrix ClabMatrix = null;
        try {
            //compare each pair of nodes
            //for each pair compare all node acols

            Vector<IAtomicConceptOfLabel> sourceACoLs = sourceContext.getMatchingContext().getAllContextACoLs();
            Vector<IAtomicConceptOfLabel> targetACoLs = targetContext.getMatchingContext().getAllContextACoLs();

            MatchManager.printMemoryUsage();

            //Initialization of matrix
            ClabMatrix = MatrixFactory.getInstance(sourceACoLs.size(), targetACoLs.size());

            MatchManager.printMemoryUsage();

            Vector<INode> sourceNodes = sourceContext.getAllNodes();
            Vector<INode> targetNodes = targetContext.getAllNodes();

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

                Vector<IAtomicConceptOfLabel> sourceNodeACoLs = sourceNode.getNodeData().getACoLs();
                Vector<IAtomicConceptOfLabel> targetNodeACoLs = targetNode.getNodeData().getACoLs();

                for (IAtomicConceptOfLabel sourceACOL : sourceNodeACoLs) {
                    for (IAtomicConceptOfLabel targetACOL : targetNodeACoLs) {
                        ClabMatrix.setElement(sourceACOL.getIndex(), targetACOL.getIndex(), getRelation(sourceACOL, targetACOL));
                    }
                }

                counter++;
                if ((SMatchConstants.LARGE_TASK < total) && (0 == (counter % reportInt)) && log.isEnabledFor(Level.INFO)) {
                    log.info(100 * counter / total + "%");
                }
            }
        } catch (Exception e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error("Exception: " + e.getMessage(), e);
                log.error("The cLab matrix is not complete due to an exception");
            }
            throw new SMatchException("The cLab matrix is not complete due to an exception", e);
        }
        return ClabMatrix;
    }

    public char getRelation(IAtomicConceptOfLabel sourceACoL, IAtomicConceptOfLabel targetACoL) {
        char result = MatchManager.IDK_RELATION;

        //if all target (golden) senses are present in source and nothing more - exact match
        //if more are in source - approximate match
        //none - mismatch
        //no senses in target, there are some in source - mismatch
        //no senses everywhere - tokens equality -> match
        //tokens do not match -> lemmas equality -> match

        //compare sets of senses
        Vector<String> sourceSenses = new Vector<String>(sourceACoL.getSenses().getSenseList());
        Vector<String> targetSenses = new Vector<String>(targetACoL.getSenses().getSenseList());

        removeUnknownSenses(sourceSenses);
        removeUnknownSenses(targetSenses);

        boolean tokensEqual = sourceACoL.getToken().toLowerCase().equals(targetACoL.getToken().toLowerCase());

        if (0 < sourceSenses.size() && 0 < targetSenses.size()) {
            //if all target (golden) senses are present in source and nothing more - exact match
            //if more are in source - approximate match
            //none - mismatch
            int idx = 0;
            while (idx < targetSenses.size()) {
                int sIdx = sourceSenses.indexOf(targetSenses.get(idx));
                if (-1 < sIdx) {
                    sourceSenses.remove(sIdx);
                    targetSenses.remove(idx);
                } else {
                    idx++;
                }
            }

            if (0 == targetSenses.size()) {
                if (0 == sourceSenses.size()) {
                    //= exact sense match
                    result = EXACT_SENSE_MATCH;
                } else {
                    //~ approximate sense match
                    //TODO how much approximate
                    //that is, the more senses are there in the synset, the more difficult is the task
                    result = APPROXIMATE_SENSE_MATCH;
                }
            } else {
                //! mismatch
                result = SENSE_MISMATCH;
            }

        } else {
            if (0 == sourceSenses.size() && 0 == targetSenses.size()) {
                //match tokens
                //compare tokens

                if (tokensEqual) {
                    result = TOKEN_MATCH;//T no senses, token match
                } else {
                    //compare lemmas
                    boolean lemmasEqual = sourceACoL.getLemma().toLowerCase().equals(targetACoL.getLemma().toLowerCase());

                    if (lemmasEqual) {
                        result = LEMMA_MATCH;//L no senses, lemmas match
                    }
                }
            } else {
                if (0 == sourceSenses.size()) {
                    //and 0 < targetSenses.size()
                    //incorrect senses
                    //no senses where they should be
                    //<
                    result = NO_SENSES;
                } else {
                    if (0 == targetSenses.size()) {
                        //and 0 < sourceSenses.size()
                        //incorrect senses
                        //senses present where they should not be
                        //>
                        result = EXTRA_SENSES;
                    } else {
                        //should not get there
                        System.out.println("should not get there 1");
                        System.exit(1);
                    }
                }
            }
        }

        return result;
    }

    private void removeUnknownSenses(Vector<String> senses) {
        int i = 0;
        while (i < senses.size()) {
            if (senses.get(i).startsWith(MatchManager.UNKNOWN_MEANING)) {
                senses.remove(i);
            } else {
                i++;
            }
        }
    }
}